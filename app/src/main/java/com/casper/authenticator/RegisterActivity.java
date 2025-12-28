package com.casper.authenticator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.casper.authenticator.crypto.CasperCrypto;
import com.casper.authenticator.crypto.KeyGenerator;
import com.casper.authenticator.models.DetectionSecrets;
import com.casper.authenticator.models.PasskeyData;
import com.casper.authenticator.network.ApiClient;
import com.casper.authenticator.network.PMSApi;
import com.casper.authenticator.network.RPApi;

import android.util.Base64;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Register Activity - Register a new passkey with RP.
 * 
 * Implements CASPER registration:
 * 1. Generate k detection secrets, select real one using PIN
 * 2. Generate ECDSA key pair
 * 3. Encrypt private key: s̃ = HKDF(w*, z) XOR s
 * 4. Generate decoy passkeys using fake secrets
 * 5. Upload encrypted passkey to PMS
 * 6. Register all public keys (real + decoys) with RP
 */
public class RegisterActivity extends AppCompatActivity {
    
    private static final int DECOY_COUNT = 4; // Number of decoy passkeys
    
    private EditText rpUrlEditText;
    private Button registerButton;
    private TextView statusTextView;
    
    private CasperCrypto casperCrypto;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        casperCrypto = new CasperCrypto(this);
        userId = casperCrypto.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please set up PIN first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        rpUrlEditText = findViewById(R.id.rpUrlEditText);
        registerButton = findViewById(R.id.registerButton);
        statusTextView = findViewById(R.id.statusTextView);
        
        registerButton.setOnClickListener(v -> registerPasskey());
    }
    
    private void registerPasskey() {
        String rpUrl = rpUrlEditText.getText().toString().trim();
        if (rpUrl.isEmpty()) {
            Toast.makeText(this, "Please enter RP URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        statusTextView.setText("Generating passkey...");
        registerButton.setEnabled(false);
        
        // Get PIN from secure storage
        String pin = getPin();
        if (pin == null) {
            Toast.makeText(this, "PIN not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        try {
            // 1. Generate detection secrets
            DetectionSecrets detectionSecrets = casperCrypto.generateDetectionSecrets(pin);
            byte[] realSecret = detectionSecrets.getRealSecretAsBytes();
            
            // 2. Generate real passkey key pair
            KeyPair realKeyPair = KeyGenerator.generateKeyPair();
            PrivateKey realPrivateKey = realKeyPair.getPrivate();
            PublicKey realPublicKey = realKeyPair.getPublic();
            
            // 3. Generate random value z
            byte[] z = KeyGenerator.generateRandomBytes(32);
            
            // 4. Encrypt private key: s̃ = HKDF(w*, z) XOR s
            byte[] encryptedPrivateKey = casperCrypto.encryptPasskey(realPrivateKey, realSecret, z);
            
            // 5. Create PasskeyData for PMS
            PasskeyData passkeyData = new PasskeyData(
                    userId,
                    rpUrl,
                    encryptedPrivateKey,
                    realPublicKey.getEncoded(),
                    detectionSecrets,
                    z
            );
            
            // 6. Upload to PMS
            PMSApi pmsApi = ApiClient.getPmsRetrofit(ApiClient.PMS_BASE_URL).create(PMSApi.class);
            Call<PasskeyData> pmsCall = pmsApi.uploadPasskey(passkeyData);
            pmsCall.enqueue(new Callback<PasskeyData>() {
                @Override
                public void onResponse(Call<PasskeyData> call, Response<PasskeyData> response) {
                    if (response.isSuccessful()) {
                        // 7. Generate decoy passkeys and register all with RP
                        registerWithRP(rpUrl, realPublicKey, detectionSecrets, z);
                    } else {
                        statusTextView.setText("PMS upload failed");
                        registerButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Failed to upload to PMS", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<PasskeyData> call, Throwable t) {
                    statusTextView.setText("Network error");
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            statusTextView.setText("Error: " + e.getMessage());
            registerButton.setEnabled(true);
            Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void registerWithRP(String rpUrl, PublicKey realPublicKey, 
                                DetectionSecrets detectionSecrets, byte[] z) {
        try {
            // Generate decoy passkeys using fake secrets
            List<String> publicKeys = new ArrayList<>();
            
            // Add real public key
            publicKeys.add(Base64.encodeToString(realPublicKey.getEncoded(), Base64.NO_WRAP));
            
            // Generate decoy passkeys using decoy secrets
            byte[][] secrets = detectionSecrets.getSecretsAsBytes();
            int realIndex = detectionSecrets.getRealSecretIndex();
            
            for (int i = 0; i < secrets.length; i++) {
                if (i != realIndex) {
                    // Generate decoy key pair (in real CASPER, this would use the decoy secret
                    // to derive a decoy passkey. For simplicity, we generate random key pairs)
                    KeyPair decoyKeyPair = KeyGenerator.generateKeyPair();
                    publicKeys.add(Base64.encodeToString(
                            decoyKeyPair.getPublic().getEncoded(), Base64.NO_WRAP));
                }
            }
            
            // Register all public keys with RP
            RPApi rpApi = ApiClient.getRpRetrofit(rpUrl).create(RPApi.class);
            RPApi.RegisterRequest request = new RPApi.RegisterRequest(
                    userId,
                    rpUrl,
                    publicKeys.toArray(new String[0])
            );
            
            Call<RPApi.RegisterResponse> rpCall = rpApi.register(request);
            rpCall.enqueue(new Callback<RPApi.RegisterResponse>() {
                @Override
                public void onResponse(Call<RPApi.RegisterResponse> call, 
                                      Response<RPApi.RegisterResponse> response) {
                    registerButton.setEnabled(true);
                    if (response.isSuccessful()) {
                        statusTextView.setText("Registration successful!");
                        Toast.makeText(RegisterActivity.this, 
                                getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                    } else {
                        statusTextView.setText("RP registration failed");
                        Toast.makeText(RegisterActivity.this, 
                                getString(R.string.registration_error), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<RPApi.RegisterResponse> call, Throwable t) {
                    registerButton.setEnabled(true);
                    statusTextView.setText("Network error");
                    Toast.makeText(RegisterActivity.this, 
                            "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            registerButton.setEnabled(true);
            statusTextView.setText("Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getPin() {
        SharedPreferences securePrefs = getSharedPreferences("secure_prefs", MODE_PRIVATE);
        return securePrefs.getString("pin", null);
    }
}

