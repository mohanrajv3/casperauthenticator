# 🔐 CASPER Authenticator - Complete Implementation Guide

A complete working implementation of **CASPER (Passkey Breach Detection)** inspired by the USENIX Security paper "CASPER: Passkey Breach Detection". This project demonstrates how to detect when encrypted passkey data is stolen from cloud storage.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [CASPER Algorithm Explained](#casper-algorithm-explained)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [Setup Instructions](#setup-instructions)
7. [How to Run](#how-to-run)
8. [Usage Guide](#usage-guide)
9. [Testing CASPER Breach Detection](#testing-casper-breach-detection)
10. [Deployment](#deployment)
11. [API Documentation](#api-documentation)
12. [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

This project implements a complete CASPER-based authenticator system with:

- **Android App** (Java) - User interface for passkey management
- **PMS Backend** (Spring Boot) - Cloud passkey storage service
- **RP Backend** (Spring Boot) - Relying party with breach detection

### Key Features

✅ **CASPER Implementation**: Real + decoy passkey approach  
✅ **Cloud Backup**: Encrypted passkey storage in PMS  
✅ **Breach Detection**: Automatic detection of stolen passkey abuse  
✅ **Cryptographic Security**: HKDF, ECDSA, secure key derivation  
✅ **Beginner-Friendly**: Well-documented, educational code  

---

## 🏗️ Architecture

```
┌─────────────────┐
│  Android App    │
│  (Java)         │
│                 │
│  - PIN Setup    │
│  - Key Gen      │
│  - Encryption   │
│  - Signing      │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│  PMS   │ │   RP   │
│(Port   │ │(Port   │
│ 8080)  │ │ 8081)  │
└────────┘ └────────┘
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│  DB    │ │  DB    │
│(H2/    │ │(H2/    │
│Postgres)│ │Postgres)│
└────────┘ └────────┘
```

### Components

1. **Android App**: Generates passkeys, encrypts using CASPER, communicates with PMS and RP
2. **PMS (Passkey Management Service)**: Stores encrypted passkey data (never decrypts)
3. **RP (Relying Party)**: Registers passkeys, verifies logins, detects breaches

---

## 🧠 CASPER Algorithm Explained

### Simple Explanation

CASPER detects when attackers steal encrypted passkey data from cloud storage and try to use it.

**How it works:**

1. **Setup Phase:**
   - User sets a PIN (low-entropy, 4-6 digits)
   - App generates k detection secrets (W) - one real (w*), others are decoys
   - Real secret is selected using PIN: `w* = W[H(PIN) mod k]`

2. **Registration Phase:**
   - Generate ECDSA key pair (real passkey)
   - Encrypt private key: `s̃ = HKDF(w*, z) XOR s`
   - Generate decoy passkeys using fake secrets
   - Upload encrypted passkey to PMS (s̃, W, z)
   - Register all public keys with RP (real + decoys)

3. **Login Phase:**
   - User enters PIN → selects real secret (w*)
   - Decrypt private key: `s = HKDF(w*, z) XOR s̃`
   - Sign challenge with private key
   - Send signature to RP

4. **Breach Detection (CD Algorithm):**
   - RP checks: Is login key in trap key set V'?
   - If YES → **BREACH DETECTED** (attacker used decoy secret)
   - If NO → Normal login (user used real secret)

### Why This Works

- **Attacker Scenario:** Steals encrypted data from PMS, doesn't know PIN
- **Attacker tries wrong PIN** → selects decoy secret → generates fake passkey
- **Fake passkey public key** → matches trap key in RP → **BREACH DETECTED** ✅

---

## 📁 Project Structure

```
CasperAuthenticator/
├── app/                                    # Android App
│   ├── src/main/java/com/casper/authenticator/
│   │   ├── WelcomeActivity.java           # Entry screen
│   │   ├── PinSetupActivity.java          # PIN setup
│   │   ├── HomeActivity.java              # Main menu
│   │   ├── RegisterActivity.java          # Register passkey
│   │   ├── LoginActivity.java             # Login with passkey
│   │   ├── RestoreActivity.java           # Restore from cloud
│   │   ├── crypto/
│   │   │   ├── CasperCrypto.java          # CASPER crypto implementation
│   │   │   ├── KeyGenerator.java          # ECDSA key generation
│   │   │   └── HKDFHelper.java            # HKDF key derivation
│   │   ├── models/
│   │   │   ├── PasskeyData.java           # Passkey data model
│   │   │   └── DetectionSecrets.java      # Detection secrets model
│   │   └── network/
│   │       ├── PMSApi.java                # PMS API interface
│   │       ├── RPApi.java                 # RP API interface
│   │       └── ApiClient.java             # Retrofit client
│   └── src/main/res/
│       └── layout/                        # UI layouts
│
├── pms-backend/casper-pms/                # PMS Service
│   ├── src/main/java/com/casper/pms/
│   │   ├── CasperPmsApplication.java
│   │   ├── controller/
│   │   │   └── PasskeyController.java     # REST endpoints
│   │   ├── model/
│   │   │   └── EncryptedPasskey.java      # Entity
│   │   ├── repository/
│   │   │   └── PasskeyRepository.java     # JPA repository
│   │   └── service/
│   │       └── PasskeyService.java        # Business logic
│   └── src/main/resources/
│       ├── application.properties
│       └── schema.sql                     # Database schema
│
├── rp-backend/casper-rp/                  # RP Service
│   ├── src/main/java/com/casper/rp/
│   │   ├── CasperRpApplication.java
│   │   ├── controller/
│   │   │   ├── AuthController.java        # Auth endpoints
│   │   │   └── WebController.java         # Web pages
│   │   ├── model/
│   │   │   ├── Passkey.java               # Entity
│   │   │   └── LoginAttempt.java          # Entity
│   │   ├── repository/
│   │   │   ├── PasskeyRepository.java
│   │   │   └── LoginAttemptRepository.java
│   │   └── service/
│   │       └── CasperDetectionService.java # CASPER detection logic
│   └── src/main/resources/
│       ├── application.properties
│       ├── schema.sql
│       └── templates/
│           ├── index.html                 # Demo page
│           └── breach.html                # Breach alert page
│
└── README.md                              # This file
```

---

## 📦 Prerequisites

### For Android App

- **Android Studio** (Arctic Fox or newer)
- **JDK 17** or newer
- **Android SDK** (API Level 24+)
- **Android device or emulator**

### For Backend Services

- **Java 17** or newer
- **Maven 3.6+**
- **PostgreSQL 12+** (optional, H2 used for local development)

### For AWS Deployment (Optional)

- **AWS Account**
- **AWS CLI** configured
- **EC2** and **RDS** access

---

## 🚀 Setup Instructions

### Step 1: Clone/Checkout Repository

```bash
cd /Users/mohan/android-app/CasperAuthenticator
# Repository should already be connected to GitHub
```

### Step 2: Setup Android App

1. **Open in Android Studio:**
   ```bash
   # Open Android Studio
   # File → Open → Select /Users/mohan/android-app/CasperAuthenticator
   ```

2. **Sync Gradle:**
   - Android Studio should automatically sync
   - If not: `File → Sync Project with Gradle Files`

3. **Build:**
   ```bash
   ./gradlew build
   ```

### Step 3: Setup PMS Backend

```bash
cd pms-backend/casper-pms

# Build with Maven
mvn clean install

# Run the service
mvn spring-boot:run
```

PMS will start on **http://localhost:8080**

### Step 4: Setup RP Backend

```bash
cd ../../rp-backend/casper-rp

# Build with Maven
mvn clean install

# Run the service
mvn spring-boot:run
```

RP will start on **http://localhost:8081**

### Step 5: Verify Setup

- **PMS:** Open http://localhost:8080/h2-console (H2 console)
- **RP:** Open http://localhost:8081/ (Demo page)

---

## 🎮 How to Run

### Running Locally (Development)

1. **Start PMS:**
   ```bash
   cd pms-backend/casper-pms
   mvn spring-boot:run
   ```

2. **Start RP:**
   ```bash
   cd rp-backend/casper-rp
   mvn spring-boot:run
   ```

3. **Run Android App:**
   - Open project in Android Studio
   - Connect Android device or start emulator
   - Click "Run" button or `Shift+F10`

### Android Emulator Network Configuration

Android emulator uses `10.0.2.2` to access host `localhost`:

- PMS: `http://10.0.2.2:8080/`
- RP: `http://10.0.2.2:8081/`

These URLs are already configured in `ApiClient.java`.

---

## 📱 Usage Guide

### Step 1: Setup PIN

1. Launch Android app
2. Enter 4-6 digit PIN
3. Confirm PIN
4. App generates user ID and stores PIN securely

### Step 2: Register Passkey

1. Tap "Register Passkey"
2. Enter RP URL: `http://10.0.2.2:8081/`
3. Tap "Register"
4. App will:
   - Generate detection secrets (real + decoys)
   - Generate ECDSA key pair
   - Encrypt private key using CASPER
   - Upload to PMS
   - Register all public keys with RP

### Step 3: Login with Passkey

1. Tap "Login with Passkey"
2. Enter RP URL: `http://10.0.2.2:8081/`
3. Tap "Login"
4. App will:
   - Fetch encrypted passkey from PMS
   - Decrypt using PIN
   - Sign challenge
   - Send to RP
   - RP verifies and checks for breach

### Step 4: Restore Passkey

1. Tap "Restore Passkey"
2. Enter RP URL and PIN
3. Tap "Restore"
4. App fetches and decrypts passkey from PMS

---

## 🧪 Testing CASPER Breach Detection

### Normal Login Flow

1. Register passkey with correct PIN
2. Login with correct PIN → ✅ **Normal login**

### Attack Simulation (Breach Detection)

To simulate an attack:

**Option 1: Modify PIN in App**
- Register passkey with PIN "1234"
- Change PIN to "5678" in app's secure storage
- Try to login → App uses wrong PIN → selects decoy secret → generates fake passkey
- RP detects breach → ⚠️ **BREACH DETECTED**

**Option 2: Create Attack Script**

Create a test script that:
1. Fetches encrypted passkey from PMS (simulating stolen data)
2. Tries wrong PIN → selects decoy secret
3. Attempts login with fake passkey
4. RP detects breach

### Expected Results

- **Normal Login:** `"breachDetected": false`
- **Breach Detected:** `"breachDetected": true`, `"message": "⚠️ BREACH DETECTED"`

---

## ☁️ Deployment

### Deploy PMS to AWS

#### 1. Setup RDS PostgreSQL

```bash
# Create RDS instance (via AWS Console or CLI)
aws rds create-db-instance \
  --db-instance-identifier casper-pms-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username postgres \
  --master-user-password YOUR_PASSWORD \
  --allocated-storage 20
```

#### 2. Update PMS Configuration

Edit `pms-backend/casper-pms/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/casperpms
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

#### 3. Build and Deploy

```bash
cd pms-backend/casper-pms
mvn clean package
# Upload JAR to EC2 or use Elastic Beanstalk
```

### Deploy RP to AWS

Similar steps as PMS, use different port (8081) and database.

---

## 📡 API Documentation

### PMS API

#### Upload Passkey

```
POST /api/passkeys
Content-Type: application/json

{
  "userId": "user-id",
  "rpId": "rp-id",
  "encryptedPrivateKey": "base64-encoded-encrypted-key",
  "publicKey": "base64-encoded-public-key",
  "detectionSecrets": {
    "secrets": ["base64-secret1", "base64-secret2", ...],
    "realSecretIndex": 0
  },
  "z": "base64-encoded-z-value"
}
```

#### Fetch Passkey

```
GET /api/passkeys/{userId}/{rpId}
```

Response:
```json
{
  "userId": "user-id",
  "rpId": "rp-id",
  "encryptedPrivateKey": "...",
  "publicKey": "...",
  "detectionSecrets": {
    "secrets": [...],
    "realSecretIndex": 0
  },
  "z": "..."
}
```

### RP API

#### Register Passkeys

```
POST /api/auth/register
Content-Type: application/json

{
  "userId": "user-id",
  "rpId": "rp-id",
  "publicKeys": ["base64-public-key1", "base64-public-key2", ...]
}
```

#### Login

```
POST /api/auth/login
Content-Type: application/json

{
  "userId": "user-id",
  "rpId": "rp-id",
  "publicKey": "base64-public-key",
  "challenge": "challenge-string",
  "signature": "base64-signature"
}
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "breachDetected": false
}
```

or (breach detected):
```json
{
  "success": true,
  "message": "⚠️ BREACH DETECTED: Decoy passkey used!",
  "breachDetected": true
}
```

---

## 🔧 Troubleshooting

### Android App Issues

**Issue:** App can't connect to backend
- **Solution:** Check that PMS (8080) and RP (8081) are running
- Verify emulator network: use `10.0.2.2` not `localhost`

**Issue:** Build errors
- **Solution:** Sync Gradle, clean build (`./gradlew clean build`)

**Issue:** PIN not found
- **Solution:** Clear app data, restart setup

### Backend Issues

**Issue:** Port already in use
- **Solution:** Change port in `application.properties` or kill process using port

**Issue:** Database connection failed
- **Solution:** Check database credentials, ensure H2/PostgreSQL is running

**Issue:** CORS errors
- **Solution:** CORS is enabled in controllers, check firewall settings

### CASPER Detection Not Working

**Issue:** Breach not detected when expected
- **Solution:** 
  - Verify decoy passkeys are registered with RP
  - Check that trap keys (V') are correctly marked
  - Ensure wrong PIN selects different secret index

---

## 📚 Additional Resources

- **CASPER Paper:** USENIX Security "CASPER: Passkey Breach Detection"
- **HKDF:** RFC 5869
- **ECDSA:** secp256r1 (P-256 curve)
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Android Developer:** https://developer.android.com

---

## 📝 License

This is an educational/research project. Use at your own risk.

---

## 🤝 Contributing

This is a demonstration project. Feel free to fork and modify for your needs.

---

## ⚠️ Security Disclaimer

This is a **research/demo implementation**. For production use:

- Use proper PIN hashing (e.g., PBKDF2, Argon2)
- Store PIN in Android Keystore or secure enclave
- Use HTTPS for all network communication
- Implement proper error handling
- Add rate limiting
- Use production-grade database
- Follow FIDO2/WebAuthn standards

---

## ✅ Summary

You now have a complete working CASPER authenticator system! 

**Quick Start:**
1. Start PMS: `cd pms-backend/casper-pms && mvn spring-boot:run`
2. Start RP: `cd rp-backend/casper-rp && mvn spring-boot:run`
3. Run Android app from Android Studio
4. Register passkey → Login → Test breach detection

**Enjoy building secure authentication systems! 🔐**

