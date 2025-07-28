#  Gmail-Project
## Overview
In this final phase, we completed the entire project, covering both frontend and backend development.
We have developed a fully functional Gmail-like email platform, available both as a web application and a native Android app with four components:
1. **C++ TCP server**  
   - A Bloom-filter–based blacklist service listening on a TCP port  
2. **Node.js API**  
   - Express server with MongoDB for user/mail storage, JWT authentication, and a TCP client to the C++ server  
3. **React frontend**  
   - Web UI (Inbox, Compose, Labels, Dark/Light mode) communicating with the Node.js API  
4. **Android app**  
   - Native mobile client (MVVM + Room + Retrofit) that connects to the same API and TCP services  

You can run the entire stack with Docker Compose or individually for development.

The system supports core Gmail-like functionality, including:

- User registration and login  
- Profile management  
- Email composition, sending, replying and forwarding  
- Draft saving and editing  
- Inbox, Sent, Drafts, Spam and Trash folders  
- Label creation, editing and assignment  
- Mail search and filtering  
- Soft and permanent deletion  

## For detailed explanations of features, components, and usage examples, please refer to the project Wiki.






## Project Architecture




### Core Components and How It All Fits Together:


### 1. **Client Layer**
- The React web app and the Android app both communicate over HTTP with the Node.js API.

### 2. **API Layer**
- The Node.js server handles data storage (MongoDB), business logic, and JWT-based authentication.
- For each spam/blacklist check, it opens a TCP connection to the Bloom-filter service.

### 3. **TCP Service**
- The C++ Bloom-filter server performs ultra-fast, memory-efficient blacklist checks and URL additions.

### 4. **Persistence**
- User accounts, emails, and labels are stored in MongoDB.

### 5. **Dev & Deployment**
- Docker Compose orchestrates all components (TCP service, API server, React UI) into a single, reproducible environment—no manual port wiring required.





### Project Structure

- **backend/**: C++ TCP server (Bloom filter)
- **api/**: the Node.js/Express server talking to MongoDB and the C++ service
- **frontend/**: React app that talks to the API
- **android/**: the Android Studio project for the mobile client
- **config/.env.local**: overridable env-vars (ports, secrets, connection strings)
- **docker-compose.yml**: builds & links the three Docker images into a single, live system
  



#  Setup & Run Guide


### 1. Clone the Repository
   
Open terminal and run:

```bash
git clone https://github.com/razemanoel/EX-5.git
cd the-repo
```

### 2. Create Environment Configuration

Create a new folder and file for environment variables:

```bash
mkdir -p config
touch config/.env.local
```

Open `config/.env.local` in your editor and paste:

```bash
#  Node.js API Server
API_PORT=3000              
JWT_SECRET="Vj4@7sF!9K#pLz^D2o7uN13X6A9Q5"

#  React Frontend 
FRONTEND_PORT=3001       
REACT_APP_API_URL=http://localhost:3000 

#  C++ TCP Blacklist Server 
TCP_PORT=5555               

# MongoDB 
MONGO_URI=mongodb://mongodb_service:27017 
MONGO_DATA_PATH=/data/db    
```

####  **Explanation of the variables:**

- **API_PORT**: Defines the port on your local machine (host) to access the Node.js server.This can be customized as needed during setup.
- **FRONTEND_PORT**: Defines the porton your local machine (host) to access the React web app.This can be customized as needed during setup.
- **TCP_PORT**: Defines the port where the C++ TCP Bloom-filter server listens for blacklist/spam checks. This can be customized as needed during setup.
- **MONGO_URI**: The full connection string for the MongoDB instance, used by the API server to persist user, mail, and label data.
- **MONGO_DATA_PATH**: Path inside the MongoDB container where the data will be stored. Used as a mount point for Docker volumes.
- **JWT_SECRET**: A secret key used by the Node.js API server to sign and verify JWT tokens for authentication. This should be a strong, unique string and kept private.
- **REACT_APP_API_URL**: Defines the way to get the api server(must be : http://localhost:{API_PORT} )

##  Docker Setup

### 3. Build & Launch with Docker Compose
   
Your docker-compose.yml (v3.8) will build and start:

• **tcpserver** (C++ service) → TCP port 5555  
• **apiserver** (Node.js API) → HTTP port 3000  
• **frontend** (React web UI) → HTTP port 3001  
• **mongodb_service** (MongoDB container) → port 27017  

Run:

```bash

docker-compose --env-file ./config/.env.local up --build
```

Wait until all containers are healthy.

####  Verify:

- TCP server at localhost:5555
- API at http://localhost:3000
- Web UI at http://localhost:3001

Docker Compose will:

1. Parse docker-compose.yml to know which services to bring up.
2. Load every KEY=VALUE pair from config/.env.local and inject them into each container's environment.
3. Substitute any ${VAR} placeholders in docker-compose.yml (if used) with those values.

####  To stop and remove containers:

```bash
docker-compose down
```

### 4. Run Services Individually

If you prefer to inspect logs or develop a single component, follow these detailed steps:

#### A. C++ TCP Server:

1. **Navigate to the backend directory**:
   ```bash
   cd backend
   ```

2. **Create and configure build directory**:
   ```bash
   mkdir -p build && cd build
   ```

3. **Generate build files and compile**:
   ```bash
   cmake ..
   make
   ```

4. **Run the TCP server**:
   ```bash
   ./tcpserver ${TCP_PORT}
   ```
   

**What to expect**: The server will display "TCP Server listening on port [PORT]" and wait for connections.

#### B. Node.js API Server:

1. **Navigate to the API directory**:
   ```bash
   cd api
   ```

2. **Install dependencies** (first time only):
   ```bash
   npm install
   ```

3. **Start the development server**:
   ```bash
   npm run dev   # Development mode with auto-reload
   # OR
   npm start     # Production mode
   ```

4. **Verify it's running**:
   - Visit http://localhost:${API_PORT}
   - The API expects the TCP server running on port ${TCP_PORT}
   - Connects to MongoDB at ${CONNECTION_STRING}

**What to expect**: Console will show "Server running on port [PORT]" and connection status to TCP server and MongoDB.

#### C. React Frontend:

1. **Navigate to the frontend directory**:
   ```bash
   cd frontend
   ```

2. **Install dependencies** (first time only):
   ```bash
   npm install
   ```

3. **Start the development server**:
   ```bash
   npm start
   ```

4. **Access the application**:
   - Automatically opens in browser at http://localhost:${REACT_PORT}
   - If not, manually navigate to the URL
   - Uses API base URL: ${REACT_APP_API_URL}

**What to expect**: Browser opens automatically, showing the React application interface.

#### Service Dependencies:
- **Frontend** depends on **API Server**
- **API Server** depends on **TCP Server** and **MongoDB**
- Start services in order: TCP Server → API Server → Frontend

##  Android Configuration

### 5. Pre-requirements:
1. **Install Android Studio**:
   - Download and install Android Studio if you haven't already from [https://developer.android.com/studio](https://developer.android.com/studio)

2. **Ensure Backend Services are Running**:
   - Make sure you have completed the Docker setup and all services are running:
     - TCP server at localhost:5555
     - API at http://localhost:3000
     - Web UI at http://localhost:3001
     - MongoDB service

### Configuration Steps:

#### 1. Open the Android Project:
- Open **Android Studio**
- Select **"Open an Existing Project"**
- Navigate to and select the `android/` folder from your project directory

#### 2. Create `config.properties` File:
- In Android Studio, navigate to: `app/src/main/res/`
- **Create a new directory** called `raw` (if it doesn't exist):
  - Right-click on `res` → New → Directory → name it `raw`
- **Create the configuration file**:
  - Right-click on the `raw` directory → New → File → name it `config.properties`

**Configure the following values based on your `config/.env.local` file:**

```properties
# Replace ${API_PORT} and ${JWT_SECRET} with actual values from config/.env.local
ip_address=10.0.2.2       # emulator → host machine; or your LAN IP on device
port=${API_PORT}          # Use the same value as API_PORT in config/.env.local (e.g. 3000)
jwt_secret=${JWT_SECRET}  # Use the same value as JWT_SECRET in config/.env.local
```

**ip_address**: Choose based on your setup:
- **Android Emulator**: Use `10.0.2.2` (this refers to your host machine from the emulator)
- **Physical Device**: Use your computer's local IP address

**To find your local IP address:**

 **Windows**:  
- Open Command Prompt (`Win + R`, type `cmd`, press Enter)  
- Run: `ipconfig`  
- Look for **"IPv4 Address"** under your network adapter (Wi-Fi or Ethernet)  
- Example output: `IPv4 Address. . . . . . . . . . . : 192.168.1.242`

 **macOS**:  
- Open Terminal  
- Run: `ifconfig`  
- Look for `inet` address under your network interface (typically `en0` for Wi-Fi)  
- Example output: `inet 192.168.1.242 netmask 0xffffff00 broadcast 192.168.1.255`

 **Linux**:  
- Open Terminal  
- Run: `ifconfig` (or `ip a` for newer systems)  
- Find `inet` address under your network interface (e.g., `wlan0` for Wi-Fi)  
- Example output: `inet 192.168.1.242 netmask 255.255.255.0 broadcast 192.168.1.255`

**port**: Use the same value as `API_PORT` in your `config/.env.local` file (default: 3000)

**jwt_secret**: Use the same value as `JWT_SECRET` in your `config/.env.local` file

**Example with actual values:**

```properties
ip_address=10.0.2.2
port=3000
jwt_secret=Vj4@7sF!9K#pLz^D2o7uN13X6A9Q5
```

#### 3. Update `network_security_config.xml`:
- Navigate to: `app/src/main/res/xml/`
- Open or create `network_security_config.xml`
- **Update the file** with the correct IP addresses for network security permissions:

**For Android Emulator:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">10.0.2.2</domain>
  </domain-config>
</network-security-config>
```

**For Physical Device (replace with your local IP):**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">192.168.1.242</domain>  <!-- Replace with your actual local IP -->
  </domain-config>
</network-security-config>
```

**For Both Emulator and Physical Device:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">10.0.2.2</domain>          <!-- For Android Emulator -->
    <domain includeSubdomains="true">192.168.1.242</domain>     <!-- Replace with your local IP -->
  </domain-config>
</network-security-config>
```

### Running the Android Application:

#### 4. Build and Run:
1. **Ensure all Docker services are running** (from the Docker setup section)
2. **In Android Studio**:
   - Wait for the project to sync and build
   - Connect an Android device via USB **OR** start an Android Virtual Device (AVD)
   - Click the **"Run"** button (green play icon) or press `Shift + F10`
   - Select your target device (emulator or physical device)

#### 5. Verify Connection:
The Android app will connect to:
- **API server** at the configured IP and port (e.g., `10.0.2.2:3000`)
- **TCP service** at port 5555 for spam/blacklist functionality

**Note**: Make sure the values in `config.properties` match your `config/.env.local` file and that the IP addresses in `network_security_config.xml` correspond to your setup (emulator vs physical device).

Once the app launches successfully, you should be able to access all Gmail-like functionality including:

- User registration and login
- Profile management
- Email composition, sending, replying and forwarding
- Draft saving and editing
- Inbox, Sent, Drafts, Spam and Trash folders
- Label creation, editing and assignment
- Mail search and filtering
- Soft and permanent deletion

##  Docker Configuration Files

####  Services Overview:
- **tcpserver**: builds from backend/Dockerfile.tcpserver, exposing port 5555 for the C++ Bloom-filter TCP server.
- **apiserver**: builds from api/Dockerfile.api, exposing port 3000 for the Node.js API (depends on tcpserver).
- **frontend**: builds from frontend/Dockerfile.react, exposing port 3001 for the React web UI (depends on apiserver).

####  Network:
All three share the internal network so they can communicate by service name (e.g., tcpserver:5555).

The **`docker-compose.yml`** at the project root then wires them all together on a single network.  
To build and run your full stack:

```bash
docker-compose --env-file ./config/.env.local up --build
```

---

##  Quick Start Summary

1. **Clone** the repository
2. **Create** environment configuration
3. **Build & Launch** with Docker Compose
4. **Verify** all services are running
5. **Configure** Android app




