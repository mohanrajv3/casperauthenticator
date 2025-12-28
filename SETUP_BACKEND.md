# Backend Setup Guide

## Maven Installation

The backend services (PMS and RP) require Maven to build and run.

### Option 1: Install Maven using Homebrew (Recommended for macOS)

```bash
# Install Homebrew if you don't have it
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Maven
brew install maven

# Verify installation
mvn -v
```

### Option 2: Use Android Studio's Built-in Terminal

Android Studio includes Java and may have Maven available. Try running the commands from Android Studio's terminal.

### Option 3: Manual Maven Installation

1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to a directory (e.g., `/usr/local/apache-maven`)
3. Add to your PATH:
   ```bash
   export PATH=$PATH:/usr/local/apache-maven/bin
   ```
4. Add to `~/.zshrc` to make it permanent:
   ```bash
   echo 'export PATH=$PATH:/usr/local/apache-maven/bin' >> ~/.zshrc
   source ~/.zshrc
   ```

## Running Backend Services

Once Maven is installed:

### PMS Backend
```bash
cd pms-backend/casper-pms
mvn clean install
mvn spring-boot:run
```

### RP Backend (in a new terminal)
```bash
cd rp-backend/casper-rp
mvn clean install
mvn spring-boot:run
```

## Alternative: Use Gradle for Backend (Not Recommended)

The backend services are configured for Maven. If you prefer Gradle, you would need to convert the `pom.xml` files to `build.gradle` files, which is not recommended for this project.

## Quick Check

Run this to verify everything is set up:
```bash
java -version    # Should show Java 17+
mvn -v          # Should show Maven version
```

