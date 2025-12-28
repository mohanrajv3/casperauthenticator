package com.casper.authenticator.models;

import android.util.Base64;

/**
 * Model class for passkey data stored in cloud PMS.
 * 
 * Contains:
 * - Encrypted passkey private key (s̃)
 * - Detection secrets (W)
 * - Random value (z)
 * - Public key (for verification)
 * - User ID and RP identifier
 */
public class PasskeyData {
    private String userId;
    private String rpId; // Relying Party identifier
    private String encryptedPrivateKey; // Base64-encoded encrypted private key (s̃)
    private String publicKey; // Base64-encoded public key
    private DetectionSecrets detectionSecrets; // Detection secrets (W)
    private String z; // Base64-encoded random value z
    
    public PasskeyData() {
        // Default constructor for JSON deserialization
    }
    
    public PasskeyData(String userId, String rpId, byte[] encryptedPrivateKey, 
                      byte[] publicKey, DetectionSecrets detectionSecrets, byte[] z) {
        this.userId = userId;
        this.rpId = rpId;
        this.encryptedPrivateKey = Base64.encodeToString(encryptedPrivateKey, Base64.NO_WRAP);
        this.publicKey = Base64.encodeToString(publicKey, Base64.NO_WRAP);
        this.detectionSecrets = detectionSecrets;
        this.z = Base64.encodeToString(z, Base64.NO_WRAP);
    }
    
    /**
     * Get encrypted private key as byte array.
     */
    public byte[] getEncryptedPrivateKeyAsBytes() {
        return Base64.decode(encryptedPrivateKey, Base64.NO_WRAP);
    }
    
    /**
     * Get public key as byte array.
     */
    public byte[] getPublicKeyAsBytes() {
        return Base64.decode(publicKey, Base64.NO_WRAP);
    }
    
    /**
     * Get z value as byte array.
     */
    public byte[] getZAsBytes() {
        return Base64.decode(z, Base64.NO_WRAP);
    }
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRpId() {
        return rpId;
    }
    
    public void setRpId(String rpId) {
        this.rpId = rpId;
    }
    
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }
    
    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public DetectionSecrets getDetectionSecrets() {
        return detectionSecrets;
    }
    
    public void setDetectionSecrets(DetectionSecrets detectionSecrets) {
        this.detectionSecrets = detectionSecrets;
    }
    
    public String getZ() {
        return z;
    }
    
    public void setZ(String z) {
        this.z = z;
    }
}

