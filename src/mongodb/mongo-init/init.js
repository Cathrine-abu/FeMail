db = db.getSiblingDB('femail-db');

db.createCollection('labels');
db.createCollection('mails');
db.createCollection('users');
