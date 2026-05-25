-- Article-Tag many-to-many relation
CREATE TABLE IF NOT EXISTS article_tag_relations (
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES article_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE INDEX idx_article_tag_article ON article_tag_relations(article_id);
CREATE INDEX idx_article_tag_tag ON article_tag_relations(tag_id);
