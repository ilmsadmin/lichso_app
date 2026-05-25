package dto

// ============================================
// AI Horoscope DTOs
// ============================================

// HoroscopeAIRequest is the payload for POST /api/v4/ai/horoscope/read
type HoroscopeAIRequest struct {
	BirthYear        int    `json:"birth_year"   validate:"required,min=1900,max=2100"`
	BirthMonth       int    `json:"birth_month"  validate:"required,min=1,max=12"`
	BirthDay         int    `json:"birth_day"    validate:"required,min=1,max=31"`
	BirthHour        *int   `json:"birth_hour"   validate:"omitempty,min=0,max=23"`
	Gender           string `json:"gender"       validate:"required,oneof=male female"`
	ReadingType      string `json:"reading_type" validate:"required,oneof=overview yearly monthly question compatibility choose_date"`
	TargetYear       *int   `json:"target_year"`
	TargetMonth      *int   `json:"target_month"`
	Question         string `json:"question"     validate:"omitempty,max=500"`
	PartnerBirthYear *int   `json:"partner_birth_year"`
	Depth            string `json:"depth"        validate:"omitempty,oneof=brief standard detailed"`
	Stream           bool   `json:"stream"` // if true caller expects SSE, handled via header
}

// BatTuPillar is a single trụ (pillar) of the four pillars
type BatTuPillar struct {
	HeavenlyStem  string `json:"heavenly_stem"`  // Thiên Can
	EarthlyBranch string `json:"earthly_branch"` // Địa Chi
	Element       string `json:"element"`        // Ngũ Hành hành chủ đạo
}

// BatTuInfo holds all four pillars
type BatTuInfo struct {
	YearPillar  BatTuPillar `json:"year_pillar"`
	MonthPillar BatTuPillar `json:"month_pillar"`
	DayPillar   BatTuPillar `json:"day_pillar"`
	HourPillar  BatTuPillar `json:"hour_pillar"`
}

// NguHanhBalance shows the count of each element
type NguHanhBalance struct {
	Kim       int    `json:"Kim"`
	Moc       int    `json:"Moc"`
	Thuy      int    `json:"Thuy"`
	Hoa       int    `json:"Hoa"`
	Tho       int    `json:"Tho"`
	Strongest string `json:"strongest"`
	Weakest   string `json:"weakest"`
}

// HoroscopeAIResponse is returned from POST /api/v4/ai/horoscope/read (non-streaming)
type HoroscopeAIResponse struct {
	SessionID      uint64         `json:"session_id"`
	BatTu          BatTuInfo      `json:"bat_tu"`
	NguHanhBalance NguHanhBalance `json:"ngu_hanh_balance"`
	AIResult       string         `json:"ai_result"`
	ModelUsed      string         `json:"model_used"`
	TokensUsed     int            `json:"tokens_used"`
	QuotaRemaining int            `json:"quota_remaining"`
}

// ============================================
// AI Article DTOs
// ============================================

// AIArticleGenerateRequest is the payload for POST /api/v4/ai/articles/generate
type AIArticleGenerateRequest struct {
	Topic         string   `json:"topic"          validate:"required,min=5,max=500"`
	CategoryID    string   `json:"category_id"    validate:"omitempty,uuid"`
	Tags          []string `json:"tags"`
	TargetLength  string   `json:"target_length"  validate:"required,oneof=short medium long"`
	WritingStyle  string   `json:"writing_style"  validate:"required,oneof=academic popular storytelling listicle"`
	TargetKeyword string   `json:"target_keyword" validate:"omitempty,max=100"`
	GenerateSEO   bool     `json:"generate_seo"`
	Model         string   `json:"model"`
	LunarContext  bool     `json:"lunar_context"`
	RefDate       string   `json:"ref_date"`  // YYYY-MM-DD
	PromptID      int      `json:"prompt_id"` // optional: use specific prompt template
}

// AIArticleQuickDraftRequest is for POST /api/v4/ai/articles/quick-draft
type AIArticleQuickDraftRequest struct {
	Topic      string `json:"topic"    validate:"required,min=5,max=500"`
	CategoryID string `json:"category_id" validate:"omitempty,uuid"`
}

// AIArticleReviewRequest is for PATCH /api/v4/ai/articles/drafts/:id/review
type AIArticleReviewRequest struct {
	Action  string `json:"action"  validate:"required,oneof=approve reject"`
	Comment string `json:"comment" validate:"omitempty,max=1000"`
}

// AIArticleStatusRequest changes the status of an AI-generated article
type AIArticleStatusRequest struct {
	Status string `json:"status" validate:"required,oneof=ai_pending draft review published"`
}

// AIArticleRefineRequest is for POST /admin/ai/articles/:id/refine
type AIArticleRefineRequest struct {
	Instruction  string `json:"instruction" validate:"omitempty,max=1000"` // optional custom instruction
	TargetLength string `json:"target_length" validate:"omitempty,oneof=short medium long"`
	WritingStyle string `json:"writing_style" validate:"omitempty,oneof=academic popular storytelling listicle"`
	Model        string `json:"model"`
	PromptID     int    `json:"prompt_id"` // optional: use a specific prompt template
}

// AILowQualityListResponse is returned from GET /admin/ai/articles/low-quality
type AILowQualityListResponse struct {
	ArticleID    string `json:"article_id"`
	Title        string `json:"title"`
	Slug         string `json:"slug"`
	Excerpt      string `json:"excerpt"`
	Status       string `json:"status"`
	WordCount    int    `json:"word_count"`
	ReadingTime  int    `json:"reading_time"`
	CategoryName string `json:"category_name,omitempty"`
	CreatedAt    string `json:"created_at"`
}

// AIBulkRewriteRequest is the payload for POST /admin/ai/articles/bulk-rewrite
type AIBulkRewriteRequest struct {
	ArticleIDs   []string `json:"article_ids" validate:"required,min=1,max=50"`
	TargetLength string   `json:"target_length" validate:"omitempty,oneof=short medium long"`
	WritingStyle string   `json:"writing_style" validate:"omitempty,oneof=academic popular storytelling listicle"`
	Model        string   `json:"model"`
	PromptID     int      `json:"prompt_id"`
}

// AIBulkRewriteResponse is returned from POST /admin/ai/articles/bulk-rewrite
type AIBulkRewriteResponse struct {
	TotalRequested int                       `json:"total_requested"`
	Succeeded      int                       `json:"succeeded"`
	Failed         int                       `json:"failed"`
	Results        []AIBulkRewriteItemResult `json:"results"`
}

// AIBulkRewriteItemResult describes the outcome of rewriting a single article
type AIBulkRewriteItemResult struct {
	ArticleID    string  `json:"article_id"`
	Title        string  `json:"title"`
	Slug         string  `json:"slug,omitempty"`
	Status       string  `json:"status"` // "success" | "error"
	NewWordCount int     `json:"new_word_count,omitempty"`
	TokensUsed   int     `json:"tokens_used,omitempty"`
	CostUSD      float64 `json:"cost_usd,omitempty"`
	Error        string  `json:"error,omitempty"`
}

// AIArticleDraftResponse is returned after generating an article
type AIArticleDraftResponse struct {
	ArticleID     string   `json:"article_id"`
	Title         string   `json:"title"`
	Excerpt       string   `json:"excerpt"`
	MetaTitle     string   `json:"meta_title,omitempty"`
	MetaDesc      string   `json:"meta_description,omitempty"`
	Slug          string   `json:"slug"`
	SuggestedTags []string `json:"suggested_tags"`
	ReadingTime   int      `json:"reading_time"`
	Status        string   `json:"status"`
	AIModel       string   `json:"ai_model"`
	TokensUsed    int      `json:"tokens_used"`
	CostUSD       float64  `json:"cost_usd"`
}

// ============================================
// AI Chat DTOs
// ============================================

// AIChatCreateRequest creates a new chat session
type AIChatCreateRequest struct {
	Title   string                 `json:"title"   validate:"omitempty,max=200"`
	Context map[string]interface{} `json:"context"`
}

// AIChatMessageRequest sends a message in an existing session
type AIChatMessageRequest struct {
	Content string `json:"content" validate:"required,min=1,max=2000"`
	Stream  bool   `json:"stream"`
}

// AIChatMessageResponse is one turn in a conversation
type AIChatMessageResponse struct {
	Role       string `json:"role"` // "user" | "assistant"
	Content    string `json:"content"`
	CreatedAt  string `json:"created_at"`
	TokensUsed int    `json:"tokens_used,omitempty"`
}

// AIChatSessionResponse is returned for GET /api/v4/ai/chat/sessions/:uuid
type AIChatSessionResponse struct {
	SessionUUID   string                  `json:"session_uuid"`
	Title         string                  `json:"title"`
	Messages      []AIChatMessageResponse `json:"messages"`
	TotalTokens   int                     `json:"total_tokens"`
	TotalCost     float64                 `json:"total_cost"`
	LastMessageAt *string                 `json:"last_message_at,omitempty"`
	CreatedAt     string                  `json:"created_at"`
}

// ============================================
// AI Usage Quota DTO
// ============================================

// AIUsageQuotaResponse shows remaining quota for a user/IP
type AIUsageQuotaResponse struct {
	QuotaType string `json:"quota_type"`
	Used      int    `json:"used"`
	Limit     int    `json:"limit"`
	Remaining int    `json:"remaining"`
	ResetAt   string `json:"reset_at"`
}

// ============================================
// AI Admin DTOs
// ============================================

// AIStatsResponse is returned by GET /api/v4/admin/ai/stats
type AIStatsResponse struct {
	TotalRequests     int64            `json:"total_requests"`
	TotalTokens       int64            `json:"total_tokens"`
	TotalCostUSD      float64          `json:"total_cost_usd"`
	ArticlesGenerated int64            `json:"articles_generated"`
	HoroscopesRead    int64            `json:"horoscopes_read"`
	ChatMessages      int64            `json:"chat_messages"`
	CostByDay         []AIStatsByDay   `json:"cost_by_day"`
	RequestsByModel   []AIStatsByModel `json:"requests_by_model"`
}

// AIStatsByDay aggregates cost per day
type AIStatsByDay struct {
	Date     string  `json:"date"`
	Requests int64   `json:"requests"`
	Tokens   int64   `json:"tokens"`
	CostUSD  float64 `json:"cost_usd"`
}

// AIStatsByModel aggregates usage per model
type AIStatsByModel struct {
	Model    string  `json:"model"`
	Requests int64   `json:"requests"`
	Tokens   int64   `json:"tokens"`
	CostUSD  float64 `json:"cost_usd"`
}

// AIPromptTemplateRequest is for create / update prompt templates
type AIPromptTemplateRequest struct {
	Name         string  `json:"name"          validate:"required,max=200"`
	Type         string  `json:"type"          validate:"required,oneof=article horoscope chat"`
	SystemPrompt string  `json:"system_prompt" validate:"required"`
	UserPrompt   string  `json:"user_prompt"   validate:"required"`
	Model        string  `json:"model"`
	MaxTokens    int     `json:"max_tokens"    validate:"omitempty,min=256,max=8192"`
	Temperature  float64 `json:"temperature"   validate:"omitempty,min=0,max=2"`
	IsActive     bool    `json:"is_active"`
}
