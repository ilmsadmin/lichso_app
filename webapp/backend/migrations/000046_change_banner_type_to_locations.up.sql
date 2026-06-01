-- Add locations column
ALTER TABLE banners ADD COLUMN locations JSONB DEFAULT '["home"]'::jsonb;

-- Migrate existing data
UPDATE banners SET locations = '["quiz_home"]'::jsonb WHERE type = 'quiz';
UPDATE banners SET locations = '["home"]'::jsonb WHERE type != 'quiz';

-- Drop old column
ALTER TABLE banners DROP COLUMN type;
