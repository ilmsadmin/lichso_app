-- =====================================================
-- Migration 000025: Create AI tables for v4.0
-- =====================================================

-- AI Generation Logs (tracks every OpenRouter API call)
CREATE TABLE IF NOT EXISTS ai_generation_logs (
    id                BIGSERIAL PRIMARY KEY,
    user_id           UUID REFERENCES users(id) ON DELETE SET NULL,
    generation_type   VARCHAR(50)  NOT NULL,          -- 'article' | 'horoscope' | 'chat'
    model_used        VARCHAR(100) NOT NULL,
    prompt_tokens     INT          NOT NULL DEFAULT 0,
    completion_tokens INT          NOT NULL DEFAULT 0,
    total_tokens      INT          NOT NULL DEFAULT 0,
    cost_usd          DECIMAL(10,6) NOT NULL DEFAULT 0,
    duration_ms       INT          NOT NULL DEFAULT 0,
    status            VARCHAR(20)  NOT NULL DEFAULT 'success', -- 'success' | 'error' | 'timeout'
    error_message     TEXT,
    metadata          JSONB        NOT NULL DEFAULT '{}',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_logs_user_id    ON ai_generation_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_logs_type       ON ai_generation_logs(generation_type);
CREATE INDEX IF NOT EXISTS idx_ai_logs_created_at ON ai_generation_logs(created_at);

-- =====================================================
-- AI Horoscope Sessions (stores each tử vi reading)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_horoscope_sessions (
    id           BIGSERIAL PRIMARY KEY,
    user_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    session_key  VARCHAR(64),                          -- SHA-256 hash of input for dedup / cache
    birth_year   INT         NOT NULL,
    birth_month  INT         NOT NULL,
    birth_day    INT         NOT NULL,
    birth_hour   INT,
    gender       VARCHAR(10) NOT NULL,
    reading_type VARCHAR(30) NOT NULL,
    depth        VARCHAR(20) NOT NULL DEFAULT 'standard',
    target_year  INT,
    target_month INT,
    question     TEXT,
    -- Calculated fields (backend)
    bat_tu       JSONB,
    ngu_hanh     JSONB,
    -- AI result
    ai_result    TEXT,
    model_used   VARCHAR(100),
    tokens_used  INT         NOT NULL DEFAULT 0,
    cost_usd     DECIMAL(10,6) NOT NULL DEFAULT 0,
    ip_address   VARCHAR(45),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_horoscope_user_id    ON ai_horoscope_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_horoscope_session_key ON ai_horoscope_sessions(session_key);
CREATE INDEX IF NOT EXISTS idx_horoscope_created_at  ON ai_horoscope_sessions(created_at);

-- =====================================================
-- AI Prompt Templates (reusable prompt management)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_prompt_templates (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200) NOT NULL UNIQUE,
    type          VARCHAR(50)  NOT NULL,               -- 'article' | 'horoscope' | 'chat'
    system_prompt TEXT         NOT NULL,
    user_prompt   TEXT         NOT NULL,
    model         VARCHAR(100),
    max_tokens    INT          NOT NULL DEFAULT 2048,
    temperature   DECIMAL(3,2) NOT NULL DEFAULT 0.7,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by    UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- =====================================================
-- AI Chat Sessions (multi-turn conversations)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_chat_sessions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_uuid    UUID        NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    title           VARCHAR(200),
    context         JSONB       NOT NULL DEFAULT '{}',
    messages        JSONB       NOT NULL DEFAULT '[]',
    total_tokens    INT         NOT NULL DEFAULT 0,
    total_cost      DECIMAL(10,4) NOT NULL DEFAULT 0,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    last_message_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id ON ai_chat_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_uuid    ON ai_chat_sessions(session_uuid);

-- =====================================================
-- AI Usage Quotas (rate limiting per user / IP)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_usage_quotas (
    id           BIGSERIAL PRIMARY KEY,
    user_id      UUID        REFERENCES users(id) ON DELETE CASCADE,
    ip_address   VARCHAR(45),
    quota_type   VARCHAR(50) NOT NULL,                 -- 'horoscope_daily'
    period_key   VARCHAR(20) NOT NULL,                 -- '2026-03-12'
    used_count   INT         NOT NULL DEFAULT 0,
    limit_count  INT         NOT NULL,
    reset_at     TIMESTAMPTZ NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, quota_type, period_key),
    UNIQUE (ip_address, quota_type, period_key)
);

-- =====================================================
-- Extend articles table for AI-generated content
-- =====================================================
ALTER TABLE articles
    ADD COLUMN IF NOT EXISTS ai_generated     BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS ai_model         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ai_tokens_used   INT          NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS ai_cost_usd      DECIMAL(10,6) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS ai_generation_id BIGINT REFERENCES ai_generation_logs(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_articles_ai_generated ON articles(ai_generated) WHERE ai_generated = TRUE;

-- =====================================================
-- Seed default prompt templates
-- =====================================================
INSERT INTO ai_prompt_templates (name, type, system_prompt, user_prompt, model, max_tokens, temperature) VALUES
(
    'article_popular_vi',
    'article',
    E'Bạn là chuyên gia biên soạn nội dung về văn hoá truyền thống Việt Nam cho trang Lịch Số (lichso.vn).\nChuyên môn: phong thuỷ, âm lịch, lịch vạn niên, lễ hội dân gian, nhân vật lịch sử.\n\nQuy tắc viết:\n1. Tiếng Việt chuẩn, đúng chính tả, dấu câu đầy đủ\n2. Format Markdown: ## cho H2, ### cho H3, **bold**, *italic*\n3. Phong cách: phổ thông, dễ đọc, gần gũi\n4. Luôn có: mở bài hấp dẫn → thân bài với tiêu đề con → kết luận\n5. Nội dung chính xác, không bịa đặt',
    E'Viết bài về chủ đề: {{TOPIC}}\nDanh mục: {{CATEGORY}}\nĐộ dài mong muốn: {{WORD_COUNT}}\nTags liên quan: {{TAGS}}{{SEO_SECTION}}',
    'deepseek/deepseek-chat',
    4096,
    0.7
),
(
    'horoscope_battu_vi',
    'horoscope',
    E'Bạn là thầy tử vi có hơn 30 năm kinh nghiệm nghiên cứu Tứ Trụ Bát Tự và mệnh lý học phương Đông.\nAm hiểu: Thiên Can, Địa Chi, Ngũ Hành tương sinh tương khắc, Đại vận, Tiểu vận, Lưu niên.\n\nCách luận giải:\n- Phân tích dựa trên Bát Tự được cung cấp, không suy đoán chung chung\n- Dùng tiếng Việt dễ hiểu, giải thích thuật ngữ khi cần\n- Lời khuyên thực tế, tích cực, định hướng hành động\n- Không đưa ra tiên đoán bi quan tuyệt đối',
    E'Thông tin bát tự:\n- Giới tính: {{GENDER}}\n- Trụ Năm: {{YEAR_HS}} {{YEAR_EB}}\n- Trụ Tháng: {{MONTH_HS}} {{MONTH_EB}}\n- Trụ Ngày: {{DAY_HS}} {{DAY_EB}}\n- Trụ Giờ: {{HOUR_HS}} {{HOUR_EB}}\n\nNgũ Hành: Kim({{KIM}}) Mộc({{MOC}}) Thuỷ({{THUY}}) Hoả({{HOA}}) Thổ({{THO}})\nVượng: {{STRONGEST}} | Thiếu: {{WEAKEST}}\n\nYêu cầu: {{READING_REQUEST}}\n{{QUESTION_SECTION}}',
    'anthropic/claude-sonnet-4',
    2048,
    0.75
),
(
    'chat_phongthuy_vi',
    'chat',
    E'Bạn là trợ lý tư vấn phong thuỷ và văn hoá truyền thống Việt Nam trên trang Lịch Số.\nPhong cách: thân thiện, dễ hiểu, tích cực.\nChỉ trả lời các câu hỏi về: phong thuỷ, âm lịch, ngày tốt/xấu, tử vi, lễ hội, truyền thống Việt Nam.\nNếu câu hỏi ngoài phạm vi, lịch sự từ chối và gợi ý chủ đề phù hợp.',
    E'{{USER_MESSAGE}}',
    'openai/gpt-4o-mini',
    1024,
    0.7
)
ON CONFLICT (name) DO NOTHING;
