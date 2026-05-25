package services

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// ============================================
// AIHoroscopeService
// ============================================

// AIHoroscopeService orchestrates AI horoscope readings
type AIHoroscopeService struct {
	openRouter   *OpenRouterService
	horoscope    *HoroscopeService
	horoRepo     *repositories.AIHoroscopeRepository
	quotaRepo    *repositories.AIQuotaRepository
	logRepo      *repositories.AILogRepository
	promptRepo   *repositories.AIPromptTemplateRepository
	cacheService *CacheService
	cfg          *config.AIConfig
	logger       *zap.Logger
}

// NewAIHoroscopeService creates a new AIHoroscopeService
func NewAIHoroscopeService(
	openRouter *OpenRouterService,
	horoscope *HoroscopeService,
	horoRepo *repositories.AIHoroscopeRepository,
	quotaRepo *repositories.AIQuotaRepository,
	logRepo *repositories.AILogRepository,
	promptRepo *repositories.AIPromptTemplateRepository,
	cacheService *CacheService,
	cfg *config.AIConfig,
	logger *zap.Logger,
) *AIHoroscopeService {
	return &AIHoroscopeService{
		openRouter:   openRouter,
		horoscope:    horoscope,
		horoRepo:     horoRepo,
		quotaRepo:    quotaRepo,
		logRepo:      logRepo,
		promptRepo:   promptRepo,
		cacheService: cacheService,
		cfg:          cfg,
		logger:       logger,
	}
}

// ============================================
// Public API
// ============================================

// GetQuota returns the current quota status for a user / IP
func (s *AIHoroscopeService) GetQuota(userID *uuid.UUID, ipAddress string, role string) (*dto.AIUsageQuotaResponse, error) {
	limit := s.quotaLimit(role)
	quota, err := s.quotaRepo.GetOrCreate(userID, ipAddress, "horoscope_daily", limit)
	if err != nil {
		return nil, err
	}

	remaining := quota.LimitCount - quota.UsedCount
	if remaining < 0 {
		remaining = 0
	}

	return &dto.AIUsageQuotaResponse{
		QuotaType: "horoscope_daily",
		Used:      quota.UsedCount,
		Limit:     quota.LimitCount,
		Remaining: remaining,
		ResetAt:   quota.ResetAt.Format(time.RFC3339),
	}, nil
}

// ReadHoroscope generates (or fetches cached) an AI horoscope reading
func (s *AIHoroscopeService) ReadHoroscope(ctx context.Context, req dto.HoroscopeAIRequest, userID *uuid.UUID, ipAddress, role string) (*dto.HoroscopeAIResponse, error) {
	// 1. Check quota
	if err := s.checkAndIncrementQuota(userID, ipAddress, role); err != nil {
		return nil, err
	}

	// 2. Build session key for cache / dedup
	sessionKey := s.buildSessionKey(req)

	// 3. Try cache
	cacheKey := "ai:horoscope:" + sessionKey
	if cached, err := s.cacheService.GetString(ctx, cacheKey); err == nil && cached != "" {
		var resp dto.HoroscopeAIResponse
		if json.Unmarshal([]byte(cached), &resp) == nil {
			return &resp, nil
		}
	}

	// 4. Build bát tự / ngũ hành info
	batTu, nguHanh := s.calculateBatTu(req)

	// 5. Build prompt
	systemPrompt, userPrompt, err := s.buildPrompt(req, batTu, nguHanh)
	if err != nil {
		s.logger.Warn("Failed to load prompt template, using default", zap.Error(err))
		systemPrompt, userPrompt = s.defaultPrompt(req, batTu, nguHanh)
	}

	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}

	model := s.cfg.DefaultHoroscopeModel
	maxTokens := s.cfg.MaxTokensHoroscope
	start := time.Now()

	// 6. Call AI
	aiResp, err := s.openRouter.Complete(ctx, model, messages, maxTokens, 0.7)
	durationMs := int(time.Since(start).Milliseconds())

	status := "success"
	errMsg := ""
	var tokensUsed int
	var costUSD float64
	aiResult := ""

	if err != nil {
		status = "error"
		errMsg = err.Error()
		s.logger.Error("OpenRouter horoscope error", zap.Error(err))
	} else {
		aiResult = aiResp.Content()
		tokensUsed = aiResp.Usage.TotalTokens
		costUSD = estimateCost(model, aiResp.Usage.PromptTokens, aiResp.Usage.CompletionTokens)
	}

	// 7. Persist log
	_ = s.logRepo.Create(&models.AIGenerationLog{
		UserID:         nil, // horoscope service uses uint64 userID — omit for now
		GenerationType: "horoscope",
		ModelUsed:      model,
		PromptTokens: func() int {
			if aiResp != nil {
				return aiResp.Usage.PromptTokens
			}
			return 0
		}(),
		CompletionTokens: func() int {
			if aiResp != nil {
				return aiResp.Usage.CompletionTokens
			}
			return 0
		}(),
		TotalTokens:  tokensUsed,
		CostUSD:      costUSD,
		DurationMs:   durationMs,
		Status:       status,
		ErrorMessage: errMsg,
	})

	if err != nil {
		return nil, fmt.Errorf("AI generation failed: %w", err)
	}

	// 8. Persist session
	batTuMap := models.JSONB{}
	nguHanhMap := models.JSONB{}
	if b, err := json.Marshal(batTu); err == nil {
		_ = json.Unmarshal(b, &batTuMap)
	}
	if b, err := json.Marshal(nguHanh); err == nil {
		_ = json.Unmarshal(b, &nguHanhMap)
	}

	session := &models.AIHoroscopeSession{
		UserID:      userID,
		SessionKey:  sessionKey,
		BirthYear:   req.BirthYear,
		BirthMonth:  req.BirthMonth,
		BirthDay:    req.BirthDay,
		BirthHour:   req.BirthHour,
		Gender:      req.Gender,
		ReadingType: req.ReadingType,
		Depth:       req.Depth,
		TargetYear:  req.TargetYear,
		TargetMonth: req.TargetMonth,
		Question:    req.Question,
		BatTu:       batTuMap,
		NguHanh:     nguHanhMap,
		AIResult:    aiResult,
		ModelUsed:   model,
		TokensUsed:  tokensUsed,
		CostUSD:     costUSD,
		IPAddress:   ipAddress,
	}
	_ = s.horoRepo.Create(session)

	// 9. Get remaining quota
	quotaResp, _ := s.GetQuota(userID, ipAddress, role)
	remaining := 0
	if quotaResp != nil {
		remaining = quotaResp.Remaining
	}

	resp := &dto.HoroscopeAIResponse{
		SessionID:      session.ID,
		BatTu:          batTu,
		NguHanhBalance: nguHanh,
		AIResult:       aiResult,
		ModelUsed:      model,
		TokensUsed:     tokensUsed,
		QuotaRemaining: remaining,
	}

	// 10. Cache the result for 24 hours
	if data, err := json.Marshal(resp); err == nil {
		_ = s.cacheService.SetString(ctx, cacheKey, string(data), 24*time.Hour)
	}

	return resp, nil
}

// ReadHoroscopeStream streams the AI result via SSE callback
func (s *AIHoroscopeService) ReadHoroscopeStream(ctx context.Context, req dto.HoroscopeAIRequest, userID *uuid.UUID, ipAddress, role string, onChunk func(string) error) (*dto.HoroscopeAIResponse, error) {
	// 1. Check quota
	if err := s.checkAndIncrementQuota(userID, ipAddress, role); err != nil {
		return nil, err
	}

	// 2. Build bát tự
	batTu, nguHanh := s.calculateBatTu(req)

	// 3. Build prompt
	systemPrompt, userPrompt, err := s.buildPrompt(req, batTu, nguHanh)
	if err != nil {
		systemPrompt, userPrompt = s.defaultPrompt(req, batTu, nguHanh)
	}

	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}

	model := s.cfg.DefaultHoroscopeModel
	maxTokens := s.cfg.MaxTokensHoroscope
	start := time.Now()

	var fullResult strings.Builder
	usage, err := s.openRouter.Stream(ctx, model, messages, maxTokens, func(delta string) error {
		fullResult.WriteString(delta)
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
	} else if usage != nil {
		tokensUsed = usage.TotalTokens
		costUSD = estimateCost(model, usage.PromptTokens, usage.CompletionTokens)
	}

	promptT := 0
	completionT := 0
	if usage != nil {
		promptT = usage.PromptTokens
		completionT = usage.CompletionTokens
	}

	_ = s.logRepo.Create(&models.AIGenerationLog{
		UserID:           userID,
		GenerationType:   "horoscope",
		ModelUsed:        model,
		PromptTokens:     promptT,
		CompletionTokens: completionT,
		TotalTokens:      tokensUsed,
		CostUSD:          costUSD,
		DurationMs:       durationMs,
		Status:           status,
		ErrorMessage:     errMsg,
	})

	if err != nil {
		return nil, fmt.Errorf("AI stream failed: %w", err)
	}

	aiResult := fullResult.String()

	batTuMap := models.JSONB{}
	nguHanhMap := models.JSONB{}
	if b, err := json.Marshal(batTu); err == nil {
		_ = json.Unmarshal(b, &batTuMap)
	}
	if b, err := json.Marshal(nguHanh); err == nil {
		_ = json.Unmarshal(b, &nguHanhMap)
	}

	session := &models.AIHoroscopeSession{
		UserID:      userID,
		SessionKey:  s.buildSessionKey(req),
		BirthYear:   req.BirthYear,
		BirthMonth:  req.BirthMonth,
		BirthDay:    req.BirthDay,
		BirthHour:   req.BirthHour,
		Gender:      req.Gender,
		ReadingType: req.ReadingType,
		Depth:       req.Depth,
		TargetYear:  req.TargetYear,
		TargetMonth: req.TargetMonth,
		Question:    req.Question,
		BatTu:       batTuMap,
		NguHanh:     nguHanhMap,
		AIResult:    aiResult,
		ModelUsed:   model,
		TokensUsed:  tokensUsed,
		CostUSD:     costUSD,
		IPAddress:   ipAddress,
	}
	_ = s.horoRepo.Create(session)

	quotaResp, _ := s.GetQuota(userID, ipAddress, role)
	remaining := 0
	if quotaResp != nil {
		remaining = quotaResp.Remaining
	}

	return &dto.HoroscopeAIResponse{
		SessionID:      session.ID,
		BatTu:          batTu,
		NguHanhBalance: nguHanh,
		AIResult:       aiResult,
		ModelUsed:      model,
		TokensUsed:     tokensUsed,
		QuotaRemaining: remaining,
	}, nil
}

// GetHistory returns paginated reading history for a user
func (s *AIHoroscopeService) GetHistory(userID *uuid.UUID, page, limit int) ([]models.AIHoroscopeSession, int64, error) {
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 50 {
		limit = 10
	}
	return s.horoRepo.ListByUser(userID, page, limit)
}

// ============================================
// Helpers
// ============================================

// checkAndIncrementQuota checks quota and increments in one step
func (s *AIHoroscopeService) checkAndIncrementQuota(userID *uuid.UUID, ipAddress, role string) error {
	limit := s.quotaLimit(role)
	quota, err := s.quotaRepo.GetOrCreate(userID, ipAddress, "horoscope_daily", limit)
	if err != nil {
		return err
	}

	if quota.UsedCount >= quota.LimitCount {
		return fmt.Errorf("quota_exceeded: bạn đã dùng hết %d lần xem tử vi AI hôm nay. Hạn reset: %s",
			quota.LimitCount, quota.ResetAt.Format("15:04 02/01"))
	}

	return s.quotaRepo.Increment(quota.ID)
}

// quotaLimit returns the daily limit based on user role
func (s *AIHoroscopeService) quotaLimit(role string) int {
	switch role {
	case "premium":
		return s.cfg.HoroscopeRateLimitPremium
	case "free":
		return s.cfg.HoroscopeRateLimitFree
	default:
		return s.cfg.HoroscopeRateLimitGuest
	}
}

// buildSessionKey hashes the input for cache / dedup
func (s *AIHoroscopeService) buildSessionKey(req dto.HoroscopeAIRequest) string {
	depth := req.Depth
	if depth == "" {
		depth = "standard"
	}
	hour := -1
	if req.BirthHour != nil {
		hour = *req.BirthHour
	}
	raw := fmt.Sprintf("%d-%d-%d-%d-%s-%s-%s",
		req.BirthYear, req.BirthMonth, req.BirthDay, hour,
		req.Gender, req.ReadingType, depth,
	)
	hash := sha256.Sum256([]byte(raw))
	return fmt.Sprintf("%x", hash)
}

// calculateBatTu derives four pillars and ngũ hành balance from birth data
func (s *AIHoroscopeService) calculateBatTu(req dto.HoroscopeAIRequest) (dto.BatTuInfo, dto.NguHanhBalance) {
	// Heavenly Stems (Thiên Can)
	stems := []string{"Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"}
	// Earthly Branches (Địa Chi)
	branches := []string{"Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"}
	// Branch elements
	branchElements := map[string]string{
		"Tý": "Thuỷ", "Sửu": "Thổ", "Dần": "Mộc", "Mão": "Mộc",
		"Thìn": "Thổ", "Tỵ": "Hoả", "Ngọ": "Hoả", "Mùi": "Thổ",
		"Thân": "Kim", "Dậu": "Kim", "Tuất": "Thổ", "Hợi": "Thuỷ",
	}
	// Stem elements
	stemElements := map[string]string{
		"Giáp": "Mộc", "Ất": "Mộc", "Bính": "Hoả", "Đinh": "Hoả",
		"Mậu": "Thổ", "Kỷ": "Thổ", "Canh": "Kim", "Tân": "Kim",
		"Nhâm": "Thuỷ", "Quý": "Thuỷ",
	}

	yearStem := stems[(req.BirthYear-4)%10]
	yearBranch := branches[(req.BirthYear-4)%12]
	monthStem := stems[((req.BirthYear-4)*12+req.BirthMonth)%10]
	monthBranch := branches[(req.BirthMonth+1)%12]

	// Day pillar — simplified deterministic calculation
	days := (req.BirthYear-1900)*365 + req.BirthMonth*30 + req.BirthDay
	dayStem := stems[days%10]
	dayBranch := branches[days%12]

	hourBranch := branches[0]
	hourStem := stems[0]
	if req.BirthHour != nil {
		hi := (*req.BirthHour + 1) / 2 % 12
		hourBranch = branches[hi]
		hourStem = stems[(days*2+hi)%10]
	}

	batTu := dto.BatTuInfo{
		YearPillar:  dto.BatTuPillar{HeavenlyStem: yearStem, EarthlyBranch: yearBranch, Element: stemElements[yearStem]},
		MonthPillar: dto.BatTuPillar{HeavenlyStem: monthStem, EarthlyBranch: monthBranch, Element: stemElements[monthStem]},
		DayPillar:   dto.BatTuPillar{HeavenlyStem: dayStem, EarthlyBranch: dayBranch, Element: stemElements[dayStem]},
		HourPillar:  dto.BatTuPillar{HeavenlyStem: hourStem, EarthlyBranch: hourBranch, Element: stemElements[hourStem]},
	}

	// Count ngũ hành
	counts := map[string]int{"Kim": 0, "Mộc": 0, "Thuỷ": 0, "Hoả": 0, "Thổ": 0}
	for _, p := range []dto.BatTuPillar{batTu.YearPillar, batTu.MonthPillar, batTu.DayPillar, batTu.HourPillar} {
		counts[p.Element]++
		if e, ok := branchElements[p.EarthlyBranch]; ok {
			counts[e]++
		}
	}

	strongest, weakest := "Kim", "Kim"
	maxC, minC := -1, 9999
	for e, c := range counts {
		if c > maxC {
			maxC = c
			strongest = e
		}
		if c < minC {
			minC = c
			weakest = e
		}
	}

	nguHanh := dto.NguHanhBalance{
		Kim:       counts["Kim"],
		Moc:       counts["Mộc"],
		Thuy:      counts["Thuỷ"],
		Hoa:       counts["Hoả"],
		Tho:       counts["Thổ"],
		Strongest: strongest,
		Weakest:   weakest,
	}

	return batTu, nguHanh
}

// buildPrompt loads the active template and fills placeholders
func (s *AIHoroscopeService) buildPrompt(req dto.HoroscopeAIRequest, batTu dto.BatTuInfo, nguHanh dto.NguHanhBalance) (system, user string, err error) {
	tpl, err := s.promptRepo.GetActiveByType("horoscope")
	if err != nil {
		return "", "", err
	}

	batTuStr := fmt.Sprintf(
		"Tứ trụ: Năm %s %s | Tháng %s %s | Ngày %s %s | Giờ %s %s\nNgũ hành mạnh nhất: %s | yếu nhất: %s",
		batTu.YearPillar.HeavenlyStem, batTu.YearPillar.EarthlyBranch,
		batTu.MonthPillar.HeavenlyStem, batTu.MonthPillar.EarthlyBranch,
		batTu.DayPillar.HeavenlyStem, batTu.DayPillar.EarthlyBranch,
		batTu.HourPillar.HeavenlyStem, batTu.HourPillar.EarthlyBranch,
		nguHanh.Strongest, nguHanh.Weakest,
	)

	depth := req.Depth
	if depth == "" {
		depth = "standard"
	}
	question := req.Question
	if question == "" {
		question = "Không có câu hỏi cụ thể"
	}

	// Build question section block
	questionSection := ""
	if req.Question != "" {
		questionSection = fmt.Sprintf("Câu hỏi của bạn: %s", req.Question)
	}

	targetYearStr := fmt.Sprintf("%d", req.BirthYear+30) // fallback
	if req.TargetYear != nil {
		targetYearStr = fmt.Sprintf("%d", *req.TargetYear)
	}

	readingRequest := req.ReadingType
	switch req.ReadingType {
	case "general":
		readingRequest = "tổng quan vận mệnh"
	case "love":
		readingRequest = "tình duyên hôn nhân"
	case "career":
		readingRequest = "sự nghiệp tài lộc"
	case "health":
		readingRequest = "sức khoẻ"
	case "year":
		readingRequest = fmt.Sprintf("vận trình năm %s", targetYearStr)
	}

	replacer := strings.NewReplacer(
		// lowercase variants (original)
		"{{bat_tu}}", batTuStr,
		"{{gender}}", req.Gender,
		"{{reading_type}}", req.ReadingType,
		"{{depth}}", depth,
		"{{question}}", question,
		"{{birth_year}}", fmt.Sprintf("%d", req.BirthYear),
		// UPPERCASE variants (used in DB templates)
		"{{STRONGEST}}", nguHanh.Strongest,
		"{{WEAKEST}}", nguHanh.Weakest,
		"{{KIM}}", fmt.Sprintf("%d", nguHanh.Kim),
		"{{MOC}}", fmt.Sprintf("%d", nguHanh.Moc),
		"{{THUY}}", fmt.Sprintf("%d", nguHanh.Thuy),
		"{{HOA}}", fmt.Sprintf("%d", nguHanh.Hoa),
		"{{THO}}", fmt.Sprintf("%d", nguHanh.Tho),
		"{{YEAR_HS}}", batTu.YearPillar.HeavenlyStem,
		"{{YEAR_EB}}", batTu.YearPillar.EarthlyBranch,
		"{{MONTH_HS}}", batTu.MonthPillar.HeavenlyStem,
		"{{MONTH_EB}}", batTu.MonthPillar.EarthlyBranch,
		"{{DAY_HS}}", batTu.DayPillar.HeavenlyStem,
		"{{DAY_EB}}", batTu.DayPillar.EarthlyBranch,
		"{{HOUR_HS}}", batTu.HourPillar.HeavenlyStem,
		"{{HOUR_EB}}", batTu.HourPillar.EarthlyBranch,
		"{{READING_REQUEST}}", readingRequest,
		"{{READING_TYPE}}", req.ReadingType,
		"{{BIRTH_YEAR}}", fmt.Sprintf("%d", req.BirthYear),
		"{{BIRTH_MONTH}}", fmt.Sprintf("%d", req.BirthMonth),
		"{{BIRTH_DAY}}", fmt.Sprintf("%d", req.BirthDay),
		"{{GENDER}}", req.Gender,
		"{{DEPTH}}", depth,
		"{{QUESTION}}", question,
		"{{TARGET_YEAR}}", targetYearStr,
		// Handlebars-style conditional section — strip tags, keep content
		"{{#QUESTION_SECTION}}", questionSection,
		"{{/QUESTION_SECTION}}", "",
		"{{#if question}}", questionSection,
		"{{/if}}", "",
	)

	return replacer.Replace(tpl.SystemPrompt), replacer.Replace(tpl.UserPrompt), nil
}

// defaultPrompt returns a hardcoded fallback prompt
func (s *AIHoroscopeService) defaultPrompt(req dto.HoroscopeAIRequest, batTu dto.BatTuInfo, nguHanh dto.NguHanhBalance) (system, user string) {
	system = `Bạn là chuyên gia tử vi phương Đông, am hiểu Bát Tự (Tứ Trụ), Ngũ Hành và phong thuỷ. 
Hãy phân tích và đưa ra nhận xét chính xác, sâu sắc và hữu ích bằng tiếng Việt.
Trả lời theo cấu trúc rõ ràng: tổng quan, điểm mạnh/yếu, vận trình năm nay, lời khuyên.`

	user = fmt.Sprintf(`Thông tin người dùng:
- Năm sinh: %d | Tháng: %d | Ngày: %d
- Giới tính: %s
- Tứ trụ: Năm %s %s | Tháng %s %s | Ngày %s %s | Giờ %s %s
- Ngũ hành mạnh nhất: %s | Yếu nhất: %s
- Loại xem: %s
- Câu hỏi: %s

Hãy phân tích tử vi chi tiết.`,
		req.BirthYear, req.BirthMonth, req.BirthDay, req.Gender,
		batTu.YearPillar.HeavenlyStem, batTu.YearPillar.EarthlyBranch,
		batTu.MonthPillar.HeavenlyStem, batTu.MonthPillar.EarthlyBranch,
		batTu.DayPillar.HeavenlyStem, batTu.DayPillar.EarthlyBranch,
		batTu.HourPillar.HeavenlyStem, batTu.HourPillar.EarthlyBranch,
		nguHanh.Strongest, nguHanh.Weakest,
		req.ReadingType,
		func() string {
			if req.Question != "" {
				return req.Question
			}
			return "Không có câu hỏi cụ thể"
		}(),
	)
	return
}

// estimateCost estimates USD cost based on model and token counts (rough approximation)
func estimateCost(model string, promptTokens, completionTokens int) float64 {
	// Rough pricing per 1M tokens (input / output) in USD
	pricing := map[string][2]float64{
		"anthropic/claude-sonnet-4":        {3.0, 15.0},
		"anthropic/claude-3.5-sonnet":      {3.0, 15.0}, // legacy alias
		"openai/gpt-4o":                    {2.5, 10.0},
		"openai/gpt-4o-mini":               {0.15, 0.60},
		"deepseek/deepseek-chat":           {0.14, 0.28},
		"google/gemini-flash-1.5":          {0.075, 0.30},
		"meta-llama/llama-3.1-8b-instruct": {0.055, 0.055},
	}

	prices, ok := pricing[model]
	if !ok {
		prices = [2]float64{1.0, 2.0} // default fallback
	}

	return float64(promptTokens)*prices[0]/1_000_000 +
		float64(completionTokens)*prices[1]/1_000_000
}
