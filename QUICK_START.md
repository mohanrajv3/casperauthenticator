# 🚀 CASPER Authenticator - Quick Start Guide

## Prerequisites

- Java 17+
- Maven 3.6+
- Android Studio
- Android SDK (API 24+)

## Quick Start (3 Steps)

### Step 1: Start PMS Backend

```bash
cd pms-backend/casper-pms
mvn spring-boot:run
```

**Wait for:** `Started CasperPmsApplication` message
**PMS runs on:** http://localhost:8080

### Step 2: Start RP Backend

Open a **new terminal**:

```bash
cd rp-backend/casper-rp
mvn spring-boot:run
```

**Wait for:** `Started CasperRpApplication` message  
**RP runs on:** http://localhost:8081

### Step 3: Run Android App

1. Open Android Studio
2. Open project: `/Users/mohan/android-app/CasperAuthenticator`
3. Connect device/emulator
4. Click **Run** button (▶️)

## Verify Setup

- ✅ PMS: http://localhost:8080/h2-console (H2 console)
- ✅ RP: http://localhost:8081/ (Demo page)
- ✅ Android app should launch

## First Run Flow

1. **Welcome Screen** → Tap "Get Started"
2. **PIN Setup** → Enter PIN (e.g., "1234") → Confirm
3. **Home Screen** → Tap "Register Passkey"
4. **Register** → Enter RP URL: `http://10.0.2.2:8081/` → Tap "Register"
5. **Home Screen** → Tap "Login with Passkey"
6. **Login** → Enter RP URL: `http://10.0.2.2:8081/` → Tap "Login"

## Testing Breach Detection

To test CASPER breach detection:

1. Register passkey with PIN "1234"
2. Clear app data (Settings → Apps → CASPER Authenticator → Clear Data)
3. Setup new PIN "5678"
4. Try to login → Should trigger breach detection ⚠️

## Troubleshooting

**Backend won't start:**
- Check Java version: `java -version` (should be 17+)
- Check if ports 8080/8081 are free: `lsof -i :8080`

**App can't connect:**
- Verify both backends are running
- For emulator: Use `10.0.2.2` not `localhost`
- Check network permissions in AndroidManifest.xml

**Build errors:**
- Sync Gradle: `File → Sync Project with Gradle Files`
- Clean build: `./gradlew clean build`

## Next Steps

See [README.md](README.md) for:
- Complete documentation
- Architecture explanation
- API documentation
- AWS deployment guide

