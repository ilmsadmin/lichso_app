// ============================================
// User Management Types
// ============================================

/**
 * Role brief info nested in user responses
 */
export interface UserRoleBrief {
  id: string;
  name: string;
  display_name: string;
}

/**
 * User data returned from the API (admin view)
 */
export interface User {
  id: string;
  email: string;
  first_name: string;
  last_name: string;
  full_name: string;
  avatar: string;
  phone: string;
  is_active: boolean;
  last_login: string | null;
  created_at: string;
  updated_at: string;
  roles: UserRoleBrief[];
}

/**
 * Create user request payload
 */
export interface CreateUserRequest {
  email: string;
  password: string;
  first_name: string;
  last_name: string;
  phone?: string;
  is_active?: boolean;
  role_ids?: string[];
}

/**
 * Update user request payload
 */
export interface UpdateUserRequest {
  email?: string;
  first_name?: string;
  last_name?: string;
  phone?: string;
  avatar?: string;
  is_active?: boolean;
  password?: string;
}

/**
 * Toggle user status request
 */
export interface ToggleUserStatusRequest {
  is_active: boolean;
}

/**
 * Set user roles request
 */
export interface SetUserRolesRequest {
  role_ids: string[];
}

/**
 * User list query parameters
 */
export interface UserListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  status?: string;
  role?: string;
}
