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

import com.casper.authenticator.adapter.AccountAdapter;
import com.casper.authenticator.database.AccountEntity;
import com.casper.authenticator.repository.AccountRepository;

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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);
        
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
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        refreshAccounts();
        // Start auto-refresh
        updateHandler.postDelayed(updateRunnable, 1000);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-refresh
        updateHandler.removeCallbacks(updateRunnable);
    }
    
    private void refreshAccounts() {
        List<AccountEntity> accounts = accountRepository.getAllAccounts();
        
        if (accounts.isEmpty()) {
            accountRecyclerView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            accountRecyclerView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);
            accountAdapter.setAccounts(accounts);
        }
    }
    
    @Override
    public void onBackPressed() {
        // Exit app when back pressed from main screen
        moveTaskToBack(true);
    }
}

