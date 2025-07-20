// Import Mongo-compatible data store (model)
const storeMails = require('../models/mailModel');
const blacklistModel = require('../models/blacklistModel');
const { getUserById, findUserByUsername } = require('../models/usersModel');

// GET /api/mails
exports.getAllMails = async (req, res) => {
    const userId = req.header('user-id');
    if (!userId) {
        return res.status(400).json({ error: 'Missing user-id header' });
    }

    try {
        const since = req.query.since;
        let userMails;
        
        if (since) {
            // Fetch only mails newer than the since timestamp
            userMails = await storeMails.getMailsByUserSince(userId, since);
        } else {
            // Fetch all mails (existing behavior)
            userMails = await storeMails.getAllMailsByUser(userId);
            const recentMails = userMails.slice(-50).reverse();
            userMails = recentMails;
        }
        
        res.status(200).json(userMails);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// POST /api/mails
exports.sendMail = async (req, res) => {
    const { subject, body, from, to, isDraft, direction } = req.body;
    const owner = req.header('user-id');

    // Basic validation
    if (!isDraft && (!subject || !body || !from || !to || !owner)) {
        return res.status(400).json({ error: 'Missing required fields' });
    }

    if (owner !== from) {
        return res.status(400).json({ error: 'Sender does not match logged in user' });
    }

    const recipients = Array.isArray(to) ? to : [to];

    const urlRegex = /(https?:\/\/)?(www\.)?[^\s]+\.[^\s]+/g;
    let urls = [
        ...(subject.match(urlRegex) || []),
        ...(body.match(urlRegex) || [])
    ];
    if (Array.isArray(to)) {
        for (const recipient of to) {
            urls.push(...(recipient.match(urlRegex) || []));
        }
    } else if (typeof to === 'string') {
        urls.push(...(to.match(urlRegex) || []));
    }

    // Check if any URL is blacklisted
    const fs = require('fs');
    const path = require('path');
    const urlsFile = path.join(__dirname, '../../data/urls.txt');
    let fileContent = '';
    if (fs.existsSync(urlsFile)) {
        fileContent = fs.readFileSync(urlsFile, 'utf-8');
    }
    const blacklistedUrls = new Set(fileContent.split('\n').filter(Boolean));
    let isSpam = false;
    for (const url of urls) {
        if (blacklistedUrls.has(url)) {
            isSpam = true;
            break;
        }
    }

    // Append URLs to data/urls.txt if mail is marked as spam (same as PATCH handler)
    if (isSpam && urls.length > 0) {
        const urlsToAppend = urls.filter(url => !blacklistedUrls.has(url));
        if (urlsToAppend.length > 0) {
            const urlsToAppendStr = urlsToAppend.join('\n') + '\n';
            fs.appendFile(urlsFile, urlsToAppendStr, (err) => {
                if (err) {
                    console.error('[urls.txt] Failed to append URLs:', err);
                }
            });
        }
    }

    const timestamp = new Date().toISOString();
    const groupId = `${Date.now()}_${from}_${Math.floor(Math.random() * 100000)}`;

    const senderUser = await getUserById(from);
    if (!senderUser) {
    return res.status(404).json({ error: 'Sender user not found' });
    }


    if (!isDraft) {
        for (const recipient of recipients) {
            const recipientUser = await findUserByUsername(recipient);
            if (!recipientUser) continue;
            if (recipientUser.id.toString() === from) continue;

            await storeMails.createMail({
                subject,
                body,
                from: senderUser.username,
                to: recipient,
                user: recipientUser.id.toString(),
                owner: recipientUser.id.toString(),
                direction: isSpam ? ['spam'] : ['received'],
                timestamp,
                isDeleted: false,
                isDraft: false,
                isStarred: false,
                isSpam,
                groupId: (senderUser.username === recipient) ? groupId : null,
                isRead: false
            });
        }

    }

    const isSelfMail =
        recipients.length === 1 &&
        recipients[0] === senderUser.username;

    const finalDirection = isSelfMail
        ? (isDraft ? ['draft'] : ['sent', 'received'])
        : Array.isArray(direction)
            ? direction
            : [direction || (isDraft ? 'draft' : (isSpam ? 'spam' : 'sent'))];

    const sentMail = await storeMails.createMail({
        subject,
        body,
        from: senderUser.username,
        to: recipients,
        user: from,
        owner: from,
        direction: isSpam ? ['spam'] : finalDirection,
        timestamp,
        isDeleted: false,
        isDraft: isDraft || false,
        isStarred: false,
        isSpam,
        groupId: isSelfMail ? groupId : null,
        isRead: false
    });

    return res.status(201).location(`/api/mails/${sentMail.id}`).json(sentMail);
};


// GET /api/mails/:id
exports.getMailById = async (req, res) => {
    const { id } = req.params;
    const userId = req.header('user-id');
    if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

    try {
        const mail = await storeMails.getMail(id);
        if (!mail || mail.owner !== userId) {
            return res.status(404).json({ error: 'Mail not found' });
        }
        res.status(200).json(mail);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// PATCH /api/mails/:id
exports.updateMail = async (req, res) => {
    const { id } = req.params;
    const userId = req.header('user-id');
    if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

    try {
        const mail = await storeMails.getMail(id);
        if (!mail || mail.owner !== userId) return res.status(404).json({ error: 'Mail not found' });

        const updatedFields = req.body;
        // Ensure PATCH can update isSpam, isDeleted, isStarred, category, etc.
        // (No code change needed if storeMails.updateMail uses $set)
        // Optionally, validate allowed fields here

        // Always set previousDirection if present in PATCH body
        if (updatedFields.previousDirection !== undefined) {
            mail.previousDirection = updatedFields.previousDirection;
        }

        // When marking as spam, save current direction as previousDirection
        if (updatedFields.isSpam === true && !mail.isSpam) {
            updatedFields.previousDirection = mail.direction;
        }

        // When unspamming, restore previousDirection and clear it
        if (mail.isSpam && updatedFields.isSpam === false && mail.previousDirection?.length > 0) {
            updatedFields.direction = mail.previousDirection;
            updatedFields.previousDirection = [];
        }

        const urlsToCheck = [];
        const urlRegex = /(https?:\/\/)?(www\.)?[^\s]+\.[^\s]+/g;

        // Extract URLs from updated fields if provided
        if (updatedFields.subject) {
            urlsToCheck.push(...(updatedFields.subject.match(urlRegex) || []));
        }
        if (updatedFields.body) {
            urlsToCheck.push(...(updatedFields.body.match(urlRegex) || []));
        }

        // If marking as spam and no URLs found in updates, extract from existing mail content
        if (updatedFields.isSpam === true && urlsToCheck.length === 0) {
            const existingUrls = [];
            if (mail.subject) {
                existingUrls.push(...(mail.subject.match(urlRegex) || []));
            }
            if (mail.body) {
                existingUrls.push(...(mail.body.match(urlRegex) || []));
            }
            urlsToCheck.push(...existingUrls);
        }

        let isSpam = typeof updatedFields.isSpam === 'boolean' ? updatedFields.isSpam : false;

        for (const url of urlsToCheck) {
            try {
                const response = await blacklistModel.isBlacklisted(url);
                const parts = response.split('\n\n');
                const body = parts[parts.length - 1].trim();
                const result = {
                    ok: true,
                    blacklisted: body === 'true true'
                };
                if (!result.ok) {
                    return res.status(500).json({ error: 'Blacklist check failed during update' });
                }
                if (result.blacklisted) {
                    isSpam = true;
                }
            } catch (err) {
                return res.status(500).json({ error: 'Blacklist check failed during update' });
            }
        }

        // Append URLs to data/urls.txt if mail is marked as spam
        if (isSpam && urlsToCheck.length > 0) {
            const fs = require('fs');
            const path = require('path');
            const urlsFile = path.join(__dirname, '../../data/urls.txt');
            let fileContent = '';
            if (fs.existsSync(urlsFile)) {
                fileContent = fs.readFileSync(urlsFile, 'utf-8');
            }
            const existingUrls = new Set(fileContent.split('\n').filter(Boolean));
            const urlsToActuallyAppend = urlsToCheck.filter(url => !existingUrls.has(url));
            if (urlsToActuallyAppend.length > 0) {
                const urlsToAppend = urlsToActuallyAppend.join('\n') + '\n';
                fs.appendFile(urlsFile, urlsToAppend, (err) => {
                    if (err) {
                        console.error('[urls.txt] Failed to append URLs:', err);
                    } else {
                    }
                });
            }
        }

        updatedFields.isSpam = isSpam;
        const wasDraft = mail.isDraft;
        const nowSent = updatedFields.isDraft === false;

        if (wasDraft && nowSent) {
            const user = await getUserById(userId);
            updatedFields.from = user.username;

            const recipients = Array.isArray(updatedFields.to) ? updatedFields.to : [updatedFields.to];
            const timestamp = new Date().toISOString();

            for (const recipient of recipients) {
                const recipientUser = await findUserByUsername(recipient);
                if (!recipientUser) continue;
                if (recipientUser.id.toString() === userId) continue;

                await storeMails.createMail({
                    subject: updatedFields.subject,
                    body: updatedFields.body,
                    from: user.username,
                    to: recipient,
                    user: recipientUser.id.toString(),
                    owner: recipientUser.id.toString(),
                    direction: ['received'],
                    timestamp,
                    isDeleted: false,
                    isDraft: false,
                    isStarred: false,
                    isSpam,
                    groupId: null,
                    isRead: false
                });
            }
        }

        if (updatedFields.category) {
            mail.category = updatedFields.category;
        }

        const updated = await storeMails.updateMail(id, updatedFields);

        return res.status(200).location(`/api/mails/${mail.id}`).json(updated);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// DELETE /api/mails/:id
exports.deleteMail = async (req, res) => {
    const { id } = req.params;
    const userId = req.header('user-id');
    if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

    try {
        const mail = await storeMails.getMail(id);
        if (!mail || mail.owner !== userId) {
            return res.status(404).json({ error: 'Mail not found' });
        }

        await storeMails.deleteMail(id);
        res.status(204).send();
    } catch (err) {
        console.error(`[DELETE] Error deleting mail id=${id} for userId=${userId}:`, err);
        res.status(500).json({ error: err.message });
    }
};
