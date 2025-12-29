package com.casper.authenticator.models;

import android.util.Base64;

/**
 * Account model for TOTP/HOTP authenticator.
 * 
 * Represents an authenticator account with:
 * - Encrypted secret (encrypted using CASPER)
 * - Account metadata (label, issuer, icon)
 * - OTP configuration (algorithm, digits, period)
 */
public class Account {
    private Long id;
    private String label;              // Account label/name
    private String issuer;             // Service issuer (e.g., "Google", "GitHub")
    private String encryptedSecret;    // CASPER-encrypted TOTP secret (Base64)
    private String algorithm;          // HMAC algorithm: "SHA1", "SHA256", "SHA512"
    private int digits;                // Code length: 6 or 8
    private int period;                // Time step: 30 or 60 seconds (for TOTP)
    private String type;               // "TOTP" or "HOTP"
    private long counter;              // Counter for HOTP
    private String iconUrl;            // Optional icon URL
    private long createdAt;
    private long updatedAt;
    
    // CASPER-specific fields
    private String detectionSecretsJson; // JSON array of detection secrets (W)
    private int realSecretIndex;        // Index of real secret
    private String zValue;              // Random value z for CASPER encryption
    
    public Account() {
        // Default constructor
    }
    
    public Account(String label, String issuer, byte[] secret, String algorithm, 
                  int digits, int period, String type) {
        this.label = label;
        this.issuer = issuer;
        this.algorithm = algorithm != null ? algorithm : "SHA1";
        this.digits = digits > 0 ? digits : 6;
        this.period = period > 0 ? period : 30;
        this.type = type != null ? type : "TOTP";
        this.counter = 0;
        // Secret will be encrypted before storage
        this.encryptedSecret = Base64.encodeToString(secret, Base64.NO_WRAP);
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getEncryptedSecret() {
        return encryptedSecret;
    }
    
    public void setEncryptedSecret(String encryptedSecret) {
        this.encryptedSecret = encryptedSecret;
    }
    
    /**
     * Get encrypted secret as byte array.
     */
    public byte[] getEncryptedSecretAsBytes() {
        return Base64.decode(encryptedSecret, Base64.NO_WRAP);
    }
    
    /**
     * Set encrypted secret from byte array.
     */
    public void setEncryptedSecret(byte[] secret) {
        this.encryptedSecret = Base64.encodeToString(secret, Base64.NO_WRAP);
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Get HMAC algorithm name for crypto operations.
     */
    public String getHmacAlgorithm() {
        switch (algorithm.toUpperCase()) {
            case "SHA256":
                return "HmacSHA256";
            case "SHA512":
                return "HmacSHA512";
            case "SHA1":
            default:
                return "HmacSHA1";
        }
    }
    
    public int getDigits() {
        return digits;
    }
    
    public void setDigits(int digits) {
        this.digits = digits;
    }
    
    public int getPeriod() {
        return period;
    }
    
    public void setPeriod(int period) {
        this.period = period;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getCounter() {
        return counter;
    }
    
    public void setCounter(long counter) {
        this.counter = counter;
    }
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // CASPER-specific getters and setters
    public String getDetectionSecretsJson() {
        return detectionSecretsJson;
    }
    
    public void setDetectionSecretsJson(String detectionSecretsJson) {
        this.detectionSecretsJson = detectionSecretsJson;
    }
    
    public int getRealSecretIndex() {
        return realSecretIndex;
    }
    
    public void setRealSecretIndex(int realSecretIndex) {
        this.realSecretIndex = realSecretIndex;
    }
    
    public String getZValue() {
        return zValue;
    }
    
    public void setZValue(String zValue) {
        this.zValue = zValue;
    }
    
    /**
     * Get display name (label or issuer + label).
     */
    public String getDisplayName() {
        if (issuer != null && !issuer.isEmpty()) {
            return issuer + (label != null && !label.isEmpty() ? " (" + label + ")" : "");
        }
        return label != null ? label : "Unknown Account";
    }
}

