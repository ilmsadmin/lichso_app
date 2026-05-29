import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  PushCampaign,
  PushStats,
  PushTemplate,
  CreateTemplateRequest,
  UserGroup,
  GroupMember,
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

// ── Templates ──────────────────────────────────────────────────────────────

export async function listTemplates(): Promise<ApiResponse<PushTemplate[]>> {
  const res = await api.get<ApiResponse<PushTemplate[]>>("/admin/push/templates");
  return res.data;
}

export async function createTemplate(data: CreateTemplateRequest): Promise<ApiResponse<PushTemplate>> {
  const res = await api.post<ApiResponse<PushTemplate>>("/admin/push/templates", data);
  return res.data;
}

export async function updateTemplate(id: string, data: CreateTemplateRequest): Promise<ApiResponse<PushTemplate>> {
  const res = await api.put<ApiResponse<PushTemplate>>(`/admin/push/templates/${id}`, data);
  return res.data;
}

export async function deleteTemplate(id: string): Promise<ApiResponse<null>> {
  const res = await api.delete<ApiResponse<null>>(`/admin/push/templates/${id}`);
  return res.data;
}

// ── User Groups ────────────────────────────────────────────────────────────

export async function listGroups(): Promise<ApiResponse<UserGroup[]>> {
  const res = await api.get<ApiResponse<UserGroup[]>>("/admin/push/groups");
  return res.data;
}

export async function createGroup(data: { name: string; description?: string }): Promise<ApiResponse<UserGroup>> {
  const res = await api.post<ApiResponse<UserGroup>>("/admin/push/groups", data);
  return res.data;
}

export async function updateGroup(id: string, data: { name: string; description?: string }): Promise<ApiResponse<UserGroup>> {
  const res = await api.put<ApiResponse<UserGroup>>(`/admin/push/groups/${id}`, data);
  return res.data;
}

export async function deleteGroup(id: string): Promise<ApiResponse<null>> {
  const res = await api.delete<ApiResponse<null>>(`/admin/push/groups/${id}`);
  return res.data;
}

export async function getGroupMembers(groupId: string): Promise<ApiResponse<GroupMember[]>> {
  const res = await api.get<ApiResponse<GroupMember[]>>(`/admin/push/groups/${groupId}/members`);
  return res.data;
}

export async function addGroupMember(groupId: string, userId: string): Promise<ApiResponse<null>> {
  const res = await api.post<ApiResponse<null>>(`/admin/push/groups/${groupId}/members`, { user_id: userId });
  return res.data;
}

export async function removeGroupMember(groupId: string, userId: string): Promise<ApiResponse<null>> {
  const res = await api.delete<ApiResponse<null>>(`/admin/push/groups/${groupId}/members/${userId}`);
  return res.data;
}
