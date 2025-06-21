// Import Express framework and create a router instance
const express = require('express');
const router = express.Router();

// Import the controller that handles the mail logic
const mailController = require('../controllers/mailController');
const authenticateToken = require('../middleware/auth');

router.get('/', authenticateToken, mailController.getAllMails);
router.post('/', authenticateToken, mailController.sendMail);
router.get('/:id', authenticateToken, mailController.getMailById);
router.patch('/:id', authenticateToken, mailController.updateMail);
router.delete('/:id', authenticateToken, mailController.deleteMail);

router.patch('/', (req, res) => {
    res.status(400).json({ error: 'Missing mail ID in URL' });
});
router.delete('/', (req, res) => {
    res.status(400).json({ error: 'Missing mail ID in URL' });
});


module.exports = router;