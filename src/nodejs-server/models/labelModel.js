const mongoose = require('mongoose');
const AutoIncrement = require('mongoose-sequence')(mongoose);

// Define the Labels schema + model
const labelsSchema = new mongoose.Schema({
  name: { type: String, required: true },
  userId: { type: String, required: true }
});

// Add auto-incrementing `id` field
labelsSchema.plugin(AutoIncrement, { inc_field: 'id' });

const Labels = mongoose.model('Labels', labelsSchema);

// CRUD functions
////
let labelId = 0;
const labels = [];

// Return a specific label by ID
const getLabel = async (id) => {
  return await Labels.findOne({ id }).lean();
};

// Create a new label with a unique ID for spacific user
const createLabel = async (name, userId) => {
  const newLabel = new Labels({ name, userId });
  const savedLabel = await newLabel.save();
  return savedLabel.toObject();
};

// Return all labels for a specific user
const getAllLabels = async (userId) => {
  return await Labels.find({ userId }).lean();
};

// Update an existing label (only the name field in this case)
const updateLabel = async (id, name, userId) => {
  const updated = await Labels.findOneAndUpdate(
    { id, userId },
    { name },
    { new: true }
  );
  return updated ? updated.toObject() : null;
};

// Delete a label by ID
const deleteLabel = async (id) => {
  const deleted = await Labels.findOneAndDelete({ id });
  return deleted ? deleted.toObject() : null;
};

// Export the CRUD functions
module.exports = {
  getAllLabels,
  getLabel,
  createLabel,
  updateLabel,
  deleteLabel,
};
