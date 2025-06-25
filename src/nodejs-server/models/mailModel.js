const mongoose = require('mongoose');
const AutoIncrement = require('mongoose-sequence')(mongoose);

// Define the Mails schema
const mailsSchema = new mongoose.Schema({
  user: { type: String, required: true },
  owner: { type: String, required: true },
  direction: { type: [String], required: true },
  subject: { type: String, required: false },
  body: { type: String, required: false },
  from: { type: String, required: true },
  to: { type: [String], required: false },
  timestamp: { type: Date, default: Date.now },
  isDeleted: { type: Boolean, default: false },
  isDraft: { type: Boolean, default: false },
  isStarred: { type: Boolean, default: false },
  isSpam: { type: Boolean, default: false },
  groupId: { type: String, default: null },
  isRead: { type: Boolean, default: false },
  category: { type: String, default: "Primary" }
});

// Add auto-incrementing `id` field
mailsSchema.plugin(AutoIncrement, {
  inc_field: 'id',
  id: 'mail_id_counter'
});

const Mails = mongoose.model('Mails', mailsSchema);

// Return all mails
const getAllMails = async () => {
  return await Mails.find().lean();
};

// Return mails that belong to a specific user
const getAllMailsByUser = async (userId) => {
  return await Mails.find({ user: userId }).sort({ timestamp: -1 }).limit(50).lean();
};

// Find and return a single mail by its ID
const getMail = async (id) => {
  return await Mails.findOne({ id }).lean();
};

// Create a new mail and store it
const createMail = async (mailData) => {
  const newMail = await Mails.create(mailData);
  const savedMail = await newMail.save();
  return savedMail.toObject();
};

// Update an existing mail by ID
const updateMail = async (id, updatedFields) => {
  const updated = await Mails.findOneAndUpdate({ id }, updatedFields);
  return updated;
};

// Delete a mail by ID
const deleteMail = async (id) => {
  const deleted = await Mails.findOneAndDelete({ id });
  return deleted;
};

// Export all methods
module.exports = {
  getAllMails,
  getMail,
  createMail,
  updateMail,
  deleteMail,
  getAllMailsByUser
};
