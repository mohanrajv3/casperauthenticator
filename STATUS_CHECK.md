# Service Status Check

## ✅ PMS Service - RUNNING

**Status:** ✅ UP  
**URL:** http://localhost:8080/  
**Health Check:** Working correctly

**Available Endpoints:**
- `GET /` - Health check with endpoint info
- `GET /health` - Simple health check
- `POST /api/passkeys` - Upload encrypted passkey
- `GET /api/passkeys/{userId}/{rpId}` - Fetch encrypted passkey
- `GET /h2-console` - Database console

---

## ⚠️ RP Service - Check Status

**URL:** http://localhost:8081/  

**To Start RP:**
```bash
cd /Users/mohan/android-app/CasperAuthenticator/rp-backend/casper-rp
mvn spring-boot:run
```

**Expected Endpoints:**
- `GET /` - Demo page
- `GET /breach` - Breach detection demo
- `POST /api/auth/register` - Register passkeys
- `POST /api/auth/login` - Login with breach detection

---

## Next Steps

1. **Start RP Service** (if not already running)
2. **Run Android App** from Android Studio
3. **Test the Complete Flow:**
   - Register a passkey
   - Login with passkey
   - Test breach detection

---

## Quick Test Commands

**Test PMS:**
```bash
curl http://localhost:8080/
curl http://localhost:8080/health
```

**Test RP:**
```bash
curl http://localhost:8081/
# Or open in browser: http://localhost:8081/
```

**Check if services are running:**
```bash
lsof -i :8080  # PMS
lsof -i :8081  # RP
```

