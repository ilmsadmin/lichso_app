-- ============================================
-- Zplus Base - PostgreSQL Initialization Script
-- ============================================
-- This script runs automatically when PostgreSQL
-- container is created for the first time.
-- ============================================

-- Enable useful extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";    -- gen_random_uuid(), crypt(), etc.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";   -- uuid_generate_v4()

-- ============================================
-- Create tables
-- ============================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100) NOT NULL DEFAULT '',
    last_name   VARCHAR(100) NOT NULL DEFAULT '',
    avatar      VARCHAR(500) DEFAULT '',
    phone       VARCHAR(20) DEFAULT '',
    is_active   BOOLEAN NOT NULL DEFAULT true,
    last_login  TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- Roles table
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

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL UNIQUE,
    display_name  VARCHAR(200) NOT NULL,
    module        VARCHAR(50) NOT NULL,
    action        VARCHAR(50) NOT NULL,
    description   TEXT DEFAULT '',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_permissions_module ON permissions(module);

-- User-Role mapping (many-to-many)
CREATE TABLE IF NOT EXISTS user_roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    assigned_by UUID REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Role-Permission mapping (many-to-many)
CREATE TABLE IF NOT EXISTS role_permissions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(role_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_agent  VARCHAR(500) DEFAULT '',
    ip_address  VARCHAR(45) DEFAULT '',
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    revoked_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- ============================================
-- Log output
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '✅ Zplus Base - PostgreSQL initialized successfully!';
END $$;
