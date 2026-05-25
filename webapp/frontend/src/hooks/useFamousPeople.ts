"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as famousPersonService from "@/services/famousPersonService";
import type {
  CreateFamousPersonRequest,
  UpdateFamousPersonRequest,
  FamousPersonListParams,
} from "@/types/famousPerson";
import { toast } from "sonner";

// ============================================
// Famous Person Hooks
// ============================================

const FAMOUS_PEOPLE_KEY = "famous-people";

/** Hook for fetching paginated famous people */
export function useFamousPeople(params?: FamousPersonListParams) {
  return useQuery({
    queryKey: [FAMOUS_PEOPLE_KEY, params],
    queryFn: () => famousPersonService.getFamousPeople(params),
  });
}

/** Hook for fetching a single famous person */
export function useFamousPerson(id: string) {
  return useQuery({
    queryKey: [FAMOUS_PEOPLE_KEY, id],
    queryFn: () => famousPersonService.getFamousPerson(id),
    enabled: !!id,
  });
}

/** Hook for creating a famous person */
export function useCreateFamousPerson() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateFamousPersonRequest) => famousPersonService.createFamousPerson(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo nhân vật nổi tiếng thành công");
        queryClient.invalidateQueries({ queryKey: [FAMOUS_PEOPLE_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo nhân vật nổi tiếng");
    },
  });
}

/** Hook for updating a famous person */
export function useUpdateFamousPerson(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateFamousPersonRequest) =>
      famousPersonService.updateFamousPerson(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật nhân vật nổi tiếng thành công");
        queryClient.invalidateQueries({ queryKey: [FAMOUS_PEOPLE_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật nhân vật nổi tiếng");
    },
  });
}

/** Hook for deleting a famous person */
export function useDeleteFamousPerson() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => famousPersonService.deleteFamousPerson(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa nhân vật nổi tiếng thành công");
        queryClient.invalidateQueries({ queryKey: [FAMOUS_PEOPLE_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa nhân vật nổi tiếng");
    },
  });
}
