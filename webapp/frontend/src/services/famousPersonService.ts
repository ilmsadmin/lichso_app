import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  FamousPerson,
  FamousPersonSummary,
  CreateFamousPersonRequest,
  UpdateFamousPersonRequest,
  FamousPersonListParams,
} from "@/types/famousPerson";

// ============================================
// Famous Person API Service
// ============================================

/**
 * Get paginated famous people (admin)
 */
export async function getFamousPeople(
  params?: FamousPersonListParams
): Promise<PaginatedResponse<FamousPersonSummary>> {
  const response = await api.get<PaginatedResponse<FamousPersonSummary>>("/admin/famous-people", {
    params,
  });
  return response.data;
}

/**
 * Get a famous person by ID
 */
export async function getFamousPerson(id: string): Promise<ApiResponse<FamousPerson>> {
  const response = await api.get<ApiResponse<FamousPerson>>(`/admin/famous-people/${id}`);
  return response.data;
}

/**
 * Create a new famous person
 */
export async function createFamousPerson(
  data: CreateFamousPersonRequest
): Promise<ApiResponse<FamousPerson>> {
  const response = await api.post<ApiResponse<FamousPerson>>("/admin/famous-people", data);
  return response.data;
}

/**
 * Update a famous person
 */
export async function updateFamousPerson(
  id: string,
  data: UpdateFamousPersonRequest
): Promise<ApiResponse<FamousPerson>> {
  const response = await api.put<ApiResponse<FamousPerson>>(`/admin/famous-people/${id}`, data);
  return response.data;
}

/**
 * Delete a famous person
 */
export async function deleteFamousPerson(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/famous-people/${id}`);
  return response.data;
}
