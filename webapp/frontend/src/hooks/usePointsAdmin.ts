"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as pointsAdminService from "@/services/pointsAdminService";
import type { AdminPointsListParams, AdjustUserPointsRequest } from "@/types/points";
import { toast } from "sonner";

const LIST_KEY = "admin-points-list";
const DETAIL_KEY = "admin-points-detail";
const DAILY_KEY = "admin-points-daily";

export function useUserPointsList(params?: AdminPointsListParams) {
  return useQuery({
    queryKey: [LIST_KEY, params],
    queryFn: () => pointsAdminService.getUserPointsList(params),
  });
}

export function useUserPointsDetail(userId: string) {
  return useQuery({
    queryKey: [DETAIL_KEY, userId],
    queryFn: () => pointsAdminService.getUserPointsDetail(userId),
    enabled: !!userId,
  });
}

export function useUserDailyPoints(userId: string, days = 30) {
  return useQuery({
    queryKey: [DAILY_KEY, userId, days],
    queryFn: () => pointsAdminService.getUserDailyPoints(userId, days),
    enabled: !!userId,
  });
}

export function useAdjustUserPoints(userId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: AdjustUserPointsRequest) =>
      pointsAdminService.adjustUserPoints(userId, data),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Đã cập nhật điểm người dùng");
        queryClient.invalidateQueries({ queryKey: [DETAIL_KEY, userId] });
        queryClient.invalidateQueries({ queryKey: [DAILY_KEY, userId] });
        queryClient.invalidateQueries({ queryKey: [LIST_KEY] });
      } else {
        toast.error(res.message || "Không thể cập nhật điểm");
      }
    },
    onError: () => toast.error("Không thể cập nhật điểm người dùng"),
  });
}
