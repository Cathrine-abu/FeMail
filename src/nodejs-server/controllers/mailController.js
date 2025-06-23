// Import the in-memory data store (acts as the "model")
const storeMails = require('../models/mailModel');

// Import the blacklist checker function via the model
const blacklistModel = require('../models/blacklistModel');

// Import the users via the model
const { getUserById, findUserByUsername } = require('../models/usersModel');

// GET /api/mails
exports.getAllMails = (req, res) => {
    const userId = req.header('user-id');
    if (!userId) {
        return res.status(400).json({ error: 'Missing user-id header' });
    }

    const userMails = storeMails.getAllMailsByUser(userId);
    const recentMails = userMails.slice(-50).reverse();
    res.status(200).json(recentMails);
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
    const urls = [
        ...(subject.match(urlRegex) || []),
        ...(body.match(urlRegex) || [])
    ];

    isSpam = false;

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
    // generate groupId to connect sent/received if from === to
    const groupId = `${Date.now()}_${from}_${Math.floor(Math.random() * 100000)}`;

    if (!isDraft) {

    recipients.forEach(recipient => {
        const recipientUser = findUserByUsername(recipient);
        if (!recipientUser) return;

        storeMails.createMail({
            subject,
            body,
            from: getUserById(from).username,
            to: recipient,
            user: recipientUser.id.toString(),
            owner: recipientUser.id.toString(),
            direction: ['received'],
            timestamp,
            isDeleted: false,
            isDraft: false,
            isStarred: false,
            isSpam,
            groupId: (getUserById(from).username === recipient) ? groupId : null,
            isRead: false
        });
    });
}


    const sentMail = storeMails.createMail({
        subject,
        body,
        from: getUserById(from).username,
        to: recipients,
        user: from,
        owner: from,
        direction: Array.isArray(direction) ? direction : [direction || "sent"],
        timestamp,
        isDeleted: false,
        isDraft: isDraft || false,
        isStarred: false,
        isSpam,
        groupId: (recipients.includes(getUserById(from).username)) ? groupId : null,
        isRead: false
    });

    return res.status(201).location(`/api/mails/${sentMail.id}`).json({id: `${sentMail.id}`,
        isSpam: `${sentMail.isSpam}`, timestamp: `${sentMail.timestamp}`});
};

// GET /api/mails/:id
exports.getMailById = (req, res) => {
 const { id } = req.params;
    const userId = req.header('user-id');
    
    if (!userId) {
        return res.status(400).json({ error: 'Missing user-id header' });
    }

    const mail = storeMails.getMail(id);

    if (!mail || mail.owner !== userId) {
        return res.status(404).json({ error: 'Mail not found' });
    }

    res.status(200).json(mail);
};

// PATCH /api/mails/:id
exports.updateMail = async (req, res) => {
    const { id } = req.params;
    const userId = req.header('user-id');
    if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

    const mail = storeMails.getMail(id);
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
    updatedFields.isSpam = isSpam;
    const wasDraft = mail.isDraft;
    const nowSent = updatedFields.isDraft === false;

    if (wasDraft && nowSent) {
        updatedFields.from = getUserById(userId).username;

        const recipients = Array.isArray(updatedFields.to) ? updatedFields.to : [updatedFields.to];
        const timestamp = new Date().toISOString();

        recipients.forEach((recipient) => {
            const recipientUser = findUserByUsername(recipient);
            if (!recipientUser) return;

            storeMails.createMail({
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

    const updated = storeMails.updateMail(id, updatedFields);

    return res.status(200).location(`/api/mails/${mail.id}`).json({
        id: `${updated.id}`,
        isSpam: `${updated.isSpam}`,
        timestamp: `${updated.timestamp}`
    });
};

// DELETE /api/mails/:id
exports.deleteMail = (req, res) => {
    const { id } = req.params;
    const userId = req.header('user-id');
    if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

    const mail = storeMails.getMail(id);
    if (!mail || mail.owner !== userId) return res.status(404).json({ error: 'Mail not found' });

    storeMails.deleteMail(id);
    res.status(204).send();
};

