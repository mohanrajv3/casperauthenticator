# 🔄 How to Sync Changes in Android Studio

## ⚠️ Important: Changes Need to be Synced!

The files I created/modified are in your project directory, but **Android Studio needs to be synced** to recognize them.

## Steps to Sync:

### Method 1: Automatic Sync (Recommended)
1. **Open Android Studio**
2. Look at the top-right corner for a notification bar saying **"Gradle files have changed"**
3. Click **"Sync Now"** button
4. Wait for sync to complete (check bottom status bar)

### Method 2: Manual Sync
1. In Android Studio, click **File → Sync Project with Gradle Files**
2. Wait for sync to complete

### Method 3: Invalidate Caches (If sync doesn't work)
1. **File → Invalidate Caches / Restart**
2. Select **"Invalidate and Restart"**
3. Wait for Android Studio to restart and re-index

---

## 📁 New Files That Need to be Recognized:

### Security Modules (in `app/src/main/java/com/casper/authenticator/security/`)
- `SecurityManager.java`
- `TimeSyncManager.java`
- `BackupManager.java`
- `SecurityLogger.java`

### Activities
- `LockScreenActivity.java`
- `QRCodeScannerActivity.java`

### Layouts
- `activity_lock_screen.xml`

---

## 🔍 How to Verify Sync Worked:

1. **Check Project Structure:**
   - In Android Studio, open `app/src/main/java/com/casper/authenticator/`
   - You should see a `security/` folder with the new files
   - You should see `LockScreenActivity.java` and `QRCodeScannerActivity.java`

2. **Check for Errors:**
   - Look at the bottom "Build" tab
   - Should see "BUILD SUCCESSFUL" (or errors if there are issues)

3. **Check Imports:**
   - Open `AccountListActivity.java`
   - Check if `import com.casper.authenticator.security.SecurityManager;` is recognized
   - Should NOT be red/underlined

---

## 🐛 If You See Errors After Sync:

### Common Issues:

1. **"Cannot resolve symbol 'SecurityManager'"**
   - Solution: Make sure the `security/` folder exists and files are there
   - Try: **File → Invalidate Caches / Restart**

2. **"Package does not exist"**
   - Solution: Check that package declaration in files matches folder structure
   - All security files should have: `package com.casper.authenticator.security;`

3. **Build Errors**
   - Solution: Check the "Build" tab for specific error messages
   - Most common: Missing dependencies (should already be in build.gradle)

---

## ✅ After Successful Sync:

1. **Clean Build:**
   - **Build → Clean Project**
   - **Build → Rebuild Project**

2. **Run the App:**
   - Click the green **Run** button (▶️)
   - App should now work without crashing!

---

## 📝 Quick Checklist:

- [ ] Opened Android Studio
- [ ] Clicked "Sync Now" or "File → Sync Project with Gradle Files"
- [ ] Waited for sync to complete
- [ ] Checked that new files appear in project structure
- [ ] No red errors in code
- [ ] Clean and rebuild project
- [ ] Run the app

---

**Note:** If Android Studio doesn't automatically detect changes, you may need to close and reopen the project, or use "Invalidate Caches / Restart".

