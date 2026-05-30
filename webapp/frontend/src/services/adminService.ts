import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type { DashboardStats, ActivityEntry, ActivityLogParams } from "@/types/dashboard";
import type {
  SettingResponse,
  UpdateSettingsGroupRequest,
  GroupedSettings,
  ScreenBackgroundResponse,
} from "@/types/settings";

// ============================================
// Admin API Service
// ============================================

/**
 * Get dashboard statistics
 */
export async function getDashboardStats(): Promise<ApiResponse<DashboardStats>> {
  const response = await api.get<ApiResponse<DashboardStats>>("/admin/dashboard/stats");
  return response.data;
}

/**
 * Get paginated activity logs
 */
export async function getActivityLogs(
  params?: ActivityLogParams
): Promise<PaginatedResponse<ActivityEntry>> {
  const response = await api.get<PaginatedResponse<ActivityEntry>>("/admin/logs", { params });
  return response.data;
}

/**
 * Get a single activity log by ID
 */
export async function getActivityLog(id: string): Promise<ApiResponse<ActivityEntry>> {
  const response = await api.get<ApiResponse<ActivityEntry>>(`/admin/logs/${id}`);
  return response.data;
}

/**
 * Export activity logs as CSV
 */
export async function exportActivityLogs(params?: ActivityLogParams): Promise<Blob> {
  const response = await api.get("/admin/logs/export", {
    params,
    responseType: "blob",
  });
  return response.data;
}

// ============================================
// Settings API
// ============================================

/**
 * Get all settings
 */
export async function getSettings(): Promise<ApiResponse<SettingResponse[]>> {
  const response = await api.get<ApiResponse<SettingResponse[]>>("/admin/settings");
  return response.data;
}

/**
 * Get settings grouped by group
 */
export async function getGroupedSettings(): Promise<ApiResponse<GroupedSettings>> {
  const response = await api.get<ApiResponse<GroupedSettings>>("/admin/settings/grouped");
  return response.data;
}

/**
 * Get settings by group
 */
export async function getSettingsByGroup(group: string): Promise<ApiResponse<SettingResponse[]>> {
  const response = await api.get<ApiResponse<SettingResponse[]>>(`/admin/settings/group/${group}`);
  return response.data;
}

/**
 * Update settings for a group
 */
export async function updateSettingsGroup(
  group: string,
  data: UpdateSettingsGroupRequest
): Promise<ApiResponse<SettingResponse[]>> {
  const response = await api.put<ApiResponse<SettingResponse[]>>(
    `/admin/settings/group/${group}`,
    data
  );
  return response.data;
}

/**
 * Update a single setting
 */
export async function updateSetting(
  key: string,
  value: unknown
): Promise<ApiResponse<SettingResponse>> {
  const response = await api.put<ApiResponse<SettingResponse>>(`/admin/settings/${key}`, { value });
  return response.data;
}

/**
 * Delete a setting
 */
export async function deleteSetting(key: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/settings/${key}`);
  return response.data;
}

// ============================================
// Screen Backgrounds API
// ============================================

/**
 * Get all screen backgrounds
 */
export async function getScreenBackgrounds(): Promise<ApiResponse<ScreenBackgroundResponse[]>> {
  const response = await api.get<ApiResponse<ScreenBackgroundResponse[]>>("/admin/screen-backgrounds");
  return response.data;
}

/**
 * Upload a screen background (Legacy file upload)
 */
export async function uploadScreenBackground(
  screenKey: string,
  file: File
): Promise<ApiResponse<ScreenBackgroundResponse>> {
  const formData = new FormData();
  formData.append("image", file);

  const response = await api.post<ApiResponse<ScreenBackgroundResponse>>(
    `/admin/screen-backgrounds/${screenKey}/upload`,
    formData,
    {
      timeout: 60000,
    }
  );
  return response.data;
}

/**
 * Set a screen background from an image URL
 */
export async function setScreenBackgroundUrl(
  screenKey: string,
  imageUrl: string
): Promise<ApiResponse<ScreenBackgroundResponse>> {
  const response = await api.put<ApiResponse<ScreenBackgroundResponse>>(
    `/admin/screen-backgrounds/${screenKey}/url`,
    { image_url: imageUrl }
  );
  return response.data;
}

/**
 * Delete a screen background
 */
export async function deleteScreenBackground(screenKey: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/screen-backgrounds/${screenKey}`);
  return response.data;
}
