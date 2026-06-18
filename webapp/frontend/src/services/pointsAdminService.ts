import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  AdminUserPointsRow,
  AdminUserPointsDetail,
  AdminDailyPointsRow,
  AdminPointsListParams,
  AdjustUserPointsRequest,
  AdjustUserPointsResult,
} from "@/types/points";

export async function getUserPointsList(
  params?: AdminPointsListParams
): Promise<PaginatedResponse<AdminUserPointsRow>> {
  const response = await api.get<PaginatedResponse<AdminUserPointsRow>>("/admin/points/users", {
    params,
  });
  return response.data;
}

export async function getUserPointsDetail(
  userId: string
): Promise<ApiResponse<AdminUserPointsDetail>> {
  const response = await api.get<ApiResponse<AdminUserPointsDetail>>(
    `/admin/points/users/${userId}`
  );
  return response.data;
}

export async function getUserDailyPoints(
  userId: string,
  days = 30
): Promise<ApiResponse<AdminDailyPointsRow[]>> {
  const response = await api.get<ApiResponse<AdminDailyPointsRow[]>>(
    `/admin/points/users/${userId}/daily`,
    { params: { days } }
  );
  return response.data;
}

export async function adjustUserPoints(
  userId: string,
  data: AdjustUserPointsRequest
): Promise<ApiResponse<AdjustUserPointsResult>> {
  const response = await api.post<ApiResponse<AdjustUserPointsResult>>(
    `/admin/points/users/${userId}/adjust`,
    data
  );
  return response.data;
}
