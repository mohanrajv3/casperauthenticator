# 🚀 Quick Start Guide - Backend Services

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Terminal/Command Prompt

## Quick Start

### Method 1: Run Script (Easiest)

```bash
# From project root
./RUN_BACKENDS.sh
```

This starts both services automatically.

### Method 2: Manual Start

**Terminal 1 - PMS Service (Port 8080):**
```bash
cd pms-backend/casper-pms
mvn spring-boot:run
```

**Terminal 2 - RP Service (Port 8081):**
```bash
cd rp-backend/casper-rp
mvn spring-boot:run
```

## Verify Services Are Running

### Check PMS (Port 8080)
```bash
curl http://localhost:8080/health
# Should return: {"status":"UP"} or similar
```

Or open in browser: http://localhost:8080/health

### Check RP (Port 8081)
```bash
curl http://localhost:8081/health
# Should return: {"status":"UP"} or similar
```

Or open in browser: http://localhost:8081/

## Port Already in Use?

### Quick Fix: Use the Kill Script (Easiest)

```bash
# From project root
./kill-ports.sh
```

This script will kill any processes on ports 8080 and 8081.

### Manual Fix: Kill All Processes on Ports

**macOS/Linux (One command for both ports):**
```bash
# Kill processes on both ports
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:8081 | xargs kill -9 2>/dev/null
echo "Ports cleared!"
```

### Detailed: Find and Kill Process

**macOS/Linux:**
```bash
# Find process using port 8080
lsof -ti:8080

# Kill it
lsof -ti:8080 | xargs kill -9

# Same for port 8081
lsof -ti:8081 | xargs kill -9
```

**Check what's using the ports:**
```bash
# See process details
lsof -i:8080
lsof -i:8081
```

**Windows:**
```cmd
# Find process using port 8080
netstat -ano | findstr :8080

# Kill it (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Change Ports (Alternative)

If you can't kill the process, change the ports:

**1. Edit PMS port:**
File: `pms-backend/casper-pms/src/main/resources/application.properties`
```properties
server.port=8082  # Change from 8080
```

**2. Edit RP port:**
File: `rp-backend/casper-rp/src/main/resources/application.properties`
```properties
server.port=8083  # Change from 8081
```

**3. Update Android App:**
File: `app/src/main/java/com/casper/authenticator/network/ApiClient.java`
```java
private static final String PMS_BASE_URL = "http://10.0.2.2:8082/";  // New port
private static final String RP_BASE_URL = "http://10.0.2.2:8083/";   // New port
```

## Running in Background

### Start in Background

**PMS:**
```bash
cd pms-backend/casper-pms
nohup mvn spring-boot:run > pms.log 2>&1 &
```

**RP:**
```bash
cd rp-backend/casper-rp
nohup mvn spring-boot:run > rp.log 2>&1 &
```

### View Logs

```bash
tail -f pms-backend/casper-pms/pms.log
tail -f rp-backend/casper-rp/rp.log
```

### Stop Background Services

```bash
# Find and kill PMS
lsof -ti:8080 | xargs kill -9

# Find and kill RP
lsof -ti:8081 | xargs kill -9
```

## Default Ports

- **PMS:** http://localhost:8080
- **RP:** http://localhost:8081
- **H2 Console (PMS):** http://localhost:8080/h2-console
- **H2 Console (RP):** http://localhost:8081/h2-console

## Troubleshooting

### Service Won't Start

1. **Check Java version:**
   ```bash
   java -version  # Should be 17+
   ```

2. **Check Maven:**
   ```bash
   mvn -version
   ```

3. **Clean and rebuild:**
   ```bash
   mvn clean install
   ```

### Connection Refused

- Make sure services are actually running
- Check firewall settings
- Verify ports are correct
- For Android emulator, use `10.0.2.2` not `localhost`

### Build Errors

```bash
# Clean build
mvn clean install

# Skip tests if needed
mvn clean install -DskipTests
```

## Next Steps

Once both services are running:

1. ✅ Verify both services respond to health checks
2. ✅ Open Android app
3. ✅ Register a passkey
4. ✅ Test login
5. ✅ Test breach detection

For full setup instructions, see [README.md](README.md).

