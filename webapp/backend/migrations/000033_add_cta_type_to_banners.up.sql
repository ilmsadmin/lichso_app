ALTER TABLE banners
ADD COLUMN IF NOT EXISTS cta_type VARCHAR(20) DEFAULT 'route';

UPDATE banners
SET cta_type = CASE
    WHEN cta_route ILIKE 'http://%' OR cta_route ILIKE 'https://%' THEN 'url'
    ELSE 'route'
END
WHERE cta_route IS NOT NULL AND cta_route <> '';

