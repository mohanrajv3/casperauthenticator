package com.casper.authenticator.network;

import com.casper.authenticator.models.PasskeyData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API interface for Passkey Management Service (PMS).
 * 
 * PMS stores encrypted passkey data but never decrypts it.
 * It stores: encrypted private key, detection secrets, random value z, and public key.
 */
public interface PMSApi {
    
    /**
     * Upload encrypted passkey data to PMS.
     * 
     * @param passkeyData Encrypted passkey data (s̃, W, z, public key)
     * @return Call with PasskeyData response
     */
    @POST("api/passkeys")
    Call<PasskeyData> uploadPasskey(@Body PasskeyData passkeyData);
    
    /**
     * Fetch encrypted passkey data from PMS.
     * 
     * @param userId User ID
     * @param rpId Relying Party ID (as query parameter to support URLs with special characters)
     * @return Call with PasskeyData response
     */
    @GET("api/passkeys/{userId}")
    Call<PasskeyData> fetchPasskey(@Path("userId") String userId, @Query("rpId") String rpId);
}

