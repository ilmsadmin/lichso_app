CREATE TABLE IF NOT EXISTS popups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    cta_type VARCHAR(20) DEFAULT 'route',
    cta_route VARCHAR(255),
    position VARCHAR(50) DEFAULT 'center',
    is_active BOOLEAN DEFAULT true,
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_popups_active ON popups (is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_popups_dates ON popups (start_date, end_date) WHERE deleted_at IS NULL AND is_active = true;
