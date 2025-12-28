package com.casper.pms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CASPER Passkey Management Service (PMS) Application
 * 
 * This service stores encrypted passkey data in the cloud.
 * It NEVER decrypts or sees the actual passkey private keys.
 * 
 * PMS stores:
 * - Encrypted private key (s̃)
 * - Detection secrets (W)
 * - Random value (z)
 * - Public key (for reference)
 */
@SpringBootApplication
public class CasperPmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CasperPmsApplication.class, args);
    }
}

