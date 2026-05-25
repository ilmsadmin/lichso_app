// ============================================
// Role Types
// ============================================

/**
 * Role data returned from the API
 */
export interface Role {
  id: string;
  name: string;
  display_name: string;
  description: string;
  is_system: boolean;
  level: number;
  created_at: string;
  updated_at: string;
  permissions?: PermissionBrief[];
  user_count?: number;
}

/**
 * Brief permission info nested in role responses
 */
export interface PermissionBrief {
  id: string;
  name: string;
  display_name: string;
  module: string;
  action: string;
}

/**
 * Create role request payload
 */
export interface CreateRoleRequest {
  name: string;
  display_name: string;
  description?: string;
  level?: number;
  permission_ids?: string[];
}

/**
 * Update role request payload
 */
export interface UpdateRoleRequest {
  name?: string;
  display_name?: string;
  description?: string;
  level?: number;
  permission_ids?: string[];
}

/**
 * Assign/unassign role request payload
 */
export interface AssignRoleRequest {
  user_id: string;
  role_id: string;
}
