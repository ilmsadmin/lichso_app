import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  Survey,
  CreateSurveyRequest,
  UpdateSurveyRequest,
  SurveyStats,
  SurveyListParams,
} from "@/types/survey";

// ============================================
// Survey API Service (Admin & Client)
// ============================================

/**
 * Get paginated surveys (admin)
 */
export async function getSurveys(
  params?: SurveyListParams
): Promise<PaginatedResponse<Survey>> {
  const response = await api.get<PaginatedResponse<Survey>>("/admin/surveys", {
    params,
  });
  return response.data;
}

/**
 * Get a survey by ID (admin)
 */
export async function getSurvey(id: string): Promise<ApiResponse<Survey>> {
  const response = await api.get<ApiResponse<Survey>>(`/admin/surveys/${id}`);
  return response.data;
}

/**
 * Create a new survey (admin)
 */
export async function createSurvey(
  data: CreateSurveyRequest
): Promise<ApiResponse<Survey>> {
  const response = await api.post<ApiResponse<Survey>>("/admin/surveys", data);
  return response.data;
}

/**
 * Update a survey (admin)
 */
export async function updateSurvey(
  id: string,
  data: UpdateSurveyRequest
): Promise<ApiResponse<Survey>> {
  const response = await api.put<ApiResponse<Survey>>(
    `/admin/surveys/${id}`,
    data
  );
  return response.data;
}

/**
 * Delete a survey (admin)
 */
export async function deleteSurvey(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/surveys/${id}`);
  return response.data;
}

/**
 * Toggle survey active status (admin)
 */
export async function toggleSurvey(id: string): Promise<ApiResponse<Survey>> {
  const response = await api.patch<ApiResponse<Survey>>(
    `/admin/surveys/${id}/toggle`
  );
  return response.data;
}

/**
 * Get survey results stats (admin)
 */
export async function getSurveyStats(id: string): Promise<ApiResponse<SurveyStats>> {
  const response = await api.get<ApiResponse<SurveyStats>>(`/admin/surveys/${id}/stats`);
  return response.data;
}
