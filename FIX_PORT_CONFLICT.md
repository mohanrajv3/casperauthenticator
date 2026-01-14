# 🔧 Fix Port Conflicts - Quick Guide

## Problem: Port Already in Use

When you see "Port 8080/8081 already in use", it usually means:
1. The service is already running
2. A previous instance didn't shut down properly
3. DevTools is trying to restart while port is occupied

## Quick Fix

### Step 1: Kill Existing Processes

**macOS/Linux:**
```bash
# Kill process on port 8080 (PMS)
lsof -ti:8080 | xargs kill -9

# Kill process on port 8081 (RP)
lsof -ti:8081 | xargs kill -9
```

**Windows:**
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F

netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### Step 2: Verify Ports Are Free

```bash
# Check if port 8080 is free
lsof -ti:8080 || echo "Port 8080 is free"

# Check if port 8081 is free
lsof -ti:8081 || echo "Port 8081 is free"
```

### Step 3: Start Services Fresh

**Start PMS:**
```bash
cd pms-backend/casper-pms
mvn spring-boot:run
```

**Start RP (in new terminal):**
```bash
cd rp-backend/casper-rp
mvn spring-boot:run
```

## Alternative: Disable DevTools (Optional)

If DevTools keeps causing restart issues, you can disable it:

**In `pom.xml`, comment out or remove:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Note:** DevTools is useful for development (hot reload), but can cause port conflicts during restarts.

## One-Command Solution

Create this script to kill and restart:

```bash
#!/bin/bash
# kill-ports.sh

echo "Killing processes on ports 8080 and 8081..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:8081 | xargs kill -9 2>/dev/null
echo "Ports cleared. You can now start services."
```

Save as `kill-ports.sh`, make executable:
```bash
chmod +x kill-ports.sh
./kill-ports.sh
```

## Check What's Using the Ports

```bash
# See what's using port 8080
lsof -i:8080

# See what's using port 8081
lsof -i:8081
```

This shows you the process name and PID.

## Prevention Tips

1. **Always stop services properly:**
   - Press `Ctrl+C` in the terminal running the service
   - Or use the PID to kill: `kill <PID>`

2. **Check before starting:**
   ```bash
   # Quick check
   lsof -ti:8080 && echo "PMS already running" || echo "Port 8080 is free"
   lsof -ti:8081 && echo "RP already running" || echo "Port 8081 is free"
   ```

3. **Use separate terminals:**
   - Don't run both services in the same terminal
   - Use separate terminal windows/tabs

4. **Use the startup script:**
   ```bash
   ./RUN_BACKENDS.sh
   ```
   This handles process management better.

