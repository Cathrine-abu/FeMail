let labelId = 0;
const labels = [];

// Return a specific label by ID
const getLabel = (id) => labels.find(l => l.id === id);

// Create a new label with a unique ID for spacific user
const createLabel = (name, userId) => {
  const newLabel = { id: (++labelId).toString(), name, userId};
  labels.push(newLabel);
  return newLabel;
};

// Return all labels for a specific user
const getAllLabels = (userId) => {
  return labels.filter(label => label.userId === userId);
};

// Update an existing label (only the name field in this case)
const updateLabel = (id, name, userId) => {
  const index = labels.findIndex(l => l.id === id && l.userId === userId);
  if (index === -1) return null;

  labels[index].name = name;
  return labels[index];
};


// Delete a label by ID
const deleteLabel = (id) => {
  const index = labels.findIndex(l => l.id === id);
  if (index === -1) return null;

  return labels.splice(index, 1)[0]; // return the deleted label
};

// Export the CRUD functions
module.exports = {
  getAllLabels,
  getLabel,
  createLabel,
  updateLabel,
  deleteLabel,
};
