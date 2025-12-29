package com.casper.authenticator.security;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Time Synchronization Manager.
 * 
 * Handles:
 * - Clock drift detection
 * - Time sync verification
 * - Grace window for OTP validation
 * - Offline time sync checks
 */
public class TimeSyncManager {
    private static final String TAG = "TimeSyncManager";
    private static final long MAX_CLOCK_DRIFT_MS = 30 * 1000; // 30 seconds
    private static final long GRACE_WINDOW_MS = 60 * 1000; // 60 seconds grace window
    
    private Context context;
    
    public TimeSyncManager(Context context) {
        this.context = context;
    }
    
    /**
     * Check if device time is synchronized (within acceptable drift).
     */
    public boolean isTimeSynchronized() {
        try {
            long deviceTime = System.currentTimeMillis();
            long serverTime = getServerTime();
            
            if (serverTime == 0) {
                // Can't reach server, assume time is OK for offline mode
                Log.w(TAG, "Cannot verify time sync (offline)");
                return true;
            }
            
            long drift = Math.abs(deviceTime - serverTime);
            boolean synced = drift < MAX_CLOCK_DRIFT_MS;
            
            if (!synced) {
                Log.w(TAG, "Clock drift detected: " + drift + "ms");
            }
            
            return synced;
        } catch (Exception e) {
            Log.e(TAG, "Error checking time sync", e);
            return true; // Assume OK if check fails
        }
    }
    
    /**
     * Get server time from NTP or HTTP time server.
     */
    private long getServerTime() {
        if (!isNetworkAvailable()) {
            return 0; // Offline
        }
        
        // Try multiple time servers
        String[] timeServers = {
            "http://worldtimeapi.org/api/timezone/UTC",
            "http://time.google.com",
            "http://time.cloudflare.com"
        };
        
        for (String server : timeServers) {
            try {
                long time = fetchTimeFromServer(server);
                if (time > 0) {
                    return time;
                }
            } catch (Exception e) {
                Log.d(TAG, "Failed to fetch time from " + server, e);
            }
        }
        
        return 0; // All servers failed
    }
    
    /**
     * Fetch time from HTTP server.
     */
    private long fetchTimeFromServer(String serverUrl) throws IOException {
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        long serverTime = connection.getDate();
        connection.disconnect();
        
        return serverTime;
    }
    
    /**
     * Check if network is available.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
    
    /**
     * Get clock drift in milliseconds.
     */
    public long getClockDrift() {
        try {
            long deviceTime = System.currentTimeMillis();
            long serverTime = getServerTime();
            
            if (serverTime == 0) {
                return 0; // Can't determine drift offline
            }
            
            return deviceTime - serverTime;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating clock drift", e);
            return 0;
        }
    }
    
    /**
     * Check if OTP should be accepted within grace window.
     */
    public boolean isWithinGraceWindow(long otpTime, int period) {
        long currentTime = System.currentTimeMillis() / 1000;
        long otpTimeStep = otpTime / period;
        long currentTimeStep = currentTime / period;
        
        // Allow previous, current, and next time step (grace window)
        long timeStepDiff = Math.abs(otpTimeStep - currentTimeStep);
        return timeStepDiff <= 1; // Within 1 time step (grace window)
    }
}

