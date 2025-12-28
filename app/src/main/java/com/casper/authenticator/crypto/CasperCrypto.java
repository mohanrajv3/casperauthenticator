package com.casper.authenticator.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.casper.authenticator.models.DetectionSecrets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;

/**
 * CASPER cryptography implementation.
 * 
 * Implements the core CASPER algorithm:
 * 1. Generate k detection secrets (W), one real (w*), others decoys
 * 2. Select real secret using PIN: w* = W[H(PIN) mod k]
 * 3. Encrypt passkey private key: s̃ = HKDF(w*, z) XOR s
 * 4. Store encrypted passkey and detection secrets in cloud PMS
 * 
 * During breach detection:
 * - If attacker uses decoy secret → generates fake passkey
 * - Fake passkey public key will be in trap key set V'
 * - RP detects breach when login key ∈ V'
 */
public class CasperCrypto {
    private static final String PREFS_NAME = "casper_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final int DETECTION_SECRET_COUNT = 5; // k = 5 detection secrets
    private static final int SECRET_LENGTH = 32; // 256 bits
    
    private Context context;
    private SharedPreferences encryptedPrefs;
    
    public CasperCrypto(Context context) {
        this.context = context;
        try {
            // Initialize encrypted shared preferences for secure storage
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encrypted storage", e);
        }
    }
    
    /**
     * Generate detection secrets and select the real one based on PIN.
     * 
     * @param pin User's PIN (low-entropy, 4-6 digits)
     * @return DetectionSecrets object containing all secrets and the real secret index
     */
    public DetectionSecrets generateDetectionSecrets(String pin) {
        byte[][] secrets = new byte[DETECTION_SECRET_COUNT][];
        
        // Generate k detection secrets
        for (int i = 0; i < DETECTION_SECRET_COUNT; i++) {
            secrets[i] = KeyGenerator.generateRandomBytes(SECRET_LENGTH);
        }
        
        // Hash PIN to select real secret index: w* = W[H(PIN) mod k]
        int realSecretIndex = selectRealSecretIndex(pin);
        
        return new DetectionSecrets(secrets, realSecretIndex);
    }
    
    /**
     * Get the real secret from DetectionSecrets object.
     * Helper method to extract real secret for encryption/decryption.
     */
    public byte[] getRealSecret(DetectionSecrets detectionSecrets) {
        return detectionSecrets.getRealSecretAsBytes();
    }
    
    /**
     * Select the real secret index from PIN using hash modulo.
     * 
     * @param pin User's PIN
     * @return Index of the real secret in the secrets array
     */
    private int selectRealSecretIndex(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] pinHash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            
            // Convert first 4 bytes to integer and take modulo
            int hashValue = ByteBuffer.wrap(Arrays.copyOf(pinHash, 4)).getInt();
            return Math.abs(hashValue % DETECTION_SECRET_COUNT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to select real secret index", e);
        }
    }
    
    /**
     * Encrypt a passkey private key using CASPER encryption.
     * 
     * Encryption: s̃ = HKDF(w*, z) XOR s
     * where:
     *   s = passkey private key (serialized)
     *   w* = real detection secret
     *   z = random value
     *   s̃ = encrypted passkey
     * 
     * @param privateKey Passkey private key to encrypt
     * @param realSecret Real detection secret (w*)
     * @param z Random value z
     * @return Encrypted passkey bytes (s̃)
     */
    public byte[] encryptPasskey(PrivateKey privateKey, byte[] realSecret, byte[] z) {
        try {
            // Serialize private key to bytes
            byte[] privateKeyBytes = privateKey.getEncoded();
            
            // Derive encryption key: HKDF(w*, z, "casper-passkey")
            byte[] info = "casper-passkey".getBytes(StandardCharsets.UTF_8);
            byte[] encryptionKey = HKDFHelper.derive(realSecret, z, info, privateKeyBytes.length);
            
            // XOR encrypt: s̃ = HKDF(w*, z) XOR s
            byte[] encrypted = new byte[privateKeyBytes.length];
            for (int i = 0; i < privateKeyBytes.length; i++) {
                encrypted[i] = (byte) (privateKeyBytes[i] ^ encryptionKey[i]);
            }
            
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt passkey", e);
        }
    }
    
    /**
     * Decrypt a passkey private key using CASPER decryption.
     * 
     * Decryption: s = HKDF(w*, z) XOR s̃
     * 
     * @param encryptedPasskey Encrypted passkey bytes (s̃)
     * @param realSecret Real detection secret (w*)
     * @param z Random value z
     * @return Decrypted private key bytes
     */
    public byte[] decryptPasskey(byte[] encryptedPasskey, byte[] realSecret, byte[] z) {
        try {
            // Derive decryption key: HKDF(w*, z, "casper-passkey")
            byte[] info = "casper-passkey".getBytes(StandardCharsets.UTF_8);
            byte[] decryptionKey = HKDFHelper.derive(realSecret, z, info, encryptedPasskey.length);
            
            // XOR decrypt: s = HKDF(w*, z) XOR s̃
            byte[] decrypted = new byte[encryptedPasskey.length];
            for (int i = 0; i < encryptedPasskey.length; i++) {
                decrypted[i] = (byte) (encryptedPasskey[i] ^ decryptionKey[i]);
            }
            
            return decrypted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt passkey", e);
        }
    }
    
    /**
     * Sign a challenge using a private key.
     * 
     * @param challenge Challenge bytes to sign
     * @param privateKey Private key to sign with
     * @return Signature bytes
     */
    public byte[] sign(byte[] challenge, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(challenge);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign challenge", e);
        }
    }
    
    /**
     * Verify a signature using a public key.
     * 
     * @param challenge Original challenge bytes
     * @param signatureBytes Signature bytes to verify
     * @param publicKey Public key to verify with
     * @return true if signature is valid
     */
    public boolean verify(byte[] challenge, byte[] signatureBytes, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(challenge);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Save user ID to secure storage.
     */
    public void saveUserId(String userId) {
        encryptedPrefs.edit().putString(KEY_USER_ID, userId).apply();
    }
    
    /**
     * Get user ID from secure storage.
     */
    public String getUserId() {
        return encryptedPrefs.getString(KEY_USER_ID, null);
    }
}

