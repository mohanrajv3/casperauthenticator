#!/bin/bash

# Script to open the project in Android Studio

PROJECT_PATH="/mnt/data/Sem-7/project 2/casperauthenticator"

echo "Opening project in Android Studio..."
echo "Project path: $PROJECT_PATH"
echo ""

# Try different ways to open Android Studio
if command -v studio.sh &> /dev/null; then
    studio.sh "$PROJECT_PATH"
elif [ -f "$HOME/.local/share/JetBrains/Toolbox/scripts/studio.sh" ]; then
    "$HOME/.local/share/JetBrains/Toolbox/scripts/studio.sh" "$PROJECT_PATH"
elif [ -f "/opt/android-studio/bin/studio.sh" ]; then
    /opt/android-studio/bin/studio.sh "$PROJECT_PATH"
elif [ -f "/snap/android-studio/current/bin/studio.sh" ]; then
    /snap/android-studio/current/bin/studio.sh "$PROJECT_PATH"
else
    echo "Android Studio not found in common locations."
    echo ""
    echo "Please try one of these:"
    echo "1. Open Android Studio manually and paste this path:"
    echo "   $PROJECT_PATH"
    echo ""
    echo "2. Or navigate to: /mnt/data/Sem-7/project 2/casperauthenticator"
    echo ""
    echo "3. Or use: File → Open → (paste path above)"
fi

