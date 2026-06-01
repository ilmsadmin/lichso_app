"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as surveyService from "@/services/surveyService";
import type {
  CreateSurveyRequest,
  UpdateSurveyRequest,
  SurveyListParams,
} from "@/types/survey";
import { toast } from "sonner";

const SURVEYS_KEY = "surveys";

/** Hook for fetching paginated surveys */
export function useSurveys(params?: SurveyListParams) {
  return useQuery({
    queryKey: [SURVEYS_KEY, params],
    queryFn: () => surveyService.getSurveys(params),
  });
}

/** Hook for fetching a single survey */
export function useSurvey(id: string) {
  return useQuery({
    queryKey: [SURVEYS_KEY, id],
    queryFn: () => surveyService.getSurvey(id),
    enabled: !!id,
  });
}

/** Hook for creating a survey */
export function useCreateSurvey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateSurveyRequest) => surveyService.createSurvey(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo khảo sát thành công");
        queryClient.invalidateQueries({ queryKey: [SURVEYS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo khảo sát");
    },
  });
}

/** Hook for updating a survey */
export function useUpdateSurvey(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateSurveyRequest) =>
      surveyService.updateSurvey(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật khảo sát thành công");
        queryClient.invalidateQueries({ queryKey: [SURVEYS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật khảo sát");
    },
  });
}

/** Hook for deleting a survey */
export function useDeleteSurvey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => surveyService.deleteSurvey(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa khảo sát thành công");
        queryClient.invalidateQueries({ queryKey: [SURVEYS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa khảo sát");
    },
  });
}

/** Hook for toggling survey active status */
export function useToggleSurvey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => surveyService.toggleSurvey(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã thay đổi trạng thái khảo sát");
        queryClient.invalidateQueries({ queryKey: [SURVEYS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể thay đổi trạng thái");
    },
  });
}

/** Hook for fetching survey statistics */
export function useSurveyStats(id: string) {
  return useQuery({
    queryKey: [SURVEYS_KEY, id, "stats"],
    queryFn: () => surveyService.getSurveyStats(id),
    enabled: !!id,
  });
}
