package com.casper.authenticator.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.Map;

/**
 * Retrofit API interface for Relying Party (RP).
 * 
 * RP handles:
 * - Passkey registration (stores public keys and trap keys)
 * - Login with signature verification
 * - CASPER breach detection
 */
public interface RPApi {
    
    /**
     * Register passkeys (real + decoys) with RP.
     * 
     * Request body should contain:
     * - publicKeys: Array of public keys (real + decoys)
     * - userId: User identifier
     * - rpId: Relying Party identifier
     * 
     * @param request Registration request with public keys
     * @return Call with registration response
     */
    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    
    /**
     * Login with passkey signature.
     * 
     * Request body should contain:
     * - userId: User identifier
     * - rpId: Relying Party identifier
     * - publicKey: Public key used for login
     * - challenge: Challenge string
     * - signature: Signature bytes (Base64 encoded)
     * 
     * @param request Login request with signature
     * @return Call with login response (includes breach detection result)
     */
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    /**
     * Request model for registration.
     */
    class RegisterRequest {
        public String userId;
        public String rpId;
        public String[] publicKeys; // Base64-encoded public keys
        
        public RegisterRequest(String userId, String rpId, String[] publicKeys) {
            this.userId = userId;
            this.rpId = rpId;
            this.publicKeys = publicKeys;
        }
    }
    
    /**
     * Response model for registration.
     */
    class RegisterResponse {
        public boolean success;
        public String message;
        
        public RegisterResponse() {
        }
    }
    
    /**
     * Request model for login.
     */
    class LoginRequest {
        public String userId;
        public String rpId;
        public String publicKey; // Base64-encoded public key
        public String challenge; // Challenge string
        public String signature; // Base64-encoded signature
        
        public LoginRequest(String userId, String rpId, String publicKey, 
                          String challenge, String signature) {
            this.userId = userId;
            this.rpId = rpId;
            this.publicKey = publicKey;
            this.challenge = challenge;
            this.signature = signature;
        }
    }
    
    /**
     * Response model for login.
     */
    class LoginResponse {
        public boolean success;
        public String message;
        public boolean breachDetected; // CASPER breach detection result
        
        public LoginResponse() {
        }
    }
}

