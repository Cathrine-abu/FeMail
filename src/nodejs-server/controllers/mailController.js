const storeMails = require('../models/mailModel');

// Import the blacklist checker function
const blacklistModel = require('../models/blacklistModel');

// Import the users
const { getUserById, findUserByUsername } = require('../models/usersModel');

// GET /api/mails
exports.getAllMails = async (req, res) => {
    const userId = req.header('user-id');
    if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

    try {
        const userMails = await storeMails.getAllMailsByUser(userId);
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

    // If more than one recipient
    const recipients = Array.isArray(to) ? to : [to];

    // Check URLs in both subject and body
    const urlRegex = /(https?:\/\/)?(www\.)?[^\s]+\.[^\s]+/g;
    const urls = [...(subject.match(urlRegex) || []), ...(body.match(urlRegex) || [])];

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
    // generate groupId to connect sent/received if from === to
    const groupId = `${Date.now()}_${from}_${Math.floor(Math.random() * 100000)}`;

    const from_user = await getUserById(from);

    try {
        if (!isDraft) {
            for (const recipient of recipients) {
                const recipientUser = await findUserByUsername(recipient);
                if (!recipientUser) continue;


                await storeMails.createMail({
                    subject,
                    body,
                    from: from_user.username,
                    to: recipient,
                    user: recipientUser.id.toString(),
                    owner: recipientUser.id.toString(),
                    direction: ['received'],
                    isSpam,
                    groupId: (from_user.username === recipient) ? groupId : null
                });
            }
        }

        const sentMail = await storeMails.createMail({
            subject,
            body,
            from: from_user.username,
            to: recipients,
            user: from,
            owner: from,
            direction: Array.isArray(direction) ? direction : [direction || "sent"],
            isDraft: isDraft || false,
            isSpam,
            groupId: (recipients.includes(from_user.username)) ? groupId : null
        });

        return res.status(201).location(`/api/mails/${sentMail.id}`).json({id: `${sentMail.id}`,
            isSpam: `${sentMail.isSpam}`, timestamp: `${sentMail.timestamp}`});
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// GET /api/mails/:id   
exports.getMailById = async (req, res) => {
    const { id } = req.params;
    const userId = req.header('user-id');
    
    if (!userId) {
        return res.status(400).json({ error: 'Missing user-id header' });
    }

    try {
        const mail = await storeMails.getMail(id);
    
        if (!mail || mail.owner !== userId) {
          return res.status(404).json({ error: 'Mail not found or unauthorized' });
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

    try {
        const mail = await storeMails.getMail(id);
        if (!mail || mail.owner !== userId) return res.status(404).json({ error: 'Mail not found' });

        updatedFields.isSpam = isSpam;
        const wasDraft = mail.isDraft;
        const nowSent = updatedFields.isDraft === false;

        if (wasDraft && nowSent) {
            const user = await getUserById(userId);
            if (!user) return res.status(404).json({ error: 'User not found' });
            
            updatedFields.from = user.username;

            const recipients = Array.isArray(updatedFields.to) ? updatedFields.to : [updatedFields.to];
            const timestamp = new Date().toISOString();

            recipients.forEach(async (recipient) => {
                const recipientUser = await findUserByUsername(recipient);
                if (!recipientUser) return;

                await storeMails.createMail({
                    subject: updatedFields.subject,
                    body: updatedFields.body,
                    from: updatedFields.from,
                    to: recipient,
                    user: recipientUser.id.toString(),
                    owner: recipientUser.id.toString(),
                    direction: ['received'],
                    timestamp,
                    isDeleted: false,
                    isDraft: false,
                    isStarred: false,
                    isSpam: isSpam,
                    groupId: null,
                    isRead: false
                });
            });
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
