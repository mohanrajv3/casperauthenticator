package com.casper.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Home Activity - Main menu of the app.
 * 
 * Provides options to:
 * - Register a new passkey
 * - Login with an existing passkey
 * - Restore passkey from cloud backup
 */
public class HomeActivity extends AppCompatActivity {
    
    private Button registerButton;
    private Button loginButton;
    private Button restoreButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        restoreButton = findViewById(R.id.restoreButton);
        
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        
        restoreButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RestoreActivity.class);
            startActivity(intent);
        });
    }
}

