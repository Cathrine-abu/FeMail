// Import Express framework and create a router instance
const express = require('express');
const router = express.Router();

// Import the controller that handles the search logic
const searchController = require('../controllers/searchController');

// Route: GET /api/mails/search/:query/
// Returns the mails that contains the result of the query 
router.get('/:query', searchController.searchMails);

// Export the router so it can be used in app.js
module.exports = router;