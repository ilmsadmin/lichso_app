-- ============================================
-- Migration: Create users table
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
