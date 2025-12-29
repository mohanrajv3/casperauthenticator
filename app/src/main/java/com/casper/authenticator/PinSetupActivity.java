package com.casper.authenticator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

/**
 * PIN Setup Activity.
 * 
 * Allows user to set a 4-6 digit PIN that protects their passkeys.
 * The PIN is used to select the real detection secret (w*) from k secrets.
 * PIN is stored locally and never sent to cloud.
 */
public class PinSetupActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "casper_prefs";
    private static final String KEY_PIN_SET = "pin_set";
    private static final String KEY_PIN_HASH = "pin_hash";
    
    private EditText pinEditText;
    private EditText confirmPinEditText;
    private Button setupButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);
        
        pinEditText = findViewById(R.id.pinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        setupButton = findViewById(R.id.setupButton);
        Button backButton = findViewById(R.id.backButton);
        
        setupButton.setOnClickListener(v -> {
            String pin = pinEditText.getText().toString();
            String confirmPin = confirmPinEditText.getText().toString();
            
            // Validate PIN
            if (pin.length() < 4) {
                Toast.makeText(this, getString(R.string.pin_too_short), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!pin.equals(confirmPin)) {
                Toast.makeText(this, getString(R.string.pin_mismatch), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Generate and save user ID
            String userId = UUID.randomUUID().toString();
            com.casper.authenticator.crypto.CasperCrypto crypto = 
                    new com.casper.authenticator.crypto.CasperCrypto(this);
            crypto.saveUserId(userId);
            
            // Save PIN to secure encrypted storage
            crypto.savePin(pin);
            
            // Also save PIN hash for verification (not the actual PIN)
            savePinHash(pin);
            
            Toast.makeText(this, getString(R.string.setup_complete), Toast.LENGTH_SHORT).show();
            
            // Navigate to Account List (main authenticator screen)
            Intent intent = new Intent(PinSetupActivity.this, AccountListActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Back button - go back to welcome (but PIN setup is required, so warn user)
        backButton.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(PinSetupActivity.this)
                .setTitle("PIN Setup Required")
                .setMessage("You must set up a PIN to use the app. Continue setup?")
                .setPositiveButton("Continue", null)
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
        });
    }
    
    /**
     * Save PIN hash for verification purposes only.
     * The actual PIN is stored securely in EncryptedSharedPreferences via CasperCrypto.
     */
    private void savePinHash(String pin) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Store hash of PIN (for verification only, not the actual PIN)
        // In production, use proper password hashing (PBKDF2, Argon2, etc.)
        String pinHash = String.valueOf(pin.hashCode());
        editor.putString(KEY_PIN_HASH, pinHash);
        editor.putBoolean(KEY_PIN_SET, true);
        editor.apply();
    }
}

