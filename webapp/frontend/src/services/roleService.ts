import api from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { Role, CreateRoleRequest, UpdateRoleRequest, AssignRoleRequest } from "@/types/role";

// ============================================
// Role API Service
// ============================================

/**
 * Get all roles
 */
export async function getRoles(): Promise<ApiResponse<Role[]>> {
  const response = await api.get<ApiResponse<Role[]>>("/admin/roles");
  return response.data;
}

/**
 * Get a role by ID
 */
export async function getRole(id: string): Promise<ApiResponse<Role>> {
  const response = await api.get<ApiResponse<Role>>(`/admin/roles/${id}`);
  return response.data;
}

/**
 * Create a new role
 */
export async function createRole(data: CreateRoleRequest): Promise<ApiResponse<Role>> {
  const response = await api.post<ApiResponse<Role>>("/admin/roles", data);
  return response.data;
}

/**
 * Update a role
 */
export async function updateRole(id: string, data: UpdateRoleRequest): Promise<ApiResponse<Role>> {
  const response = await api.put<ApiResponse<Role>>(`/admin/roles/${id}`, data);
  return response.data;
}

/**
 * Delete a role
 */
export async function deleteRole(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/roles/${id}`);
  return response.data;
}

/**
 * Assign a role to a user
 */
export async function assignRole(data: AssignRoleRequest): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>("/admin/roles/assign", data);
  return response.data;
}

/**
 * Unassign a role from a user
 */
export async function unassignRole(data: AssignRoleRequest): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>("/admin/roles/unassign", data);
  return response.data;
}
