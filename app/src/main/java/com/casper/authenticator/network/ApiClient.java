package com.casper.authenticator.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit API client for network requests.
 * 
 * Provides configured Retrofit instances for PMS and RP APIs.
 */
public class ApiClient {
    // Default base URLs - should be configured based on deployment
    public static final String PMS_BASE_URL = "http://10.0.2.2:8080/"; // Android emulator localhost
    public static final String RP_BASE_URL = "http://10.0.2.2:8081/"; // Android emulator localhost
    
    private static Retrofit pmsRetrofit = null;
    private static Retrofit rpRetrofit = null;
    
    /**
     * Get Retrofit instance for PMS API.
     * 
     * @param baseUrl Base URL of PMS service
     * @return Configured Retrofit instance
     */
    public static Retrofit getPmsRetrofit(String baseUrl) {
        if (pmsRetrofit == null || !pmsRetrofit.baseUrl().toString().equals(baseUrl)) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            
            pmsRetrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return pmsRetrofit;
    }
    
    /**
     * Get Retrofit instance for RP API.
     * 
     * @param baseUrl Base URL of RP service
     * @return Configured Retrofit instance
     */
    public static Retrofit getRpRetrofit(String baseUrl) {
        if (rpRetrofit == null || !rpRetrofit.baseUrl().toString().equals(baseUrl)) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            
            rpRetrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return rpRetrofit;
    }
}

