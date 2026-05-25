CREATE TABLE IF NOT EXISTS user_streaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_visit_date DATE,
    total_visits INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id)
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_key VARCHAR(50) NOT NULL,
    achievement_name VARCHAR(200) NOT NULL,
    description TEXT,
    badge VARCHAR(10),
    progress INT NOT NULL DEFAULT 0,
    target INT NOT NULL DEFAULT 1,
    unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, achievement_key)
);

CREATE INDEX idx_user_streaks_user_id ON user_streaks(user_id);
CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_unlocked ON user_achievements(unlocked) WHERE unlocked = TRUE;
