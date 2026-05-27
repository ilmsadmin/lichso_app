CREATE TABLE IF NOT EXISTS banners (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    subtitle TEXT,
    image_url VARCHAR(500),
    cta_text VARCHAR(100),
    cta_route VARCHAR(255),
    bg_color VARCHAR(20),
    type VARCHAR(50) DEFAULT 'feature',
    is_active BOOLEAN DEFAULT true,
    sort_order INT DEFAULT 0,
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_banners_active ON banners (is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_banners_sort ON banners (sort_order) WHERE deleted_at IS NULL;
CREATE INDEX idx_banners_dates ON banners (start_date, end_date) WHERE deleted_at IS NULL AND is_active = true;
