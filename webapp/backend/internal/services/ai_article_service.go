package services

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// ============================================
// AIArticleService
// ============================================

// AIArticleService generates articles using OpenRouter AI
type AIArticleService struct {
	openRouter   *OpenRouterService
	articleRepo  *repositories.ArticleRepository
	categoryRepo *repositories.ArticleCategoryRepository
	tagRepo      *repositories.ArticleTagRepository
	logRepo      *repositories.AILogRepository
	promptRepo   *repositories.AIPromptTemplateRepository
	imageSearch  *ImageSearchService
	cfg          *config.AIConfig
	logger       *zap.Logger
}

// NewAIArticleService creates a new AIArticleService
func NewAIArticleService(
	openRouter *OpenRouterService,
	articleRepo *repositories.ArticleRepository,
	categoryRepo *repositories.ArticleCategoryRepository,
	tagRepo *repositories.ArticleTagRepository,
	logRepo *repositories.AILogRepository,
	promptRepo *repositories.AIPromptTemplateRepository,
	imageSearch *ImageSearchService,
	cfg *config.AIConfig,
	logger *zap.Logger,
) *AIArticleService {
	return &AIArticleService{
		openRouter:   openRouter,
		articleRepo:  articleRepo,
		categoryRepo: categoryRepo,
		tagRepo:      tagRepo,
		logRepo:      logRepo,
		promptRepo:   promptRepo,
		imageSearch:  imageSearch,
		cfg:          cfg,
		logger:       logger,
	}
}

// ============================================
// Public API
// ============================================

// GenerateArticle generates a full article (non-streaming) and saves it as a draft
func (s *AIArticleService) GenerateArticle(ctx context.Context, req dto.AIArticleGenerateRequest, authorID uuid.UUID) (*dto.AIArticleDraftResponse, error) {
	model := req.Model
	if model == "" {
		model = s.cfg.DefaultArticleModel
	}

	systemPrompt, userPrompt := s.buildArticlePrompt(req)
	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}

	start := time.Now()
	aiResp, err := s.openRouter.Complete(ctx, model, messages, s.cfg.MaxTokensArticle, 0.8)
	durationMs := int(time.Since(start).Milliseconds())

	status := "success"
	errMsg := ""
	var tokensUsed int
	var costUSD float64

	if err != nil {
		status = "error"
		errMsg = err.Error()
		_ = s.logEntry(authorID, "article", model, 0, 0, 0, 0, durationMs, status, errMsg)
		return nil, fmt.Errorf("AI article generation failed: %w", err)
	}

	tokensUsed = aiResp.Usage.TotalTokens
	costUSD = estimateCost(model, aiResp.Usage.PromptTokens, aiResp.Usage.CompletionTokens)
	rawContent := aiResp.Content()

	_ = s.logEntry(authorID, "article", model, aiResp.Usage.PromptTokens, aiResp.Usage.CompletionTokens, tokensUsed, costUSD, durationMs, status, errMsg)

	return s.saveArticleDraft(ctx, req, authorID, rawContent, model, tokensUsed, costUSD)
}

// GenerateArticleStream streams the article generation and saves it when complete
func (s *AIArticleService) GenerateArticleStream(ctx context.Context, req dto.AIArticleGenerateRequest, authorID uuid.UUID, onChunk func(string) error) (*dto.AIArticleDraftResponse, error) {
	model := req.Model
	if model == "" {
		model = s.cfg.DefaultArticleModel
	}

	systemPrompt, userPrompt := s.buildArticlePrompt(req)
	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}

	start := time.Now()
	var rawContent strings.Builder

	usage, err := s.openRouter.Stream(ctx, model, messages, s.cfg.MaxTokensArticle, func(delta string) error {
		rawContent.WriteString(delta)
		return onChunk(delta)
	})

	durationMs := int(time.Since(start).Milliseconds())
	status := "success"
	errMsg := ""
	var tokensUsed int
	var costUSD float64

	if err != nil {
		status = "error"
		errMsg = err.Error()
		_ = s.logEntry(authorID, "article", model, 0, 0, 0, 0, durationMs, status, errMsg)
		return nil, fmt.Errorf("AI article stream failed: %w", err)
	}

	if usage != nil {
		tokensUsed = usage.TotalTokens
		costUSD = estimateCost(model, usage.PromptTokens, usage.CompletionTokens)
		_ = s.logEntry(authorID, "article", model, usage.PromptTokens, usage.CompletionTokens, tokensUsed, costUSD, durationMs, status, errMsg)
	}

	return s.saveArticleDraft(ctx, req, authorID, rawContent.String(), model, tokensUsed, costUSD)
}

// QuickDraft generates a short article draft from a topic only
// Articles created via QuickDraft use ai_pending status (waiting queue for refinement)
func (s *AIArticleService) QuickDraft(ctx context.Context, req dto.AIArticleQuickDraftRequest, authorID uuid.UUID) (*dto.AIArticleDraftResponse, error) {
	full := dto.AIArticleGenerateRequest{
		Topic:        req.Topic,
		CategoryID:   req.CategoryID,
		TargetLength: "short",
		WritingStyle: "popular",
		GenerateSEO:  false,
	}
	// Use a dedicated context with generous timeout — Fiber's request context
	// can be cancelled prematurely before the AI call completes.
	// 120s allows for AI generation (~60s) + Pexels image search/download (~30s) + DB save.
	aiCtx, cancel := context.WithTimeout(context.Background(), 120*time.Second)
	defer cancel()
	return s.generateWithStatus(aiCtx, full, authorID, models.ArticleStatusAIPending)
}

// generateWithStatus is a helper to call GenerateArticle but override the saved status
func (s *AIArticleService) generateWithStatus(ctx context.Context, req dto.AIArticleGenerateRequest, authorID uuid.UUID, status string) (*dto.AIArticleDraftResponse, error) {
	model := req.Model
	if model == "" {
		model = s.cfg.DefaultArticleModel
	}
	systemPrompt, userPrompt := s.buildArticlePrompt(req)
	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}
	start := time.Now()
	aiResp, err := s.openRouter.Complete(ctx, model, messages, s.cfg.MaxTokensArticle, 0.8)
	durationMs := int(time.Since(start).Milliseconds())
	if err != nil {
		_ = s.logEntry(authorID, "article", model, 0, 0, 0, 0, durationMs, "error", err.Error())
		return nil, fmt.Errorf("AI article generation failed: %w", err)
	}
	tokensUsed := aiResp.Usage.TotalTokens
	costUSD := estimateCost(model, aiResp.Usage.PromptTokens, aiResp.Usage.CompletionTokens)
	_ = s.logEntry(authorID, "article", model, aiResp.Usage.PromptTokens, aiResp.Usage.CompletionTokens, tokensUsed, costUSD, durationMs, "success", "")
	return s.saveArticleDraftWithStatus(ctx, req, authorID, aiResp.Content(), model, tokensUsed, costUSD, status)
}

// SuggestTopics returns topic ideas for a category using AI
func (s *AIArticleService) SuggestTopics(ctx context.Context, categoryID string, model string) ([]string, error) {
	// Resolve human-readable category name
	catName := ""
	if categoryID != "" {
		if parsed, err := uuid.Parse(categoryID); err == nil {
			if cat, err := s.categoryRepo.GetByID(parsed); err == nil && cat != nil {
				catName = cat.Name
			}
		}
	}

	// Build the scope description for the prompt
	var scopeDesc string
	if catName != "" {
		scopeDesc = fmt.Sprintf("danh mục \"%s\"", catName)
	} else {
		scopeDesc = "các chủ đề phong thuỷ, tử vi, tâm linh, văn hoá dân gian Việt Nam"
	}

	messages := []OpenRouterMessage{
		{Role: "system", Content: "Bạn là biên tập viên nội dung am hiểu về phong thuỷ, tử vi, văn hoá tâm linh và lịch pháp Việt Nam. Chỉ trả về JSON array, không có text nào khác."},
		{Role: "user", Content: fmt.Sprintf(
			"Đề xuất 10 chủ đề bài viết hấp dẫn, đa dạng, có tiềm năng SEO cao về %s. "+
				"Mỗi chủ đề nên cụ thể, gần gũi với người đọc Việt Nam. "+
				"Chỉ trả về JSON array of strings. Ví dụ: [\"chủ đề 1\",\"chủ đề 2\"]", scopeDesc)},
	}

	// Use caller-specified model or default to gpt-4o-mini for speed
	if model == "" {
		model = "openai/gpt-4o-mini"
	}

	// Set a tight deadline so the HTTP handler doesn't hang forever
	ctx, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()

	resp, err := s.openRouter.Complete(ctx, model, messages, 300, 0.9)
	if err != nil {
		return nil, err
	}

	content := strings.TrimSpace(resp.Content())

	// Extract JSON array — find first '[' and last ']' to handle extra text/fences
	start := strings.Index(content, "[")
	end := strings.LastIndex(content, "]")
	if start >= 0 && end > start {
		content = content[start : end+1]
	}

	var topics []string
	if err := json.Unmarshal([]byte(content), &topics); err != nil {
		// fallback: strip fences then split by newline
		content = strings.TrimSpace(content)
		for _, line := range strings.Split(content, "\n") {
			line = strings.TrimSpace(line)
			line = strings.Trim(line, `",'`)
			line = strings.TrimLeft(line, "0123456789.-) ")
			line = strings.TrimSuffix(line, ",")
			line = strings.TrimSpace(line)
			if len(line) > 5 {
				topics = append(topics, line)
			}
		}
	}

	// Deduplicate and clean
	seen := map[string]bool{}
	var clean []string
	for _, t := range topics {
		t = strings.TrimSpace(t)
		if t != "" && !seen[t] {
			seen[t] = true
			clean = append(clean, t)
		}
	}

	return clean, nil
}

// ============================================
// Helpers
// ============================================

// buildArticlePrompt constructs system and user prompts from request
func (s *AIArticleService) buildArticlePrompt(req dto.AIArticleGenerateRequest) (system, user string) {
	// Try to load the specified prompt template (by ID first, then by active type)
	var tpl *models.AIPromptTemplate
	if req.PromptID > 0 {
		if t, err := s.promptRepo.GetByID(uint64(req.PromptID)); err == nil && t.IsActive {
			tpl = t
		}
	}
	if tpl == nil {
		if t, err := s.promptRepo.GetActiveByType("article"); err == nil {
			tpl = t
		}
	}
	if tpl != nil {
		lengthMap := map[string]string{
			"short":  "800-1200 từ",
			"medium": "1500-2000 từ",
			"long":   "2500-3500 từ",
		}
		styleMap := map[string]string{
			"academic":     "học thuật, trang trọng",
			"popular":      "phổ thông, dễ đọc",
			"storytelling": "kể chuyện, cảm xúc",
			"listicle":     "dạng danh sách (listicle)",
		}
		seo := "Không"
		if req.GenerateSEO {
			seo = "Có — thêm meta_title và meta_description ở cuối"
		}

		replacer := strings.NewReplacer(
			"{{topic}}", req.Topic,
			"{{length}}", lengthMap[req.TargetLength],
			"{{style}}", styleMap[req.WritingStyle],
			"{{keyword}}", req.TargetKeyword,
			"{{seo}}", seo,
		)
		return replacer.Replace(tpl.SystemPrompt), replacer.Replace(tpl.UserPrompt)
	}

	// Default fallback
	system = `Bạn là nhà báo và chuyên gia nội dung về lịch sử, phong thuỷ, tử vi và tâm linh Việt Nam.
Viết bài bằng tiếng Việt, giọng văn chuyên nghiệp nhưng gần gũi.

<quy-tắc-output — tuân-thủ-tuyệt-đối>
• Trả về HTML thuần túy, KHÔNG bọc trong code fence (không dùng ` + "```" + `html, không dùng ` + "```" + `).
• Bắt đầu NGAY bằng thẻ <h1>Tiêu đề bài viết</h1>.
• Dùng: <h2> mục lớn | <h3> mục nhỏ | <p> đoạn văn | <ul><li> / <ol><li> danh sách | <strong> nhấn mạnh | <em> thuật ngữ | <table><thead><tbody> bảng.
• KHÔNG dùng: <html> <head> <body> <style> <script>.
• Đặt đúng 2 dòng này ở CUỐI CÙNG:
  <!-- META_TITLE: Tiêu đề SEO tối đa 60 ký tự, chứa từ khoá chính -->
  <!-- META_DESC: Mô tả 130–160 ký tự, chứa từ khoá, gợi click -->
• Viết tiếng Việt chuẩn, đúng chính tả, đủ dấu câu.
</quy-tắc-output>`

	lengthDesc := map[string]string{
		"short":  "800–1200 từ",
		"medium": "1500–2000 từ",
		"long":   "2500–3500 từ",
	}
	styleDesc := map[string]string{
		"academic":     "học thuật",
		"popular":      "phổ thông",
		"storytelling": "kể chuyện",
		"listicle":     "danh sách",
	}

	user = fmt.Sprintf(
		"Viết bài về chủ đề: **%s**\n- Độ dài: %s\n- Phong cách: %s",
		req.Topic,
		lengthDesc[req.TargetLength],
		styleDesc[req.WritingStyle],
	)
	if req.TargetKeyword != "" {
		user += fmt.Sprintf("\n- Từ khoá SEO chính: %s", req.TargetKeyword)
	}
	if req.GenerateSEO {
		user += "\n- Thêm meta_title và meta_description ở cuối bài dưới dạng <!-- META_TITLE: ... --> <!-- META_DESC: ... -->"
	}

	return
}

// saveArticleDraft parses AI output and creates a draft article
func (s *AIArticleService) saveArticleDraft(ctx context.Context, req dto.AIArticleGenerateRequest, authorID uuid.UUID, rawContent, model string, tokensUsed int, costUSD float64) (*dto.AIArticleDraftResponse, error) {
	return s.saveArticleDraftWithStatus(ctx, req, authorID, rawContent, model, tokensUsed, costUSD, models.ArticleStatusDraft)
}

// saveArticleDraftWithStatus parses AI output and creates an article with the given status
func (s *AIArticleService) saveArticleDraftWithStatus(ctx context.Context, req dto.AIArticleGenerateRequest, authorID uuid.UUID, rawContent, model string, tokensUsed int, costUSD float64, status string) (*dto.AIArticleDraftResponse, error) {
	// Sanitize UTF-8 before parsing — AI streaming can produce partial multi-byte sequences
	rawContent = sanitizeUTF8(rawContent)

	title, content, metaTitle, metaDesc := parseArticleOutput(rawContent)

	if title == "" {
		title = req.Topic
	}

	slug := utils.GenerateSlug(title)
	if exists, _ := s.articleRepo.SlugExists(slug, nil); exists {
		slug = fmt.Sprintf("%s-%d", slug, time.Now().UnixMilli())
	}

	excerpt := extractExcerpt(content, 200)
	readingTime := utils.CalculateReadingTime(content)

	if metaTitle == "" && req.GenerateSEO {
		metaTitle = title
	}

	var catID *uuid.UUID
	if req.CategoryID != "" {
		if parsed, err := uuid.Parse(req.CategoryID); err == nil {
			catID = &parsed
		}
	}

	article := &models.Article{
		Title:           title,
		Slug:            slug,
		Excerpt:         excerpt,
		Content:         content,
		AuthorID:        &authorID,
		CategoryID:      catID,
		Status:          status,
		MetaTitle:       metaTitle,
		MetaDescription: metaDesc,
		ReadingTime:     readingTime,
	}

	// Auto-search and attach a featured image from Pexels
	if s.imageSearch != nil && s.imageSearch.IsAvailable() {
		searchQuery := req.Topic
		if title != "" && title != req.Topic {
			searchQuery = title
		}
		if imgPath := s.imageSearch.SearchAndDownload(searchQuery); imgPath != "" {
			article.FeaturedImage = imgPath
			article.OGImage = imgPath
			s.logger.Info("Auto-attached Pexels image",
				zap.String("topic", req.Topic),
				zap.String("image", imgPath),
			)
		}
	}

	if err := s.articleRepo.Create(article); err != nil {
		return nil, fmt.Errorf("save article draft: %w", err)
	}

	return &dto.AIArticleDraftResponse{
		ArticleID:   article.ID.String(),
		Title:       title,
		Excerpt:     excerpt,
		MetaTitle:   metaTitle,
		MetaDesc:    metaDesc,
		Slug:        slug,
		ReadingTime: readingTime,
		Status:      status,
		AIModel:     model,
		TokensUsed:  tokensUsed,
		CostUSD:     costUSD,
	}, nil
}

// RefineArticle uses AI to rewrite an existing article with more detail
func (s *AIArticleService) RefineArticle(ctx context.Context, articleID uuid.UUID, req dto.AIArticleRefineRequest, authorID uuid.UUID) (*dto.AIArticleDraftResponse, error) {
	article, err := s.articleRepo.GetByID(articleID)
	if err != nil {
		return nil, fmt.Errorf("article not found: %w", err)
	}

	model := req.Model
	if model == "" {
		model = s.cfg.DefaultArticleModel
	}
	targetLength := req.TargetLength
	if targetLength == "" {
		targetLength = "long"
	}
	writingStyle := req.WritingStyle
	if writingStyle == "" {
		writingStyle = "popular"
	}

	var systemPrompt, userPrompt string

	if req.PromptID > 0 {
		if tpl, err2 := s.promptRepo.GetByID(uint64(req.PromptID)); err2 == nil && tpl.IsActive {
			lengthMap := map[string]string{"short": "800-1200 từ", "medium": "1500-2000 từ", "long": "2500-3500 từ"}
			styleMap := map[string]string{"academic": "học thuật", "popular": "phổ thông", "storytelling": "kể chuyện", "listicle": "danh sách"}
			r := strings.NewReplacer("{{topic}}", article.Title, "{{length}}", lengthMap[targetLength], "{{style}}", styleMap[writingStyle], "{{keyword}}", "", "{{seo}}", "Không")
			systemPrompt = r.Replace(tpl.SystemPrompt)
			userPrompt = r.Replace(tpl.UserPrompt)
		}
	}

	if systemPrompt == "" {
		systemPrompt = `Bạn là chuyên gia viết nội dung chuyên sâu về lịch sử, phong thuỷ, tử vi và tâm linh Việt Nam.
Viết bài bằng tiếng Việt, chuyên nghiệp, chi tiết và có chiều sâu.

<quy-tắc-output — tuân-thủ-tuyệt-đối>
• Trả về HTML thuần túy, KHÔNG bọc trong code fence (không dùng ` + "```" + `html, không dùng ` + "```" + `).
• Bắt đầu NGAY bằng thẻ <h1>Tiêu đề bài viết</h1>.
• Dùng: <h2> mục lớn | <h3> mục nhỏ | <p> đoạn văn | <ul><li> / <ol><li> danh sách | <strong> nhấn mạnh | <em> thuật ngữ | <table><thead><tbody> bảng.
• KHÔNG dùng: <html> <head> <body> <style> <script>.
• Đặt đúng 2 dòng này ở CUỐI CÙNG:
  <!-- META_TITLE: Tiêu đề SEO tối đa 60 ký tự, chứa từ khoá chính -->
  <!-- META_DESC: Mô tả 130–160 ký tự, chứa từ khoá, gợi click -->
• Viết tiếng Việt chuẩn, đúng chính tả, đủ dấu câu.
</quy-tắc-output>`
		lengthMap := map[string]string{"short": "1000-1500 từ", "medium": "2000-2500 từ", "long": "3000-4000 từ"}
		styleMap := map[string]string{"academic": "học thuật", "popular": "phổ thông, dễ đọc", "storytelling": "kể chuyện, cảm xúc", "listicle": "dạng danh sách"}
		userPrompt = fmt.Sprintf(
			"Viết lại và mở rộng chi tiết bài sau với độ dài %s, phong cách %s.\nTrả về HTML thuần túy, bắt đầu bằng <h1>.\n\nBài gốc:\n---\n%s\n---",
			lengthMap[targetLength], styleMap[writingStyle], article.Content,
		)
		if req.Instruction != "" {
			userPrompt += fmt.Sprintf("\n\nYêu cầu đặc biệt: %s", req.Instruction)
		}
	}

	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}

	start := time.Now()
	aiResp, err := s.openRouter.Complete(ctx, model, messages, s.cfg.MaxTokensArticle, 0.75)
	durationMs := int(time.Since(start).Milliseconds())

	if err != nil {
		_ = s.logEntry(authorID, "article_refine", model, 0, 0, 0, 0, durationMs, "error", err.Error())
		return nil, fmt.Errorf("AI refine failed: %w", err)
	}

	tokensUsed2 := aiResp.Usage.TotalTokens
	costUSD2 := estimateCost(model, aiResp.Usage.PromptTokens, aiResp.Usage.CompletionTokens)
	_ = s.logEntry(authorID, "article_refine", model, aiResp.Usage.PromptTokens, aiResp.Usage.CompletionTokens, tokensUsed2, costUSD2, durationMs, "success", "")

	rawContent := sanitizeUTF8(aiResp.Content())
	newTitle, newContent, newMetaTitle, newMetaDesc := parseArticleOutput(rawContent)
	if newTitle == "" {
		newTitle = article.Title
	}
	newExcerpt := extractExcerpt(newContent, 200)
	newReadingTime := utils.CalculateReadingTime(newContent)

	article.Content = newContent
	article.Title = newTitle
	article.Excerpt = newExcerpt
	article.ReadingTime = newReadingTime
	article.Status = models.ArticleStatusDraft
	if newMetaTitle != "" {
		article.MetaTitle = newMetaTitle
	}
	if newMetaDesc != "" {
		article.MetaDescription = newMetaDesc
	}

	// Auto-fetch Pexels image if article has no featured image yet
	if article.FeaturedImage == "" && s.imageSearch != nil && s.imageSearch.IsAvailable() {
		searchQuery := newTitle
		if searchQuery == "" {
			searchQuery = article.Title
		}
		if imgPath := s.imageSearch.SearchAndDownload(searchQuery); imgPath != "" {
			article.FeaturedImage = imgPath
			if article.OGImage == "" {
				article.OGImage = imgPath
			}
			s.logger.Info("Auto-attached Pexels image on refine",
				zap.String("article_id", article.ID.String()),
				zap.String("image", imgPath),
			)
		}
	}

	if err := s.articleRepo.Update(article); err != nil {
		return nil, fmt.Errorf("update article: %w", err)
	}

	return &dto.AIArticleDraftResponse{
		ArticleID:   article.ID.String(),
		Title:       newTitle,
		Excerpt:     newExcerpt,
		MetaTitle:   newMetaTitle,
		MetaDesc:    newMetaDesc,
		Slug:        article.Slug,
		ReadingTime: newReadingTime,
		Status:      models.ArticleStatusDraft,
		AIModel:     model,
		TokensUsed:  tokensUsed2,
		CostUSD:     costUSD2,
	}, nil
}

// UpdateArticleStatus changes the status of an AI-generated article
func (s *AIArticleService) UpdateArticleStatus(ctx context.Context, articleID uuid.UUID, newStatus string) error {
	article, err := s.articleRepo.GetByID(articleID)
	if err != nil {
		return fmt.Errorf("article not found: %w", err)
	}
	article.Status = newStatus
	return s.articleRepo.Update(article)
}

// ListAIArticles lists articles by status (for the AI article management queue)
func (s *AIArticleService) ListAIArticles(ctx context.Context, status string, page, pageSize int) ([]models.Article, int64, error) {
	return s.articleRepo.ListAll(page, pageSize, status, "", nil)
}

// ============================================
// Low-Quality (< 500 words) Article Methods
// ============================================

// ListLowQualityArticles returns articles with fewer than maxWords words.
// Supports same filters as admin articles list: status, search, categoryID.
func (s *AIArticleService) ListLowQualityArticles(ctx context.Context, maxWords, page, pageSize int, status, search string, categoryID *uuid.UUID) ([]dto.AILowQualityListResponse, int64, error) {
	articles, total, err := s.articleRepo.ListLowQuality(maxWords, page, pageSize, status, search, categoryID)
	if err != nil {
		return nil, 0, err
	}

	var result []dto.AILowQualityListResponse
	for _, a := range articles {
		wordCount := len(strings.Fields(a.Content))
		catName := ""
		if a.Category != nil {
			catName = a.Category.Name
		}
		result = append(result, dto.AILowQualityListResponse{
			ArticleID:    a.ID.String(),
			Title:        a.Title,
			Slug:         a.Slug,
			Excerpt:      a.Excerpt,
			WordCount:    wordCount,
			ReadingTime:  a.ReadingTime,
			Status:       a.Status,
			CategoryName: catName,
			CreatedAt:    a.CreatedAt.Format("2006-01-02T15:04:05Z"),
		})
	}

	return result, total, nil
}

// CountLowQualityArticles returns the count of articles under the word threshold.
func (s *AIArticleService) CountLowQualityArticles(ctx context.Context, maxWords int, status string) (int64, error) {
	return s.articleRepo.CountLowQuality(maxWords, status)
}

// BulkRewriteLowQuality rewrites multiple low-quality articles using AI.
// Each article is refined individually; failures don't stop the batch.
func (s *AIArticleService) BulkRewriteLowQuality(ctx context.Context, req dto.AIBulkRewriteRequest, authorID uuid.UUID) (*dto.AIBulkRewriteResponse, error) {
	resp := &dto.AIBulkRewriteResponse{
		TotalRequested: len(req.ArticleIDs),
	}

	for _, idStr := range req.ArticleIDs {
		artID, err := uuid.Parse(idStr)
		if err != nil {
			resp.Failed++
			resp.Results = append(resp.Results, dto.AIBulkRewriteItemResult{
				ArticleID: idStr,
				Status:    "error",
				Error:     "ID không hợp lệ",
			})
			continue
		}

		refineReq := dto.AIArticleRefineRequest{
			TargetLength: req.TargetLength,
			WritingStyle: req.WritingStyle,
			Model:        req.Model,
			PromptID:     req.PromptID,
			Instruction:  "Viết lại bài viết này với nội dung dài hơn, chi tiết hơn, chuẩn SEO. Bài gốc quá ngắn (dưới 500 từ) nên chưa đạt chất lượng. Hãy mở rộng với thông tin hữu ích, cấu trúc rõ ràng, và tối ưu SEO.",
		}

		result, err := s.RefineArticle(ctx, artID, refineReq, authorID)
		if err != nil {
			resp.Failed++
			resp.Results = append(resp.Results, dto.AIBulkRewriteItemResult{
				ArticleID: idStr,
				Status:    "error",
				Error:     err.Error(),
			})
			s.logger.Warn("BulkRewrite failed for article",
				zap.String("article_id", idStr),
				zap.Error(err),
			)
			continue
		}

		// Get the updated article to count new words
		article, _ := s.articleRepo.GetByID(artID)
		newWordCount := 0
		articleSlug := ""
		if article != nil {
			newWordCount = len(strings.Fields(article.Content))
			articleSlug = article.Slug
		}

		resp.Succeeded++
		resp.Results = append(resp.Results, dto.AIBulkRewriteItemResult{
			ArticleID:    idStr,
			Title:        result.Title,
			Slug:         articleSlug,
			Status:       "success",
			NewWordCount: newWordCount,
			TokensUsed:   result.TokensUsed,
			CostUSD:      result.CostUSD,
		})
	}

	return resp, nil
}

// logEntry creates an AIGenerationLog record
func (s *AIArticleService) logEntry(authorID uuid.UUID, genType, model string, promptT, completionT, totalT int, cost float64, durationMs int, status, errMsg string) error {
	uid := authorID
	return s.logRepo.Create(&models.AIGenerationLog{
		UserID:           &uid,
		GenerationType:   genType,
		ModelUsed:        model,
		PromptTokens:     promptT,
		CompletionTokens: completionT,
		TotalTokens:      totalT,
		CostUSD:          cost,
		DurationMs:       durationMs,
		Status:           status,
		ErrorMessage:     errMsg,
	})
}

// parseArticleOutput extracts title, content, and SEO fields from raw AI output (HTML or markdown)
func parseArticleOutput(raw string) (title, content, metaTitle, metaDesc string) {
	// Strip outer code fence wrapping (AI sometimes wraps output in ```html ... ``` or ```markdown ... ```)
	raw = strings.TrimSpace(raw)
	if strings.HasPrefix(raw, "```") {
		if idx := strings.Index(raw, "\n"); idx != -1 {
			raw = raw[idx+1:]
		}
		if idx := strings.LastIndex(raw, "```"); idx != -1 {
			raw = raw[:idx]
		}
		raw = strings.TrimSpace(raw)
	}

	// Detect if output is HTML (starts with <h1> or contains HTML tags)
	isHTML := strings.HasPrefix(raw, "<h1>") || strings.HasPrefix(raw, "<h1 ") ||
		strings.Contains(raw[:min(500, len(raw))], "<h2>") ||
		strings.Contains(raw[:min(500, len(raw))], "<p>")

	if isHTML {
		return parseHTMLArticleOutput(raw)
	}

	// Fallback: parse as Markdown
	return parseMarkdownArticleOutput(raw)
}

// parseHTMLArticleOutput parses HTML output from AI
func parseHTMLArticleOutput(raw string) (title, content, metaTitle, metaDesc string) {
	lines := strings.Split(raw, "\n")
	var contentLines []string

	for _, line := range lines {
		trimmed := strings.TrimSpace(line)

		// Extract <h1> title
		if title == "" && strings.Contains(trimmed, "<h1>") {
			start := strings.Index(trimmed, "<h1>") + 4
			end := strings.Index(trimmed, "</h1>")
			if end > start {
				title = strings.TrimSpace(trimmed[start:end])
				// Don't include <h1> in content body — it's the article title
				continue
			}
		}

		// Parse inline META tags
		if strings.Contains(trimmed, "<!-- META_TITLE:") {
			start := strings.Index(trimmed, "<!-- META_TITLE:") + len("<!-- META_TITLE:")
			end := strings.Index(trimmed, "-->")
			if end > start {
				metaTitle = strings.TrimSpace(trimmed[start:end])
			}
			continue
		}
		if strings.Contains(trimmed, "<!-- META_DESC:") {
			start := strings.Index(trimmed, "<!-- META_DESC:") + len("<!-- META_DESC:")
			end := strings.Index(trimmed, "-->")
			if end > start {
				metaDesc = strings.TrimSpace(trimmed[start:end])
			}
			continue
		}

		contentLines = append(contentLines, line)
	}

	content = strings.TrimSpace(strings.Join(contentLines, "\n"))
	return
}

// parseMarkdownArticleOutput parses Markdown output from AI (legacy fallback)
func parseMarkdownArticleOutput(raw string) (title, content, metaTitle, metaDesc string) {
	lines := strings.Split(raw, "\n")
	var contentLines []string
	foundTitle := false

	for _, line := range lines {
		trimmed := strings.TrimSpace(line)

		if !foundTitle && strings.HasPrefix(trimmed, "# ") {
			title = strings.TrimPrefix(trimmed, "# ")
			foundTitle = true
			continue
		}

		// Parse inline META tags
		if strings.Contains(trimmed, "<!-- META_TITLE:") {
			start := strings.Index(trimmed, "<!-- META_TITLE:") + len("<!-- META_TITLE:")
			end := strings.Index(trimmed, "-->")
			if end > start {
				metaTitle = strings.TrimSpace(trimmed[start:end])
			}
			continue
		}
		if strings.Contains(trimmed, "<!-- META_DESC:") {
			start := strings.Index(trimmed, "<!-- META_DESC:") + len("<!-- META_DESC:")
			end := strings.Index(trimmed, "-->")
			if end > start {
				metaDesc = strings.TrimSpace(trimmed[start:end])
			}
			continue
		}

		contentLines = append(contentLines, line)
	}

	content = strings.Join(contentLines, "\n")
	return
}

// extractExcerpt returns the first n runes of plain text (UTF-8 safe), supports both HTML and Markdown
func extractExcerpt(input string, maxLen int) string {
	input = strings.TrimSpace(input)

	// Strip code fences
	if strings.HasPrefix(input, "```") {
		if idx := strings.Index(input, "\n"); idx != -1 {
			input = input[idx+1:]
		}
		input = strings.TrimSuffix(strings.TrimSpace(input), "```")
	}

	// Strip HTML tags if content looks like HTML
	isHTML := strings.Contains(input[:min(300, len(input))], "<p>") ||
		strings.Contains(input[:min(300, len(input))], "<h2>")

	var plain []string

	if isHTML {
		// Simple HTML tag stripper for excerpt
		text := input
		// Remove all HTML tags
		for {
			start := strings.Index(text, "<")
			if start < 0 {
				break
			}
			end := strings.Index(text[start:], ">")
			if end < 0 {
				break
			}
			text = text[:start] + " " + text[start+end+1:]
		}
		// Clean up whitespace
		for _, line := range strings.Split(text, "\n") {
			line = strings.TrimSpace(line)
			if line != "" {
				plain = append(plain, line)
				if len([]rune(strings.Join(plain, " "))) >= maxLen {
					break
				}
			}
		}
	} else {
		// Markdown: strip headers/formatting
		for _, l := range strings.Split(input, "\n") {
			l = strings.TrimSpace(l)
			if l == "" || strings.HasPrefix(l, "#") || strings.HasPrefix(l, "---") ||
				strings.HasPrefix(l, "```") {
				continue
			}
			l = strings.ReplaceAll(l, "**", "")
			l = strings.ReplaceAll(l, "__", "")
			l = strings.ReplaceAll(l, "*", "")
			plain = append(plain, l)
			if len([]rune(strings.Join(plain, " "))) >= maxLen {
				break
			}
		}
	}

	result := strings.Join(plain, " ")
	runes := []rune(result)
	if len(runes) > maxLen {
		return string(runes[:maxLen]) + "..."
	}
	return result
}

// sanitizeUTF8 removes invalid UTF-8 byte sequences from a string.
// strings.ToValidUTF8 replaces each invalid sequence with the replacement string (empty = drop).
func sanitizeUTF8(s string) string {
	return strings.ToValidUTF8(s, "")
}
