-- Reminders: users can set reminders for holidays, death anniversaries, etc.
CREATE TABLE IF NOT EXISTS reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT DEFAULT '',
    reminder_type VARCHAR(30) NOT NULL DEFAULT 'custom',
    -- For lunar dates: is_lunar=true, lunar_day/lunar_month are used
    is_lunar BOOLEAN DEFAULT FALSE,
    solar_day INT,
    solar_month INT,
    lunar_day INT,
    lunar_month INT,
    -- Recurring yearly
    is_recurring BOOLEAN DEFAULT TRUE,
    -- Remind before N days
    remind_before_days INT DEFAULT 1,
    -- Notification preferences
    notify_email BOOLEAN DEFAULT FALSE,
    notify_push BOOLEAN DEFAULT TRUE,
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    last_notified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_reminders_user_id ON reminders(user_id);
CREATE INDEX idx_reminders_type ON reminders(reminder_type);
CREATE INDEX idx_reminders_active ON reminders(is_active);
CREATE INDEX idx_reminders_deleted_at ON reminders(deleted_at);
CREATE INDEX idx_reminders_lunar ON reminders(is_lunar, lunar_month, lunar_day);
CREATE INDEX idx_reminders_solar ON reminders(is_lunar, solar_month, solar_day);
