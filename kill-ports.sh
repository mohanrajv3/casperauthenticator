#!/bin/bash

# CASPER Authenticator - Kill processes on ports 8080 and 8081

echo "🔧 Killing processes on ports 8080 and 8081..."

# Kill port 8080 (PMS)
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "  Killing process on port 8080..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    echo "  ✓ Port 8080 cleared"
else
    echo "  ✓ Port 8080 is already free"
fi

# Kill port 8081 (RP)
if lsof -ti:8081 > /dev/null 2>&1; then
    echo "  Killing process on port 8081..."
    lsof -ti:8081 | xargs kill -9 2>/dev/null
    echo "  ✓ Port 8081 cleared"
else
    echo "  ✓ Port 8081 is already free"
fi

echo ""
echo "✅ Done! Ports are now free. You can start the services."
echo ""
echo "To start services:"
echo "  ./RUN_BACKENDS.sh"
echo ""
echo "Or start manually:"
echo "  cd pms-backend/casper-pms && mvn spring-boot:run"
echo "  cd rp-backend/casper-rp && mvn spring-boot:run"

