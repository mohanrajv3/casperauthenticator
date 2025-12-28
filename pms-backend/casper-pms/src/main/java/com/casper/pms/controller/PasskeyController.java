package com.casper.pms.controller;

import com.casper.pms.model.EncryptedPasskey;
import com.casper.pms.service.PasskeyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Passkey Management Service.
 * 
 * Provides endpoints for uploading and fetching encrypted passkey data.
 * PMS never decrypts the data - it only stores and retrieves encrypted bytes.
 */
@RestController
@RequestMapping("/api/passkeys")
@CrossOrigin(origins = "*") // Allow Android app to connect
public class PasskeyController {
    
    @Autowired
    private PasskeyService passkeyService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Upload encrypted passkey data to PMS.
     * 
     * Request body format:
     * {
     *   "userId": "user-id",
     *   "rpId": "rp-id",
     *   "encryptedPrivateKey": "base64-encoded-encrypted-key",
     *   "publicKey": "base64-encoded-public-key",
     *   "detectionSecrets": {
     *     "secrets": ["base64-secret1", "base64-secret2", ...],
     *     "realSecretIndex": 0
     *   },
     *   "z": "base64-encoded-z-value"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadPasskey(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String rpId = (String) request.get("rpId");
            String encryptedPrivateKey = (String) request.get("encryptedPrivateKey");
            String publicKey = (String) request.get("publicKey");
            String z = (String) request.get("z");
            
            // Extract detection secrets
            Map<String, Object> detectionSecretsMap = (Map<String, Object>) request.get("detectionSecrets");
            JsonNode secretsNode = objectMapper.valueToTree(detectionSecretsMap.get("secrets"));
            String[] detectionSecrets = objectMapper.convertValue(secretsNode, String[].class);
            Integer realSecretIndex = (Integer) detectionSecretsMap.get("realSecretIndex");
            
            EncryptedPasskey passkey = passkeyService.uploadPasskey(
                    userId, rpId, encryptedPrivateKey, publicKey,
                    detectionSecrets, z, realSecretIndex
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Passkey uploaded successfully");
            response.put("id", passkey.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to upload passkey: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Fetch encrypted passkey data from PMS.
     * 
     * Response format:
     * {
     *   "userId": "user-id",
     *   "rpId": "rp-id",
     *   "encryptedPrivateKey": "base64-encoded-encrypted-key",
     *   "publicKey": "base64-encoded-public-key",
     *   "detectionSecrets": {
     *     "secrets": ["base64-secret1", "base64-secret2", ...],
     *     "realSecretIndex": 0
     *   },
     *   "z": "base64-encoded-z-value"
     * }
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> fetchPasskey(
            @PathVariable String userId,
            @RequestParam String rpId) {
        try {
            EncryptedPasskey passkey = passkeyService.fetchPasskey(userId, rpId);
            
            // Parse detection secrets JSON array
            String[] detectionSecrets = objectMapper.readValue(
                    passkey.getDetectionSecrets(), String[].class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", passkey.getUserId());
            response.put("rpId", passkey.getRpId());
            response.put("encryptedPrivateKey", passkey.getEncryptedPrivateKey());
            response.put("publicKey", passkey.getPublicKey());
            
            Map<String, Object> detectionSecretsMap = new HashMap<>();
            detectionSecretsMap.put("secrets", detectionSecrets);
            detectionSecretsMap.put("realSecretIndex", passkey.getRealSecretIndex());
            response.put("detectionSecrets", detectionSecretsMap);
            
            response.put("z", passkey.getZValue());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch passkey: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}

