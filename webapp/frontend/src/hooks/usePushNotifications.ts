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
