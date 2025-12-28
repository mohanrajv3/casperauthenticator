# Quick Fix Guide

## ✅ Gradle Wrapper Fixed

The Gradle wrapper script (`gradlew`) has been created. You can now use:

```bash
./gradlew build
./gradlew clean
./gradlew tasks
```

## ⚠️ Maven Not Installed

For the backend services (PMS and RP), you need Maven installed.

### Quick Install (macOS with Homebrew):

```bash
brew install maven
```

Then verify:
```bash
mvn -v
```

### After Installing Maven:

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

## Alternative: Use Android Studio

You can also:
1. Open the project in Android Studio
2. Use Android Studio's built-in terminal
3. Build the Android app directly from Android Studio (no need for command line)

For the backend services, you'll still need Maven installed, or you can use Android Studio's terminal which might have Maven available.

## Summary

- ✅ Gradle wrapper: Ready to use (`./gradlew`)
- ⚠️ Maven: Needs to be installed for backend services
- ✅ Android app: Can build from Android Studio or command line

See `SETUP_BACKEND.md` for detailed Maven installation instructions.

