-- Add deleted_at column for soft delete support (GORM gorm.DeletedAt)
ALTER TABLE article_categories ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_article_categories_deleted_at ON article_categories(deleted_at);

ALTER TABLE article_tags ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_article_tags_deleted_at ON article_tags(deleted_at);

ALTER TABLE articles ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_articles_deleted_at ON articles(deleted_at);

ALTER TABLE quotes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_quotes_deleted_at ON quotes(deleted_at);

ALTER TABLE famous_people ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_famous_people_deleted_at ON famous_people(deleted_at);

ALTER TABLE events ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_events_deleted_at ON events(deleted_at);

ALTER TABLE folk_festivals ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_folk_festivals_deleted_at ON folk_festivals(deleted_at);
