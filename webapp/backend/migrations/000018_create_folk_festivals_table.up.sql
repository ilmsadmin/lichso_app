-- Folk Festivals: traditional folk festivals (supports lunar and solar calendar)
CREATE TABLE IF NOT EXISTS folk_festivals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    alternate_name VARCHAR(500),
    calendar_type VARCHAR(10) NOT NULL DEFAULT 'lunar',
    lunar_day INT,
    lunar_month INT,
    solar_day INT,
    solar_month INT,
    duration_days INT DEFAULT 1,
    festival_type VARCHAR(50) NOT NULL DEFAULT 'folk_festival',
    region VARCHAR(255),
    country VARCHAR(100) DEFAULT 'Việt Nam',
    short_description TEXT,
    traditions TEXT[] DEFAULT '{}',
    image_url VARCHAR(500),
    gallery_urls TEXT[] DEFAULT '{}',
    article_id UUID REFERENCES articles(id) ON DELETE SET NULL,
    importance VARCHAR(20) NOT NULL DEFAULT 'medium',
    tags TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_festivals_lunar ON folk_festivals(lunar_month, lunar_day) WHERE calendar_type IN ('lunar', 'both') AND is_active = TRUE;
CREATE INDEX idx_festivals_solar ON folk_festivals(solar_month, solar_day) WHERE calendar_type IN ('solar', 'both') AND is_active = TRUE;
CREATE INDEX idx_festivals_type ON folk_festivals(festival_type);
CREATE INDEX idx_festivals_slug ON folk_festivals(slug);
CREATE INDEX idx_festivals_tags ON folk_festivals USING GIN(tags);
CREATE INDEX idx_festivals_article ON folk_festivals(article_id);
