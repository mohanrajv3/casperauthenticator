package com.casper.authenticator.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.casper.authenticator.R;
import com.casper.authenticator.crypto.TOTPGenerator;
import com.casper.authenticator.database.AccountEntity;
import com.casper.authenticator.repository.AccountRepository;

import java.util.List;

/**
 * RecyclerView Adapter for displaying accounts with OTP codes.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    
    private List<AccountEntity> accounts;
    private AccountRepository accountRepository;
    private Context context;
    
    public AccountAdapter(Context context, AccountRepository accountRepository) {
        this.context = context;
        this.accountRepository = accountRepository;
    }
    
    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountEntity account = accounts.get(position);
        holder.bind(account);
    }
    
    @Override
    public int getItemCount() {
        return accounts != null ? accounts.size() : 0;
    }
    
    class AccountViewHolder extends RecyclerView.ViewHolder {
        private TextView labelTextView;
        private TextView issuerTextView;
        private TextView otpTextView;
        private TextView timerTextView;
        private View itemView;
        
        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            labelTextView = itemView.findViewById(R.id.labelTextView);
            issuerTextView = itemView.findViewById(R.id.issuerTextView);
            otpTextView = itemView.findViewById(R.id.otpTextView);
            timerTextView = itemView.findViewById(R.id.timerTextView);
        }
        
        public void bind(AccountEntity account) {
            // Set account info
            labelTextView.setText(account.label != null ? account.label : "Unknown");
            issuerTextView.setText(account.issuer != null ? account.issuer : "");
            
            // Generate and display OTP
            updateOTP(account);
            
            // Update countdown timer
            updateTimer(account);
            
            // Copy OTP on click
            itemView.setOnClickListener(v -> copyOTP(account));
        }
        
        private void updateOTP(AccountEntity account) {
            try {
                // Decrypt secret
                byte[] secret = accountRepository.decryptSecret(account);
                
                // Generate OTP code
                String algorithm = account.algorithm != null ? 
                    ("Hmac" + account.algorithm.toUpperCase()) : "HmacSHA1";
                if (!algorithm.contains("Hmac")) {
                    algorithm = "HmacSHA1";
                }
                
                String otp;
                if ("HOTP".equals(account.type)) {
                    otp = TOTPGenerator.generateHOTP(
                        secret, account.counter, algorithm, account.digits);
                } else {
                    otp = TOTPGenerator.generateCurrentTOTP(
                        secret, account.period, algorithm, account.digits);
                }
                
                // Format OTP for display (e.g., "123 456")
                String formattedOTP = formatOTP(otp, account.digits);
                otpTextView.setText(formattedOTP);
                
            } catch (Exception e) {
                otpTextView.setText("Error");
                timerTextView.setText("");
            }
        }
        
        private String formatOTP(String otp, int digits) {
            if (digits == 6) {
                return otp.substring(0, 3) + " " + otp.substring(3);
            } else if (digits == 8) {
                return otp.substring(0, 4) + " " + otp.substring(4);
            }
            return otp;
        }
        
        private void updateTimer(AccountEntity account) {
            if ("HOTP".equals(account.type)) {
                timerTextView.setText("");
                return;
            }
            
            int remaining = TOTPGenerator.getRemainingSeconds(account.period);
            timerTextView.setText(remaining + "s");
        }
        
        private void copyOTP(AccountEntity account) {
            try {
                byte[] secret = accountRepository.decryptSecret(account);
                String algorithm = account.algorithm != null ? 
                    ("Hmac" + account.algorithm.toUpperCase()) : "HmacSHA1";
                if (!algorithm.contains("Hmac")) {
                    algorithm = "HmacSHA1";
                }
                
                String otp;
                if ("HOTP".equals(account.type)) {
                    otp = TOTPGenerator.generateHOTP(
                        secret, account.counter, algorithm, account.digits);
                } else {
                    otp = TOTPGenerator.generateCurrentTOTP(
                        secret, account.period, algorithm, account.digits);
                }
                
                // Copy to clipboard with auto-clear (30 seconds)
                com.casper.authenticator.security.SecurityManager securityManager = 
                    new com.casper.authenticator.security.SecurityManager(context);
                securityManager.copyToClipboardWithAutoClear(otp, 30000); // Clear after 30 seconds
                
                Toast.makeText(context, "OTP code copied to clipboard (auto-clears in 30s)", 
                    Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Toast.makeText(context, "Failed to copy OTP", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

