-- ============================================
-- V3: User Notes & Countdowns (Personal Calendar Features)
-- ============================================

-- User personal notes on specific dates
CREATE TABLE IF NOT EXISTS user_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note_date DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    color VARCHAR(20) DEFAULT '#3b82f6',    -- Display color (hex)
    is_pinned BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_user_notes_user ON user_notes(user_id);
CREATE INDEX idx_user_notes_date ON user_notes(user_id, note_date);
CREATE INDEX idx_user_notes_deleted ON user_notes(deleted_at);

-- User countdown events
CREATE TABLE IF NOT EXISTS user_countdowns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_date DATE NOT NULL,
    target_time TIME,                         -- Optional specific time
    color VARCHAR(20) DEFAULT '#ef4444',
    icon VARCHAR(50) DEFAULT '🎯',
    is_recurring BOOLEAN NOT NULL DEFAULT false,
    recurring_type VARCHAR(20),               -- yearly, monthly
    notify_before_days INT DEFAULT 1,         -- Days before to send notification
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_user_countdowns_user ON user_countdowns(user_id);
CREATE INDEX idx_user_countdowns_target ON user_countdowns(user_id, target_date);
CREATE INDEX idx_user_countdowns_active ON user_countdowns(is_active) WHERE is_active = true;
CREATE INDEX idx_user_countdowns_deleted ON user_countdowns(deleted_at);

COMMENT ON TABLE user_notes IS 'Personal notes attached to specific calendar dates';
COMMENT ON TABLE user_countdowns IS 'Personal countdown timers to important dates';
