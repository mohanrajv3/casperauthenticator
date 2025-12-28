package com.casper.rp.repository;

import com.casper.rp.model.Passkey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Passkey entities.
 */
@Repository
public interface PasskeyRepository extends JpaRepository<Passkey, Long> {
    
    /**
     * Find all passkeys for a user and RP.
     */
    List<Passkey> findByUserIdAndRpId(String userId, String rpId);
    
    /**
     * Find passkey by public key, user ID, and RP ID.
     */
    Optional<Passkey> findByPublicKeyAndUserIdAndRpId(String publicKey, String userId, String rpId);
    
    /**
     * Find real passkey for a user and RP.
     */
    Optional<Passkey> findByUserIdAndRpIdAndIsRealTrue(String userId, String rpId);
    
    /**
     * Find all decoy (trap) passkeys for a user and RP.
     */
    List<Passkey> findByUserIdAndRpIdAndIsRealFalse(String userId, String rpId);
}

