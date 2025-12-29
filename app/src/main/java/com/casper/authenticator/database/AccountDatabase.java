package com.casper.authenticator.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database for Account storage.
 * 
 * Uses Room with encryption support for secure storage.
 * All account secrets are CASPER-encrypted before storage.
 */
@Database(entities = {AccountEntity.class}, version = 1, exportSchema = false)
public abstract class AccountDatabase extends RoomDatabase {
    
    private static AccountDatabase INSTANCE;
    private static final String DATABASE_NAME = "casper_accounts_db";
    
    /**
     * Get AccountDao instance.
     */
    public abstract AccountDao accountDao();
    
    /**
     * Get database instance (singleton).
     * 
     * Note: For production, consider using SQLCipher for encryption at database level.
     * For now, we rely on CASPER encryption of individual secrets.
     */
    public static synchronized AccountDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AccountDatabase.class,
                    DATABASE_NAME
            )
            .allowMainThreadQueries() // For simplicity - consider using background threads in production
            .build();
        }
        return INSTANCE;
    }
    
    /**
     * Close database instance.
     */
    public static void closeInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}

