let mailId = 0;
const mails = [];

// Return all mails
const getAllMails = () => mails;

// Return mails that belong to a specific user
const getAllMailsByUser = (userId) => mails.filter(m => m.user === userId);

// Find and return a single mail by its ID
const getMail = (id) => mails.find(m => m.id === id);

// Create a new mail and store it
const createMail = (mailData) => {
  const mail = {
    id: (++mailId).toString(),
    user: mailData.user,
    owner: mailData.owner,        
    direction: mailData.direction, 
    subject: mailData.subject,
    body: mailData.body,
    from: mailData.from,
    to: mailData.to,
    timestamp: mailData.timestamp,
    isDeleted: mailData.isDeleted,
    isDraft: mailData.isDraft,
    isStarred: mailData.isStarred,
    isSpam: mailData.isSpam,
    groupId: mailData.groupId,
    isRead: mailData.isRead,
    category: mailData.category || "Primary"

  };
  mails.push(mail);
  return mail;
};

// Update an existing mail by ID
const updateMail = (id, updatedFields) => {
  const index = mails.findIndex(m => m.id === id);
  if (index === -1) return null;

  mails[index] = {
    ...mails[index],
    ...updatedFields
  };
  return mails[index];
};

// Delete a mail by ID
const deleteMail = (id) => {
  const index = mails.findIndex(m => m.id === id);
  if (index === -1) return null;

  return mails.splice(index, 1)[0];
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
