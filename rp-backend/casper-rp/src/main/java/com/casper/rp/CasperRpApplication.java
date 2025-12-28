package com.casper.rp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CASPER Relying Party (RP) Application
 * 
 * This service implements the Relying Party for CASPER passkey authentication.
 * 
 * RP responsibilities:
 * - Register passkeys (stores public keys and trap keys V')
 * - Verify login signatures
 * - Detect breaches using CASPER's CD algorithm
 *   - If login key ∈ V' → breach detected
 *   - If login key ∈ V → normal login
 */
@SpringBootApplication
public class CasperRpApplication {
    public static void main(String[] args) {
        SpringApplication.run(CasperRpApplication.class, args);
    }
}

