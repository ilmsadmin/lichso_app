-- ============================================
-- Migration: Create permissions table
-- ============================================

CREATE TABLE IF NOT EXISTS permissions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL UNIQUE,
    display_name  VARCHAR(200) NOT NULL,
    module        VARCHAR(50) NOT NULL,
    action        VARCHAR(50) NOT NULL,
    description   TEXT DEFAULT '',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_permissions_name ON permissions(name);
CREATE INDEX IF NOT EXISTS idx_permissions_module ON permissions(module);
CREATE UNIQUE INDEX IF NOT EXISTS idx_permissions_module_action ON permissions(module, action);
