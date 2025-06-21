// Import Express framework and create a router instance
const express = require('express');
const router = express.Router();

// Import the controller that handles the label logic
const labelController = require('../controllers/labelController');

// Route: GET /api/mails
// Returns all the label stored in memory
router.get('/', labelController.getAllLabels);

// Route: POST /api/mails
// Accepts new label and adds it to memory
router.post('/', labelController.sendLabel);

router.patch('/', (req, res) => {
    res.status(400).json({ error: 'Missing label ID in URL' });
});
router.delete('/', (req, res) => {
    res.status(400).json({ error: 'Missing label ID in URL' });
});

router.get('/:id', labelController.getLabelById);
router.patch('/:id', labelController.updateLabel);
router.delete('/:id', labelController.deleteLabel);

// Export the router so it can be used in app.js
module.exports = router;
