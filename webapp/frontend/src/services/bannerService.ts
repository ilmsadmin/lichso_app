import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  Banner,
  CreateBannerRequest,
  UpdateBannerRequest,
  BannerListParams,
  BannerOrder,
} from "@/types/banner";

// ============================================
// Banner API Service
// ============================================

/**
 * Get paginated banners (admin)
 */
export async function getBanners(
  params?: BannerListParams
): Promise<PaginatedResponse<Banner>> {
  const response = await api.get<PaginatedResponse<Banner>>("/admin/banners", {
    params,
  });
  return response.data;
}

/**
 * Get a banner by ID
 */
export async function getBanner(id: string): Promise<ApiResponse<Banner>> {
  const response = await api.get<ApiResponse<Banner>>(`/admin/banners/${id}`);
  return response.data;
}

/**
 * Create a new banner
 */
export async function createBanner(
  data: CreateBannerRequest
): Promise<ApiResponse<Banner>> {
  const response = await api.post<ApiResponse<Banner>>("/admin/banners", data);
  return response.data;
}

/**
 * Update a banner
 */
export async function updateBanner(
  id: string,
  data: UpdateBannerRequest
): Promise<ApiResponse<Banner>> {
  const response = await api.put<ApiResponse<Banner>>(
    `/admin/banners/${id}`,
    data
  );
  return response.data;
}

/**
 * Delete a banner
 */
export async function deleteBanner(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/banners/${id}`);
  return response.data;
}

/**
 * Toggle banner active status
 */
export async function toggleBanner(id: string): Promise<ApiResponse<Banner>> {
  const response = await api.patch<ApiResponse<Banner>>(
    `/admin/banners/${id}/toggle`
  );
  return response.data;
}

/**
 * Reorder banners
 */
export async function reorderBanners(
  orders: BannerOrder[]
): Promise<ApiResponse<null>> {
  const response = await api.put<ApiResponse<null>>("/admin/banners/reorder", {
    orders,
  });
  return response.data;
}
