package com.casper.rp.controller;

import com.casper.rp.model.LoginAttempt;
import com.casper.rp.repository.LoginAttemptRepository;
import com.casper.rp.service.CasperDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * 
 * Handles passkey registration and login with CASPER breach detection.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow Android app to connect
public class AuthController {
    
    @Autowired
    private CasperDetectionService detectionService;
    
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    /**
     * Register passkeys with RP.
     * 
     * Request body:
     * {
     *   "userId": "user-id",
     *   "rpId": "rp-id",
     *   "publicKeys": ["base64-public-key1", "base64-public-key2", ...]
     * }
     * 
     * First public key is real, others are decoy (trap) keys.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String rpId = (String) request.get("rpId");
            
            @SuppressWarnings("unchecked")
            java.util.List<String> publicKeysList = (java.util.List<String>) request.get("publicKeys");
            String[] publicKeys = publicKeysList.toArray(new String[0]);
            
            // Register passkeys (real + decoys)
            detectionService.registerPasskeys(userId, rpId, publicKeys);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Passkeys registered successfully");
            response.put("realKeyCount", 1);
            response.put("decoyKeyCount", publicKeys.length - 1);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Login with passkey signature.
     * 
     * Request body:
     * {
     *   "userId": "user-id",
     *   "rpId": "rp-id",
     *   "publicKey": "base64-public-key",
     *   "challenge": "challenge-string",
     *   "signature": "base64-signature"
     * }
     * 
     * Performs:
     * 1. Signature verification
     * 2. CASPER breach detection (CD algorithm)
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String rpId = (String) request.get("rpId");
            String publicKeyBase64 = (String) request.get("publicKey");
            String challenge = (String) request.get("challenge");
            String signatureBase64 = (String) request.get("signature");
            
            // Decode public key
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            // Decode signature
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            
            // Verify signature
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(challenge.getBytes());
            boolean signatureValid = signature.verify(signatureBytes);
            
            if (!signatureValid) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid signature");
                errorResponse.put("breachDetected", false);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Perform CASPER breach detection
            boolean breachDetected = detectionService.detectBreach(userId, rpId, publicKeyBase64);
            
            // Record login attempt
            LoginAttempt attempt = new LoginAttempt();
            attempt.setUserId(userId);
            attempt.setRpId(rpId);
            attempt.setPublicKey(publicKeyBase64);
            attempt.setBreachDetected(breachDetected);
            loginAttemptRepository.save(attempt);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", breachDetected ? 
                    "⚠️ BREACH DETECTED: Decoy passkey used!" : 
                    "Login successful");
            response.put("breachDetected", breachDetected);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Login failed: " + e.getMessage());
            errorResponse.put("breachDetected", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

