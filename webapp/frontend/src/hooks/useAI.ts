"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState, useCallback, useRef } from "react";
import * as aiService from "@/services/aiService";
import type {
  HoroscopeAIRequest,
  AIChatCreateRequest,
  AIChatMessageRequest,
  AIArticleGenerateRequest,
  AIArticleQuickDraftRequest,
  AIArticleRefineRequest,
  AIBulkRewriteRequest,
  AIPromptTemplateRequest,
  AIStreamDelta,
} from "@/types/ai";
import { toast } from "sonner";

// ============================================
// Query Keys
// ============================================
const AI_QUOTA_KEY = "ai-horoscope-quota";
const AI_HOROSCOPE_HISTORY_KEY = "ai-horoscope-history";
const AI_CHAT_SESSIONS_KEY = "ai-chat-sessions";
const AI_CHAT_SESSION_KEY = "ai-chat-session";
const AI_STATS_KEY = "ai-stats";
const AI_LOGS_KEY = "ai-logs";
const AI_PROMPTS_KEY = "ai-prompts";

// ============================================
// Horoscope Hooks
// ============================================

/** Fetch current quota for the logged-in user / guest */
export function useHoroscopeQuota() {
  return useQuery({
    queryKey: [AI_QUOTA_KEY],
    queryFn: () => aiService.getHoroscopeQuota(),
    staleTime: 60_000,
  });
}

/** Fetch reading history (auth required) */
export function useHoroscopeHistory(page = 1, limit = 10) {
  return useQuery({
    queryKey: [AI_HOROSCOPE_HISTORY_KEY, page, limit],
    queryFn: () => aiService.getHoroscopeHistory(page, limit),
  });
}

/** Non-streaming horoscope read */
export function useReadHoroscope() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: HoroscopeAIRequest) => aiService.readHoroscope(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_QUOTA_KEY] });
      queryClient.invalidateQueries({ queryKey: [AI_HOROSCOPE_HISTORY_KEY] });
    },
    onError: (err: Error) => {
      toast.error(err.message || "Lỗi xem tử vi AI");
    },
  });
}

/**
 * useStreamHoroscope — manages SSE streaming state for AI horoscope
 * Returns { stream, isStreaming, result, error, startStream, reset }
 */
export function useStreamHoroscope() {
  const queryClient = useQueryClient();
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamText, setStreamText] = useState("");
  const [summary, setSummary] = useState<AIStreamDelta | null>(null);
  const [error, setError] = useState<string | null>(null);
  const abortRef = useRef<AbortController | null>(null);

  const startStream = useCallback(
    async (req: HoroscopeAIRequest) => {
      setIsStreaming(true);
      setStreamText("");
      setSummary(null);
      setError(null);

      try {
        await aiService.readHoroscopeStream(
          req,
          (delta) => setStreamText((prev) => prev + delta),
          (done) => {
            setSummary(done);
            setIsStreaming(false);
            queryClient.invalidateQueries({ queryKey: [AI_QUOTA_KEY] });
            queryClient.invalidateQueries({
              queryKey: [AI_HOROSCOPE_HISTORY_KEY],
            });
          },
          (err) => {
            setError(err);
            setIsStreaming(false);
            toast.error(err);
          }
        );
      } catch (e) {
        const msg = e instanceof Error ? e.message : "Lỗi kết nối";
        setError(msg);
        setIsStreaming(false);
        toast.error(msg);
      }
    },
    [queryClient]
  );

  const reset = useCallback(() => {
    abortRef.current?.abort();
    setIsStreaming(false);
    setStreamText("");
    setSummary(null);
    setError(null);
  }, []);

  return { isStreaming, streamText, summary, error, startStream, reset };
}

// ============================================
// Chat Hooks
// ============================================

/** List all active chat sessions for the user */
export function useChatSessions(page = 1, limit = 20) {
  return useQuery({
    queryKey: [AI_CHAT_SESSIONS_KEY, page, limit],
    queryFn: () => aiService.listChatSessions(page, limit),
  });
}

/** Fetch a single chat session with messages */
export function useChatSession(uuid: string) {
  return useQuery({
    queryKey: [AI_CHAT_SESSION_KEY, uuid],
    queryFn: () => aiService.getChatSession(uuid),
    enabled: !!uuid,
  });
}

/** Create a new chat session */
export function useCreateChatSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: AIChatCreateRequest) =>
      aiService.createChatSession(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_CHAT_SESSIONS_KEY] });
    },
    onError: () => toast.error("Lỗi tạo phiên chat"),
  });
}

/** Delete a chat session */
export function useDeleteChatSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (uuid: string) => aiService.deleteChatSession(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_CHAT_SESSIONS_KEY] });
      toast.success("Đã xoá phiên chat");
    },
    onError: () => toast.error("Lỗi xoá phiên chat"),
  });
}

/**
 * useStreamChat — SSE streaming for chat messages
 * Manages local message buffer + streaming state
 */
export function useStreamChat(sessionUUID: string) {
  const queryClient = useQueryClient();
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamText, setStreamText] = useState("");
  const [error, setError] = useState<string | null>(null);

  const sendMessage = useCallback(
    async (req: AIChatMessageRequest) => {
      setIsStreaming(true);
      setStreamText("");
      setError(null);

      try {
        await aiService.sendChatMessageStream(
          sessionUUID,
          req,
          (delta) => setStreamText((prev) => prev + delta),
          () => {
            setIsStreaming(false);
            queryClient.invalidateQueries({
              queryKey: [AI_CHAT_SESSION_KEY, sessionUUID],
            });
          },
          (err) => {
            setError(err);
            setIsStreaming(false);
            toast.error(err);
          }
        );
      } catch (e) {
        const msg = e instanceof Error ? e.message : "Lỗi kết nối";
        setError(msg);
        setIsStreaming(false);
        toast.error(msg);
      }
    },
    [sessionUUID, queryClient]
  );

  return { isStreaming, streamText, error, sendMessage };
}

// ============================================
// Admin — Article Generation Hooks
// ============================================

/** Non-streaming article generation */
export function useGenerateAIArticle() {
  return useMutation({
    mutationFn: (req: AIArticleGenerateRequest) =>
      aiService.generateAIArticle(req),
    onError: () => toast.error("Lỗi tạo bài viết AI"),
  });
}

/** Quick draft */
export function useQuickDraft() {
  return useMutation({
    mutationFn: (req: AIArticleQuickDraftRequest) =>
      aiService.quickDraftAIArticle(req),
    onError: (err: unknown) => {
      const msg =
        (err as { response?: { data?: { message?: string; errors?: Record<string, string> } } })
          ?.response?.data?.message ?? "Lỗi tạo nháp nhanh";
      toast.error(msg);
    },
  });
}

/** SSE article generation */
export function useStreamArticleGeneration() {
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamText, setStreamText] = useState("");
  const [draftResult, setDraftResult] = useState<AIStreamDelta | null>(null);
  const [error, setError] = useState<string | null>(null);

  const startGeneration = useCallback(async (req: AIArticleGenerateRequest) => {
    setIsStreaming(true);
    setStreamText("");
    setDraftResult(null);
    setError(null);

    try {
      await aiService.generateAIArticleStream(
        req,
        (delta) => setStreamText((prev) => prev + delta),
        (done) => {
          setDraftResult(done);
          setIsStreaming(false);
          toast.success("Đã tạo bài viết AI!");
        },
        (err) => {
          setError(err);
          setIsStreaming(false);
          toast.error(err);
        }
      );
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Lỗi kết nối";
      setError(msg);
      setIsStreaming(false);
    }
  }, []);

  const reset = useCallback(() => {
    setIsStreaming(false);
    setStreamText("");
    setDraftResult(null);
    setError(null);
  }, []);

  return { isStreaming, streamText, draftResult, error, startGeneration, reset };
}

/** Suggest article topics */
export function useSuggestTopics(categoryId?: string, model?: string) {
  return useQuery({
    queryKey: ["ai-topic-suggestions", categoryId, model],
    queryFn: () => aiService.suggestArticleTopics(categoryId, model),
    enabled: false, // only fetch on demand
  });
}

// ============================================
// Admin — Stats & Logs Hooks
// ============================================

export function useAIStats(days = 30) {
  return useQuery({
    queryKey: [AI_STATS_KEY, days],
    queryFn: () => aiService.getAIStats(days),
    staleTime: 60_000, // 1 min — was 5 min, prevents stale old-shape data
  });
}

export function useAILogs(params?: {
  type?: string;
  model?: string;
  from?: string;
  to?: string;
  page?: number;
  limit?: number;
}) {
  return useQuery({
    queryKey: [AI_LOGS_KEY, params],
    queryFn: () => aiService.getAILogs(params),
  });
}

// ============================================
// Admin — Prompt Template Hooks
// ============================================

export function usePromptTemplates(type?: string) {
  return useQuery({
    queryKey: [AI_PROMPTS_KEY, type],
    queryFn: () => aiService.listPromptTemplates(type),
  });
}

export function useCreatePromptTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: AIPromptTemplateRequest) =>
      aiService.createPromptTemplate(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_PROMPTS_KEY] });
      toast.success("Đã tạo prompt template");
    },
    onError: () => toast.error("Lỗi tạo prompt template"),
  });
}

export function useUpdatePromptTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, req }: { id: number; req: AIPromptTemplateRequest }) =>
      aiService.updatePromptTemplate(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_PROMPTS_KEY] });
      toast.success("Đã cập nhật prompt template");
    },
    onError: () => toast.error("Lỗi cập nhật"),
  });
}

export function useDeletePromptTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => aiService.deletePromptTemplate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_PROMPTS_KEY] });
      toast.success("Đã xoá prompt template");
    },
    onError: () => toast.error("Lỗi xoá"),
  });
}

// ============================================
// Admin — AI Article Queue Hooks
// ============================================

const AI_ARTICLES_KEY = "ai-articles";

/** Fetch articles by status: 'ai_pending' or 'draft' */
export function useAIArticles(status: string, page = 1, limit = 20) {
  return useQuery({
    queryKey: [AI_ARTICLES_KEY, status, page, limit],
    queryFn: () => aiService.listAIArticles(status, page, limit),
    staleTime: 30_000,
  });
}

/** Refine an ai_pending article with AI — moves it to 'draft' */
export function useRefineAIArticle() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, req }: { id: string; req: AIArticleRefineRequest }) =>
      aiService.refineAIArticle(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_ARTICLES_KEY] });
      toast.success("Đã viết chi tiết bài viết!");
    },
    onError: () => toast.error("Lỗi viết chi tiết bài viết"),
  });
}

/** Update article status (e.g., draft → published) */
export function useUpdateAIArticleStatus() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      aiService.updateAIArticleStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [AI_ARTICLES_KEY] });
      toast.success("Đã cập nhật trạng thái bài viết");
    },
    onError: () => toast.error("Lỗi cập nhật trạng thái"),
  });
}

// ============================================
// Admin — Low-Quality Articles Hooks
// ============================================

const AI_LOW_QUALITY_KEY = "ai-low-quality-articles";

/** Fetch low-quality articles (< maxWords words) with admin-level filters */
export function useLowQualityArticles(params: {
  max_words?: number;
  page?: number;
  limit?: number;
  status?: string;
  search?: string;
  category_id?: string;
} = {}) {
  return useQuery({
    queryKey: [AI_LOW_QUALITY_KEY, params],
    queryFn: () => aiService.listLowQualityArticles(params),
    staleTime: 30_000,
  });
}

/** Fetch count of low-quality articles */
export function useLowQualityCount(maxWords = 500, status?: string) {
  return useQuery({
    queryKey: [AI_LOW_QUALITY_KEY, "count", maxWords, status],
    queryFn: () => aiService.countLowQualityArticles(maxWords, status),
    staleTime: 30_000,
  });
}

/** Bulk rewrite low-quality articles with AI */
export function useBulkRewriteArticles() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: AIBulkRewriteRequest) =>
      aiService.bulkRewriteArticles(req),
    onSuccess: (data) => {
      const resp = data?.data;
      if (resp) {
        toast.success(`Đã viết lại ${resp.succeeded}/${resp.total_requested} bài viết`);
      }
      queryClient.invalidateQueries({ queryKey: [AI_LOW_QUALITY_KEY] });
      queryClient.invalidateQueries({ queryKey: [AI_ARTICLES_KEY] });
    },
    onError: () => toast.error("Lỗi viết lại bài viết hàng loạt"),
  });
}