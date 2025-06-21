const store = require('../models/mailModel');

// GET /api/mails/search/:query
exports.searchMails = (req, res) => {
  const { query } = req.params;
  const userId = req.header('user-id');

  if (!query) {
    return res.status(400).json({ error: 'Missing search query' });
  }

  if (!userId) {
    return res.status(400).json({ error: 'Missing user-id header' });
  }

  const lowerQuery = query.toLowerCase();

  const userMails = store.getAllMailsByUser(userId);

  const results = userMails.filter(mail => 
    (mail.subject || '').toLowerCase().includes(lowerQuery) ||
    (mail.body || '').toLowerCase().includes(lowerQuery) ||
    (mail.from || '').toLowerCase().includes(lowerQuery) ||
    (Array.isArray(mail.to)
      ? mail.to.some(recipient => recipient.toLowerCase().includes(lowerQuery))
      : (mail.to || '').toLowerCase().includes(lowerQuery))
  );

  res.status(200).json(results);
};
