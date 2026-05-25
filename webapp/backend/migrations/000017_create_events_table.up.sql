-- Events: historical events, national days, international days
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    event_date DATE,
    event_day INT NOT NULL,
    event_month INT NOT NULL,
    event_year INT,
    is_recurring BOOLEAN DEFAULT TRUE,
    event_type VARCHAR(50) NOT NULL DEFAULT 'historical_event',
    country VARCHAR(100),
    country_code VARCHAR(5),
    flag_emoji VARCHAR(10),
    short_description TEXT,
    image_url VARCHAR(500),
    article_id UUID REFERENCES articles(id) ON DELETE SET NULL,
    importance VARCHAR(20) NOT NULL DEFAULT 'medium',
    tags TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_events_date ON events(event_month, event_day) WHERE is_active = TRUE;
CREATE INDEX idx_events_type ON events(event_type);
CREATE INDEX idx_events_country ON events(country_code);
CREATE INDEX idx_events_importance ON events(importance);
CREATE INDEX idx_events_slug ON events(slug);
CREATE INDEX idx_events_tags ON events USING GIN(tags);
CREATE INDEX idx_events_article ON events(article_id);
