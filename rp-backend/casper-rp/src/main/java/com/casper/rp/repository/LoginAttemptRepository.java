package com.casper.rp.repository;

import com.casper.rp.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for LoginAttempt entities.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    
    /**
     * Find all login attempts for a user and RP.
     */
    List<LoginAttempt> findByUserIdAndRpId(String userId, String rpId);
    
    /**
     * Find all breach detection events.
     */
    List<LoginAttempt> findByBreachDetectedTrue();
}

