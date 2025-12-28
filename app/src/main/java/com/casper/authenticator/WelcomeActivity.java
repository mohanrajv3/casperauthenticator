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
        setContentView(R.layout.activity_welcome);
        
        TextView titleTextView = findViewById(R.id.welcomeTitle);
        TextView subtitleTextView = findViewById(R.id.welcomeSubtitle);
        Button getStartedButton = findViewById(R.id.getStartedButton);
        
        // Check if PIN is already set
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean pinSet = prefs.getBoolean(KEY_PIN_SET, false);
        
        if (pinSet) {
            // PIN already set, go directly to home
            navigateToHome();
            return;
        }
        
        // Setup button click listener
        getStartedButton.setOnClickListener(v -> {
            // Navigate to PIN setup
            Intent intent = new Intent(WelcomeActivity.this, PinSetupActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}

