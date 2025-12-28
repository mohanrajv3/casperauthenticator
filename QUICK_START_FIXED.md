# 🚀 Quick Start Guide - Corrected

## Important: Correct Directory Structure

The project is located at:
```
/Users/mohan/android-app/CasperAuthenticator/
```

**NOT** at `/Users/mohan/rp-backend/` or `/Users/mohan/pms-backend/`

---

## Step 1: Navigate to Project Root

```bash
cd /Users/mohan/android-app/CasperAuthenticator
```

Verify you're in the right place:
```bash
pwd
# Should show: /Users/mohan/android-app/CasperAuthenticator

ls
# Should show: app/, pms-backend/, rp-backend/, README.md, etc.
```

---

## Step 2: Start Backend Services

### Option 1: Use the Startup Script (Easiest)

```bash
./RUN_BACKENDS.sh
```

This will start both PMS and RP services.

### Option 2: Manual Start (Two Terminals)

**Terminal 1 - PMS:**
```bash
cd /Users/mohan/android-app/CasperAuthenticator/pms-backend/casper-pms
mvn spring-boot:run
```

**Terminal 2 - RP:**
```bash
cd /Users/mohan/android-app/CasperAuthenticator/rp-backend/casper-rp
mvn spring-boot:run
```

---

## Step 3: Verify Services Are Running

- **PMS:** http://localhost:8080/ (should show health check)
- **RP:** http://localhost:8081/ (should show demo page)

---

## Step 4: Run Android App

1. Open Android Studio
2. Open project: `/Users/mohan/android-app/CasperAuthenticator`
3. Run the app on device/emulator

---

## Common Mistakes to Avoid

❌ **Wrong:** `cd /Users/mohan/rp-backend/casper-rp`  
✅ **Correct:** `cd /Users/mohan/android-app/CasperAuthenticator/rp-backend/casper-rp`

❌ **Wrong:** Running commands from wrong directory  
✅ **Correct:** Always start from project root or use full paths

---

## Quick Reference

**Project Root:**
```bash
/Users/mohan/android-app/CasperAuthenticator
```

**PMS Backend:**
```bash
/Users/mohan/android-app/CasperAuthenticator/pms-backend/casper-pms
```

**RP Backend:**
```bash
/Users/mohan/android-app/CasperAuthenticator/rp-backend/casper-rp
```

**Android App:**
```bash
/Users/mohan/android-app/CasperAuthenticator/app
```

---

## Troubleshooting

### "No POM in this directory"
- You're in the wrong directory
- Navigate to the correct path shown above

### "Maven not found"
- Install Maven: `brew install maven`
- See `SETUP_BACKEND.md` for details

### "Port already in use"
- Kill the process: `lsof -ti:8080 | xargs kill` (for port 8080)
- Or change the port in `application.properties`

