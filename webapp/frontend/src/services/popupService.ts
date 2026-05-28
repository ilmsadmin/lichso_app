import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  Popup,
  CreatePopupRequest,
  UpdatePopupRequest,
  PopupListParams,
} from "@/types/popup";

// ============================================
// Popup API Service
// ============================================

/**
 * Get paginated popups (admin)
 */
export async function getPopups(
  params?: PopupListParams
): Promise<PaginatedResponse<Popup>> {
  const response = await api.get<PaginatedResponse<Popup>>("/admin/popups", {
    params,
  });
  return response.data;
}

/**
 * Get a popup by ID
 */
export async function getPopup(id: string): Promise<ApiResponse<Popup>> {
  const response = await api.get<ApiResponse<Popup>>(`/admin/popups/${id}`);
  return response.data;
}

/**
 * Create a new popup
 */
export async function createPopup(
  data: CreatePopupRequest
): Promise<ApiResponse<Popup>> {
  const response = await api.post<ApiResponse<Popup>>("/admin/popups", data);
  return response.data;
}

/**
 * Update a popup
 */
export async function updatePopup(
  id: string,
  data: UpdatePopupRequest
): Promise<ApiResponse<Popup>> {
  const response = await api.put<ApiResponse<Popup>>(
    `/admin/popups/${id}`,
    data
  );
  return response.data;
}

/**
 * Delete a popup
 */
export async function deletePopup(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/popups/${id}`);
  return response.data;
}

/**
 * Toggle popup active status
 */
export async function togglePopup(id: string): Promise<ApiResponse<Popup>> {
  const response = await api.patch<ApiResponse<Popup>>(
    `/admin/popups/${id}/toggle`
  );
  return response.data;
}
