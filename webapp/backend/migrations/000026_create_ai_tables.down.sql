-- Revert migration 000025: Drop AI tables

ALTER TABLE articles
    DROP COLUMN IF EXISTS ai_generated,
    DROP COLUMN IF EXISTS ai_model,
    DROP COLUMN IF EXISTS ai_tokens_used,
    DROP COLUMN IF EXISTS ai_cost_usd,
    DROP COLUMN IF EXISTS ai_generation_id;

DROP TABLE IF EXISTS ai_usage_quotas;
DROP TABLE IF EXISTS ai_chat_sessions;
DROP TABLE IF EXISTS ai_prompt_templates;
DROP TABLE IF EXISTS ai_horoscope_sessions;
DROP TABLE IF EXISTS ai_generation_logs;
