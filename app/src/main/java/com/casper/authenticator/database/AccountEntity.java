package com.casper.authenticator.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

/**
 * Room Entity for Account storage.
 * 
 * Accounts are stored with CASPER-encrypted secrets.
 * Secrets are never stored in plaintext.
 */
@Entity(tableName = "accounts", indices = {@Index(value = {"label", "issuer"})})
public class AccountEntity {
    
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    // Account metadata
    public String label;           // Account label/name
    public String issuer;          // Service issuer (e.g., "Google", "GitHub")
    public String iconUrl;         // Optional icon URL
    
    // Encrypted secret (CASPER-encrypted TOTP/HOTP secret)
    public String encryptedSecret; // Base64-encoded encrypted secret
    
    // CASPER encryption metadata
    public String detectionSecretsJson; // JSON array of detection secrets (W)
    public int realSecretIndex;         // Index of real secret
    public String zValue;               // Base64-encoded random value z
    
    // OTP configuration
    public String algorithm;       // "SHA1", "SHA256", "SHA512"
    public int digits;             // 6 or 8
    public int period;             // Time step: 30 or 60 seconds (for TOTP)
    public String type;            // "TOTP" or "HOTP"
    public long counter;           // Counter for HOTP
    
    // Timestamps
    public long createdAt;
    public long updatedAt;
    
    // Getters and Setters (Room uses these)
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
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    public String getEncryptedSecret() {
        return encryptedSecret;
    }
    
    public void setEncryptedSecret(String encryptedSecret) {
        this.encryptedSecret = encryptedSecret;
    }
    
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
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
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
}

