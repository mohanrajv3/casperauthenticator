# Secure Storage Implementation

## Current Implementation

The app uses **EncryptedSharedPreferences** from AndroidX Security library for secure storage.

### What's Stored Securely

1. **PIN** - Encrypted using AES-256-GCM
2. **User ID** - Encrypted using AES-256-GCM

### How It Works

```java
// EncryptedSharedPreferences uses:
- MasterKey: Generated and stored in Android Keystore
- Encryption: AES-256-GCM for values
- Key Encryption: AES-256-SIV for keys
```

### Storage Locations

**Secure Storage (EncryptedSharedPreferences):**
- File: `casper_prefs.xml` (encrypted)
- Location: `/data/data/com.casper.authenticator/shared_prefs/casper_prefs.xml`
- Encryption: AES-256-GCM (values), AES-256-SIV (keys)
- Master Key: Stored in Android Keystore

**Regular Storage (for PIN hash verification only):**
- File: `casper_prefs.xml` (unencrypted, only contains PIN hash)
- Purpose: Quick verification that PIN is set (not the actual PIN)

## Security Features

✅ **PIN Encryption**: PIN is encrypted at rest using AES-256-GCM  
✅ **Android Keystore**: Master key stored in hardware-backed keystore (if available)  
✅ **Encrypted Keys**: Even preference keys are encrypted (AES-256-SIV)  
✅ **Secure by Default**: No plain text storage of sensitive data  

## Limitations & Future Improvements

### Current Limitations

1. **PIN Hash**: Uses simple `hashCode()` for verification (not cryptographically secure)
2. **No Biometric Lock**: PIN can be retrieved by any app component with context
3. **No PIN Attempt Limiting**: No lockout after failed attempts

### Recommended Improvements for Production

1. **Use Android Keystore for PIN**:
   ```java
   // Store PIN in Android Keystore using KeyGenParameterSpec
   // This requires biometric authentication to access
   ```

2. **Proper PIN Hashing**:
   ```java
   // Use PBKDF2 or Argon2 instead of hashCode()
   SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
   PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, 100000, 256);
   ```

3. **Biometric Authentication**:
   ```java
   // Use BiometricPrompt to require fingerprint/face unlock
   // before accessing PIN
   ```

4. **PIN Attempt Limiting**:
   ```java
   // Lock app after N failed PIN attempts
   // Clear sensitive data after lockout
   ```

## Code Location

- **Secure Storage Class**: `CasperCrypto.java`
- **PIN Storage Methods**: 
  - `savePin(String pin)` - Save PIN securely
  - `getPin()` - Retrieve PIN from secure storage
- **User ID Storage**:
  - `saveUserId(String userId)` - Save user ID securely
  - `getUserId()` - Retrieve user ID from secure storage

## How to Use

```java
// Initialize secure storage
CasperCrypto crypto = new CasperCrypto(context);

// Save PIN securely
crypto.savePin("1234");

// Retrieve PIN securely
String pin = crypto.getPin();
```

## Security Notes

- ⚠️ **Demo Purpose**: Current implementation is suitable for demonstration
- 🔒 **Production**: Requires additional security measures (listed above)
- 📱 **Android Version**: EncryptedSharedPreferences works on Android 6.0+ (API 23+)
- 🔑 **Hardware Security**: On devices with hardware-backed keystore, master key is hardware-protected

