"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as popupService from "@/services/popupService";
import type {
  CreatePopupRequest,
  UpdatePopupRequest,
  PopupListParams,
} from "@/types/popup";
import { toast } from "sonner";

// ============================================
// Popup Hooks
// ============================================

const POPUPS_KEY = "popups";

/** Hook for fetching paginated popups */
export function usePopups(params?: PopupListParams) {
  return useQuery({
    queryKey: [POPUPS_KEY, params],
    queryFn: () => popupService.getPopups(params),
  });
}

/** Hook for fetching a single popup */
export function usePopup(id: string) {
  return useQuery({
    queryKey: [POPUPS_KEY, id],
    queryFn: () => popupService.getPopup(id),
    enabled: !!id,
  });
}

/** Hook for creating a popup */
export function useCreatePopup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreatePopupRequest) => popupService.createPopup(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo popup thành công");
        queryClient.invalidateQueries({ queryKey: [POPUPS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo popup");
    },
  });
}

/** Hook for updating a popup */
export function useUpdatePopup(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdatePopupRequest) =>
      popupService.updatePopup(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật popup thành công");
        queryClient.invalidateQueries({ queryKey: [POPUPS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật popup");
    },
  });
}

/** Hook for deleting a popup */
export function useDeletePopup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => popupService.deletePopup(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa popup thành công");
        queryClient.invalidateQueries({ queryKey: [POPUPS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa popup");
    },
  });
}

/** Hook for toggling a popup's active status */
export function useTogglePopup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => popupService.togglePopup(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã thay đổi trạng thái popup");
        queryClient.invalidateQueries({ queryKey: [POPUPS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể thay đổi trạng thái");
    },
  });
}
