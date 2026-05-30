INSERT INTO screen_backgrounds (screen_key, screen_name, is_active) VALUES
    ('home_header',     'Header Trang chủ',     true),
    ('calendar_header', 'Header Lịch âm dương',  true),
    ('prayers_header',  'Header Văn Khấn',        true),
    ('tools_header',    'Header Tiện ích',         true)
ON CONFLICT (screen_key) DO NOTHING;
