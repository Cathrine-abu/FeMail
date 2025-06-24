const store = require('../models/mailModel');

// GET /api/mails/search/:query
exports.searchMails = async (req, res) => {
  const { query } = req.params;
  const userId = req.header('user-id');

  if (!userId || !query) {
    return res.status(400).json({ error: 'Missing user-id header or  search query' });
  }

  const lowerQuery = query.toLowerCase();

  try {
    const userMails = await store.getAllMailsByUser(userId);
    const results = userMails.filter(mail => 
      (mail.subject || '').toLowerCase().includes(lowerQuery) ||
      (mail.body || '').toLowerCase().includes(lowerQuery) ||
      (mail.from || '').toLowerCase().includes(lowerQuery) ||
      (Array.isArray(mail.to)
        ? mail.to.some(recipient => recipient.toLowerCase().includes(lowerQuery))
        : (mail.to || '').toLowerCase().includes(lowerQuery))
    );
    res.status(200).json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
