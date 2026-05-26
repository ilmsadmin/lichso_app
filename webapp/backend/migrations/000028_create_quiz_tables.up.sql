-- Quiz questions
CREATE TABLE IF NOT EXISTS quiz_questions (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT NOT NULL,
    option_a    VARCHAR(500) NOT NULL,
    option_b    VARCHAR(500) NOT NULL,
    option_c    VARCHAR(500) NOT NULL,
    option_d    VARCHAR(500) NOT NULL,
    correct     CHAR(1) NOT NULL CHECK (correct IN ('a','b','c','d')),
    hint        TEXT,
    explanation TEXT,
    category    VARCHAR(100) NOT NULL DEFAULT 'history_vn',
    difficulty  VARCHAR(20) NOT NULL DEFAULT 'medium',
    article_id  BIGINT,  -- optional link to articles table
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_category   ON quiz_questions(category);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_difficulty ON quiz_questions(difficulty);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_active     ON quiz_questions(is_active);

-- Daily quiz sets (admin curates 5 questions per day)
CREATE TABLE IF NOT EXISTS quiz_daily_sets (
    id           BIGSERIAL PRIMARY KEY,
    date         DATE UNIQUE NOT NULL,
    question_ids BIGINT[] NOT NULL,
    created_by   UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_quiz_daily_sets_date ON quiz_daily_sets(date);

-- User quiz sessions
CREATE TABLE IF NOT EXISTS quiz_sessions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_type VARCHAR(20) NOT NULL DEFAULT 'daily',  -- 'daily' | 'topic'
    category     VARCHAR(100),
    question_ids BIGINT[] NOT NULL DEFAULT '{}',
    answers      JSONB NOT NULL DEFAULT '[]',
    score        INT NOT NULL DEFAULT 0,
    total        INT NOT NULL DEFAULT 0,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    started_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at  TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_quiz_sessions_user_id ON quiz_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_sessions_started_at ON quiz_sessions(started_at);

-- User quiz scores (aggregated for leaderboard)
CREATE TABLE IF NOT EXISTS quiz_scores (
    user_id      UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(200) NOT NULL DEFAULT '',
    avatar_url   VARCHAR(500),
    total_score  INT NOT NULL DEFAULT 0,
    week_score   INT NOT NULL DEFAULT 0,
    month_score  INT NOT NULL DEFAULT 0,
    best_streak  INT NOT NULL DEFAULT 0,
    cur_streak   INT NOT NULL DEFAULT 0,
    last_quiz    DATE,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_quiz_scores_week_score  ON quiz_scores(week_score DESC);
CREATE INDEX IF NOT EXISTS idx_quiz_scores_month_score ON quiz_scores(month_score DESC);
CREATE INDEX IF NOT EXISTS idx_quiz_scores_total_score ON quiz_scores(total_score DESC);
