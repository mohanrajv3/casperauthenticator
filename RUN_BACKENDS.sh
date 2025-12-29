#!/bin/bash

# CASPER Authenticator - Backend Services Startup Script

echo "🔐 CASPER Authenticator - Backend Services"
echo "=========================================="
echo ""

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Kill existing processes on ports 8080 and 8081
echo "Checking for existing processes on ports 8080 and 8081..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:8081 | xargs kill -9 2>/dev/null
sleep 1
echo "Ports cleared (if any processes were running)"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed."
    echo ""
    echo "Install Maven:"
    echo "  brew install maven"
    echo ""
    echo "Or see SETUP_BACKEND.md for detailed instructions."
    exit 1
fi

echo "Starting PMS (Passkey Management Service) on port 8080..."
echo ""
cd pms-backend/casper-pms
mvn spring-boot:run &
PMS_PID=$!
cd "$SCRIPT_DIR"

echo ""
echo "Waiting 3 seconds for PMS to start..."
sleep 3

echo ""
echo "Starting RP (Relying Party) on port 8081..."
echo ""
cd rp-backend/casper-rp
mvn spring-boot:run &
RP_PID=$!
cd "$SCRIPT_DIR"

echo ""
echo "✅ Backend services started!"
echo ""
echo "PMS (Port 8080): PID $PMS_PID"
echo "RP (Port 8081):  PID $RP_PID"
echo ""
echo "To stop services, press Ctrl+C or run:"
echo "  kill $PMS_PID $RP_PID"
echo ""
echo "Access services:"
echo "  PMS: http://localhost:8080/"
echo "  RP:  http://localhost:8081/"
echo ""
echo "Press Ctrl+C to stop all services..."

# Wait for user interrupt
trap "echo ''; echo 'Stopping services...'; kill $PMS_PID $RP_PID 2>/dev/null; exit" INT
wait

