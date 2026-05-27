"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as bannerService from "@/services/bannerService";
import type {
  CreateBannerRequest,
  UpdateBannerRequest,
  BannerListParams,
  BannerOrder,
} from "@/types/banner";
import { toast } from "sonner";

// ============================================
// Banner Hooks
// ============================================

const BANNERS_KEY = "banners";

/** Hook for fetching paginated banners */
export function useBanners(params?: BannerListParams) {
  return useQuery({
    queryKey: [BANNERS_KEY, params],
    queryFn: () => bannerService.getBanners(params),
  });
}

/** Hook for fetching a single banner */
export function useBanner(id: string) {
  return useQuery({
    queryKey: [BANNERS_KEY, id],
    queryFn: () => bannerService.getBanner(id),
    enabled: !!id,
  });
}

/** Hook for creating a banner */
export function useCreateBanner() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateBannerRequest) => bannerService.createBanner(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo banner thành công");
        queryClient.invalidateQueries({ queryKey: [BANNERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo banner");
    },
  });
}

/** Hook for updating a banner */
export function useUpdateBanner(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateBannerRequest) =>
      bannerService.updateBanner(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật banner thành công");
        queryClient.invalidateQueries({ queryKey: [BANNERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật banner");
    },
  });
}

/** Hook for deleting a banner */
export function useDeleteBanner() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => bannerService.deleteBanner(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa banner thành công");
        queryClient.invalidateQueries({ queryKey: [BANNERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa banner");
    },
  });
}

/** Hook for toggling a banner's active status */
export function useToggleBanner() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => bannerService.toggleBanner(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã thay đổi trạng thái banner");
        queryClient.invalidateQueries({ queryKey: [BANNERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể thay đổi trạng thái");
    },
  });
}

/** Hook for reordering banners */
export function useReorderBanners() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (orders: BannerOrder[]) => bannerService.reorderBanners(orders),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã sắp xếp lại banner");
        queryClient.invalidateQueries({ queryKey: [BANNERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể sắp xếp lại");
    },
  });
}
