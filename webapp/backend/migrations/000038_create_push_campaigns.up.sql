CREATE TABLE IF NOT EXISTS push_campaigns (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(255) NOT NULL,
    body         TEXT         NOT NULL,
    image_url    VARCHAR(500) NOT NULL DEFAULT '',
    click_action VARCHAR(500) NOT NULL DEFAULT '',
    data_payload TEXT         NOT NULL DEFAULT '{}',
    target_type  VARCHAR(20)  NOT NULL DEFAULT 'all',
    target_users TEXT         NOT NULL DEFAULT '',
    status       VARCHAR(20)  NOT NULL DEFAULT 'draft',
    scheduled_at TIMESTAMPTZ,
    sent_at      TIMESTAMPTZ,
    sent_count   INT          NOT NULL DEFAULT 0,
    fail_count   INT          NOT NULL DEFAULT 0,
    created_by   UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_push_campaigns_status     ON push_campaigns (status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_push_campaigns_created_at ON push_campaigns (created_at DESC) WHERE deleted_at IS NULL;
