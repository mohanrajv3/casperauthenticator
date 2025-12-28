package com.casper.authenticator.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

/**
 * Key generator for ECDSA key pairs.
 * 
 * Generates ECDSA key pairs on secp256r1 curve (P-256).
 * Used to create passkey public/private key pairs.
 */
public class KeyGenerator {
    private static final String ALGORITHM = "EC";
    private static final String CURVE = "secp256r1"; // P-256 curve
    private static final String PROVIDER = "AndroidOpenSSL";
    
    /**
     * Generate a new ECDSA key pair on secp256r1 curve.
     * 
     * @return KeyPair containing public and private keys
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE);
            keyGen.initialize(ecSpec, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchProviderException e) {
            // Fallback to default provider
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
                ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE);
                keyGen.initialize(ecSpec, new SecureRandom());
                return keyGen.generateKeyPair();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to generate key pair", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }
    
    /**
     * Generate random bytes using SecureRandom.
     * Used for generating detection secrets and other random values.
     * 
     * @param length Number of bytes to generate
     * @return Random byte array
     */
    public static byte[] generateRandomBytes(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}

