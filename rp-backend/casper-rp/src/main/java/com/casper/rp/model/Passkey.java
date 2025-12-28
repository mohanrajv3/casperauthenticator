package com.casper.rp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class for registered passkeys at RP.
 * 
 * Stores public keys of all passkeys (real + decoys).
 * The first public key is the real one, others are decoys (trap keys).
 */
@Entity
@Table(name = "passkeys")
public class Passkey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "rp_id", nullable = false)
    private String rpId;
    
    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey; // Base64-encoded public key
    
    @Column(name = "is_real", nullable = false)
    private Boolean isReal; // true if real passkey, false if decoy (trap key)
    
    @Column(name = "key_index", nullable = false)
    private Integer keyIndex; // Index in the registration order (0 = real, 1+ = decoys)
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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
    
    public Boolean getIsReal() {
        return isReal;
    }
    
    public void setIsReal(Boolean isReal) {
        this.isReal = isReal;
    }
    
    public Integer getKeyIndex() {
        return keyIndex;
    }
    
    public void setKeyIndex(Integer keyIndex) {
        this.keyIndex = keyIndex;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

