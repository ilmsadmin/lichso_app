-- Quotes: famous quotes displayed daily
CREATE TABLE IF NOT EXISTS quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote TEXT NOT NULL,
    original_quote TEXT,
    original_language VARCHAR(10) DEFAULT 'vi',
    author VARCHAR(255) NOT NULL,
    author_bio TEXT,
    author_birth_year INT,
    author_death_year INT,
    author_nationality VARCHAR(100),
    author_image_url VARCHAR(500),
    tags TEXT[] DEFAULT '{}',
    day_of_year INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quotes_day_of_year ON quotes(day_of_year) WHERE is_active = TRUE;
CREATE INDEX idx_quotes_author ON quotes(author);
CREATE INDEX idx_quotes_tags ON quotes USING GIN(tags);
CREATE INDEX idx_quotes_active ON quotes(is_active) WHERE is_active = TRUE;
