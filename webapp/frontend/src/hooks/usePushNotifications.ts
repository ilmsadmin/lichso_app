"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as pushService from "@/services/pushNotificationService";
import type { CampaignListParams, CreateCampaignRequest, UpdateCampaignRequest } from "@/types/push-notification";
import { toast } from "sonner";

const CAMPAIGNS_KEY = "push-campaigns";
const STATS_KEY = "push-stats";

export function usePushStats() {
  return useQuery({
    queryKey: [STATS_KEY],
    queryFn: () => pushService.getPushStats(),
    staleTime: 30_000,
  });
}

export function usePushCampaigns(params?: CampaignListParams) {
  return useQuery({
    queryKey: [CAMPAIGNS_KEY, params],
    queryFn: () => pushService.listCampaigns(params),
  });
}

export function usePushCampaign(id: string) {
  return useQuery({
    queryKey: [CAMPAIGNS_KEY, id],
    queryFn: () => pushService.getCampaign(id),
    enabled: !!id,
  });
}

export function useCreateCampaign() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCampaignRequest) => pushService.createCampaign(data),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Tạo chiến dịch thành công");
        queryClient.invalidateQueries({ queryKey: [CAMPAIGNS_KEY] });
      }
    },
    onError: () => toast.error("Không thể tạo chiến dịch"),
  });
}

export function useUpdateCampaign() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateCampaignRequest }) =>
      pushService.updateCampaign(id, data),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Cập nhật chiến dịch thành công");
        queryClient.invalidateQueries({ queryKey: [CAMPAIGNS_KEY] });
      }
    },
    onError: () => toast.error("Không thể cập nhật chiến dịch"),
  });
}

export function useDeleteCampaign() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => pushService.deleteCampaign(id),
    onSuccess: () => {
      toast.success("Đã xóa chiến dịch");
      queryClient.invalidateQueries({ queryKey: [CAMPAIGNS_KEY] });
    },
    onError: () => toast.error("Không thể xóa chiến dịch"),
  });
}

export function useSendCampaign() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => pushService.sendCampaign(id),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Đang gửi thông báo...");
        queryClient.invalidateQueries({ queryKey: [CAMPAIGNS_KEY] });
      }
    },
    onError: () => toast.error("Không thể gửi chiến dịch"),
  });
}

// ── Templates ──────────────────────────────────────────────────────────────

const TEMPLATES_KEY = "push-templates";
const GROUPS_KEY = "push-groups";

export function usePushTemplates() {
  return useQuery({
    queryKey: [TEMPLATES_KEY],
    queryFn: () => pushService.listTemplates(),
    staleTime: 60_000,
  });
}

export function useCreateTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: import("@/types/push-notification").CreateTemplateRequest) =>
      pushService.createTemplate(data),
    onSuccess: () => {
      toast.success("Đã tạo mẫu thông báo");
      queryClient.invalidateQueries({ queryKey: [TEMPLATES_KEY] });
    },
    onError: () => toast.error("Không thể tạo mẫu"),
  });
}

export function useUpdateTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: import("@/types/push-notification").CreateTemplateRequest }) =>
      pushService.updateTemplate(id, data),
    onSuccess: () => {
      toast.success("Đã cập nhật mẫu");
      queryClient.invalidateQueries({ queryKey: [TEMPLATES_KEY] });
    },
    onError: () => toast.error("Không thể cập nhật mẫu"),
  });
}

export function useDeleteTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => pushService.deleteTemplate(id),
    onSuccess: () => {
      toast.success("Đã xóa mẫu");
      queryClient.invalidateQueries({ queryKey: [TEMPLATES_KEY] });
    },
    onError: () => toast.error("Không thể xóa mẫu"),
  });
}

// ── User Groups ────────────────────────────────────────────────────────────

export function usePushGroups() {
  return useQuery({
    queryKey: [GROUPS_KEY],
    queryFn: () => pushService.listGroups(),
    staleTime: 30_000,
  });
}

export function useGroupMembers(groupId: string) {
  return useQuery({
    queryKey: [GROUPS_KEY, groupId, "members"],
    queryFn: () => pushService.getGroupMembers(groupId),
    enabled: !!groupId,
  });
}

export function useCreateGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string; description?: string }) => pushService.createGroup(data),
    onSuccess: () => {
      toast.success("Đã tạo nhóm");
      queryClient.invalidateQueries({ queryKey: [GROUPS_KEY] });
    },
    onError: () => toast.error("Không thể tạo nhóm"),
  });
}

export function useUpdateGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: { name: string; description?: string } }) =>
      pushService.updateGroup(id, data),
    onSuccess: () => {
      toast.success("Đã cập nhật nhóm");
      queryClient.invalidateQueries({ queryKey: [GROUPS_KEY] });
    },
    onError: () => toast.error("Không thể cập nhật nhóm"),
  });
}

export function useDeleteGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => pushService.deleteGroup(id),
    onSuccess: () => {
      toast.success("Đã xóa nhóm");
      queryClient.invalidateQueries({ queryKey: [GROUPS_KEY] });
    },
    onError: () => toast.error("Không thể xóa nhóm"),
  });
}

export function useAddGroupMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, userId }: { groupId: string; userId: string }) =>
      pushService.addGroupMember(groupId, userId),
    onSuccess: (_data, vars) => {
      toast.success("Đã thêm thành viên");
      queryClient.invalidateQueries({ queryKey: [GROUPS_KEY, vars.groupId, "members"] });
      queryClient.invalidateQueries({ queryKey: [GROUPS_KEY] });
    },
    onError: () => toast.error("Không thể thêm thành viên"),
  });
}

export function useRemoveGroupMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, userId }: { groupId: string; userId: string }) =>
      pushService.removeGroupMember(groupId, userId),
    onSuccess: (_data, vars) => {
      toast.success("Đã xóa thành viên");
      queryClient.invalidateQueries({ queryKey: [GROUPS_KEY, vars.groupId, "members"] });
      queryClient.invalidateQueries({ queryKey: [GROUPS_KEY] });
    },
    onError: () => toast.error("Không thể xóa thành viên"),
  });
}
