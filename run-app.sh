#!/bin/bash

# Script to build and run the CASPER Authenticator Android app

echo "=========================================="
echo "CASPER Authenticator - Build & Run Script"
echo "=========================================="
echo ""

# Navigate to project directory
cd "$(dirname "$0")"

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME is not set. Trying to find Android SDK..."
    if [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
        echo "✓ Found Android SDK at $ANDROID_HOME"
    else
        echo "❌ Android SDK not found. Please set ANDROID_HOME or install Android SDK."
        exit 1
    fi
fi

# Check if adb is available
if ! command -v adb &> /dev/null; then
    if [ -f "$ANDROID_HOME/platform-tools/adb" ]; then
        export PATH="$PATH:$ANDROID_HOME/platform-tools"
    else
        echo "❌ adb not found. Please install Android SDK Platform Tools."
        exit 1
    fi
fi

echo ""
echo "Step 1: Checking connected devices..."
echo "--------------------------------------"
adb devices

DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo ""
    echo "⚠️  No devices found!"
    echo "Please:"
    echo "  1. Start an Android emulator, OR"
    echo "  2. Connect a physical device with USB debugging enabled"
    echo ""
    echo "Waiting 5 seconds for you to connect a device..."
    sleep 5
    
    DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)
    if [ "$DEVICE_COUNT" -eq 0 ]; then
        echo "❌ Still no devices found. Exiting."
        exit 1
    fi
fi

echo "✓ Found $DEVICE_COUNT device(s)"
echo ""

echo "Step 2: Cleaning project..."
echo "--------------------------------------"
./gradlew clean
if [ $? -ne 0 ]; then
    echo "❌ Clean failed. Check errors above."
    exit 1
fi
echo "✓ Clean successful"
echo ""

echo "Step 3: Building APK..."
echo "--------------------------------------"
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Build failed. Check errors above."
    exit 1
fi
echo "✓ Build successful"
echo ""

echo "Step 4: Installing APK on device..."
echo "--------------------------------------"
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "❌ Installation failed. Check errors above."
    exit 1
fi
echo "✓ Installation successful"
echo ""

echo "Step 5: Launching app..."
echo "--------------------------------------"
adb shell am start -n com.casper.authenticator/.WelcomeActivity
if [ $? -ne 0 ]; then
    echo "⚠️  Could not launch app automatically. Please launch manually."
else
    echo "✓ App launched"
fi
echo ""

echo "=========================================="
echo "✓ App is now running!"
echo "=========================================="
echo ""
echo "To view logs, run:"
echo "  adb logcat | grep -E '(WelcomeActivity|AccountListActivity|CasperCrypto|AccountDatabase|AndroidRuntime)'"
echo ""
echo "Or to see all logs:"
echo "  adb logcat"
echo ""

