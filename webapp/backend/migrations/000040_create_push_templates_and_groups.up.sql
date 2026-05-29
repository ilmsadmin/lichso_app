-- Push notification templates
CREATE TABLE IF NOT EXISTS push_templates (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    body         TEXT         NOT NULL,
    image_url    VARCHAR(500) NOT NULL DEFAULT '',
    click_action VARCHAR(500) NOT NULL DEFAULT '',
    data_payload TEXT         NOT NULL DEFAULT '{}',
    created_by   UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

-- User groups for targeted campaigns
CREATE TABLE IF NOT EXISTS user_groups (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL DEFAULT '',
    member_count INT          NOT NULL DEFAULT 0,
    created_by   UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

-- Group memberships
CREATE TABLE IF NOT EXISTS user_group_members (
    group_id UUID NOT NULL REFERENCES user_groups(id) ON DELETE CASCADE,
    user_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id)
);

-- Add template_id and target_group_id to campaigns
ALTER TABLE push_campaigns
    ADD COLUMN IF NOT EXISTS template_id    UUID REFERENCES push_templates(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS target_group_id UUID REFERENCES user_groups(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_push_templates_deleted ON push_templates(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_user_groups_deleted    ON user_groups(deleted_at)    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_user_group_members_user ON user_group_members(user_id);
