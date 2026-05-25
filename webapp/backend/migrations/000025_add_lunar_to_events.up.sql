-- Add is_lunar flag and lunar date columns to events table
-- is_lunar = true  → event_day/event_month refer to Lunar calendar
-- is_lunar = false → event_day/event_month refer to Solar calendar (default, existing behaviour)

ALTER TABLE events
    ADD COLUMN IF NOT EXISTS is_lunar BOOLEAN NOT NULL DEFAULT FALSE;

-- Rename existing index to clarify it covers both solar and lunar lookups
-- (index already covers event_month, event_day which we keep using for both calendars)
COMMENT ON COLUMN events.is_lunar IS 'TRUE = event_day/event_month are Lunar (Âm lịch); FALSE = Solar (Dương lịch)';
