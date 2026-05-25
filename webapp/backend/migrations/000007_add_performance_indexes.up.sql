-- Performance indexes for query optimization
-- Users table
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users (is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_users_is_active_created_at ON users (is_active, created_at DESC);

-- User roles table
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles (role_id);

-- Role permissions table
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions (role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions (permission_id);

-- Refresh tokens table
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked_at ON refresh_tokens (revoked_at);

-- Roles table
CREATE INDEX IF NOT EXISTS idx_roles_name_perf ON roles (name);
CREATE INDEX IF NOT EXISTS idx_roles_is_system ON roles (is_system);

-- Permissions table
CREATE INDEX IF NOT EXISTS idx_permissions_name_perf ON permissions (name);
CREATE INDEX IF NOT EXISTS idx_permissions_module ON permissions (module);
