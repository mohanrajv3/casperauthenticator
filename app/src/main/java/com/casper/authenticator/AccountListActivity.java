package com.casper.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;

import com.casper.authenticator.adapter.AccountAdapter;
import com.casper.authenticator.database.AccountEntity;
import com.casper.authenticator.repository.AccountRepository;
import com.casper.authenticator.security.SecurityManager;

import java.util.List;

/**
 * Main Account List Activity - Displays all accounts with OTP codes.
 * 
 * This is the main screen of the authenticator app.
 * Shows all accounts with real-time OTP codes and countdown timers.
 */
public class AccountListActivity extends AppCompatActivity {
    
    private RecyclerView accountRecyclerView;
    private AccountAdapter accountAdapter;
    private AccountRepository accountRepository;
    private TextView emptyStateTextView;
    private Button addAccountButton;
    private Handler updateHandler;
    private Runnable updateRunnable;
    private SecurityManager securityManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_account_list);
            
            // Initialize security manager (only after UI is set up)
            try {
                securityManager = new SecurityManager(this);
                
                // Check if app is locked (only if PIN is already set)
                SharedPreferences prefs = getSharedPreferences("casper_prefs", MODE_PRIVATE);
                boolean pinSet = prefs.getBoolean("pin_set", false);
                
                if (pinSet && securityManager.isLocked()) {
                    // Redirect to lock screen
                    android.content.Intent lockIntent = new android.content.Intent(
                        this, LockScreenActivity.class);
                    startActivity(lockIntent);
                    finish();
                    return;
                }
                
                // Enable security features (only if PIN is set)
                if (pinSet) {
                    securityManager.enableScreenshotProtection(this);
                    securityManager.startAutoLockTimer();
                }
            } catch (Exception e) {
                android.util.Log.e("AccountListActivity", "Error initializing security manager", e);
                // Continue without security features if initialization fails
            }
            
            // Initialize repository
            accountRepository = new AccountRepository(this);
            
            // Find views
            accountRecyclerView = findViewById(R.id.accountRecyclerView);
            emptyStateTextView = findViewById(R.id.emptyStateTextView);
            addAccountButton = findViewById(R.id.addAccountButton);
            
            // Setup RecyclerView
            accountAdapter = new AccountAdapter(this, accountRepository);
            accountRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            accountRecyclerView.setAdapter(accountAdapter);
            
            // Add account button
            addAccountButton.setOnClickListener(v -> {
                Intent intent = new Intent(AccountListActivity.this, AddAccountActivity.class);
                startActivity(intent);
            });
            
            // Setup auto-refresh for OTP codes (every second)
            updateHandler = new Handler(Looper.getMainLooper());
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    refreshAccounts();
                    updateHandler.postDelayed(this, 1000); // Update every second
                }
            };
        } catch (Exception e) {
            android.util.Log.e("AccountListActivity", "Error in onCreate", e);
            // If initialization fails, finish the activity and go back
            finish();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check lock status (only if PIN is set and security manager is initialized)
        try {
            SharedPreferences prefs = getSharedPreferences("casper_prefs", MODE_PRIVATE);
            boolean pinSet = prefs.getBoolean("pin_set", false);
            
            if (pinSet && securityManager != null && securityManager.isLocked()) {
                android.content.Intent lockIntent = new android.content.Intent(
                    this, LockScreenActivity.class);
                startActivity(lockIntent);
                finish();
                return;
            }
            
            // Update last active time (only if PIN is set)
            if (pinSet && securityManager != null) {
                securityManager.updateLastActiveTime();
                securityManager.startAutoLockTimer();
            }
        } catch (Exception e) {
            android.util.Log.e("AccountListActivity", "Error in onResume security check", e);
        }
        
        refreshAccounts();
        // Start auto-refresh
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.postDelayed(updateRunnable, 1000);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-refresh
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
        // Stop auto-lock timer
        if (securityManager != null) {
            securityManager.stopAutoLockTimer();
        }
    }
    
    private void refreshAccounts() {
        try {
            if (accountRepository == null) {
                return;
            }
            List<AccountEntity> accounts = accountRepository.getAllAccounts();
            
            if (accounts.isEmpty()) {
                accountRecyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            } else {
                accountRecyclerView.setVisibility(View.VISIBLE);
                emptyStateTextView.setVisibility(View.GONE);
                accountAdapter.setAccounts(accounts);
            }
        } catch (Exception e) {
            android.util.Log.e("AccountListActivity", "Error refreshing accounts", e);
        }
    }
    
    @Override
    public void onBackPressed() {
        // Exit app when back pressed from main screen
        moveTaskToBack(true);
    }
}

