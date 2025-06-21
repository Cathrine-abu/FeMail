const users = []     // Array to store user data
let IdCounter = 0      // Variable to keep track of the next user ID

const registerUser = (username, password, full_name, image, phone, birth_date, gender) => {
  const newUser = { id: ++IdCounter, username, password, full_name, image, phone, birth_date, gender }
  users.push(newUser)
  return newUser
}

const getUserById = (id) => users.find(user => user.id === parseInt(id))

const findUserByUsername = (username) => users.find(user => user.username === username);

module.exports = {
  registerUser,
  getUserById,
  findUserByUsername
};


