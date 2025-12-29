package com.casper.authenticator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.casper.authenticator.security.SecurityManager;

/**
 * Lock Screen Activity - Unlock the app with PIN or Biometric.
 */
public class LockScreenActivity extends AppCompatActivity {
    
    private EditText pinEditText;
    private Button unlockButton;
    private Button biometricButton;
    private TextView lockoutTextView;
    
    private SecurityManager securityManager;
    private static final String PREFS_NAME = "casper_prefs";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        
        securityManager = new SecurityManager(this);
        
        // Enable screenshot protection
        securityManager.enableScreenshotProtection(this);
        
        pinEditText = findViewById(R.id.pinEditText);
        unlockButton = findViewById(R.id.unlockButton);
        biometricButton = findViewById(R.id.biometricButton);
        lockoutTextView = findViewById(R.id.lockoutTextView);
        
        // Check if biometric is available
        if (securityManager.isBiometricAvailable()) {
            biometricButton.setVisibility(View.VISIBLE);
            biometricButton.setOnClickListener(v -> authenticateWithBiometric());
        } else {
            biometricButton.setVisibility(View.GONE);
        }
        
        unlockButton.setOnClickListener(v -> unlockWithPin());
        
        // Check lockout status
        updateLockoutStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateLockoutStatus();
    }
    
    /**
     * Update lockout status display.
     */
    private void updateLockoutStatus() {
        if (!securityManager.canAttemptPin()) {
            long remaining = securityManager.getRemainingLockoutTime();
            lockoutTextView.setText("Too many failed attempts. Try again in " + 
                (remaining / 60) + " minutes.");
            lockoutTextView.setVisibility(View.VISIBLE);
            unlockButton.setEnabled(false);
        } else {
            lockoutTextView.setVisibility(View.GONE);
            unlockButton.setEnabled(true);
        }
    }
    
    /**
     * Unlock with PIN.
     */
    private void unlockWithPin() {
        if (!securityManager.canAttemptPin()) {
            updateLockoutStatus();
            return;
        }
        
        String enteredPin = pinEditText.getText().toString().trim();
        
        if (enteredPin.isEmpty()) {
            Toast.makeText(this, "Please enter PIN", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verify PIN
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        com.casper.authenticator.crypto.CasperCrypto crypto = 
            new com.casper.authenticator.crypto.CasperCrypto(this);
        String storedPin = crypto.getPin();
        
        if (storedPin != null && storedPin.equals(enteredPin)) {
            // PIN correct
            securityManager.resetPinAttempts();
            securityManager.unlockApp();
            navigateToMain();
        } else {
            // PIN incorrect
            securityManager.recordFailedPinAttempt();
            pinEditText.setText("");
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            updateLockoutStatus();
        }
    }
    
    /**
     * Authenticate with biometric.
     */
    private void authenticateWithBiometric() {
        securityManager.authenticateWithBiometric(this, new SecurityManager.BiometricCallback() {
            @Override
            public void onSuccess() {
                securityManager.resetPinAttempts();
                securityManager.unlockApp();
                navigateToMain();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(LockScreenActivity.this, 
                    "Biometric authentication failed: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Navigate to main screen.
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, AccountListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

