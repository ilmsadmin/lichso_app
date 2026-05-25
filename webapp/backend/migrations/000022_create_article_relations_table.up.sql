-- ============================================
-- V3: Article Relations (Related Articles)
-- ============================================

-- Relation types for article relationships
CREATE TYPE article_relation_type AS ENUM ('related', 'series', 'reference', 'translation');

-- Article relations table: tracks relationships between articles
CREATE TABLE IF NOT EXISTS article_relations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    target_article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    relation_type article_relation_type NOT NULL DEFAULT 'related',
    sort_order INT NOT NULL DEFAULT 0,
    is_bidirectional BOOLEAN NOT NULL DEFAULT true,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Prevent duplicate relations
    CONSTRAINT uq_article_relation UNIQUE (source_article_id, target_article_id, relation_type),
    -- Prevent self-referencing
    CONSTRAINT chk_no_self_relation CHECK (source_article_id != target_article_id)
);

-- Indexes for fast lookups
CREATE INDEX idx_article_relations_source ON article_relations(source_article_id);
CREATE INDEX idx_article_relations_target ON article_relations(target_article_id);
CREATE INDEX idx_article_relations_type ON article_relations(relation_type);
CREATE INDEX idx_article_relations_source_type ON article_relations(source_article_id, relation_type);

COMMENT ON TABLE article_relations IS 'Stores relationships between articles (related, series, references)';
COMMENT ON COLUMN article_relations.is_bidirectional IS 'If true, relation works both ways (A related to B = B related to A)';
