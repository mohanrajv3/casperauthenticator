# Backend API Endpoints

## PMS (Passkey Management Service) - Port 8080

The PMS service is running successfully! The 404 error you saw is normal - there's no root endpoint by default.

### Health Check Endpoint (NEW)
- **GET** `http://localhost:8080/` - Service status and endpoint list
- **GET** `http://localhost:8080/health` - Simple health check

### API Endpoints

#### Upload Passkey
```
POST http://localhost:8080/api/passkeys
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
GET http://localhost:8080/api/passkeys/{userId}/{rpId}

Example: GET http://localhost:8080/api/passkeys/user123/rp456
```

#### H2 Console (for database access)
```
GET http://localhost:8080/h2-console
```

---

## RP (Relying Party) - Port 8081

### Web Pages
- **GET** `http://localhost:8081/` - Demo page with CASPER explanation
- **GET** `http://localhost:8081/breach` - Breach detection demo page

### API Endpoints

#### Register Passkeys
```
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "userId": "user-id",
  "rpId": "rp-id",
  "publicKeys": ["base64-public-key1", "base64-public-key2", ...]
}
```

#### Login
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "userId": "user-id",
  "rpId": "rp-id",
  "publicKey": "base64-public-key",
  "challenge": "challenge-string",
  "signature": "base64-signature"
}
```

---

## Testing the Services

### Test PMS Health Endpoint
```bash
curl http://localhost:8080/
```

### Test RP Home Page
Open in browser: `http://localhost:8081/`

### Access H2 Console (PMS Database)
1. Open: `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:casperpms`
3. Username: `sa`
4. Password: (leave empty)

---

## Common Issues

### 404 Error on Root Path
- **Normal behavior** - The root path (`/`) doesn't have a mapping by default
- Use the health check endpoint: `http://localhost:8080/health`
- Or use the API endpoints listed above

### Service Not Responding
- Check if service is running: Look for "Started CasperPmsApplication" or "Started CasperRpApplication" in logs
- Verify port is not in use: `lsof -i :8080` or `lsof -i :8081`
- Check logs for errors

