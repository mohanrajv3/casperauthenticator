package com.casper.authenticator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.casper.authenticator.crypto.CasperCrypto;

/**
 * Welcome screen - Entry point of the app.
 * 
 * Checks if PIN is already set up:
 * - If PIN exists → go to HomeActivity
 * - If no PIN → go to PinSetupActivity
 */
public class WelcomeActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "casper_prefs";
    private static final String KEY_PIN_SET = "pin_set";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_welcome);
            
            TextView titleTextView = findViewById(R.id.welcomeTitle);
            TextView subtitleTextView = findViewById(R.id.welcomeSubtitle);
            Button getStartedButton = findViewById(R.id.getStartedButton);
            
            if (getStartedButton == null) {
                android.util.Log.e("WelcomeActivity", "getStartedButton is null");
                return;
            }
            
            // Check if PIN is already set
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean pinSet = prefs.getBoolean(KEY_PIN_SET, false);
            
            if (pinSet) {
                // PIN already set, go directly to home
                // Use postDelayed to ensure UI is ready
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        navigateToHome();
                    } catch (Exception e) {
                        android.util.Log.e("WelcomeActivity", "Error navigating to home", e);
                        // If navigation fails, show welcome screen
                    }
                }, 100);
                return;
            }
            
            // Setup button click listener
            getStartedButton.setOnClickListener(v -> {
                try {
                    // Navigate to PIN setup
                    Intent intent = new Intent(WelcomeActivity.this, PinSetupActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("WelcomeActivity", "Error starting PinSetupActivity", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("WelcomeActivity", "Error in onCreate", e);
            // Show error message to user
            android.widget.Toast.makeText(this, "Error initializing app. Please restart.", android.widget.Toast.LENGTH_LONG).show();
        }
    }
    
    private void navigateToHome() {
        // Navigate to AccountListActivity (main authenticator screen)
        Intent intent = new Intent(WelcomeActivity.this, AccountListActivity.class);
        startActivity(intent);
        finish();
    }
}

