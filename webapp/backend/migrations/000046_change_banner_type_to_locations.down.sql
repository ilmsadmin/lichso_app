-- Add type column back
ALTER TABLE banners ADD COLUMN type VARCHAR(50) DEFAULT 'feature';

-- Revert data (approximate mapping)
UPDATE banners SET type = 'quiz' WHERE locations @> '["quiz_home"]'::jsonb;
UPDATE banners SET type = 'feature' WHERE NOT (locations @> '["quiz_home"]'::jsonb);

-- Drop locations column
ALTER TABLE banners DROP COLUMN locations;
