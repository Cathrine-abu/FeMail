const express = require('express');
const router = express.Router();

const tokensController = require('../controllers/tokensController');
// Login
router.route('/')
  .post(tokensController.loginUser);

module.exports = router;
