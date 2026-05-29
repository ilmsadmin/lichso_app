import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  PushCampaign,
  PushStats,
  CreateCampaignRequest,
  UpdateCampaignRequest,
  CampaignListParams,
} from "@/types/push-notification";

export async function getPushStats(): Promise<ApiResponse<PushStats>> {
  const res = await api.get<ApiResponse<PushStats>>("/admin/push/stats");
  return res.data;
}

export async function listCampaigns(
  params?: CampaignListParams
): Promise<PaginatedResponse<PushCampaign>> {
  const res = await api.get<PaginatedResponse<PushCampaign>>("/admin/push/campaigns", { params });
  return res.data;
}

export async function getCampaign(id: string): Promise<ApiResponse<PushCampaign>> {
  const res = await api.get<ApiResponse<PushCampaign>>(`/admin/push/campaigns/${id}`);
  return res.data;
}

export async function createCampaign(
  data: CreateCampaignRequest
): Promise<ApiResponse<PushCampaign>> {
  const res = await api.post<ApiResponse<PushCampaign>>("/admin/push/campaigns", data);
  return res.data;
}

export async function updateCampaign(
  id: string,
  data: UpdateCampaignRequest
): Promise<ApiResponse<PushCampaign>> {
  const res = await api.put<ApiResponse<PushCampaign>>(`/admin/push/campaigns/${id}`, data);
  return res.data;
}

export async function deleteCampaign(id: string): Promise<ApiResponse<null>> {
  const res = await api.delete<ApiResponse<null>>(`/admin/push/campaigns/${id}`);
  return res.data;
}

export async function sendCampaign(id: string): Promise<ApiResponse<null>> {
  const res = await api.post<ApiResponse<null>>(`/admin/push/campaigns/${id}/send`);
  return res.data;
}
