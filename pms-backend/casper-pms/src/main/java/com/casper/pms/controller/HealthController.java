package com.casper.pms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for PMS.
 * Provides a simple endpoint to verify the service is running.
 */
@RestController
public class HealthController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CASPER Passkey Management Service (PMS)");
        response.put("message", "Service is running");
        response.put("endpoints", Map.of(
            "uploadPasskey", "POST /api/passkeys",
            "fetchPasskey", "GET /api/passkeys/{userId}/{rpId}",
            "h2Console", "GET /h2-console"
        ));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }
}

