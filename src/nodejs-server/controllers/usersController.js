const usersModel = require('../models/usersModel');

// POST /api/users – Register new user
exports.registerUser = (req, res) => {
  try {
    const { username, password, full_name, image, phone, birth_date, gender } = req.body;
    
    // Validate presence of all fields
    if (!username || !password || !full_name || !phone || !birth_date || !gender || !image) {
      return res.status(400).json({ error: 'Missing one or more required fields' });
    }

    // Validate image is base64
    if (!image.startsWith('data:image/')) {
      return res.status(400).json({ error: 'Invalid image format. Must be base64 encoded image.' });
    }

    // Username: must contain only lowercase letters and numbers, and at least 3 letters
    if (!/^[a-z0-9]+$/.test(username) || (username.match(/[a-z]/g) || []).length < 3) {
      return res.status(400).json({ error: 'Invalid username format' });
    }

    // Password: at least 8 characters, must contain letters and digits
    if (
      typeof password !== 'string' ||
      password.length < 8 ||
      !/\d/.test(password) ||
      !/[A-Za-z]/.test(password)
    ) {
      return res.status(400).json({ error: 'Password must be at least 8 characters and include letters and numbers' });
    }

    // Phone: must be exactly 10 digits
    if (!/^\d{10}$/.test(phone)) {
      return res.status(400).json({ error: 'Phone number must contain exactly 10 digits' });
    }

    // Birth date: must be a valid date and user must be at least 14 years old
    const birth = new Date(birth_date);
    const today = new Date();
    const age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    const dayDiff = today.getDate() - birth.getDate();
    const is14OrOlder = age > 14 || (age === 14 && (monthDiff > 0 || (monthDiff === 0 && dayDiff >= 0)));
    if (isNaN(birth.getTime()) || !is14OrOlder) {
      return res.status(400).json({ error: 'User must be at least 14 years old' });
    }

    // Gender: must be one of the allowed values
    const validGenders = ['male', 'female', 'other'];
    if (!validGenders.includes(gender)) {
      return res.status(400).json({ error: 'Invalid gender value' });
    }

    // Check if the username already exists
    const existingUser = usersModel.findUserByUsername(username);
    if (existingUser) {
      return res.status(400).json({ error: 'Username already exists' });
    }

    // Register new user
    const newUser = usersModel.registerUser(username, password, full_name, image, phone, birth_date, gender);
    res.status(201).json({ message: 'User registered successfully', user: newUser });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
};

// GET /api/users/:id – Get user details
exports.getUserById = (req, res) => {
  try {
    const userId = parseInt(req.params.id);
    const user = usersModel.getUserById(userId);

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    // Return public user data (excluding password)
    const { id, username, full_name, image, phone, birth_date, gender } = user;
    res.status(200).json({ id, username, full_name, image, phone, birth_date, gender });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
};

// GET /api/users/username/:username – Get user details by username
exports.getUserByUsername = (req, res) => {
  try {
    const username = req.params.username;
    const user = usersModel.findUserByUsername(username);

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    // Return public user data (excluding password)
    const { id, username: uname, full_name, image, phone, birth_date, gender } = user;
    res.status(200).json({ id, username: uname, full_name, image, phone, birth_date, gender });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
};