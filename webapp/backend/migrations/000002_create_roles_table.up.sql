-- ============================================
-- Migration: Create roles table
-- ============================================

CREATE TABLE IF NOT EXISTS roles (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(50) NOT NULL UNIQUE,
    display_name  VARCHAR(100) NOT NULL,
    description   TEXT DEFAULT '',
    is_system     BOOLEAN NOT NULL DEFAULT false,
    level         INTEGER NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_roles_level ON roles(level);
