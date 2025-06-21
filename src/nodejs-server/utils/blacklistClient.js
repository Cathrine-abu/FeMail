const net = require('net')

// Get user arguments
const args = process.argv.slice(2);
const blacklistIP = args[0];
const blacklistPORT = parseInt(args[1], 10);

function connectAsync(client, port, host) {
  return new Promise((resolve, reject) => {
    client.connect(port, host, () => resolve());
    client.on('error', reject);
  });
}

function waitForEnd(client) {
  return new Promise((resolve, reject) => {
    let response = '';

    client.on('data', (data) => {
      response += data.toString();
    });

    client.on('end', () => resolve(response.trim()));
    client.on('close', () => resolve(response.trim()));
    client.on('error', reject);
  });
}

// Sends commands to the C++ blacklist server
async function sendToBlacklistServer(command) {
  const client = new net.Socket();

  try {
    await connectAsync(client, blacklistPORT, blacklistIP);  
    client.write(`${command}\n`);
    const response = await waitForEnd(client);
    client.destroy();
    return response;
  }
  catch (err) {
    client.destroy();
    throw err;
  }
}

// Export model API
module.exports = {
  sendToBlacklistServer,
};
