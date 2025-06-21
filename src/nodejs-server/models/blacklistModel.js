const blacklistClient = require('../utils/blacklistClient');

// Return URL ID
const getUrl = (id) => {
  return id
}

// Return if a URL is blacklisted
const isBlacklisted = async (id) => {
  return await blacklistClient.sendToBlacklistServer(`GET ${id}`)
}

// Add a new URL to the blacklist
const addBlacklistUrl = async (url) => {
  return await blacklistClient.sendToBlacklistServer(`POST ${url}`)
};

// Delete URL from blacklist by ID
const deleteBlacklistUrl = async (id) => {
  return await blacklistClient.sendToBlacklistServer(`DELETE ${id}`)
};

// Export the CRUD functions
module.exports = {
  getUrl,
  isBlacklisted,
  addBlacklistUrl,
  deleteBlacklistUrl
};
