package com.casper.authenticator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.casper.authenticator.repository.AccountRepository;

import org.apache.commons.codec.binary.Base32;

/**
 * Add Account Activity - Manual account entry.
 * 
 * Allows users to manually enter account details for TOTP/HOTP.
 * QR code scanning will be added separately.
 */
public class AddAccountActivity extends AppCompatActivity {
    
    private EditText labelEditText;
    private EditText issuerEditText;
    private EditText secretEditText;
    private Spinner algorithmSpinner;
    private Spinner digitsSpinner;
    private Spinner periodSpinner;
    private Button saveButton;
    private Button cancelButton;
    
    private AccountRepository accountRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        
        accountRepository = new AccountRepository(this);
        
        // Find views
        labelEditText = findViewById(R.id.labelEditText);
        issuerEditText = findViewById(R.id.issuerEditText);
        secretEditText = findViewById(R.id.secretEditText);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);
        digitsSpinner = findViewById(R.id.digitsSpinner);
        periodSpinner = findViewById(R.id.periodSpinner);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        
        // Setup spinners (for now, using simple defaults)
        // TODO: Implement proper spinner adapters
        
        saveButton.setOnClickListener(v -> saveAccount());
        cancelButton.setOnClickListener(v -> finish());
    }
    
    private void saveAccount() {
        String label = labelEditText.getText().toString().trim();
        String issuer = issuerEditText.getText().toString().trim();
        String secretString = secretEditText.getText().toString().trim().replaceAll("\\s+", "");
        
        // Validation
        if (label.isEmpty()) {
            Toast.makeText(this, "Label is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (secretString.isEmpty()) {
            Toast.makeText(this, "Secret key is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Decode Base32 secret
            byte[] secret = decodeBase32(secretString);
            
            // Default values (can be enhanced with spinners)
            String algorithm = "SHA1";
            int digits = 6;
            int period = 30;
            String type = "TOTP";
            
            // Add account (will be encrypted using CASPER)
            accountRepository.addAccount(label, issuer, secret, algorithm, digits, period, type);
            
            Toast.makeText(this, "Account added successfully", Toast.LENGTH_SHORT).show();
            finish();
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to add account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Decode Base32 string to byte array.
     * Handles both Base32 and Base64 encoded secrets.
     */
    private byte[] decodeBase32(String secretString) {
        // Try Base32 first (standard for TOTP)
        try {
            Base32 base32Decoder = new Base32();
            return base32Decoder.decode(secretString.toUpperCase().replaceAll("\\s+", ""));
        } catch (Exception e) {
            // If Base32 fails, try Base64 (some implementations use Base64)
            try {
                return android.util.Base64.decode(secretString.replaceAll("\\s+", ""), 
                    android.util.Base64.NO_WRAP);
            } catch (Exception e2) {
                throw new IllegalArgumentException("Invalid secret format. Expected Base32 or Base64.", e2);
            }
        }
    }
}

