import api from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { Permission, GroupedPermissions } from "@/types/permission";

// ============================================
// Permission API Service
// ============================================

/**
 * Get all permissions
 */
export async function getPermissions(): Promise<ApiResponse<Permission[]>> {
  const response = await api.get<ApiResponse<Permission[]>>("/admin/permissions");
  return response.data;
}

/**
 * Get permissions grouped by module
 */
export async function getPermissionsGrouped(): Promise<ApiResponse<GroupedPermissions[]>> {
  const response = await api.get<ApiResponse<GroupedPermissions[]>>("/admin/permissions/grouped");
  return response.data;
}

/**
 * Get all available permission modules
 */
export async function getPermissionModules(): Promise<ApiResponse<string[]>> {
  const response = await api.get<ApiResponse<string[]>>("/admin/permissions/modules");
  return response.data;
}
