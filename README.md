# 📬 FeMail – Full-Stack Gmail-like Mail App (Web + Android)

## 📑 Contents
- [Introduction](#-introduction)
- [Architecture](#-architecture)
- [API Endpoints](#-api-endpoints)
- [Client Capabilities](#️-client-capabilities)
- [Project Structure](#-project-structure)
- [Setup, Deployment & Usage](#️-setup-deployment--usage)

## 📌 Introduction
FeMail is a **cross-platform, full-stack Gmail-like email system.**</br>
It offers a fully functional **webmail application** built with React, alongside a **native Android client**, both connected to a shared **RESTful backend** powered by **Node.js** and **MongoDB**.</br>
FeMail enables users to **register, log in, send, receive, edit, and manage emails and labels** in real time</br>
It supports common Gmail views like Inbox, Drafts, Sent, and Spam, and includes a built-in search function and spam filtering mechanism using a **Bloom Filter** TCP server.</br>
All emails and **user data are dynamically retrieved** from the backend – *no hardcoded data*.</br>
Authentication-protected routes use **JWT tokens**, and the backend stores all persistent data in a MongoDB database.</br>
The Android and web clients are both connected to the same backend and provide a **consistent user experience across platforms**.

## 🧱 Architecture
**🔍 Bloom Filter Server (C++)**</br>
- A standalone **TCP server written in C++** that implements a Bloom Filter to efficiently detect blacklisted URLs
- Responsible for **adding and deleting URLs** from the shared blacklist filter
- The blacklist data is **shared across all clients** (web and Android) to ensure consistent spam filtering
- Saves its data to a **designated file** for persistence
- Communicates with the backend over **TCP sockets**, returning a spam status for any URL passed to it

**🛠️ Backend API (Node.js + Express)**
- A **RESTful API** built using Node.js and Express, acting as the core of the system
- Handles all business logic: **users, emails, labels**, with full **create/edit/delete** functionality 
- Implements **JWT-based authentication** to secure routes
- Stores all persistent data in **MongoDB**
- Follows an **MVC architecture** to separate models, controllers, and routes
- Connects to the C++ blacklist server to filter out spam links before sending emails

**💻 Web App (React)**
- A dynamic, single-page Gmail-like client built with **React, HTML, CSS, and JavaScript**
- Communicates with the backend via REST API
- Renders emails, labels, and user data fetched from the server—**no hardcoded content**
- Provides *all* standard Gmail functionalities, including viewing, sending, editing, labeling emails, live search, and theme toggling
- Uses **JWT** stored in localStorage to authenticate requests

**📱 Android App (Java)**
- A native Android client built in **Android Studio using Java**
- Uses **Retrofit** to interact with the same Node.js backend as the web client
- Offers all Gmail functionalities identical to the web app: viewing, sending, editing, labeling emails, live search, and theme toggling
- Follows **MVVM architecture** for maintainability and testability
- Designed for a **Gmail-like experience** on mobile

## 📡 API Endpoints
### 👤 Users
* `POST /api/users` – Register a new user (with name, image, etc.)
* `GET /api/users/:id` – Get full user profile by ID
* `POST /api/tokens` – Login and receive JWT token (used for protected requests)

### ✉️ Mails
* `GET /api/mails` – Get 50 most recent mails (sent and received)
* `POST /api/mails` – Send a mail (includes automatic blacklist check)
* `GET /api/mails/:id` – Get mail details by ID
* `PATCH /api/mails/:id` – Update mail details by ID
* `DELETE /api/mails/:id` – Delete mail by ID

### 🏷️ Labels
* `GET /api/labels` – List all labels
* `POST /api/labels` – Create a new label
* `GET /api/labels/:id` – Get label details by ID
* `PATCH /api/labels/:id` – Update label details by ID
* `DELETE /api/labels/:id` – Delete label by ID

### 🔍 Search
* `GET /api/mails/search/:query` – Search mails by keyword

### 🚫 Blacklist
* `POST /api/blacklist/` – Add a URL to the blacklist
* `DELETE /api/blacklist/:id` – Delete a URL from the blacklist by ID
* (Automatically called by `POST /api/mails` to check URLs in sent mails)

## 🖥️ Client Capabilities
### 🧭 Available Screens
- Login / Register – Secure forms with input validation and image upload
- Mail Views – Navigate between Inbox, Sent, Drafts, Spam, and Starred categories via sidebar. view emails filtered by category with sorting
- Compose / Edit Email – Rich form to create or modify emails with recipients, subject and body
- Email Details – Full email content view with spam, starred, and delete actions
- Labels Management – Create, edit, delete labels and assign them to emails
- Search Results – Real-time search across emails by subject, body, sender, and recipients
- User Profile – View and edit user info, including profile image and mail
- Theme Toggle – Switch between light and dark mode via top bar toggle

### 🔐 Access Control
- JWT-based authentication secures the app
- Only **Register** and **Login** screens are accessible without authentication
- All other screens require a valid JWT token
- Protected routes automatically redirect unauthorized users to the login page
- Tokens are securely stored in **localStorage** (web) and **SharedPreferences** (Android)

### 🌓 Theme Mode
- Light and Dark mode toggle available
- Users can switch modes via a toggle in the top navigation bar
- Responsive design ensures good experience on both desktop and mobile

## 📁 Project Structure
* `data/` - Bloom filter URLs data file
* `.env` - Environment variables configuration file
* `src/android-app/` - Android mail client application
* `src/blacklist/` - Bloom filter implementation and TCP server in C++
* `src/femail-web/` - React web mail client application
* `src/mongodb/` - MongoDB data management setup and scripts
* `src/nodejs-server/` - Node.js REST API server with MVC structure (models, controllers, routes)
* `src/nodejs-server/utils/` - Utilities including TCP client socket for blacklist communication
* `src/tests/` - Test code for blacklist server
* `Dockerfile.blacklist` – Docker build file for the Bloom filter blacklist C++ server
* `Dockerfile.frontend` – Docker build file for the React web client
* `Dockerfile.web` – Docker build file for the Node.js backend API server
* `docker-compose.yml` - Docker Compose configuration for multi-container orchestration

## ⚙️ Setup, Deployment & Usage  
Instructions for running and deploying the application (both web and Android) can be found in [wiki/setup&deploy](wiki/setup&deploy.md)
### 🖥️ Web Usage  
Detailed explanation and user guide for the web application are available in [wiki/web-usage](wiki/web-usage.md)
### 📱 Android Usage  
Detailed explanation and user guide for the Android application are available in `[wiki/android-usage](wiki/android-usage.md)
