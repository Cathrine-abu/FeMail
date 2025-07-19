// Import the Express framework
const express = require('express');
const path = require('path');
const app = express();
const cors = require('cors');
const mongoose = require('mongoose');

const PORT = 8080;
const MONGO_URI = process.env.MONGO_URI || 'mongodb://mongo-container:27017/femail-db';

// MongoDB Connection
mongoose.connect(MONGO_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
})
.then(() =>
    // Start the server and listen for requests
    app.listen(PORT, () => {}))
.catch((err) => console.error('MongoDB connection error:', err));

// Middleware to parse incoming JSON requests with increased size limit
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));
app.use(cors());

// Import route handlers
const mailRoutes = require('./routes/mails');
const labelRoutes = require('./routes/labels');
const searchRoutes = require('./routes/search');
const userRoutes = require('./routes/users');
const tokenRoutes = require('./routes/tokens');
const blacklistRoutes = require('./routes/blacklist');

// Register routes
app.use('/api/mails', mailRoutes);           // For mail-related endpoints
app.use('/api/labels', labelRoutes);         // For label-related endpoints
app.use('/api/mails/search', searchRoutes);  // For search functionality
app.use('/api/users', userRoutes);           // For user-related endpoints    
app.use('/api/tokens', tokenRoutes);         // For token-related endpoints
app.use('/api/blacklist', blacklistRoutes);  // For blacklist-related endpoints
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
