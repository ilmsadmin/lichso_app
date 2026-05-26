-- 1. Ví điểm chung toàn app
CREATE TABLE IF NOT EXISTS point_wallets (
    user_id          UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    balance          INT NOT NULL DEFAULT 0,
    lifetime_earned  INT NOT NULL DEFAULT 0,
    lifetime_spent   INT NOT NULL DEFAULT 0,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Nhật ký giao dịch ví điểm
CREATE TABLE IF NOT EXISTS point_transactions (
    id               BIGSERIAL PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount           INT NOT NULL,
    direction        VARCHAR(10) NOT NULL CHECK (direction IN ('earn', 'spend')),
    source           VARCHAR(50) NOT NULL, -- 'quiz_daily_complete', 'quiz_assist_hint', 'daily_checkin'...
    source_id        VARCHAR(100),         -- UUID của session hoặc ID câu hỏi liên quan
    idempotency_key  VARCHAR(100) UNIQUE,  -- Tránh trừ điểm trùng lặp khi mất mạng
    metadata         JSONB,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_point_transactions_user_id ON point_transactions(user_id);

-- 3. Ghi nhận sử dụng trợ giúp trong phiên chơi
CREATE TABLE IF NOT EXISTS quiz_assist_usages (
    id               BIGSERIAL PRIMARY KEY,
    session_id       UUID NOT NULL REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id      BIGINT NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    assist_type      VARCHAR(20) NOT NULL CHECK (assist_type IN ('fifty_fifty', 'hint', 'extra_time')),
    cost_app_points  INT NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (session_id, question_id, assist_type)
);

-- 4. Tiến trình năng lực theo chủ đề (Category Mastery)
CREATE TABLE IF NOT EXISTS quiz_category_masteries (
    id               BIGSERIAL PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category         VARCHAR(100) NOT NULL,
    answered_count   INT NOT NULL DEFAULT 0,
    correct_count    INT NOT NULL DEFAULT 0,
    correct_rate     NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    current_streak   INT NOT NULL DEFAULT 0,
    best_streak      INT NOT NULL DEFAULT 0,
    mastery_level    INT NOT NULL DEFAULT 0,
    title            VARCHAR(100) NOT NULL DEFAULT '',
    last_unlocked_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, category)
);

-- 5. Huy hiệu hành vi người dùng
CREATE TABLE IF NOT EXISTS user_badges (
    id               BIGSERIAL PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_key        VARCHAR(100) NOT NULL,
    source           VARCHAR(100) NOT NULL,
    unlocked_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    metadata         JSONB,
    UNIQUE (user_id, badge_key)
);

-- 6. Bổ sung thông tin quản trị và nguồn tham khảo vào quiz_questions
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS source_title VARCHAR(200);
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS source_url   VARCHAR(500);
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS source_note  TEXT;
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS review_status VARCHAR(20) NOT NULL DEFAULT 'draft';
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS reviewed_by  UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS reviewed_at  TIMESTAMPTZ;
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS published_at TIMESTAMPTZ;
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS version      INT NOT NULL DEFAULT 1;

-- 7. Bổ sung XP và cột hỗ trợ điểm thi đấu mới vào quiz_scores & quiz_sessions
ALTER TABLE quiz_scores ADD COLUMN IF NOT EXISTS xp INT NOT NULL DEFAULT 0;
ALTER TABLE quiz_sessions ADD COLUMN IF NOT EXISTS score_v2 INT NOT NULL DEFAULT 0;
