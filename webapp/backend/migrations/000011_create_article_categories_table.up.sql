-- Article Categories: hierarchical categories for articles
CREATE TABLE IF NOT EXISTS article_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id UUID REFERENCES article_categories(id) ON DELETE SET NULL,
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_article_categories_slug ON article_categories(slug);
CREATE INDEX idx_article_categories_parent ON article_categories(parent_id);
CREATE INDEX idx_article_categories_active ON article_categories(is_active) WHERE is_active = TRUE;
