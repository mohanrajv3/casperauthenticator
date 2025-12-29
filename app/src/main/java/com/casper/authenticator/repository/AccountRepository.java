package com.casper.authenticator.repository;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.casper.authenticator.crypto.CasperCrypto;
import com.casper.authenticator.database.AccountDao;
import com.casper.authenticator.database.AccountDatabase;
import com.casper.authenticator.database.AccountEntity;
import com.casper.authenticator.models.DetectionSecrets;
import com.google.gson.Gson;

import java.util.List;

/**
 * Repository for Account management.
 * 
 * Handles:
 * - CASPER encryption/decryption of TOTP secrets
 * - Account CRUD operations
 * - Secret lifecycle management
 */
public class AccountRepository {
    private static final String TAG = "AccountRepository";
    
    private AccountDao accountDao;
    private CasperCrypto casperCrypto;
    private Gson gson;
    
    public AccountRepository(Context context) {
        AccountDatabase database = AccountDatabase.getInstance(context);
        this.accountDao = database.accountDao();
        this.casperCrypto = new CasperCrypto(context);
        this.gson = new Gson();
    }
    
    /**
     * Add a new account with CASPER encryption.
     * 
     * @param label Account label
     * @param issuer Service issuer
     * @param secret Plaintext TOTP secret (will be encrypted)
     * @param algorithm HMAC algorithm
     * @param digits Code digits (6 or 8)
     * @param period Time period (30 or 60 seconds)
     * @param type "TOTP" or "HOTP"
     * @return AccountEntity ID
     */
    public long addAccount(String label, String issuer, byte[] secret, 
                          String algorithm, int digits, int period, String type) {
        try {
            // Get PIN for CASPER encryption
            String pin = casperCrypto.getPin();
            if (pin == null) {
                throw new IllegalStateException("PIN not set. Please set up PIN first.");
            }
            
            // Generate detection secrets
            DetectionSecrets detectionSecrets = casperCrypto.generateDetectionSecrets(pin);
            byte[] realSecret = detectionSecrets.getRealSecretAsBytes();
            
            // Generate random value z
            byte[] z = com.casper.authenticator.crypto.KeyGenerator.generateRandomBytes(32);
            
            // Encrypt TOTP secret using CASPER: encrypted = HKDF(w*, z) XOR secret
            byte[] encryptionKey = com.casper.authenticator.crypto.HKDFHelper.derive(
                realSecret, z, "casper-totp-secret".getBytes(), secret.length);
            
            byte[] encryptedSecret = new byte[secret.length];
            for (int i = 0; i < secret.length; i++) {
                encryptedSecret[i] = (byte) (secret[i] ^ encryptionKey[i]);
            }
            
            // Convert detection secrets to JSON
            String detectionSecretsJson = gson.toJson(detectionSecrets);
            
            // Create account entity
            AccountEntity account = new AccountEntity();
            account.label = label;
            account.issuer = issuer;
            account.encryptedSecret = Base64.encodeToString(encryptedSecret, Base64.NO_WRAP);
            account.detectionSecretsJson = detectionSecretsJson;
            account.realSecretIndex = detectionSecrets.getRealSecretIndex();
            account.zValue = Base64.encodeToString(z, Base64.NO_WRAP);
            account.algorithm = algorithm != null ? algorithm : "SHA1";
            account.digits = digits > 0 ? digits : 6;
            account.period = period > 0 ? period : 30;
            account.type = type != null ? type : "TOTP";
            account.counter = 0;
            account.createdAt = System.currentTimeMillis();
            account.updatedAt = System.currentTimeMillis();
            
            // Insert into database
            long id = accountDao.insert(account);
            Log.d(TAG, "Account added: " + label + " (ID: " + id + ")");
            return id;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to add account", e);
            throw new RuntimeException("Failed to add account", e);
        }
    }
    
    /**
     * Decrypt TOTP secret for account using CASPER.
     * 
     * @param account Account entity
     * @return Decrypted TOTP secret
     */
    public byte[] decryptSecret(AccountEntity account) {
        try {
            // Get PIN
            String pin = casperCrypto.getPin();
            if (pin == null) {
                throw new IllegalStateException("PIN not set");
            }
            
            // Parse detection secrets
            DetectionSecrets detectionSecrets = gson.fromJson(
                account.detectionSecretsJson, DetectionSecrets.class);
            
            // Select real secret using PIN (verify index matches)
            int calculatedIndex = calculateRealSecretIndex(pin, detectionSecrets.getSecretsAsBytes().length);
            if (calculatedIndex != account.realSecretIndex) {
                throw new SecurityException("PIN verification failed - index mismatch");
            }
            
            byte[] realSecret = detectionSecrets.getSecretsAsBytes()[account.realSecretIndex];
            byte[] z = Base64.decode(account.zValue, Base64.NO_WRAP);
            byte[] encryptedSecret = Base64.decode(account.encryptedSecret, Base64.NO_WRAP);
            
            // Decrypt: secret = HKDF(w*, z) XOR encrypted
            byte[] decryptionKey = com.casper.authenticator.crypto.HKDFHelper.derive(
                realSecret, z, "casper-totp-secret".getBytes(), encryptedSecret.length);
            
            byte[] decryptedSecret = new byte[encryptedSecret.length];
            for (int i = 0; i < encryptedSecret.length; i++) {
                decryptedSecret[i] = (byte) (encryptedSecret[i] ^ decryptionKey[i]);
            }
            
            return decryptedSecret;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt secret for account: " + account.label, e);
            throw new RuntimeException("Failed to decrypt secret", e);
        }
    }
    
    /**
     * Calculate real secret index from PIN (same logic as CasperCrypto).
     */
    private int calculateRealSecretIndex(String pin, int secretCount) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] pinHash = digest.digest(pin.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            int hashValue = java.nio.ByteBuffer.wrap(
                java.util.Arrays.copyOf(pinHash, 4)).getInt();
            return Math.abs(hashValue % secretCount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate real secret index", e);
        }
    }
    
    /**
     * Get all accounts.
     */
    public List<AccountEntity> getAllAccounts() {
        return accountDao.getAllAccounts();
    }
    
    /**
     * Get account by ID.
     */
    public AccountEntity getAccountById(long id) {
        return accountDao.getAccountById(id);
    }
    
    /**
     * Update account (metadata only, secret remains encrypted).
     */
    public void updateAccount(AccountEntity account) {
        account.updatedAt = System.currentTimeMillis();
        accountDao.update(account);
    }
    
    /**
     * Delete account.
     */
    public void deleteAccount(AccountEntity account) {
        accountDao.delete(account);
    }
    
    /**
     * Delete account by ID.
     */
    public void deleteAccountById(long id) {
        accountDao.deleteById(id);
    }
    
    /**
     * Search accounts.
     */
    public List<AccountEntity> searchAccounts(String query) {
        return accountDao.searchAccounts("%" + query + "%");
    }
    
    /**
     * Get account count.
     */
    public int getAccountCount() {
        return accountDao.getAccountCount();
    }
}

