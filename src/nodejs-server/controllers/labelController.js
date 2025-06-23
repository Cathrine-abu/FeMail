// Import the in-memory data store
const storeLabels = require('../models/labelModel'); // This is the Mongoose model

// GET /api/labels
exports.getAllLabels = async (req, res) => {
  const userId = req.header('user-id');
  if (!userId) return res.status(400).json({ error: 'Missing user-id header' });

  try {
    const labels = await storeLabels.getAllLabels(userId);
    const showLabels = labels.map(({ id, name }) => ({ id, name }));
    res.status(200).json(showLabels);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// POST /api/labels
exports.sendLabel = async (req, res) => {
  const { name } = req.body;
  const userId = req.header('user-id');

  // Check if name is missing
  if (!name || !userId) {
    return res.status(400).json({ error: 'Missing label name or user-id' });
  }

  // Use the model function to create and store the label
  try {
    const newLabel = await storeLabels.createLabel(name, userId);
  // Respond with 201 Created and Location header
    res.status(201)
      .location(`/api/labels/${newLabel.id}`)
      .json({ id: newLabel.id });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// GET /api/labels/:id
exports.getLabelById = async (req, res) => {
  const userId = req.header('user-id');
  const { id } = req.params;

  try {
    const label = await storeLabels.getLabel(id);

    if (!label || label.userId !== userId) {
      return res.status(404).json({ error: 'Label not found or unauthorized' });
    }

    res.status(200).json({ id: label.id, name: label.name });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// PATCH /api/labels/:id
exports.updateLabel = async (req, res) => {
  const { id } = req.params;
  const userId = req.header('user-id');
  const { name } = req.body;

  if (!userId) {
    return res.status(400).json({ error: 'Missing user-id header' });
  }

  try {
    const updated = await storeLabels.updateLabel(id, name, userId);

    if (!updated) {
      return res.status(404).json({ error: 'Label not found or unauthorized' });
    }

    res.status(204).send();
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// DELETE /api/labels/:id
exports.deleteLabel = async (req, res) => {
  const userId = req.header('user-id');
  const { id } = req.params;

  try {
    const label = await storeLabels.getLabel(id);

    if (!label || label.userId !== userId) {
      return res.status(404).json({ error: 'Label not found or unauthorized' });
    }
    storeLabels.deleteLabel(id);
    res.status(204).send();
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
