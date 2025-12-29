# 🏗️ CASPER Authenticator - Complete Implementation Guide

## ✅ Completed (Foundation)

1. ✅ **Back Buttons Added** - All activities now have navigation
2. ✅ **Secure Storage** - PIN encrypted with EncryptedSharedPreferences
3. ✅ **TOTP Generator** - RFC 6238 implementation created
4. ✅ **Account Model** - Account entity with CASPER integration
5. ✅ **Dependencies Added** - Room, Biometric, QR Code libraries

---

## 🎯 What Makes This a Production Authenticator

### Current State (Demo)
- Basic passkey registration/login
- CASPER breach detection
- Simple UI

### Target State (Production)
- **TOTP/HOTP Codes** - Real OTP generation like Google Authenticator
- **Multiple Accounts** - Manage multiple services
- **QR Code Setup** - Scan QR codes to add accounts
- **Security First** - Biometric lock, screenshot protection
- **Professional UX** - Countdown timers, copy buttons, dark mode

---

## 🏛️ Architecture Overview

### CASPER + TOTP Integration

```
User PIN → CASPER Detection Secrets (W)
                ↓
        Select Real Secret (w*)
                ↓
        Encrypt TOTP Secret: encrypted_secret = HKDF(w*, z) XOR secret
                ↓
        Store: encrypted_secret + detection_secrets + z
                ↓
        When needed: Decrypt → Generate TOTP Code
```

### Key Components

1. **TOTPGenerator** - Generates OTP codes (RFC 6238/4226)
2. **CasperCrypto** - Encrypts/decrypts secrets using CASPER
3. **Account Model** - Stores account info + CASPER metadata
4. **Room Database** - Encrypted storage for accounts
5. **AccountRepository** - Manages account operations

---

## 📋 Implementation Checklist

### Phase 1: Core TOTP Functionality ⚠️ IN PROGRESS

- [x] TOTP Generator (RFC 6238)
- [x] Account Model
- [ ] Account Database (Room with encryption)
- [ ] Account Repository
- [ ] CASPER encryption for TOTP secrets
- [ ] Account List Screen
- [ ] OTP Display with Countdown

### Phase 2: Account Management

- [ ] QR Code Scanner Activity
- [ ] Manual Account Entry
- [ ] Account Details Screen
- [ ] Edit/Delete Accounts
- [ ] Account Search/Filter

### Phase 3: Security Features

- [ ] Biometric Authentication
- [ ] Auto-lock Screen
- [ ] Screenshot Protection
- [ ] Clipboard Auto-clear
- [ ] Root Detection

### Phase 4: UX Enhancements

- [ ] Material Design 3
- [ ] Dark Mode
- [ ] Smooth Animations
- [ ] Loading States
- [ ] Error Handling

### Phase 5: Backup & Restore

- [ ] Encrypted Backup Export
- [ ] QR Code Backup
- [ ] Restore Functionality
- [ ] Integrity Validation

---

## 🔧 Next Steps to Continue Building

### Step 1: Create Account Database

```java
// Room Database Entity
@Entity(tableName = "accounts")
public class AccountEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    public String label;
    public String issuer;
    public String encryptedSecret;  // CASPER-encrypted
    public String detectionSecretsJson;
    public int realSecretIndex;
    public String zValue;
    // ... other fields
}
```

### Step 2: Create Account Repository

```java
@Dao
public interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY label ASC")
    LiveData<List<AccountEntity>> getAllAccounts();
    
    @Insert
    void insert(AccountEntity account);
    
    @Update
    void update(AccountEntity account);
    
    @Delete
    void delete(AccountEntity account);
}
```

### Step 3: Create Main Account List Activity

- Display all accounts
- Show OTP codes with countdown
- Copy OTP on tap
- Add account button
- Account detail/edit screens

### Step 4: QR Code Scanner

- Camera permission
- ZXing integration
- Parse `otpauth://` URLs
- Extract secret, issuer, label, algorithm, digits, period
- Create account with CASPER encryption

---

## 🎨 UI/UX Improvements Needed

### Main Screen (Account List)
```
┌─────────────────────────┐
│  CASPER Authenticator   │
├─────────────────────────┤
│  Google                  │
│  123456  [29s] [Copy]   │
├─────────────────────────┤
│  GitHub                  │
│  789012  [15s] [Copy]   │
├─────────────────────────┤
│  Microsoft               │
│  345678  [02s] [Copy]   │
├─────────────────────────┤
│  [+ Add Account]         │
└─────────────────────────┘
```

### Add Account Screen
```
┌─────────────────────────┐
│  Add Account            │
├─────────────────────────┤
│  [Scan QR Code]         │
│  OR                     │
│  Enter Manually:        │
│  Label:  [Google]       │
│  Secret: [JBSWY3DPE...] │
│  Algorithm: [SHA1 ▼]    │
│  Digits: [6 ▼]          │
│  Period: [30s ▼]        │
│                         │
│  [Cancel]  [Save]       │
└─────────────────────────┘
```

---

## 📚 Key Files to Create

1. `AccountEntity.java` - Room entity
2. `AccountDao.java` - Database access
3. `AccountDatabase.java` - Room database
4. `AccountRepository.java` - Business logic
5. `AccountListActivity.java` - Main screen
6. `QRScanActivity.java` - QR code scanner
7. `AddAccountActivity.java` - Manual entry
8. `AccountDetailActivity.java` - View/edit account
9. `LockScreenActivity.java` - App lock
10. `OTPViewModel.java` - ViewModel for OTP generation

---

## 🔐 Security Implementation Details

### CASPER Encryption Flow for TOTP Secrets

```java
// When adding account:
1. User scans QR → Gets TOTP secret (plaintext)
2. Generate k detection secrets (W)
3. Select real secret using PIN: w* = W[H(PIN) mod k]
4. Encrypt TOTP secret: encrypted = HKDF(w*, z) XOR secret
5. Store: encrypted + detection_secrets + z + metadata

// When generating OTP:
1. Get PIN from secure storage
2. Select real secret: w* = W[H(PIN) mod k]
3. Decrypt secret: secret = HKDF(w*, z) XOR encrypted
4. Generate TOTP code from decrypted secret
5. Display code with countdown timer
```

---

## 🚀 Quick Start Implementation Order

1. **Account Database** (Room + Entity)
2. **Account Repository** (CRUD operations)
3. **Main Account List Screen** (Display accounts)
4. **OTP Generation Integration** (Use TOTPGenerator)
5. **Add Account (Manual)** (Simple form first)
6. **QR Code Scanner** (Camera integration)
7. **Security Lock** (Biometric/PIN)
8. **Polish & Test** (UX improvements)

---

## 📝 Code Examples

See individual implementation files for complete code.

