package com.casper.rp.service;

import com.casper.rp.model.Passkey;
import com.casper.rp.repository.PasskeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CASPER Breach Detection Service.
 * 
 * Implements CASPER's Compromise Detection (CD) algorithm:
 * 
 * CD Algorithm:
 * 1. During registration, store all public keys
 * 2. Mark first key as real (V), others as trap keys (V')
 * 3. During login:
 *    - If login key ∈ V' → BREACH DETECTED (decoy passkey used)
 *    - If login key ∈ V → Normal login (real passkey used)
 * 
 * This detects when an attacker steals encrypted passkey data from PMS
 * and tries to use a decoy secret to decrypt, resulting in a fake passkey.
 */
@Service
@Transactional
public class CasperDetectionService {
    
    @Autowired
    private PasskeyRepository passkeyRepository;
    
    /**
     * Register passkeys (real + decoys) with RP.
     * 
     * First public key is the real passkey (V).
     * All other public keys are decoy/trap keys (V').
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @param publicKeys Array of Base64-encoded public keys
     */
    public void registerPasskeys(String userId, String rpId, String[] publicKeys) {
        // Delete existing passkeys for this user+RP combination
        List<Passkey> existing = passkeyRepository.findByUserIdAndRpId(userId, rpId);
        passkeyRepository.deleteAll(existing);
        
        // Register all passkeys
        for (int i = 0; i < publicKeys.length; i++) {
            Passkey passkey = new Passkey();
            passkey.setUserId(userId);
            passkey.setRpId(rpId);
            passkey.setPublicKey(publicKeys[i]);
            passkey.setKeyIndex(i);
            
            // First key (index 0) is real, others are decoys (trap keys)
            passkey.setIsReal(i == 0);
            
            passkeyRepository.save(passkey);
        }
    }
    
    /**
     * Perform CASPER breach detection during login.
     * 
     * CD Algorithm:
     * - Check if login public key is in trap key set V'
     * - If yes → breach detected (attacker used decoy secret)
     * - If no → normal login (user used real secret)
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @param loginPublicKey Base64-encoded public key used for login
     * @return true if breach detected, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean detectBreach(String userId, String rpId, String loginPublicKey) {
        // Find the passkey with this public key
        Passkey loginKey = passkeyRepository
                .findByPublicKeyAndUserIdAndRpId(loginPublicKey, userId, rpId)
                .orElse(null);
        
        if (loginKey == null) {
            // Key not found in registered passkeys - invalid login
            return false;
        }
        
        // CASPER Detection Logic:
        // If the login key is a trap key (decoy), breach is detected
        // If the login key is the real key, normal login
        return !loginKey.getIsReal(); // Breach detected if it's a decoy key
    }
    
    /**
     * Get all trap keys (V') for a user and RP.
     * These are the decoy public keys used for breach detection.
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @return List of decoy passkeys (trap keys)
     */
    @Transactional(readOnly = true)
    public List<Passkey> getTrapKeys(String userId, String rpId) {
        return passkeyRepository.findByUserIdAndRpIdAndIsRealFalse(userId, rpId);
    }
    
    /**
     * Get the real passkey (V) for a user and RP.
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @return Real passkey, or null if not found
     */
    @Transactional(readOnly = true)
    public Passkey getRealPasskey(String userId, String rpId) {
        return passkeyRepository.findByUserIdAndRpIdAndIsRealTrue(userId, rpId)
                .orElse(null);
    }
}

