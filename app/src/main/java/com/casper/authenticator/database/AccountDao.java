package com.casper.authenticator.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for Account entities.
 * 
 * Provides database operations for account management.
 */
@Dao
public interface AccountDao {
    
    /**
     * Get all accounts, sorted by label.
     */
    @Query("SELECT * FROM accounts ORDER BY label ASC, issuer ASC")
    List<AccountEntity> getAllAccounts();
    
    /**
     * Get account by ID.
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    AccountEntity getAccountById(long id);
    
    /**
     * Search accounts by label or issuer.
     */
    @Query("SELECT * FROM accounts WHERE label LIKE :query OR issuer LIKE :query ORDER BY label ASC")
    List<AccountEntity> searchAccounts(String query);
    
    /**
     * Check if account exists (by label and issuer).
     */
    @Query("SELECT COUNT(*) FROM accounts WHERE label = :label AND issuer = :issuer")
    int accountExists(String label, String issuer);
    
    /**
     * Insert a new account.
     */
    @Insert
    long insert(AccountEntity account);
    
    /**
     * Update an existing account.
     */
    @Update
    void update(AccountEntity account);
    
    /**
     * Delete an account.
     */
    @Delete
    void delete(AccountEntity account);
    
    /**
     * Delete account by ID.
     */
    @Query("DELETE FROM accounts WHERE id = :id")
    void deleteById(long id);
    
    /**
     * Get total account count.
     */
    @Query("SELECT COUNT(*) FROM accounts")
    int getAccountCount();
}

