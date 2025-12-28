package com.casper.pms.service;

import com.casper.pms.model.EncryptedPasskey;
import com.casper.pms.repository.PasskeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service class for passkey management operations.
 * 
 * Handles storing and retrieving encrypted passkey data.
 * This service NEVER decrypts the data - it only stores and retrieves it.
 */
@Service
@Transactional
public class PasskeyService {
    
    @Autowired
    private PasskeyRepository passkeyRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Upload encrypted passkey data to PMS.
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @param encryptedPrivateKey Base64-encoded encrypted private key (s̃)
     * @param publicKey Base64-encoded public key
     * @param detectionSecrets Array of Base64-encoded detection secrets (W)
     * @param zValue Base64-encoded random value z
     * @param realSecretIndex Index of the real secret in detection secrets array
     * @return Saved EncryptedPasskey entity
     */
    public EncryptedPasskey uploadPasskey(String userId, String rpId,
                                         String encryptedPrivateKey, String publicKey,
                                         String[] detectionSecrets, String zValue,
                                         Integer realSecretIndex) {
        EncryptedPasskey passkey = passkeyRepository.findByUserIdAndRpId(userId, rpId)
                .orElse(new EncryptedPasskey());
        
        passkey.setUserId(userId);
        passkey.setRpId(rpId);
        passkey.setEncryptedPrivateKey(encryptedPrivateKey);
        passkey.setPublicKey(publicKey);
        
        // Store detection secrets as JSON array
        try {
            String secretsJson = objectMapper.writeValueAsString(detectionSecrets);
            passkey.setDetectionSecrets(secretsJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize detection secrets", e);
        }
        
        passkey.setZValue(zValue);
        passkey.setRealSecretIndex(realSecretIndex);
        
        return passkeyRepository.save(passkey);
    }
    
    /**
     * Fetch encrypted passkey data from PMS.
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @return EncryptedPasskey entity
     * @throws RuntimeException if passkey not found
     */
    @Transactional(readOnly = true)
    public EncryptedPasskey fetchPasskey(String userId, String rpId) {
        return passkeyRepository.findByUserIdAndRpId(userId, rpId)
                .orElseThrow(() -> new RuntimeException("Passkey not found for user: " + userId + ", rp: " + rpId));
    }
    
    /**
     * Check if passkey exists for user and RP.
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean passkeyExists(String userId, String rpId) {
        return passkeyRepository.existsByUserIdAndRpId(userId, rpId);
    }
}

