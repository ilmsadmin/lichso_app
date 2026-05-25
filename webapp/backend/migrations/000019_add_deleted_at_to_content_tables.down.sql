-- Remove deleted_at column from content tables
DROP INDEX IF EXISTS idx_folk_festivals_deleted_at;
ALTER TABLE folk_festivals DROP COLUMN IF EXISTS deleted_at;

DROP INDEX IF EXISTS idx_events_deleted_at;
ALTER TABLE events DROP COLUMN IF EXISTS deleted_at;

DROP INDEX IF EXISTS idx_famous_people_deleted_at;
ALTER TABLE famous_people DROP COLUMN IF EXISTS deleted_at;

DROP INDEX IF EXISTS idx_quotes_deleted_at;
ALTER TABLE quotes DROP COLUMN IF EXISTS deleted_at;

DROP INDEX IF EXISTS idx_articles_deleted_at;
ALTER TABLE articles DROP COLUMN IF EXISTS deleted_at;

DROP INDEX IF EXISTS idx_article_tags_deleted_at;
ALTER TABLE article_tags DROP COLUMN IF EXISTS deleted_at;

DROP INDEX IF EXISTS idx_article_categories_deleted_at;
ALTER TABLE article_categories DROP COLUMN IF EXISTS deleted_at;
