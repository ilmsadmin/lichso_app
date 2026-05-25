"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as festivalService from "@/services/festivalService";
import type {
  CreateFolkFestivalRequest,
  UpdateFolkFestivalRequest,
  FolkFestivalListParams,
} from "@/types/festival";
import { toast } from "sonner";

// ============================================
// Folk Festival Hooks
// ============================================

const FESTIVALS_KEY = "folk-festivals";

/** Hook for fetching paginated folk festivals */
export function useFolkFestivals(params?: FolkFestivalListParams) {
  return useQuery({
    queryKey: [FESTIVALS_KEY, params],
    queryFn: () => festivalService.getFolkFestivals(params),
  });
}

/** Hook for fetching a single folk festival */
export function useFolkFestival(id: string) {
  return useQuery({
    queryKey: [FESTIVALS_KEY, id],
    queryFn: () => festivalService.getFolkFestival(id),
    enabled: !!id,
  });
}

/** Hook for creating a folk festival */
export function useCreateFolkFestival() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateFolkFestivalRequest) => festivalService.createFolkFestival(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo lễ hội dân gian thành công");
        queryClient.invalidateQueries({ queryKey: [FESTIVALS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo lễ hội dân gian");
    },
  });
}

/** Hook for updating a folk festival */
export function useUpdateFolkFestival(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateFolkFestivalRequest) => festivalService.updateFolkFestival(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật lễ hội dân gian thành công");
        queryClient.invalidateQueries({ queryKey: [FESTIVALS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật lễ hội dân gian");
    },
  });
}

/** Hook for deleting a folk festival */
export function useDeleteFolkFestival() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => festivalService.deleteFolkFestival(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa lễ hội dân gian thành công");
        queryClient.invalidateQueries({ queryKey: [FESTIVALS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa lễ hội dân gian");
    },
  });
}
