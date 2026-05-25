-- Bookmarks: users can bookmark important dates
CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    solar_date DATE NOT NULL,
    title VARCHAR(255) NOT NULL DEFAULT '',
    note TEXT DEFAULT '',
    color VARCHAR(20) DEFAULT 'amber',
    is_recurring BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    UNIQUE(user_id, solar_date, title)
);

CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_solar_date ON bookmarks(solar_date);
CREATE INDEX idx_bookmarks_user_date ON bookmarks(user_id, solar_date);
CREATE INDEX idx_bookmarks_deleted_at ON bookmarks(deleted_at);
