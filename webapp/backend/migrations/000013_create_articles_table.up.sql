-- Articles: blog posts, detailed content about people, events, festivals, horoscope, etc.
CREATE TYPE article_status AS ENUM ('draft', 'review', 'published', 'archived');

CREATE TABLE IF NOT EXISTS articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    excerpt TEXT,
    content TEXT NOT NULL,
    featured_image VARCHAR(500),
    category_id UUID REFERENCES article_categories(id) ON DELETE SET NULL,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    status article_status NOT NULL DEFAULT 'draft',
    published_at TIMESTAMPTZ,
    scheduled_at TIMESTAMPTZ,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    og_image VARCHAR(500),
    view_count INT DEFAULT 0,
    reading_time INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_articles_slug ON articles(slug);
CREATE INDEX idx_articles_status ON articles(status) WHERE is_active = TRUE;
CREATE INDEX idx_articles_category ON articles(category_id);
CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_published ON articles(published_at DESC) WHERE status = 'published';
CREATE INDEX idx_articles_featured ON articles(is_featured) WHERE is_featured = TRUE AND status = 'published';
CREATE INDEX idx_articles_view_count ON articles(view_count DESC);
CREATE INDEX idx_articles_search ON articles USING GIN(
    to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(excerpt, '') || ' ' || coalesce(content, ''))
);
