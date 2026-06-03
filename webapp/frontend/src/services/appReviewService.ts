import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type { AppReview, AppReviewListParams, UpdateAppReviewRequest } from "@/types/app-review";

export async function getAppReviews(
  params?: AppReviewListParams
): Promise<PaginatedResponse<AppReview>> {
  const response = await api.get<PaginatedResponse<AppReview>>("/admin/app-reviews", { params });
  return response.data;
}

export async function getAppReview(id: string): Promise<ApiResponse<AppReview>> {
  const response = await api.get<ApiResponse<AppReview>>(`/admin/app-reviews/${id}`);
  return response.data;
}

export async function updateAppReview(
  id: string,
  payload: UpdateAppReviewRequest
): Promise<ApiResponse<AppReview>> {
  const response = await api.patch<ApiResponse<AppReview>>(`/admin/app-reviews/${id}`, payload);
  return response.data;
}
