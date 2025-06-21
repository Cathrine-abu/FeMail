// This module manages user data, including adding new users and retrieving user information by ID or username.
const express = require('express');
const router = express.Router();

const usersController = require('../controllers/usersController');

// Registration
router.route('/')
  .post(usersController.registerUser);
  
router.route('/:id')
  .get(usersController.getUserById);

router.route('/username/:username')
  .get(usersController.getUserByUsername);

module.exports = router;