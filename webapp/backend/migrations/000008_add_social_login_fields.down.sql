-- ============================================
-- Rollback: Remove social login fields from users table
-- ============================================

DROP INDEX IF EXISTS idx_users_provider_provider_id;

ALTER TABLE users ALTER COLUMN password SET NOT NULL;

ALTER TABLE users DROP COLUMN IF EXISTS provider_id;
ALTER TABLE users DROP COLUMN IF EXISTS provider;
