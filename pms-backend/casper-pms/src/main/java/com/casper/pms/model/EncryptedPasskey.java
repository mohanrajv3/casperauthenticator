package com.casper.pms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class for encrypted passkey data stored in PMS.
 * 
 * PMS stores encrypted passkey information but never decrypts it.
 * Only the user's device can decrypt using the PIN-selected real secret.
 */
@Entity
@Table(name = "encrypted_passkeys", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "rp_id"}))
public class EncryptedPasskey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "rp_id", nullable = false)
    private String rpId;
    
    @Column(name = "encrypted_private_key", columnDefinition = "TEXT", nullable = false)
    private String encryptedPrivateKey; // Base64-encoded encrypted private key (s̃)
    
    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey; // Base64-encoded public key
    
    @Column(name = "detection_secrets", columnDefinition = "TEXT", nullable = false)
    private String detectionSecrets; // JSON array of Base64-encoded detection secrets (W)
    
    @Column(name = "z_value", columnDefinition = "TEXT", nullable = false)
    private String zValue; // Base64-encoded random value z
    
    @Column(name = "real_secret_index", nullable = false)
    private Integer realSecretIndex; // Index of the real secret in detection secrets array
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
    
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }
    
    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public String getDetectionSecrets() {
        return detectionSecrets;
    }
    
    public void setDetectionSecrets(String detectionSecrets) {
        this.detectionSecrets = detectionSecrets;
    }
    
    public String getZValue() {
        return zValue;
    }
    
    public void setZValue(String zValue) {
        this.zValue = zValue;
    }
    
    public Integer getRealSecretIndex() {
        return realSecretIndex;
    }
    
    public void setRealSecretIndex(Integer realSecretIndex) {
        this.realSecretIndex = realSecretIndex;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

