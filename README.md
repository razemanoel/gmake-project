
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

For detailed explanations of features, components, and usage examples, please refer to the project **Wiki**.  


How It All Fits Together
1.	Client Layer
o	The React web app and the Android app both communicate over HTTP with the Node.js API.
2.	API Layer
o	The Node.js server handles data storage (MongoDB), business logic, and JWT-based authentication.
o	For each spam/blacklist check, it opens a TCP connection to the Bloom-filter service.
3.	TCP Service
o	The C++ Bloom-filter server performs ultra-fast, memory-efficient blacklist checks and URL additions.
4.	Persistence
o	User accounts, emails, and labels are stored in MongoDB.
5.	Dev & Deployment
o	Docker Compose orchestrates all components (TCP service, API server, React UI) into a single, reproducible environment—no manual port wiring required.




•  backend/: C++ TCP server (Bloom filter)
•  api/: the Node.js/Express server talking to MongoDB and the C++ service
•  frontend/: React app that talks to the API
•  android/: the Android Studio project for the mobile client
•  config/.env.local: overridable env‐vars (ports, secrets, connection strings)
•  docker-compose.yml: builds & links the three Docker images into a single, live system.







Gmail-Style Application: Setup & Run Guide

-Clone the Repository
   
-Open terminal and run:

-git clone https://github.com/razemanoel/EX-5.git

-cd the-repo

-Create Environment Configuration

-Create a new folder and file for environment variables:

mkdir -p config
touch config/.env.local
Open config/.env.local in your editor and paste:
# Node.js API server
APP_PORT=3000
CONTAINER_PORT=3000

# C++ TCP (Bloom filter) server
TCP_PORT=5555

# MongoDB connection
CONNECTION_STRING=mongodb://host.docker.internal:27017

# JWT signing secret
JWT_SECRET="Vj4@7sF!9K#pLz^D2o7uN13X6A9Q5"

# React frontend
REACT_APP_API_URL=http://localhost:${APP_PORT}
REACT_PORT=3001
3. Build & Launch with Docker Compose
Your docker-compose.yml (v3.8) will build and start:
• tcpserver (C++ service) → TCP port 5555
• apiserver (Node.js API) → HTTP port 3000
• frontend (React web UI) → HTTP port 3001
Run:
docker-compose --env-file ./config/.env.local up --build
Wait until all containers are healthy.
Verify:
TCP server at localhost:5555
API at http://localhost:3000
Web UI at http://localhost:3001

Docker Compose will:
1. Parse docker-compose.yml to know which services to bring up.
2. Load every KEY=VALUE pair from config/.env.local and inject them into each container’s environment.
3. Substitute any ${VAR} placeholders in docker-compose.yml (if used) with those values.


To stop and remove containers:
docker-compose down
4. Run Services Individually
If you prefer to inspect logs or develop a single component:
A. C++ TCP Server:
cd backend
mkdir -p build && cd build
cmake ..
make
./tcpserver ${TCP_PORT}
B. Node.js API Server:
cd api
npm install
npm run dev   # or npm start
Visit http://localhost:${APP_PORT}
Expects the TCP server on ${TCP_PORT}
Connects to MongoDB at ${CONNECTION_STRING}
C. React Frontend:
cd frontend
npm install
npm start
Opens in browser at http://localhost:${REACT_PORT}
Uses API base URL: ${REACT_APP_API_URL}
5. Configure & Run the Android App
Open Android Studio → Open an Existing Project → select the android/ folder.
Create app/src/main/res/raw/config.properties with:
ip_address=10.0.2.2       # emulator → host machine; or your LAN IP on device
port=${APP_PORT}          # e.g. 3000
jwt_secret=${JWT_SECRET}
Update res/xml/network_security_config.xml:
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">10.0.2.2</domain>
  </domain-config>
</network-security-config>
Run the app on an emulator or physical device. It will connect to:
API at ${APP_PORT}
TCP service at ${TCP_PORT}








Docker:
1. backend/Dockerfile.tcpserver
dockerfile
CopyEdit
# Stage 1: build the C++ TCP (Bloom-filter) server
FROM gcc:latest AS builder

WORKDIR /usr/src/app

# Install CMake and other build tools
RUN apt-get update && \
    apt-get install -y cmake git && \
    rm -rf /var/lib/apt/lists/*

# Copy all sources and build
COPY . .
RUN rm -rf build && mkdir build && cd build && cmake ../backend && make

# Stage 2: runtime image
FROM gcc:latest AS runtime

WORKDIR /app

# (Optional) install any runtime deps 
RUN apt-get update && \
    apt-get install -y python3 && \
    rm -rf /var/lib/apt/lists/*

# Copy the compiled server and any data files
COPY --from=builder /usr/src/app/build/server ./server
COPY backend/data ./data

RUN chmod +x ./server

# Expose the TCP port (matches compose mapping)
EXPOSE 5555

# Default command: server <port> <other args if any>
CMD ["./server", "5555", "16", "1"]
________________________________________
2. api/Dockerfile.api
dockerfile
CopyEdit
FROM node:18

# Create application directory
WORKDIR /usr/api

# Install dependencies
COPY api/package*.json ./
RUN npm install

# Copy application code
COPY api/ .

# Expose the API port
EXPOSE 3000

# Start the server
CMD ["node", "server.js"]
________________________________________
3. frontend/Dockerfile.react
dockerfile
CopyEdit
# Stage 1: build the React app
FROM node:18 AS build

WORKDIR /usr/src/app

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ .
RUN npm run build

# Stage 2: serve the build
FROM node:18-alpine

WORKDIR /usr/src/app

# Install a simple static server
RUN npm install -g serve

# Copy built files
COPY --from=build /usr/src/app/build ./build

# Expose the frontend port
EXPOSE 3001

# Serve the React build
CMD ["serve", "-s", "build", "-l", "3001"]



Here’s the complete docker-compose.yml you’ll need alongside the Dockerfiles:
yaml
CopyEdit
version: '3.8'

services:
  tcpserver:
    build:
      context: .
      dockerfile: backend/Dockerfile.tcpserver
    ports:
      - "5555:5555"
    networks:
      - internal

  apiserver:
    build:
      context: .
      dockerfile: api/Dockerfile.api
    ports:
      - "3000:3000"
    depends_on:
      - tcpserver
    networks:
      - internal

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.react
    ports:
      - "3001:3001"
    depends_on:
      - apiserver
    networks:
      - internal

networks:
  internal:
•	Services
o	tcpserver: builds from backend/Dockerfile.tcpserver, exposing port 5555 for the C++ Bloom-filter TCP server.
o	apiserver: builds from api/Dockerfile.api, exposing port 3000 for the Node.js API (depends on tcpserver).
o	frontend: builds from frontend/Dockerfile.react, exposing port 3001 for the React web UI (depends on apiserver).
•	Network
All three share the internal network so they can communicate by service name (e.g., tcpserver:5555).
Place this file at the root of your project as docker-compose.yml, then run:
bash
CopyEdit
docker-compose --env-file ./config/.env.local up --build
to build and launch all three containers together.


