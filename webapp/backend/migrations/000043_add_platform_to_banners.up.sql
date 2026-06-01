ALTER TABLE banners ADD COLUMN IF NOT EXISTS platform VARCHAR(20) NOT NULL DEFAULT 'all';

-- Filter active banners by platform (all/android/ios)
CREATE INDEX IF NOT EXISTS idx_banners_platform ON banners (platform) WHERE deleted_at IS NULL AND is_active = true;
