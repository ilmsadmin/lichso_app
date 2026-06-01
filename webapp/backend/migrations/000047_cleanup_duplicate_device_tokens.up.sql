-- Cleanup historical duplicate device tokens, keeping only the latest one per device_id
UPDATE device_tokens
SET deleted_at = NOW(), is_active = FALSE
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY device_id
                   ORDER BY last_seen DESC, updated_at DESC, created_at DESC, id DESC
               ) as rn
        FROM device_tokens
        WHERE device_id IS NOT NULL AND device_id != '' AND deleted_at IS NULL
    ) t
    WHERE t.rn > 1
);
