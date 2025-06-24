const mongoose = require('mongoose');
const AutoIncrement = require('mongoose-sequence')(mongoose);

// Define the Labels schema
const usersSchema = new mongoose.Schema({
  username: { type: String, required: true },
  password: { type: String, required: true },
  full_name: { type: String, required: true },
  image: { type: String, required: true },
  phone: { type: String, required: true },
  birth_date: { type: Date, required: true },
  gender: { type: String, required: true }
});

// Add auto-incrementing `id` field
usersSchema.plugin(AutoIncrement, {
  inc_field: 'id',
  id: 'user_id_counter'
});

const Users = mongoose.model('Users', usersSchema);

const registerUser = async (username, password, full_name, image, phone, birth_date, gender) => {
  const newUser = new Users({ username, password, full_name, image, phone, birth_date, gender });
  const savedUser = await newUser.save();
  return savedUser.toObject();
}

const getUserById = async (id) => {
  return await Users.findOne({ id: parseInt(id) }).lean();
}

const findUserByUsername = async (username) => {
  return await Users.findOne({ username }).lean();
}

module.exports = {
  registerUser,
  getUserById,
  findUserByUsername
};


