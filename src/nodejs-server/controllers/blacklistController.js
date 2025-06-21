const http = require('http');
const storeUrls = require('../models/blacklistModel');

// POST /api/blacklist
exports.addBlacklistUrl = async (req, res) => {
  const { url } = req.body;

  // Check if url is missing
  if (!url) {
    return res.status(400).json({ error: "Missing URL" });
  }

  // Use the model function to add the url
  const response = await storeUrls.addBlacklistUrl(url);

  // Check returned status code and message
  const [statusCodeStr, ...messageParts] = response.split(' ');
  const statusCode = parseInt(statusCodeStr, 10);
  const message = messageParts.join(' ');

  // Return answer
  if (isNaN(statusCode)) {
    return res.status(500).send();
  }
  if (statusCode == 201) {
    return res.status(201).location(`/api/blacklist/${url}`).send();
  }
  if (statusCode == 400 && message != "Bad Request") {
    return res.status(statusCode).json({ error: message });
  }
  return res.status(statusCode).send();
};

// DELETE /api/blacklist/:id
exports.deleteBlacklistUrl = async (req, res) => {
  const { id } = req.params;
  const url = storeUrls.getUrl(id);

  // Check if url is missing
  if (!url) {
    return res.status(404).json({ error: "Missing URL" });
  }

  // Use the model function to delete the url
  const response = await storeUrls.deleteBlacklistUrl(url);
  
  // Check returned status code and message
  const [statusCodeStr, ...messageParts] = response.split(' ');
  const statusCode = parseInt(statusCodeStr, 10);
  const message = messageParts.join(' ');
  
  // Return answer
  if (isNaN(statusCode)) {
    return res.status(500).send();
  }
  if (statusCode == 400 && message != "Bad Request") {
    return res.status(statusCode).json({ error: message });
  }
  return res.status(statusCode).send();
};
