import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  User,
  UserDetail,
  CreateUserRequest,
  UpdateUserRequest,
  ToggleUserStatusRequest,
  SetUserRolesRequest,
  UserListParams,
} from "@/types/user";

// ============================================
// User API Service
// ============================================

/**
 * Get paginated users
 */
export async function getUsers(params?: UserListParams): Promise<PaginatedResponse<User>> {
  const response = await api.get<PaginatedResponse<User>>("/admin/users", {
    params,
  });
  return response.data;
}

/**
 * Get a user by ID
 */
export async function getUser(id: string): Promise<ApiResponse<User>> {
  const response = await api.get<ApiResponse<User>>(`/admin/users/${id}`);
  return response.data;
}

/**
 * Get full enriched user detail for admin view
 */
export async function getUserDetail(id: string): Promise<ApiResponse<UserDetail>> {
  const response = await api.get<ApiResponse<UserDetail>>(`/admin/users/${id}/detail`);
  return response.data;
}

/**
 * Create a new user
 */
export async function createUser(data: CreateUserRequest): Promise<ApiResponse<User>> {
  const response = await api.post<ApiResponse<User>>("/admin/users", data);
  return response.data;
}

/**
 * Update a user
 */
export async function updateUser(id: string, data: UpdateUserRequest): Promise<ApiResponse<User>> {
  const response = await api.put<ApiResponse<User>>(`/admin/users/${id}`, data);
  return response.data;
}

/**
 * Delete a user
 */
export async function deleteUser(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/users/${id}`);
  return response.data;
}

/**
 * Toggle user active status
 */
export async function toggleUserStatus(
  id: string,
  data: ToggleUserStatusRequest
): Promise<ApiResponse<User>> {
  const response = await api.patch<ApiResponse<User>>(`/admin/users/${id}/status`, data);
  return response.data;
}

/**
 * Set user roles
 */
export async function setUserRoles(
  id: string,
  data: SetUserRolesRequest
): Promise<ApiResponse<User>> {
  const response = await api.put<ApiResponse<User>>(`/admin/users/${id}/roles`, data);
  return response.data;
}

/**
 * Export users to CSV
 */
export async function exportUsers(params?: UserListParams): Promise<Blob> {
  const response = await api.get("/admin/users/export", {
    params,
    responseType: "blob",
  });
  return response.data;
}
