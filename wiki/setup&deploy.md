# 🚀 Running the Application
This guide explains how to run the app

## ⚙️ Setup and Usage
1. Clone the repository:
   ```bash
   git clone https://github.com/Fisher-Shira/FeMail.git
   ```
2. Navigate to the project directory:
   ```bash
   cd FeMail
   ```
3. Open Docker Desktop on your computer
4. Change the `.env` file as you wish (or keep the default values)
   **Fields explanation**
   * All fields are mandatory — don't delete any of the given fields
   * The fields:
     * `BLACKLIST_IP` - The IP that the blacklist server is running on
       * If the blacklist server is on localhost, use `host.docker.internal`
       * Else, find your IP:
         - `hostname -I` command on Linux
         - `ipconfig` command on Windows
     * `BLACKLIST_PORT` - The port you want the blacklist server to run on
     * `NUM_OF_BITS` - The number of bits for the Bloom filter array
     * `HASH_ARGS` - Series of numbers that describe the hash (as many hashes as you want)
     * `FRONTEND_PORT` - The port you want the frontend to run on
     * `JWT_SECRET`: Secret key used to sign JWT tokens
     * `JWT_EXPIRATION`: JWT token expiration time (e.g., 1h for 1 hour)
     * `MONGO_URI`: MongoDB connection URI pointing to your database container or instance
5. Run the app using Docker Compose (Linux and Windows):
   ```bash
   docker-compose up --build
   ```
   **💡 Clarifications**
   * The Node.js (web) server runs on port **8080**
   * Please make sure that port `8080` is available on your machine (i.e., not used by other applications or blocked by firewall) and **don’t use this port in the `.env` file**

6. Wait for Docker Compose to finish starting all containers (`blacklistserver`, `web`, `frontend`)
   Once everything is up, the app is ready to use

## 🖥️ Using Web Application
After completing all [Setup and Usage](#setup-and-usage) steps
1. open your browser and go to:
   - `http://localhost:FRONTEND_PORT` — to access the web app from your local machine
   - `http://<your-ip>:FRONTEND_PORT` — to access it from another device on the same network</br>
`FRONTEND_PORT` is the same port you set in the `.env` file</br>
2. ✅ You can now start using the full application!</br>
For Detailed explanation and user guide for the web application, see [wiki/web-usage](web-usage.md)

## 📱 Using Android Application
After completing all [Setup and Usage](#setup-and-usage) steps
1. Open the Android project in **Android Studio**
   - Allow Gradle to sync if needed
2. Launch the emulator
   - Start an emulator from the **AVD Manager**, or connect a physical device with debugging enabled
3. Run the app
   - Click the green **Run** button in Android Studio, or press **Shift + F10**
   - The app should build and launch on your emulator/device
4. ✅ You can now start using the full application!</br>
For Detailed explanation and user guide for the web application, see [wiki/android-usage](android-usage.md)