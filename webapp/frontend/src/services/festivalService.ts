import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  FolkFestival,
  FolkFestivalSummary,
  CreateFolkFestivalRequest,
  UpdateFolkFestivalRequest,
  FolkFestivalListParams,
} from "@/types/festival";

// ============================================
// Folk Festival API Service
// ============================================

/**
 * Get paginated folk festivals (admin)
 */
export async function getFolkFestivals(
  params?: FolkFestivalListParams
): Promise<PaginatedResponse<FolkFestivalSummary>> {
  const response = await api.get<PaginatedResponse<FolkFestivalSummary>>("/admin/festivals", {
    params,
  });
  return response.data;
}

/**
 * Get a folk festival by ID
 */
export async function getFolkFestival(id: string): Promise<ApiResponse<FolkFestival>> {
  const response = await api.get<ApiResponse<FolkFestival>>(`/admin/festivals/${id}`);
  return response.data;
}

/**
 * Create a new folk festival
 */
export async function createFolkFestival(
  data: CreateFolkFestivalRequest
): Promise<ApiResponse<FolkFestival>> {
  const response = await api.post<ApiResponse<FolkFestival>>("/admin/festivals", data);
  return response.data;
}

/**
 * Update a folk festival
 */
export async function updateFolkFestival(
  id: string,
  data: UpdateFolkFestivalRequest
): Promise<ApiResponse<FolkFestival>> {
  const response = await api.put<ApiResponse<FolkFestival>>(`/admin/festivals/${id}`, data);
  return response.data;
}

/**
 * Delete a folk festival
 */
export async function deleteFolkFestival(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/festivals/${id}`);
  return response.data;
}
