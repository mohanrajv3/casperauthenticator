package com.casper.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.casper.authenticator.repository.AccountRepository;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.ScanIntentIntegrator;

import org.apache.commons.codec.binary.Base32;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * QR Code Scanner Activity for adding accounts.
 * 
 * Supports:
 * - otpauth:// TOTP URLs
 * - otpauth:// HOTP URLs
 * - Manual secret entry fallback
 */
public class QRCodeScannerActivity extends AppCompatActivity {
    
    private AccountRepository accountRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        accountRepository = new AccountRepository(this);
        
        // Start QR code scanner
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanIntentIntegrator.QR_CODE);
        options.setPrompt("Scan QR code");
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(false);
        
        ScanIntentIntegrator integrator = new ScanIntentIntegrator(this);
        integrator.initiateScan(options);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        com.journeyapps.barcodescanner.ScanIntentResult result = 
            com.journeyapps.barcodescanner.ScanIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        
        if (result != null && result.getContents() != null) {
            String qrContent = result.getContents();
            handleQRCode(qrContent);
        } else {
            Toast.makeText(this, "QR code scan cancelled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Handle scanned QR code content.
     */
    private void handleQRCode(String qrContent) {
        try {
            if (qrContent.startsWith("otpauth://")) {
                parseOTPAuthURL(qrContent);
            } else {
                // Assume it's a raw secret
                addAccountFromSecret(qrContent, "Manual Entry", "");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse QR code: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    /**
     * Parse otpauth:// URL.
     * Format: otpauth://totp/Issuer:Label?secret=SECRET&issuer=Issuer&algorithm=SHA1&digits=6&period=30
     */
    private void parseOTPAuthURL(String url) throws Exception {
        // Remove otpauth:// prefix
        String content = url.substring(10);
        
        // Split type and path
        int slashIndex = content.indexOf('/');
        if (slashIndex == -1) {
            throw new IllegalArgumentException("Invalid otpauth URL format");
        }
        
        String type = content.substring(0, slashIndex).toUpperCase();
        String path = content.substring(slashIndex + 1);
        
        // Parse path and parameters
        int questionIndex = path.indexOf('?');
        String labelPart = questionIndex != -1 ? path.substring(0, questionIndex) : path;
        String paramsPart = questionIndex != -1 ? path.substring(questionIndex + 1) : "";
        
        // Decode label (may contain : separator for issuer:label)
        labelPart = URLDecoder.decode(labelPart, "UTF-8");
        String[] labelParts = labelPart.split(":");
        String issuer = labelParts.length > 1 ? labelParts[0] : "";
        String label = labelParts.length > 1 ? labelParts[1] : labelParts[0];
        
        // Parse parameters
        Map<String, String> params = parseParams(paramsPart);
        
        // Extract secret (required)
        String secret = params.get("secret");
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Secret is required");
        }
        
        // Extract other parameters with defaults
        String algorithm = params.getOrDefault("algorithm", "SHA1").toUpperCase();
        int digits = Integer.parseInt(params.getOrDefault("digits", "6"));
        int period = Integer.parseInt(params.getOrDefault("period", "30"));
        
        // Use issuer from params if available
        if (params.containsKey("issuer") && !params.get("issuer").isEmpty()) {
            issuer = params.get("issuer");
        }
        
        // Add account
        byte[] secretBytes = decodeBase32(secret);
        accountRepository.addAccount(
            label, issuer, secretBytes, algorithm, digits, period, type);
        
        Toast.makeText(this, "Account added: " + label, Toast.LENGTH_SHORT).show();
        finish();
    }
    
    /**
     * Parse URL parameters.
     */
    private Map<String, String> parseParams(String paramsString) {
        Map<String, String> params = new HashMap<>();
        if (paramsString == null || paramsString.isEmpty()) {
            return params;
        }
        
        String[] pairs = paramsString.split("&");
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex != -1) {
                String key = URLDecoder.decode(pair.substring(0, equalsIndex), java.nio.charset.StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(equalsIndex + 1), java.nio.charset.StandardCharsets.UTF_8);
                params.put(key.toLowerCase(), value);
            }
        }
        
        return params;
    }
    
    /**
     * Add account from raw secret.
     */
    private void addAccountFromSecret(String secret, String label, String issuer) {
        try {
            byte[] secretBytes = decodeBase32(secret);
            accountRepository.addAccount(label, issuer, secretBytes, "SHA1", 6, 30, "TOTP");
            Toast.makeText(this, "Account added", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to add account: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Decode Base32 string.
     */
    private byte[] decodeBase32(String secret) {
        Base32 base32 = new Base32();
        return base32.decode(secret.toUpperCase().replaceAll("\\s+", ""));
    }
}

