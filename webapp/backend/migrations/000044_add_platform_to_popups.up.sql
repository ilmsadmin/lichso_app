ALTER TABLE popups ADD COLUMN IF NOT EXISTS platform VARCHAR(20) NOT NULL DEFAULT 'all';

-- Filter active popups by platform (all/android/ios)
CREATE INDEX IF NOT EXISTS idx_popups_platform ON popups (platform) WHERE deleted_at IS NULL AND is_active = true;
