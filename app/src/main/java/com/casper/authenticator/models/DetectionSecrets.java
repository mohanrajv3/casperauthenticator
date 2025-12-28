package com.casper.authenticator.models;

import android.util.Base64;

/**
 * Model class for CASPER detection secrets.
 * 
 * Contains k detection secrets (W), where exactly one is the real secret (w*).
 * The real secret is selected based on the user's PIN.
 */
public class DetectionSecrets {
    private String[] secrets; // Base64-encoded detection secrets (W)
    private int realSecretIndex; // Index of the real secret (w*)
    
    public DetectionSecrets() {
        // Default constructor for JSON deserialization
    }
    
    public DetectionSecrets(byte[][] secretBytes, int realSecretIndex) {
        this.secrets = new String[secretBytes.length];
        for (int i = 0; i < secretBytes.length; i++) {
            this.secrets[i] = Base64.encodeToString(secretBytes[i], Base64.NO_WRAP);
        }
        this.realSecretIndex = realSecretIndex;
    }
    
    /**
     * Get all secrets as byte arrays.
     */
    public byte[][] getSecretsAsBytes() {
        byte[][] result = new byte[secrets.length][];
        for (int i = 0; i < secrets.length; i++) {
            result[i] = Base64.decode(secrets[i], Base64.NO_WRAP);
        }
        return result;
    }
    
    /**
     * Get the real secret (w*) as byte array.
     */
    public byte[] getRealSecretAsBytes() {
        return Base64.decode(secrets[realSecretIndex], Base64.NO_WRAP);
    }
    
    // Getters and setters
    public String[] getSecrets() {
        return secrets;
    }
    
    public void setSecrets(String[] secrets) {
        this.secrets = secrets;
    }
    
    public int getRealSecretIndex() {
        return realSecretIndex;
    }
    
    public void setRealSecretIndex(int realSecretIndex) {
        this.realSecretIndex = realSecretIndex;
    }
}

