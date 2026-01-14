#!/bin/bash

# Script to check app logs for errors

echo "=========================================="
echo "CASPER Authenticator - Log Viewer"
echo "=========================================="
echo ""
echo "Filtering logs for app-related messages..."
echo "Press Ctrl+C to stop"
echo ""
echo "--------------------------------------"
echo ""

# Filter for app-related log tags
adb logcat -c  # Clear log buffer first
adb logcat | grep -E "(WelcomeActivity|AccountListActivity|CasperCrypto|AccountDatabase|AccountRepository|AndroidRuntime|FATAL)"

