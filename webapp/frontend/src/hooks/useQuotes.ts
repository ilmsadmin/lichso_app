"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as quoteService from "@/services/quoteService";
import type { CreateQuoteRequest, UpdateQuoteRequest, QuoteListParams } from "@/types/quote";
import { toast } from "sonner";

// ============================================
// Quote Hooks
// ============================================

const QUOTES_KEY = "quotes";

/** Hook for fetching paginated quotes */
export function useQuotes(params?: QuoteListParams) {
  return useQuery({
    queryKey: [QUOTES_KEY, params],
    queryFn: () => quoteService.getQuotes(params),
  });
}

/** Hook for fetching a single quote */
export function useQuote(id: string) {
  return useQuery({
    queryKey: [QUOTES_KEY, id],
    queryFn: () => quoteService.getQuote(id),
    enabled: !!id,
  });
}

/** Hook for creating a quote */
export function useCreateQuote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateQuoteRequest) => quoteService.createQuote(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo danh ngôn thành công");
        queryClient.invalidateQueries({ queryKey: [QUOTES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo danh ngôn");
    },
  });
}

/** Hook for updating a quote */
export function useUpdateQuote(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateQuoteRequest) => quoteService.updateQuote(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật danh ngôn thành công");
        queryClient.invalidateQueries({ queryKey: [QUOTES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật danh ngôn");
    },
  });
}

/** Hook for deleting a quote */
export function useDeleteQuote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => quoteService.deleteQuote(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa danh ngôn thành công");
        queryClient.invalidateQueries({ queryKey: [QUOTES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa danh ngôn");
    },
  });
}
