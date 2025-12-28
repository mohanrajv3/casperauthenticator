package com.casper.rp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class for login attempts.
 * 
 * Records all login attempts including breach detection results.
 */
@Entity
@Table(name = "login_attempts")
public class LoginAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "rp_id", nullable = false)
    private String rpId;
    
    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey; // Base64-encoded public key used for login
    
    @Column(name = "breach_detected", nullable = false)
    private Boolean breachDetected; // CASPER breach detection result
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRpId() {
        return rpId;
    }
    
    public void setRpId(String rpId) {
        this.rpId = rpId;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public Boolean getBreachDetected() {
        return breachDetected;
    }
    
    public void setBreachDetected(Boolean breachDetected) {
        this.breachDetected = breachDetected;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

