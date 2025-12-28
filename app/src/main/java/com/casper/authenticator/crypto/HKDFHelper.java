package com.casper.authenticator.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HKDF (HMAC-based Key Derivation Function) helper class.
 * 
 * Implements HKDF-SHA256 as specified in RFC 5869.
 * Used in CASPER to derive encryption keys from secrets.
 * 
 * HKDF(ikm, salt, info) = Extract(ikm, salt) + Expand(extracted, info, length)
 */
public class HKDFHelper {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int HASH_LENGTH = 32; // SHA-256 output length
    
    /**
     * Derive a key using HKDF-SHA256.
     * 
     * @param ikm Input Key Material (the secret)
     * @param salt Salt value (can be empty)
     * @param info Context/application specific information
     * @param length Desired output length in bytes
     * @return Derived key bytes
     */
    public static byte[] derive(byte[] ikm, byte[] salt, byte[] info, int length) {
        try {
            // Extract phase: HMAC(ikm, salt)
            byte[] prk = extract(ikm, salt);
            
            // Expand phase: Generate output key material
            return expand(prk, info, length);
        } catch (Exception e) {
            throw new RuntimeException("HKDF derivation failed", e);
        }
    }
    
    /**
     * Extract phase: Generate pseudo-random key (PRK) from IKM and salt.
     */
    private static byte[] extract(byte[] ikm, byte[] salt) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance(HMAC_SHA256);
        
        // If salt is empty or null, use zero-filled salt
        if (salt == null || salt.length == 0) {
            salt = new byte[HASH_LENGTH];
        }
        
        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(salt, HMAC_SHA256);
        mac.init(keySpec);
        return mac.doFinal(ikm);
    }
    
    /**
     * Expand phase: Generate output key material from PRK.
     */
    private static byte[] expand(byte[] prk, byte[] info, int length) throws Exception {
        if (length > 255 * HASH_LENGTH) {
            throw new IllegalArgumentException("Length exceeds maximum allowed");
        }
        
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance(HMAC_SHA256);
        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(prk, HMAC_SHA256);
        mac.init(keySpec);
        
        int numBlocks = (length + HASH_LENGTH - 1) / HASH_LENGTH;
        byte[] output = new byte[length];
        byte[] t = new byte[0];
        
        for (int i = 0; i < numBlocks; i++) {
            mac.reset();
            mac.init(keySpec);
            mac.update(t);
            if (info != null) {
                mac.update(info);
            }
            mac.update((byte) (i + 1));
            t = mac.doFinal();
            
            int offset = i * HASH_LENGTH;
            int copyLength = Math.min(HASH_LENGTH, length - offset);
            System.arraycopy(t, 0, output, offset, copyLength);
        }
        
        return output;
    }
}

