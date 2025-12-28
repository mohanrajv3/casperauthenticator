package com.casper.pms.repository;

import com.casper.pms.model.EncryptedPasskey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for EncryptedPasskey entities.
 * 
 * Provides database operations for encrypted passkey data.
 */
@Repository
public interface PasskeyRepository extends JpaRepository<EncryptedPasskey, Long> {
    
    /**
     * Find encrypted passkey by user ID and RP ID.
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @return Optional EncryptedPasskey
     */
    Optional<EncryptedPasskey> findByUserIdAndRpId(String userId, String rpId);
    
    /**
     * Check if passkey exists for user and RP.
     * 
     * @param userId User identifier
     * @param rpId Relying Party identifier
     * @return true if exists
     */
    boolean existsByUserIdAndRpId(String userId, String rpId);
}

