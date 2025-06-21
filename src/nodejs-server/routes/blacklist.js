// Import Express framework and create a router instance
const express = require('express');
const router = express.Router();

// Import the controller that handles the blacklist logic
const blacklistController = require('../controllers/blacklistController');

// Route: POST /api/blacklist
// Add url to blacklist
router.post('/', blacklistController.addBlacklistUrl);

// Route: DELETE /api/blacklist/:id
// Delete url from blacklist
router.delete('/:id', blacklistController.deleteBlacklistUrl);

// Export the router so it can be used in app.js
module.exports = router;
