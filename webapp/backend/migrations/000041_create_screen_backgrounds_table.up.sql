CREATE TABLE IF NOT EXISTS screen_backgrounds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_key VARCHAR(50) NOT NULL,
    screen_name VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    image_path VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_by VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT screen_backgrounds_screen_key_unique UNIQUE (screen_key)
);

CREATE INDEX IF NOT EXISTS idx_screen_backgrounds_deleted_at ON screen_backgrounds(deleted_at);

-- Seed default screen list
INSERT INTO screen_backgrounds (screen_key, screen_name, is_active) VALUES
    ('home',            'Trang chủ',             true),
    ('calendar',        'Lịch âm dương',          true),
    ('prayers',         'Văn Khấn',               true),
    ('profile',         'Hồ sơ',                  true),
    ('tools',           'Công cụ',                true),
    ('chat',            'AI Chat',                true),
    ('history',         'Ngày này năm xưa',       true),
    ('search',          'Tìm kiếm',               true),
    ('bookmarks',       'Ngày đã lưu',            true),
    ('settings',        'Cài đặt',                true),
    ('quiz_home',       'Trắc nghiệm',            true),
    ('knowledge_feed',  'Bài viết khám phá',      true)
ON CONFLICT (screen_key) DO NOTHING;
