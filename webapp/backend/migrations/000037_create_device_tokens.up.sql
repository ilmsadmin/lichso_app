CREATE TABLE IF NOT EXISTS device_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    token       VARCHAR(512) NOT NULL,
    platform    VARCHAR(20)  NOT NULL DEFAULT 'android',
    app_version VARCHAR(50)  NOT NULL DEFAULT '',
    device_id   VARCHAR(255) NOT NULL DEFAULT '',
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    last_seen   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);

-- Full unique constraint on token (required for ON CONFLICT upsert)
ALTER TABLE device_tokens ADD CONSTRAINT device_tokens_token_key UNIQUE (token);
CREATE INDEX IF NOT EXISTS idx_device_tokens_user_id ON device_tokens (user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_device_tokens_active  ON device_tokens (is_active) WHERE deleted_at IS NULL;
