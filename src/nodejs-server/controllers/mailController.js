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
        const userMails = await storeMails.getAllMailsByUser(userId);
        const recentMails = userMails.slice(-50).reverse();
        res.status(200).json(recentMails);
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
    const urls = [
        ...(subject.match(urlRegex) || []),
        ...(body.match(urlRegex) || [])
    ];

    let isSpam = false;

    for (const url of urls) {
        try {
            const response = await blacklistModel.isBlacklisted(url);
            const parts = response.split('\n\n');
            const body = parts[parts.length - 1].trim();
            const result = {
                ok: true,
                blacklisted: body === 'true true'
            };
            if (!result.ok) {
                return res.status(500).json({ error: 'Blacklist check failed' });
            }
            if (result.blacklisted) {
                isSpam = true;
            }
        } catch (err) {
            return res.status(500).json({ error: 'Blacklist check failed' });
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
                direction: ['received'],
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
        ? (isDraft ? ['draft', 'received'] : ['sent', 'received'])
        : Array.isArray(direction)
            ? direction
            : [direction || (isDraft ? 'draft' : 'sent')];


    const sentMail = storeMails.createMail({
        subject,
        body,
        from: senderUser.username,
        to: recipients,
        user: from,
        owner: from,
        direction: finalDirection,
        timestamp,
        isDeleted: false,
        isDraft: isDraft || false,
        isStarred: false,
        isSpam,
        groupId: isSelfMail ? groupId : null,
        isRead: false
    });

    return res.status(201).location(`/api/mails/${sentMail.id}`).json({
        id: `${sentMail.id}`,
        isSpam: `${sentMail.isSpam}`,
        timestamp: `${sentMail.timestamp}`
    });
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
        const urlsToCheck = [];
        const urlRegex = /(https?:\/\/)?(www\.)?[^\s]+\.[^\s]+/g;

        if (updatedFields.subject) {
            urlsToCheck.push(...(updatedFields.subject.match(urlRegex) || []));
        }
        if (updatedFields.body) {
            urlsToCheck.push(...(updatedFields.body.match(urlRegex) || []));
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
            const urlsToAppend = urlsToCheck.join('\n') + '\n';
            fs.appendFile(urlsFile, urlsToAppend, (err) => {
                if (err) {
                    console.error('Failed to append URLs to urls.txt:', err);
                }
            });
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

        return res.status(200).location(`/api/mails/${mail.id}`).json({
            id: `${updated.id}`,
            isSpam: `${updated.isSpam}`,
            timestamp: `${updated.timestamp}`
        });
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
        if (!mail || mail.owner !== userId) return res.status(404).json({ error: 'Mail not found' });

        await storeMails.deleteMail(id);
        res.status(204).send();
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
