// Import the in-memory data store
const storeLabels = require('../models/labelModel');

// GET /api/labels
// Returns all existing labels for user
exports.getAllLabels = (req, res) => {
 const userId = req.header('user-id');
  if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

  const labels = storeLabels.getAllLabels(userId);
  const showLabels = labels.map(({ id, name }) => ({ id, name }));
  res.status(200).json(showLabels);
};

// POST /api/labels
// Adds a new label to the memory store
exports.sendLabel = (req, res) => {
  const { name } = req.body;
  const userId = req.header('user-id');

  // Check if name is missing
  if (!name || !userId) {
    return res.status(400).json({ error: 'Missing label name or user-id' });
  }

  // Use the model function to create and store the label
  const newLabel = storeLabels.createLabel(name, userId);

  // Respond with 201 Created and Location header
  res.status(201)
    .location(`/api/labels/${newLabel.id}`)
    .json({id: `${newLabel.id}`});
};

// GET /api/labels/:id
exports.getLabelById = (req, res) => {
  const userId = req.header('user-id');
  const { id } = req.params;
  const label = storeLabels.getLabel(id);

  if (!label || label.userId !== userId) {
    return res.status(404).json({ error: 'Label not found or unauthorized' });
  }

  res.status(200).json({ id: label.id, name: label.name });
};

// PATCH /api/labels/:id
exports.updateLabel = (req, res) => {
  const { id } = req.params;
  const userId = req.header('user-id');
  const { name } = req.body;

  if (!userId) {
    return res.status(400).json({ error: 'Missing user-id header' });
  }

  const updated = storeLabels.updateLabel(id, name, userId);

  if (!updated) {
    return res.status(404).json({ error: 'Label not found or unauthorized' });
  }

  res.status(204).send();
};


// DELETE /api/labels/:id
exports.deleteLabel = (req, res) => {
  const userId = req.header('user-id');
  const { id } = req.params;
  const label = storeLabels.getLabel(id);
  
  if (!label || label.userId !== userId) {
    return res.status(404).json({ error: 'Label not found or unauthorized' });
  }
  
  storeLabels.deleteLabel(id);
  res.status(204).send();
};
