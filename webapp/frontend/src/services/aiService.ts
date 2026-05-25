import api from "@/lib/api";
import axios from "axios";
import Cookies from "js-cookie";
import { ACCESS_TOKEN_KEY, API_URL } from "@/lib/constants";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  HoroscopeAIRequest,
  HoroscopeAIResponse,
  AIUsageQuotaResponse,
  AIHoroscopeSession,
  AIArticleGenerateRequest,
  AIArticleQuickDraftRequest,
  AIArticleDraftResponse,
  AIArticleListResponse,
  AIArticleRefineRequest,
  AILowQualityArticle,
  AIBulkRewriteRequest,
  AIBulkRewriteResponse,
  AIChatCreateRequest,
  AIChatMessageRequest,
  AIChatMessageResponse,
  AIChatSessionResponse,
  AIStatsResponse,
  AILogEntry,
  AIPromptTemplate,
  AIPromptTemplateRequest,
  AIStreamDelta,
} from "@/types/ai";

// ============================================
// SSE Streaming helper
// ============================================

/**
 * Open an SSE connection to a POST endpoint, calling onChunk for each
 * streamed delta and onDone when the [DONE] signal arrives.
 */
export async function streamPost<T>(
  url: string,
  body: unknown,
  onChunk: (delta: string) => void,
  onDone?: (summary: AIStreamDelta) => void,
  onError?: (error: string) => void
): Promise<void> {
  // Helper that performs the actual fetch with a given token
  const doFetch = (token: string) =>
    fetch(`${API_URL}${url}?stream=true`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });

  let response = await doFetch(getToken());

  // If 401, try to refresh via axios (which has the full refresh logic) then retry once
  if (response.status === 401) {
    try {
      const refreshTokenKey = process.env.NEXT_PUBLIC_REFRESH_TOKEN_KEY || "zplus_refresh_token";
      const refreshToken = Cookies.get(refreshTokenKey);
      if (refreshToken) {
        const { data } = await axios.post(
          `${API_URL}/auth/refresh`,
          { refresh_token: refreshToken }
        );
        const accessTokenKey = process.env.NEXT_PUBLIC_ACCESS_TOKEN_KEY || "zplus_access_token";
        Cookies.set(accessTokenKey, data.data.access_token, {
          secure: process.env.NODE_ENV === "production",
          sameSite: "lax",
        });
        // Retry with fresh token
        response = await doFetch(data.data.access_token);
      }
    } catch {
      // refresh failed — fall through to the error below
    }
  }


  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Stream request failed: ${response.status} ${text}`);
  }

  const reader = response.body?.getReader();
  if (!reader) throw new Error("No response body");

  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split("\n");
    buffer = lines.pop() ?? "";

    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed.startsWith("data: ")) continue;

      const data = trimmed.slice(6);
      if (data === "[DONE]") break;

      try {
        const chunk = JSON.parse(data) as AIStreamDelta;
        if (chunk.error) {
          onError?.(chunk.error);
          return;
        }
        if (chunk.done) {
          onDone?.(chunk);
          return;
        }
        if (chunk.delta) {
          onChunk(chunk.delta);
        }
      } catch {
        // skip malformed lines
      }
    }
  }
}

/** Retrieve JWT token from cookies (same store as axios interceptor) */
function getToken(): string {
  if (typeof window === "undefined") return "";
  return Cookies.get(ACCESS_TOKEN_KEY) ?? "";
}

// ============================================
// AI Horoscope
// ============================================

/** POST /api/ai/horoscope/read — non-streaming */
export async function readHoroscope(
  req: HoroscopeAIRequest
): Promise<ApiResponse<HoroscopeAIResponse>> {
  const res = await api.post<ApiResponse<HoroscopeAIResponse>>(
    "/ai/horoscope/read",
    { ...req, stream: false }
  );
  return res.data;
}

/** POST /api/ai/horoscope/read — SSE streaming */
export async function readHoroscopeStream(
  req: HoroscopeAIRequest,
  onChunk: (delta: string) => void,
  onDone?: (summary: AIStreamDelta) => void,
  onError?: (error: string) => void
): Promise<void> {
  return streamPost("/ai/horoscope/read", req, onChunk, onDone, onError);
}

/** GET /api/ai/horoscope/quota */
export async function getHoroscopeQuota(): Promise<
  ApiResponse<AIUsageQuotaResponse>
> {
  const res = await api.get<ApiResponse<AIUsageQuotaResponse>>(
    "/ai/horoscope/quota"
  );
  return res.data;
}

/** GET /api/ai/horoscope/history */
export async function getHoroscopeHistory(
  page = 1,
  limit = 10
): Promise<ApiResponse<PaginatedResponse<AIHoroscopeSession>>> {
  const res = await api.get<ApiResponse<PaginatedResponse<AIHoroscopeSession>>>(
    "/ai/horoscope/history",
    { params: { page, limit } }
  );
  return res.data;
}

// ============================================
// AI Chat
// ============================================

/** POST /api/ai/chat/sessions */
export async function createChatSession(
  req: AIChatCreateRequest
): Promise<ApiResponse<AIChatSessionResponse>> {
  const res = await api.post<ApiResponse<AIChatSessionResponse>>(
    "/ai/chat/sessions",
    req
  );
  return res.data;
}

/** GET /api/ai/chat/sessions */
export async function listChatSessions(
  page = 1,
  limit = 20
): Promise<ApiResponse<{ data: AIChatSessionResponse[]; total: number }>> {
  const res = await api.get<
    ApiResponse<{ data: AIChatSessionResponse[]; total: number }>
  >("/ai/chat/sessions", { params: { page, limit } });
  return res.data;
}

/** GET /api/ai/chat/sessions/:uuid */
export async function getChatSession(
  uuid: string
): Promise<ApiResponse<AIChatSessionResponse>> {
  const res = await api.get<ApiResponse<AIChatSessionResponse>>(
    `/ai/chat/sessions/${uuid}`
  );
  return res.data;
}

/** DELETE /api/ai/chat/sessions/:uuid */
export async function deleteChatSession(
  uuid: string
): Promise<ApiResponse<null>> {
  const res = await api.delete<ApiResponse<null>>(
    `/ai/chat/sessions/${uuid}`
  );
  return res.data;
}

/** POST /api/ai/chat/sessions/:uuid/messages — non-streaming */
export async function sendChatMessage(
  uuid: string,
  req: AIChatMessageRequest
): Promise<ApiResponse<AIChatMessageResponse>> {
  const res = await api.post<ApiResponse<AIChatMessageResponse>>(
    `/ai/chat/sessions/${uuid}/messages`,
    { ...req, stream: false }
  );
  return res.data;
}

/** POST /api/ai/chat/sessions/:uuid/messages — SSE streaming */
export async function sendChatMessageStream(
  uuid: string,
  req: AIChatMessageRequest,
  onChunk: (delta: string) => void,
  onDone?: (summary: AIStreamDelta) => void,
  onError?: (error: string) => void
): Promise<void> {
  return streamPost(
    `/ai/chat/sessions/${uuid}/messages`,
    req,
    onChunk,
    onDone,
    onError
  );
}

// ============================================
// Admin — AI Article Generation
// ============================================

/** POST /api/admin/ai/articles/generate — non-streaming */
export async function generateAIArticle(
  req: AIArticleGenerateRequest
): Promise<ApiResponse<AIArticleDraftResponse>> {
  const res = await api.post<ApiResponse<AIArticleDraftResponse>>(
    "/admin/ai/articles/generate",
    req
  );
  return res.data;
}

/** POST /api/admin/ai/articles/generate — SSE streaming */
export async function generateAIArticleStream(
  req: AIArticleGenerateRequest,
  onChunk: (delta: string) => void,
  onDone?: (summary: AIStreamDelta) => void,
  onError?: (error: string) => void
): Promise<void> {
  return streamPost(
    "/admin/ai/articles/generate",
    req,
    onChunk,
    onDone,
    onError
  );
}

/** POST /api/admin/ai/articles/quick-draft */
export async function quickDraftAIArticle(
  req: AIArticleQuickDraftRequest
): Promise<ApiResponse<AIArticleDraftResponse>> {
  const res = await api.post<ApiResponse<AIArticleDraftResponse>>(
    "/admin/ai/articles/quick-draft",
    req,
    { timeout: 120000 } // AI generation (~60s) + image search (~30s) + buffer
  );
  return res.data;
}

/** GET /api/admin/ai/articles/topics */
export async function suggestArticleTopics(
  categoryId?: string,
  model?: string
): Promise<ApiResponse<{ topics: string[] }>> {
  const res = await api.get<ApiResponse<{ topics: string[] }>>(
    "/admin/ai/articles/topics",
    {
      params: { category_id: categoryId, model },
      timeout: 60000, // AI call có thể mất 30-60s
    }
  );
  return res.data;
}

// ============================================
// Admin — Stats & Logs
// ============================================

/** GET /api/admin/ai/stats */
export async function getAIStats(
  days = 30
): Promise<ApiResponse<AIStatsResponse>> {
  const res = await api.get<ApiResponse<AIStatsResponse>>(
    "/admin/ai/stats",
    { params: { days } }
  );
  return res.data;
}

/** GET /api/admin/ai/logs */
export async function getAILogs(params?: {
  type?: string;
  model?: string;
  from?: string;
  to?: string;
  page?: number;
  limit?: number;
}): Promise<ApiResponse<{ data: AILogEntry[]; total: number; page: number; limit: number }>> {
  const res = await api.get<ApiResponse<{ data: AILogEntry[]; total: number; page: number; limit: number }>>(
    "/admin/ai/logs",
    { params }
  );
  return res.data;
}

// ============================================
// Admin — Prompt Templates
// ============================================

/** GET /api/admin/ai/prompts */
export async function listPromptTemplates(
  type?: string
): Promise<ApiResponse<AIPromptTemplate[]>> {
  const res = await api.get<ApiResponse<AIPromptTemplate[]>>(
    "/admin/ai/prompts",
    { params: { type } }
  );
  return res.data;
}

/** GET /api/admin/ai/prompts/:id */
export async function getPromptTemplate(
  id: number
): Promise<ApiResponse<AIPromptTemplate>> {
  const res = await api.get<ApiResponse<AIPromptTemplate>>(
    `/admin/ai/prompts/${id}`
  );
  return res.data;
}

/** POST /api/admin/ai/prompts */
export async function createPromptTemplate(
  req: AIPromptTemplateRequest
): Promise<ApiResponse<AIPromptTemplate>> {
  const res = await api.post<ApiResponse<AIPromptTemplate>>(
    "/admin/ai/prompts",
    req
  );
  return res.data;
}

/** PUT /api/admin/ai/prompts/:id */
export async function updatePromptTemplate(
  id: number,
  req: AIPromptTemplateRequest
): Promise<ApiResponse<AIPromptTemplate>> {
  const res = await api.put<ApiResponse<AIPromptTemplate>>(
    `/admin/ai/prompts/${id}`,
    req
  );
  return res.data;
}

/** DELETE /api/admin/ai/prompts/:id */
export async function deletePromptTemplate(
  id: number
): Promise<ApiResponse<null>> {
  const res = await api.delete<ApiResponse<null>>(
    `/admin/ai/prompts/${id}`
  );
  return res.data;
}

// ============================================
// Admin — Test Connection
// ============================================

export interface AITestConnectionResult {
  model: string;
  reply: string;
  latency_ms: number;
}

/** POST /api/admin/ai/test-connection */
export async function testAIConnection(params: {
  api_key: string;
  base_url?: string;
  model?: string;
}): Promise<ApiResponse<AITestConnectionResult>> {
  const res = await api.post<ApiResponse<AITestConnectionResult>>(
    "/admin/ai/test-connection",
    params
  );
  return res.data;
}

// ============================================
// Admin — AI Article Queue
// ============================================

/** GET /api/admin/ai/articles?status=ai_pending|draft&page=1&limit=20 */
export async function listAIArticles(
  status: string,
  page = 1,
  limit = 20
): Promise<ApiResponse<AIArticleListResponse>> {
  const res = await api.get<ApiResponse<AIArticleListResponse>>(
    "/admin/ai/articles",
    { params: { status, page, limit } }
  );
  return res.data;
}

/** POST /api/admin/ai/articles/:id/refine */
export async function refineAIArticle(
  id: string,
  req: AIArticleRefineRequest
): Promise<ApiResponse<AIArticleDraftResponse>> {
  const res = await api.post<ApiResponse<AIArticleDraftResponse>>(
    `/admin/ai/articles/${id}/refine`,
    req,
    { timeout: 120000 } // refine gọi AI, có thể mất đến 2 phút
  );
  return res.data;
}

/** PATCH /api/admin/ai/articles/:id/status */
export async function updateAIArticleStatus(
  id: string,
  status: string
): Promise<ApiResponse<null>> {
  const res = await api.patch<ApiResponse<null>>(
    `/admin/ai/articles/${id}/status`,
    { status }
  );
  return res.data;
}

// ============================================
// Admin — Low-Quality Articles
// ============================================

/** GET /api/admin/ai/articles/low-quality?max_words=500&page=1&limit=20&status=&search=&category_id= */
export async function listLowQualityArticles(
  params: {
    max_words?: number;
    page?: number;
    limit?: number;
    status?: string;
    search?: string;
    category_id?: string;
  } = {}
): Promise<ApiResponse<{ data: AILowQualityArticle[]; total: number; page: number; limit: number; max_words: number }>> {
  const res = await api.get<ApiResponse<{ data: AILowQualityArticle[]; total: number; page: number; limit: number; max_words: number }>>(
    "/admin/ai/articles/low-quality",
    { params: { max_words: params.max_words ?? 500, page: params.page ?? 1, limit: params.limit ?? 20, status: params.status, search: params.search, category_id: params.category_id } }
  );
  return res.data;
}

/** GET /api/admin/ai/articles/low-quality/count?max_words=500&status= */
export async function countLowQualityArticles(
  maxWords = 500,
  status?: string
): Promise<ApiResponse<{ count: number; max_words: number }>> {
  const res = await api.get<ApiResponse<{ count: number; max_words: number }>>(
    "/admin/ai/articles/low-quality/count",
    { params: { max_words: maxWords, status } }
  );
  return res.data;
}

/** POST /api/admin/ai/articles/bulk-rewrite */
export async function bulkRewriteArticles(
  req: AIBulkRewriteRequest
): Promise<ApiResponse<AIBulkRewriteResponse>> {
  const res = await api.post<ApiResponse<AIBulkRewriteResponse>>(
    "/admin/ai/articles/bulk-rewrite",
    req,
    { timeout: 600000 } // 10 minutes — bulk AI calls can be slow
  );
  return res.data;
}

