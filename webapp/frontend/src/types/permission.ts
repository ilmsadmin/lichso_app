// ============================================
// Permission Types
// ============================================

/**
 * Permission data returned from the API
 */
export interface Permission {
  id: string;
  name: string;
  display_name: string;
  module: string;
  action: string;
  description: string;
  created_at: string;
}

/**
 * Permissions grouped by module
 */
export interface GroupedPermissions {
  module: string;
  permissions: Permission[];
}

/**
 * All available permission modules
 */
export type PermissionModule =
  | "users"
  | "roles"
  | "permissions"
  | "settings"
  | "dashboard"
  | "logs";

/**
 * All available permission actions
 */
export type PermissionAction =
  | "create"
  | "read"
  | "update"
  | "delete"
  | "export"
  | "assign"
  | "stats";
