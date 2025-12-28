#!/bin/bash

# CASPER Authenticator - Setup Verification Script

echo "🔐 CASPER Authenticator - Setup Verification"
echo "=============================================="
echo ""

# Check Java version
echo "Checking Java..."
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "✓ Java: $JAVA_VERSION"
echo ""

# Check Maven
echo "Checking Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✓ Maven: $MVN_VERSION"
else
    echo "✗ Maven not found. Please install Maven 3.6+"
fi
echo ""

# Check Android SDK (basic check)
echo "Checking Android setup..."
if [ -d "$ANDROID_HOME" ] || [ -d "$HOME/Library/Android/sdk" ]; then
    echo "✓ Android SDK path found"
else
    echo "⚠ Android SDK path not set. Make sure Android Studio is installed."
fi
echo ""

# Check project structure
echo "Checking project structure..."
if [ -d "app" ] && [ -d "pms-backend" ] && [ -d "rp-backend" ]; then
    echo "✓ Project structure OK"
else
    echo "✗ Project structure incomplete"
fi
echo ""

# Check if ports are available
echo "Checking ports..."
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠ Port 8080 is in use (PMS)"
else
    echo "✓ Port 8080 available (PMS)"
fi

if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠ Port 8081 is in use (RP)"
else
    echo "✓ Port 8081 available (RP)"
fi
echo ""

echo "Setup verification complete!"
echo ""
echo "Next steps:"
echo "1. Start PMS: cd pms-backend/casper-pms && mvn spring-boot:run"
echo "2. Start RP: cd rp-backend/casper-rp && mvn spring-boot:run"
echo "3. Run Android app from Android Studio"
echo ""
echo "For detailed instructions, see README.md or QUICK_START.md"

