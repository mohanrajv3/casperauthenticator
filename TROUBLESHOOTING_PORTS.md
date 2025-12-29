# 🔧 Troubleshooting Port Conflicts

## Common Issue: "Port Already in Use"

You're trying to start a service but get:
```
Web server failed to start. Port 8081 was already in use.
```

## Why This Happens

1. **Service already running** - You started it earlier and it's still running
2. **DevTools auto-restart** - Spring Boot DevTools tries to restart while port is occupied
3. **Process didn't shut down properly** - Previous instance crashed or wasn't stopped cleanly

## Quick Solutions

### Solution 1: Use the Kill Script (Recommended)

```bash
./kill-ports.sh
```

This automatically kills processes on ports 8080 and 8081.

### Solution 2: Manual Kill Commands

```bash
# Kill port 8080 (PMS)
lsof -ti:8080 | xargs kill -9

# Kill port 8081 (RP)
lsof -ti:8081 | xargs kill -9
```

### Solution 3: Check What's Running

```bash
# See what's using port 8080
lsof -i:8080

# See what's using port 8081
lsof -i:8081
```

This shows you the process name and PID, so you can decide if you want to kill it.

## Step-by-Step Fix

### 1. Kill Existing Processes

```bash
./kill-ports.sh
```

### 2. Verify Ports Are Free

```bash
# Should return nothing if ports are free
lsof -ti:8080
lsof -ti:8081
```

### 3. Start Services Fresh

```bash
# Terminal 1
cd pms-backend/casper-pms
mvn spring-boot:run

# Terminal 2 (after PMS starts)
cd rp-backend/casper-rp
mvn spring-boot:run
```

## Prevention Tips

1. **Always stop services properly:**
   - Press `Ctrl+C` in the terminal
   - Wait for graceful shutdown

2. **Check before starting:**
   ```bash
   # Quick check
   lsof -ti:8080 && echo "PMS running" || echo "Port 8080 free"
   lsof -ti:8081 && echo "RP running" || echo "Port 8081 free"
   ```

3. **Use separate terminals:**
   - Don't try to run both services in the same terminal window
   - Use separate terminal windows/tabs

4. **Use the startup script:**
   ```bash
   ./RUN_BACKENDS.sh
   ```
   This script automatically kills existing processes before starting.

## DevTools Auto-Restart Issue

Spring Boot DevTools can cause issues if it tries to restart while the service is running.

**Symptoms:**
- Service starts successfully
- Then you see "Port already in use" error
- DevTools is trying to restart

**Solutions:**

1. **Kill the process and restart:**
   ```bash
   ./kill-ports.sh
   # Then start service again
   ```

2. **Disable DevTools (optional):**
   
   Edit `pom.xml` and comment out:
   ```xml
   <!--
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <scope>runtime</scope>
       <optional>true</optional>
   </dependency>
   -->
   ```
   
   Then rebuild:
   ```bash
   mvn clean install
   ```

   **Note:** DevTools provides hot-reload which is useful for development, but disabling it prevents restart conflicts.

## Alternative: Change Ports

If you can't kill the processes or want to run multiple instances:

1. **Edit PMS port:**
   File: `pms-backend/casper-pms/src/main/resources/application.properties`
   ```properties
   server.port=8082  # Change from 8080
   ```

2. **Edit RP port:**
   File: `rp-backend/casper-rp/src/main/resources/application.properties`
   ```properties
   server.port=8083  # Change from 8081
   ```

3. **Update Android app:**
   File: `app/src/main/java/com/casper/authenticator/network/ApiClient.java`
   ```java
   private static final String PMS_BASE_URL = "http://10.0.2.2:8082/";
   private static final String RP_BASE_URL = "http://10.0.2.2:8083/";
   ```

## Still Having Issues?

1. **Check Java processes:**
   ```bash
   jps -l | grep casper
   ```
   
2. **Kill all Java processes (nuclear option - be careful!):**
   ```bash
   # This kills ALL Java processes - use with caution
   killall java
   ```

3. **Restart your terminal/computer:**
   Sometimes processes get stuck and need a fresh start.

## Quick Reference Commands

```bash
# Kill ports (easiest)
./kill-ports.sh

# Check ports
lsof -i:8080
lsof -i:8081

# Kill specific port
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9

# Start services
./RUN_BACKENDS.sh  # Automatic
# OR manually in separate terminals
```

