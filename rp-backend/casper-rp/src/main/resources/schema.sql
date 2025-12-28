-- CASPER RP Database Schema
-- PostgreSQL schema for Relying Party Service

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS login_attempts CASCADE;
DROP TABLE IF EXISTS passkeys CASCADE;

-- Passkeys Table
-- Stores registered passkey public keys (real + decoys/trap keys)
CREATE TABLE passkeys (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    rp_id VARCHAR(255) NOT NULL,
    public_key TEXT NOT NULL,         -- Base64-encoded public key
    is_real BOOLEAN NOT NULL,         -- true = real passkey (V), false = decoy/trap key (V')
    key_index INTEGER NOT NULL,       -- Index in registration order (0 = real, 1+ = decoys)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Login Attempts Table
-- Records all login attempts with breach detection results
CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    rp_id VARCHAR(255) NOT NULL,
    public_key TEXT NOT NULL,         -- Base64-encoded public key used for login
    breach_detected BOOLEAN NOT NULL, -- CASPER breach detection result
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for faster lookups
CREATE INDEX idx_passkeys_user_rp ON passkeys(user_id, rp_id);
CREATE INDEX idx_passkeys_public_key ON passkeys(public_key);
CREATE INDEX idx_login_attempts_user_rp ON login_attempts(user_id, rp_id);
CREATE INDEX idx_login_attempts_breach ON login_attempts(breach_detected);
CREATE INDEX idx_login_attempts_timestamp ON login_attempts(timestamp);

-- Comments
COMMENT ON TABLE passkeys IS 'Stores registered passkey public keys. First key (is_real=true) is real (V), others are trap keys (V\')';
COMMENT ON COLUMN passkeys.is_real IS 'true = real passkey (V), false = decoy/trap key (V\') for CASPER breach detection';
COMMENT ON TABLE login_attempts IS 'Records login attempts with CASPER breach detection results';
COMMENT ON COLUMN login_attempts.breach_detected IS 'CASPER CD algorithm: true if login key ∈ V\' (trap keys), false if ∈ V (real key)';

