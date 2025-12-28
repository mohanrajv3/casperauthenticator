package com.casper.authenticator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.casper.authenticator.crypto.CasperCrypto;
import com.casper.authenticator.models.DetectionSecrets;
import com.casper.authenticator.models.PasskeyData;
import com.casper.authenticator.network.ApiClient;
import com.casper.authenticator.network.PMSApi;
import com.casper.authenticator.network.RPApi;

import android.util.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login Activity - Login with passkey.
 * 
 * Implements CASPER login:
 * 1. Fetch encrypted passkey from PMS
 * 2. Decrypt private key using PIN-selected real secret
 * 3. Sign challenge with private key
 * 4. Send signature to RP
 * 5. RP performs CASPER breach detection
 */
public class LoginActivity extends AppCompatActivity {
    
    private EditText rpUrlEditText;
    private Button loginButton;
    private TextView statusTextView;
    
    private CasperCrypto casperCrypto;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        casperCrypto = new CasperCrypto(this);
        userId = casperCrypto.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please set up PIN first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        rpUrlEditText = findViewById(R.id.rpUrlEditText);
        loginButton = findViewById(R.id.loginButton);
        statusTextView = findViewById(R.id.statusTextView);
        
        loginButton.setOnClickListener(v -> loginWithPasskey());
    }
    
    private void loginWithPasskey() {
        String rpUrl = rpUrlEditText.getText().toString().trim();
        if (rpUrl.isEmpty()) {
            Toast.makeText(this, "Please enter RP URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        statusTextView.setText("Fetching passkey...");
        loginButton.setEnabled(false);
        
        // Get PIN from secure storage
        String pin = getPin();
        if (pin == null) {
            Toast.makeText(this, "PIN not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Fetch encrypted passkey from PMS
        PMSApi pmsApi = ApiClient.getPmsRetrofit(ApiClient.PMS_BASE_URL).create(PMSApi.class);
        Call<PasskeyData> call = pmsApi.fetchPasskey(userId, rpUrl);
        call.enqueue(new Callback<PasskeyData>() {
            @Override
            public void onResponse(Call<PasskeyData> call, Response<PasskeyData> response) {
                if (response.isSuccessful()) {
                    PasskeyData passkeyData = response.body();
                    if (passkeyData != null) {
                        // Decrypt and login
                        performLogin(passkeyData, rpUrl, pin);
                    } else {
                        statusTextView.setText("Passkey not found");
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Passkey not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    statusTextView.setText("Failed to fetch passkey");
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Failed to fetch passkey", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<PasskeyData> call, Throwable t) {
                statusTextView.setText("Network error");
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void performLogin(PasskeyData passkeyData, String rpUrl, String pin) {
        try {
            statusTextView.setText("Decrypting passkey...");
            
            // Select real secret using PIN
            DetectionSecrets detectionSecrets = passkeyData.getDetectionSecrets();
            byte[] realSecret = detectionSecrets.getRealSecretAsBytes();
            
            // Decrypt private key: s = HKDF(w*, z) XOR s̃
            byte[] encryptedPrivateKey = passkeyData.getEncryptedPrivateKeyAsBytes();
            byte[] z = passkeyData.getZAsBytes();
            byte[] privateKeyBytes = casperCrypto.decryptPasskey(encryptedPrivateKey, realSecret, z);
            
            // Reconstruct private key from bytes
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            
            // Generate challenge (in real implementation, RP would send this)
            String challenge = "login_challenge_" + System.currentTimeMillis();
            byte[] challengeBytes = challenge.getBytes();
            
            // Sign challenge
            statusTextView.setText("Signing challenge...");
            byte[] signature = casperCrypto.sign(challengeBytes, privateKey);
            
            // Get public key
            byte[] publicKeyBytes = passkeyData.getPublicKeyAsBytes();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
            
            // Send login request to RP
            statusTextView.setText("Sending login request...");
            RPApi rpApi = ApiClient.getRpRetrofit(rpUrl).create(RPApi.class);
            RPApi.LoginRequest request = new RPApi.LoginRequest(
                    userId,
                    rpUrl,
                    Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP),
                    challenge,
                    Base64.encodeToString(signature, Base64.NO_WRAP)
            );
            
            Call<RPApi.LoginResponse> loginCall = rpApi.login(request);
            loginCall.enqueue(new Callback<RPApi.LoginResponse>() {
                @Override
                public void onResponse(Call<RPApi.LoginResponse> call, 
                                      Response<RPApi.LoginResponse> response) {
                    loginButton.setEnabled(true);
                    if (response.isSuccessful()) {
                        RPApi.LoginResponse loginResponse = response.body();
                        if (loginResponse != null) {
                            if (loginResponse.breachDetected) {
                                statusTextView.setText("⚠️ BREACH DETECTED!");
                                Toast.makeText(LoginActivity.this, 
                                        "CASPER detected a breach! Decoy passkey was used.", 
                                        Toast.LENGTH_LONG).show();
                            } else {
                                statusTextView.setText("Login successful!");
                                Toast.makeText(LoginActivity.this, 
                                        getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        statusTextView.setText("Login failed");
                        Toast.makeText(LoginActivity.this, 
                                getString(R.string.login_error), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<RPApi.LoginResponse> call, Throwable t) {
                    loginButton.setEnabled(true);
                    statusTextView.setText("Network error");
                    Toast.makeText(LoginActivity.this, 
                            "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            loginButton.setEnabled(true);
            statusTextView.setText("Error: " + e.getMessage());
            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getPin() {
        SharedPreferences securePrefs = getSharedPreferences("secure_prefs", MODE_PRIVATE);
        return securePrefs.getString("pin", null);
    }
}

