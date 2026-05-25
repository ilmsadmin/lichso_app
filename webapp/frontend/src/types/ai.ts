// ============================================
// AI Types — Phase 4 (v4.0)
// Mirrors backend dto/ai_dto.go
// ============================================

// ============================================
// Horoscope AI
// ============================================

export type HoroscopeReadingType =
  | "overview"
  | "yearly"
  | "monthly"
  | "question"
  | "compatibility"
  | "choose_date";

export type HoroscopeDepth = "brief" | "standard" | "detailed";

export interface HoroscopeAIRequest {
  birth_year: number;
  birth_month: number;
  birth_day: number;
  birth_hour?: number;
  gender: "male" | "female";
  reading_type: HoroscopeReadingType;
  target_year?: number;
  target_month?: number;
  question?: string;
  partner_birth_year?: number;
  depth?: HoroscopeDepth;
  stream?: boolean;
}

export interface BatTuPillar {
  heavenly_stem: string;
  earthly_branch: string;
  element: string;
}

export interface BatTuInfo {
  year_pillar: BatTuPillar;
  month_pillar: BatTuPillar;
  day_pillar: BatTuPillar;
  hour_pillar: BatTuPillar;
}

export interface NguHanhBalance {
  Kim: number;
  Moc: number;
  Thuy: number;
  Hoa: number;
  Tho: number;
  strongest: string;
  weakest: string;
}

export interface HoroscopeAIResponse {
  session_id: number;
  bat_tu: BatTuInfo;
  ngu_hanh_balance: NguHanhBalance;
  ai_result: string;
  model_used: string;
  tokens_used: number;
  quota_remaining: number;
}

export interface AIUsageQuotaResponse {
  quota_type: string;
  used: number;
  limit: number;
  remaining: number;
  reset_at: string;
}

export interface AIHoroscopeSession {
  id: number;
  birth_year: number;
  birth_month: number;
  birth_day: number;
  birth_hour?: number;
  gender: string;
  reading_type: string;
  depth: string;
  ai_result: string;
  model_used: string;
  tokens_used: number;
  created_at: string;
}

// ============================================
// AI Article
// ============================================

export type ArticleTargetLength = "short" | "medium" | "long";
export type ArticleWritingStyle =
  | "academic"
  | "popular"
  | "storytelling"
  | "listicle";

export interface AIArticleGenerateRequest {
  topic: string;
  category_id?: string; // UUID
  tags?: string[];
  target_length: ArticleTargetLength;
  writing_style: ArticleWritingStyle;
  target_keyword?: string;
  generate_seo?: boolean;
  model?: string;
  lunar_context?: boolean;
  ref_date?: string;
  prompt_id?: number;
}

export interface AIArticleQuickDraftRequest {
  topic: string;
  category_id?: string; // UUID
}

export interface AIArticleDraftResponse {
  article_id: string;  // UUID
  title: string;
  excerpt: string;
  meta_title?: string;
  meta_description?: string;
  slug: string;
  suggested_tags: string[];
  reading_time: number;
  status: string;
  ai_model: string;
  tokens_used: number;
  cost_usd: number;
}

// Article item returned from the pending queue / draft list
export interface AIArticleItem {
  id: string;           // UUID
  title: string;
  excerpt: string;
  slug: string;
  status: string;
  reading_time: number;
  created_at: string;
  updated_at: string;
  ai_model?: string;
  tokens_used?: number;
  cost_usd?: number;
  category?: { id: number; name: string; slug: string };
}

export interface AIArticleListResponse {
  data: AIArticleItem[];
  total: number;
  page: number;
  page_size: number;
}

export interface AIArticleRefineRequest {
  instruction?: string;
  target_length?: "short" | "medium" | "long";
  writing_style?: "academic" | "popular" | "storytelling" | "listicle";
  model?: string;
  prompt_id?: number;
}

// Low-quality article (< 500 words) types
export interface AILowQualityArticle {
  article_id: string;
  title: string;
  slug: string;
  excerpt: string;
  word_count: number;
  reading_time: number;
  status: string;
  category_name?: string;
  created_at: string;
}

export interface AIBulkRewriteRequest {
  article_ids: string[];
  target_length?: "short" | "medium" | "long";
  writing_style?: "academic" | "popular" | "storytelling" | "listicle";
  model?: string;
  prompt_id?: number;
}

export interface AIBulkRewriteItemResult {
  article_id: string;
  title: string;
  slug?: string;
  status: "success" | "error";
  new_word_count?: number;
  tokens_used?: number;
  cost_usd?: number;
  error?: string;
}

export interface AIBulkRewriteResponse {
  total_requested: number;
  succeeded: number;
  failed: number;
  results: AIBulkRewriteItemResult[];
}

// ============================================
// AI Chat
// ============================================

export interface AIChatCreateRequest {
  title?: string;
  context?: Record<string, unknown>;
}

export interface AIChatMessageRequest {
  content: string;
  stream?: boolean;
}

export interface AIChatMessageResponse {
  role: "user" | "assistant";
  content: string;
  created_at: string;
  tokens_used?: number;
}

export interface AIChatSessionResponse {
  session_uuid: string;
  title: string;
  messages: AIChatMessageResponse[];
  total_tokens: number;
  total_cost: number;
  last_message_at?: string;
  created_at: string;
}

// ============================================
// Admin — Logs
// ============================================

export interface AILogEntry {
  id: number;
  user_id?: string;
  generation_type: string;
  model_used: string;
  prompt_tokens: number;
  completion_tokens: number;
  total_tokens: number;
  cost_usd: number;
  duration_ms: number;
  status: string;
  error_message?: string;
  created_at: string;
}

// ============================================
// Admin — Stats
// ============================================

export interface AIStatsByDay {
  date: string;
  requests: number;
  tokens: number;
  cost_usd: number;
}

export interface AIStatsByModel {
  model: string;
  requests: number;
  tokens: number;
  cost_usd: number;
}

export interface AIStatsResponse {
  total_requests: number;
  total_tokens: number;
  total_cost_usd: number;
  articles_generated: number;
  horoscopes_read: number;
  chat_messages: number;
  cost_by_day: AIStatsByDay[];
  requests_by_model: AIStatsByModel[];
}

export interface AIPromptTemplate {
  id: number;
  name: string;
  type: "article" | "horoscope" | "chat";
  system_prompt: string;
  user_prompt: string;
  model?: string;
  max_tokens: number;
  temperature: number;
  is_active: boolean;
  created_at: string;
  updated_at: string;
}

export interface AIPromptTemplateRequest {
  name: string;
  type: "article" | "horoscope" | "chat";
  system_prompt: string;
  user_prompt: string;
  model?: string;
  max_tokens?: number;
  temperature?: number;
  is_active: boolean;
}

// ============================================
// SSE streaming helpers
// ============================================

export interface AIStreamDelta {
  delta?: string;
  error?: string;
  done?: boolean;
  session_id?: number;
  quota_remaining?: number;
  tokens_used?: number;
  bat_tu?: BatTuInfo;
  ngu_hanh?: NguHanhBalance;
  result?: AIArticleDraftResponse;
}
