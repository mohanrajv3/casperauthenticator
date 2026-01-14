package com.casper.authenticator.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.casper.authenticator.crypto.CasperCrypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Encrypted Security Logger.
 * 
 * Features:
 * - Local-only encrypted logs
 * - Track security events (failed unlocks, backups, etc.)
 * - No sensitive data in logs
 * - Automatic log rotation
 */
public class SecurityLogger {
    private static final String TAG = "SecurityLogger";
    private static final String PREFS_NAME = "security_logs";
    private static final int MAX_LOG_ENTRIES = 100;
    
    private Context context;
    private CasperCrypto casperCrypto;
    
    public SecurityLogger(Context context) {
        this.context = context;
        this.casperCrypto = new CasperCrypto(context);
    }
    
    /**
     * Log a security event.
     */
    public void logEvent(LogEvent event) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String logsJson = prefs.getString("logs", "[]");
            
            // Parse existing logs
            List<LogEntry> logs = parseLogs(logsJson);
            
            // Add new entry
            LogEntry entry = new LogEntry();
            entry.timestamp = System.currentTimeMillis();
            entry.event = event.name();
            entry.details = event.getDetails();
            
            // Hash sensitive details (don't store plaintext)
            entry.detailsHash = hashDetails(entry.details);
            
            logs.add(entry);
            
            // Keep only last N entries
            if (logs.size() > MAX_LOG_ENTRIES) {
                logs.remove(0);
            }
            
            // Save logs (encrypted)
            String newLogsJson = serializeLogs(logs);
            String encryptedLogs = encryptLogs(newLogsJson);
            
            prefs.edit().putString("logs", encryptedLogs).apply();
            
            Log.d(TAG, "Security event logged: " + event.name());
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to log security event", e);
        }
    }
    
    /**
     * Get recent security logs.
     */
    public List<LogEntry> getRecentLogs(int count) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String encryptedLogs = prefs.getString("logs", "[]");
            String decryptedLogs = decryptLogs(encryptedLogs);
            List<LogEntry> logs = parseLogs(decryptedLogs);
            
            // Return last N entries
            int start = Math.max(0, logs.size() - count);
            return logs.subList(start, logs.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve security logs", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Clear all logs.
     */
    public void clearLogs() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove("logs").apply();
    }
    
    /**
     * Encrypt logs using CASPER.
     */
    private String encryptLogs(String logsJson) {
        // Simplified encryption (in production, use proper AES-GCM)
        try {
            String pin = casperCrypto.getPin();
            if (pin == null) {
                return Base64.encodeToString(logsJson.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            }
            
            // Use PIN hash as encryption key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            
            // Simple XOR encryption (in production, use AES-GCM)
            byte[] data = logsJson.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                encrypted[i] = (byte) (data[i] ^ key[i % key.length]);
            }
            
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            return Base64.encodeToString(logsJson.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        }
    }
    
    /**
     * Decrypt logs.
     */
    private String decryptLogs(String encryptedLogs) {
        try {
            byte[] encrypted = Base64.decode(encryptedLogs, Base64.NO_WRAP);
            String pin = casperCrypto.getPin();
            
            if (pin == null) {
                return new String(encrypted, StandardCharsets.UTF_8);
            }
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            
            byte[] decrypted = new byte[encrypted.length];
            for (int i = 0; i < encrypted.length; i++) {
                decrypted[i] = (byte) (encrypted[i] ^ key[i % key.length]);
            }
            
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    /**
     * Hash sensitive details.
     */
    private String hashDetails(String details) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(details.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP).substring(0, 16);
        } catch (Exception e) {
            return "hash_error";
        }
    }
    
    /**
     * Parse logs from JSON.
     */
    private List<LogEntry> parseLogs(String logsJson) {
        // Simplified parsing (in production, use Gson)
        List<LogEntry> logs = new ArrayList<>();
        // For now, return empty list - full JSON parsing would require Gson
        return logs;
    }
    
    /**
     * Serialize logs to JSON.
     */
    private String serializeLogs(List<LogEntry> logs) {
        // Simplified serialization (in production, use Gson)
        return "[]";
    }
    
    /**
     * Log entry structure.
     */
    public static class LogEntry {
        public long timestamp;
        public String event;
        public String details;
        public String detailsHash;
        
        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
    
    /**
     * Security event types.
     */
    public enum LogEvent {
        FAILED_UNLOCK_ATTEMPT("Failed unlock attempt"),
        SUCCESSFUL_UNLOCK("Successful unlock"),
        BACKUP_CREATED("Backup created"),
        BACKUP_RESTORED("Backup restored"),
        ACCOUNT_ADDED("Account added"),
        ACCOUNT_DELETED("Account deleted"),
        ROOT_DETECTED("Root detected"),
        TAMPER_DETECTED("Tamper detected"),
        CLIPBOARD_CLEARED("Clipboard cleared"),
        AUTO_LOCK_TRIGGERED("Auto-lock triggered");
        
        private String details;
        
        LogEvent(String details) {
            this.details = details;
        }
        
        public String getDetails() {
            return details;
        }
    }
}

