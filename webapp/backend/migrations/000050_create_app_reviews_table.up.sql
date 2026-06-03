CREATE TABLE IF NOT EXISTS app_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    platform VARCHAR(20) NOT NULL,
    app_version VARCHAR(50) NOT NULL DEFAULT '',
    device_id VARCHAR(255) NOT NULL DEFAULT '',
    device_name VARCHAR(255) NOT NULL DEFAULT '',
    os_version VARCHAR(100) NOT NULL DEFAULT '',
    stars SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    review_text TEXT NOT NULL DEFAULT '',
    review_flow VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'new',
    admin_note TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_app_reviews_created_at ON app_reviews (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_app_reviews_user_id ON app_reviews (user_id);
CREATE INDEX IF NOT EXISTS idx_app_reviews_platform ON app_reviews (platform);
CREATE INDEX IF NOT EXISTS idx_app_reviews_status ON app_reviews (status);
CREATE INDEX IF NOT EXISTS idx_app_reviews_stars ON app_reviews (stars);
CREATE INDEX IF NOT EXISTS idx_app_reviews_deleted_at ON app_reviews (deleted_at);
