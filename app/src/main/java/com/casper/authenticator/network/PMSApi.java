package com.casper.authenticator.network;

import com.casper.authenticator.models.PasskeyData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

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
     * @param rpId Relying Party ID
     * @return Call with PasskeyData response
     */
    @GET("api/passkeys/{userId}/{rpId}")
    Call<PasskeyData> fetchPasskey(@Path("userId") String userId, @Path("rpId") String rpId);
}

