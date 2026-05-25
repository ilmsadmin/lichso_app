-- ============================================
-- Migration: Add social login fields to users table
-- ============================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(20) NOT NULL DEFAULT 'local';
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255) DEFAULT '';

-- Allow NULL password for social login users
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- Index for social login lookups
CREATE INDEX IF NOT EXISTS idx_users_provider_provider_id ON users(provider, provider_id);
