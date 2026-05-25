-- ============================================
-- V3: Daily Content Schedule
-- Allows admin to assign content (quotes, events, articles, etc.) to specific dates
-- ============================================

-- Content types that can be scheduled
CREATE TYPE daily_content_type AS ENUM ('quote', 'event', 'article', 'famous_person', 'folk_festival', 'custom');

-- Schedule modes
CREATE TYPE schedule_mode AS ENUM ('fixed_date', 'recurring_annual', 'day_of_year', 'lunar_date');

-- Daily content schedule table
CREATE TABLE IF NOT EXISTS daily_content_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Content reference
    content_type daily_content_type NOT NULL,
    content_id UUID,                              -- FK to the content table (article, quote, etc.)
    custom_title VARCHAR(500),                    -- For custom content type
    custom_content TEXT,                           -- For custom content type
    custom_image VARCHAR(500),                    -- For custom content type
    
    -- Scheduling
    schedule_mode schedule_mode NOT NULL DEFAULT 'fixed_date',
    fixed_date DATE,                              -- For fixed_date mode
    day_of_year INT,                              -- For day_of_year mode (1-366)
    recurring_month INT,                          -- For recurring_annual mode (1-12)
    recurring_day INT,                            -- For recurring_annual mode (1-31)
    lunar_month INT,                              -- For lunar_date mode
    lunar_day INT,                                -- For lunar_date mode
    year_filter INT,                              -- Optional: only show in specific year
    
    -- Display settings
    display_priority INT NOT NULL DEFAULT 0,      -- Higher = shown first
    display_section VARCHAR(100) NOT NULL DEFAULT 'main',  -- main, sidebar, banner, etc.
    
    -- Metadata
    is_active BOOLEAN NOT NULL DEFAULT true,
    start_date DATE,                              -- Optional: start showing from this date
    end_date DATE,                                -- Optional: stop showing after this date
    
    -- Admin tracking
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,

    -- Constraints
    CONSTRAINT chk_day_of_year CHECK (day_of_year IS NULL OR (day_of_year >= 1 AND day_of_year <= 366)),
    CONSTRAINT chk_recurring_month CHECK (recurring_month IS NULL OR (recurring_month >= 1 AND recurring_month <= 12)),
    CONSTRAINT chk_recurring_day CHECK (recurring_day IS NULL OR (recurring_day >= 1 AND recurring_day <= 31)),
    CONSTRAINT chk_lunar_month CHECK (lunar_month IS NULL OR (lunar_month >= 1 AND lunar_month <= 12)),
    CONSTRAINT chk_lunar_day CHECK (lunar_day IS NULL OR (lunar_day >= 1 AND lunar_day <= 30)),
    CONSTRAINT chk_content_or_custom CHECK (
        content_id IS NOT NULL OR (custom_title IS NOT NULL AND custom_content IS NOT NULL)
    )
);

-- Indexes for efficient date-based lookups
CREATE INDEX idx_dcs_content_type ON daily_content_schedules(content_type);
CREATE INDEX idx_dcs_schedule_mode ON daily_content_schedules(schedule_mode);
CREATE INDEX idx_dcs_fixed_date ON daily_content_schedules(fixed_date) WHERE fixed_date IS NOT NULL;
CREATE INDEX idx_dcs_day_of_year ON daily_content_schedules(day_of_year) WHERE day_of_year IS NOT NULL;
CREATE INDEX idx_dcs_recurring ON daily_content_schedules(recurring_month, recurring_day) WHERE recurring_month IS NOT NULL;
CREATE INDEX idx_dcs_lunar ON daily_content_schedules(lunar_month, lunar_day) WHERE lunar_month IS NOT NULL;
CREATE INDEX idx_dcs_active ON daily_content_schedules(is_active) WHERE is_active = true;
CREATE INDEX idx_dcs_section_priority ON daily_content_schedules(display_section, display_priority DESC);
CREATE INDEX idx_dcs_deleted_at ON daily_content_schedules(deleted_at);

COMMENT ON TABLE daily_content_schedules IS 'Admin-managed schedule for daily content display on calendar views';
