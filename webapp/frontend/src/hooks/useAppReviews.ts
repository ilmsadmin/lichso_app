"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import * as appReviewService from "@/services/appReviewService";
import type { AppReviewListParams, UpdateAppReviewRequest } from "@/types/app-review";

const APP_REVIEWS_KEY = "app-reviews";

export function useAppReviews(params?: AppReviewListParams) {
  return useQuery({
    queryKey: [APP_REVIEWS_KEY, params],
    queryFn: () => appReviewService.getAppReviews(params),
  });
}

export function useAppReview(id: string) {
  return useQuery({
    queryKey: [APP_REVIEWS_KEY, id],
    queryFn: () => appReviewService.getAppReview(id),
    enabled: !!id,
  });
}

export function useUpdateAppReview(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateAppReviewRequest) => appReviewService.updateAppReview(id, payload),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã cập nhật trạng thái đánh giá");
        queryClient.invalidateQueries({ queryKey: [APP_REVIEWS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật đánh giá");
    },
  });
}
