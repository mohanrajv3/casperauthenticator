package com.casper.authenticator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.casper.authenticator.crypto.CasperCrypto;
import com.casper.authenticator.models.PasskeyData;
import com.casper.authenticator.network.ApiClient;
import com.casper.authenticator.network.PMSApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Restore Activity - Restore passkey from cloud backup.
 * 
 * Allows user to restore their encrypted passkey from PMS using their PIN.
 * This demonstrates cloud backup functionality - the passkey is encrypted
 * and can only be decrypted with the correct PIN.
 */
public class RestoreActivity extends AppCompatActivity {
    
    private EditText rpUrlEditText;
    private EditText pinEditText;
    private Button restoreButton;
    private TextView statusTextView;
    
    private CasperCrypto casperCrypto;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        
        casperCrypto = new CasperCrypto(this);
        userId = casperCrypto.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please set up PIN first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        rpUrlEditText = findViewById(R.id.rpUrlEditText);
        pinEditText = findViewById(R.id.pinEditText);
        restoreButton = findViewById(R.id.restoreButton);
        statusTextView = findViewById(R.id.statusTextView);
        
        restoreButton.setOnClickListener(v -> restorePasskey());
    }
    
    private void restorePasskey() {
        String rpUrl = rpUrlEditText.getText().toString().trim();
        String pin = pinEditText.getText().toString().trim();
        
        if (rpUrl.isEmpty()) {
            Toast.makeText(this, "Please enter RP URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (pin.length() < 4) {
            Toast.makeText(this, getString(R.string.pin_too_short), Toast.LENGTH_SHORT).show();
            return;
        }
        
        statusTextView.setText("Fetching passkey from cloud...");
        restoreButton.setEnabled(false);
        
        // Fetch encrypted passkey from PMS
        PMSApi pmsApi = ApiClient.getPmsRetrofit(ApiClient.PMS_BASE_URL).create(PMSApi.class);
        Call<PasskeyData> call = pmsApi.fetchPasskey(userId, rpUrl);
        call.enqueue(new Callback<PasskeyData>() {
            @Override
            public void onResponse(Call<PasskeyData> call, Response<PasskeyData> response) {
                restoreButton.setEnabled(true);
                if (response.isSuccessful()) {
                    PasskeyData passkeyData = response.body();
                    if (passkeyData != null) {
                        // Verify PIN can decrypt the passkey
                        try {
                            com.casper.authenticator.models.DetectionSecrets detectionSecrets = 
                                    passkeyData.getDetectionSecrets();
                            byte[] realSecret = detectionSecrets.getRealSecretAsBytes();
                            
                            // Try to decrypt (if PIN is wrong, this will fail or produce garbage)
                            byte[] encryptedPrivateKey = passkeyData.getEncryptedPrivateKeyAsBytes();
                            byte[] z = passkeyData.getZAsBytes();
                            byte[] privateKeyBytes = casperCrypto.decryptPasskey(
                                    encryptedPrivateKey, realSecret, z);
                            
                            // If we get here, decryption succeeded
                            statusTextView.setText("Passkey restored successfully!");
                            Toast.makeText(RestoreActivity.this, 
                                    getString(R.string.restore_success), Toast.LENGTH_SHORT).show();
                            
                            // Navigate back to home
                            finish();
                            
                        } catch (Exception e) {
                            statusTextView.setText("Restore failed: Invalid PIN");
                            Toast.makeText(RestoreActivity.this, 
                                    getString(R.string.restore_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        statusTextView.setText("Passkey not found");
                        Toast.makeText(RestoreActivity.this, "Passkey not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    statusTextView.setText("Failed to fetch passkey");
                    Toast.makeText(RestoreActivity.this, "Failed to fetch passkey", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<PasskeyData> call, Throwable t) {
                restoreButton.setEnabled(true);
                statusTextView.setText("Network error");
                Toast.makeText(RestoreActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

