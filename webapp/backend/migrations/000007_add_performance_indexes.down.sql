-- Drop performance indexes
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_is_active;
DROP INDEX IF EXISTS idx_users_created_at;
DROP INDEX IF EXISTS idx_users_is_active_created_at;
DROP INDEX IF EXISTS idx_user_roles_user_id;
DROP INDEX IF EXISTS idx_user_roles_role_id;
DROP INDEX IF EXISTS idx_role_permissions_role_id;
DROP INDEX IF EXISTS idx_role_permissions_permission_id;
DROP INDEX IF EXISTS idx_refresh_tokens_user_id;
DROP INDEX IF EXISTS idx_refresh_tokens_token;
DROP INDEX IF EXISTS idx_refresh_tokens_expires_at;
DROP INDEX IF EXISTS idx_refresh_tokens_revoked_at;
DROP INDEX IF EXISTS idx_roles_name_perf;
DROP INDEX IF EXISTS idx_roles_is_system;
DROP INDEX IF EXISTS idx_permissions_name_perf;
DROP INDEX IF EXISTS idx_permissions_module;
