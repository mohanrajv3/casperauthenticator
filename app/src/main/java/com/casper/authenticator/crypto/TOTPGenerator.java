package com.casper.authenticator.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

/**
 * TOTP (Time-based One-Time Password) Generator.
 * 
 * Implements RFC 6238 - TOTP: Time-Based One-Time Password Algorithm.
 * Uses HMAC-SHA1, HMAC-SHA256, or HMAC-SHA512 for code generation.
 * 
 * CASPER Integration:
 * - TOTP secrets are encrypted using CASPER before storage
 * - Decrypted secrets are used here to generate codes
 * - Secrets are never stored in plaintext
 */
public class TOTPGenerator {
    
    private static final int[] DIGITS_POWER = {
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000
    };
    
    /**
     * Generate a TOTP code.
     * 
     * @param secret Secret key (decrypted from CASPER-encrypted storage)
     * @param time Time step in seconds (usually current Unix time / timeStep)
     * @param algorithm HMAC algorithm ("HmacSHA1", "HmacSHA256", "HmacSHA512")
     * @param digits Number of digits in the code (6 or 8)
     * @return TOTP code as string
     */
    public static String generateTOTP(byte[] secret, long time, String algorithm, int digits) {
        try {
            // Calculate time step (T = (current time) / (time step X))
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(time).array();
            
            // Generate HMAC
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(secret, algorithm);
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(timeBytes);
            
            // Dynamic truncation (RFC 6238)
            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary = ((hmac[offset] & 0x7f) << 24) |
                        ((hmac[offset + 1] & 0xff) << 16) |
                        ((hmac[offset + 2] & 0xff) << 8) |
                        (hmac[offset + 3] & 0xff);
            
            int otp = binary % DIGITS_POWER[digits];
            
            // Format as string with leading zeros
            return String.format("%0" + digits + "d", otp);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP", e);
        }
    }
    
    /**
     * Generate TOTP for current time.
     * 
     * @param secret Secret key
     * @param timeStep Time step in seconds (default: 30)
     * @param algorithm HMAC algorithm
     * @param digits Number of digits
     * @return TOTP code
     */
    public static String generateCurrentTOTP(byte[] secret, int timeStep, String algorithm, int digits) {
        long currentTime = System.currentTimeMillis() / 1000;
        long timeCounter = currentTime / timeStep;
        return generateTOTP(secret, timeCounter, algorithm, digits);
    }
    
    /**
     * Generate TOTP with default settings (HMAC-SHA1, 30s period, 6 digits).
     * 
     * @param secret Secret key
     * @return TOTP code
     */
    public static String generateDefaultTOTP(byte[] secret) {
        return generateCurrentTOTP(secret, 30, "HmacSHA1", 6);
    }
    
    /**
     * Calculate remaining seconds until next code.
     * 
     * @param timeStep Time step in seconds
     * @return Seconds remaining
     */
    public static int getRemainingSeconds(int timeStep) {
        long currentTime = System.currentTimeMillis() / 1000;
        return (int) (timeStep - (currentTime % timeStep));
    }
    
    /**
     * Generate HOTP (HMAC-based One-Time Password) - RFC 4226.
     * 
     * @param secret Secret key
     * @param counter Counter value
     * @param algorithm HMAC algorithm
     * @param digits Number of digits
     * @return HOTP code
     */
    public static String generateHOTP(byte[] secret, long counter, String algorithm, int digits) {
        try {
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
            
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(secret, algorithm);
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(counterBytes);
            
            // Dynamic truncation
            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary = ((hmac[offset] & 0x7f) << 24) |
                        ((hmac[offset + 1] & 0xff) << 16) |
                        ((hmac[offset + 2] & 0xff) << 8) |
                        (hmac[offset + 3] & 0xff);
            
            int otp = binary % DIGITS_POWER[digits];
            
            return String.format("%0" + digits + "d", otp);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HOTP", e);
        }
    }
}

