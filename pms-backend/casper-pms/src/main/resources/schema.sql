-- CASPER PMS Database Schema
-- PostgreSQL schema for Passkey Management Service

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS encrypted_passkeys CASCADE;

-- Encrypted Passkeys Table
-- Stores encrypted passkey data from Android app
CREATE TABLE encrypted_passkeys (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    rp_id VARCHAR(255) NOT NULL,
    encrypted_private_key TEXT NOT NULL,  -- Base64-encoded encrypted private key (s̃)
    public_key TEXT NOT NULL,              -- Base64-encoded public key
    detection_secrets TEXT NOT NULL,       -- JSON array of Base64-encoded detection secrets (W)
    z_value TEXT NOT NULL,                 -- Base64-encoded random value z
    real_secret_index INTEGER NOT NULL,    -- Index of real secret in detection secrets array
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, rp_id)
);

-- Index for faster lookups
CREATE INDEX idx_encrypted_passkeys_user_rp ON encrypted_passkeys(user_id, rp_id);
CREATE INDEX idx_encrypted_passkeys_user ON encrypted_passkeys(user_id);

-- Comments (PostgreSQL syntax - not supported by H2, kept for documentation)
-- COMMENT ON TABLE encrypted_passkeys IS 'Stores encrypted passkey data. PMS never decrypts this data.';
-- COMMENT ON COLUMN encrypted_passkeys.encrypted_private_key IS 'Encrypted private key: s̃ = HKDF(w*, z) XOR s';
-- COMMENT ON COLUMN encrypted_passkeys.detection_secrets IS 'Array of k detection secrets (W), one real (w*), others decoys';
-- COMMENT ON COLUMN encrypted_passkeys.real_secret_index IS 'Index of real secret selected by PIN: w* = W[H(PIN) mod k]';

