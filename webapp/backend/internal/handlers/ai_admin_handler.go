package handlers

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// ============================================
// AIAdminHandler
// ============================================

// AIAdminHandler handles admin endpoints for AI management
type AIAdminHandler struct {
	logRepo    *repositories.AILogRepository
	promptRepo *repositories.AIPromptTemplateRepository
	openRouter *services.OpenRouterService
	validator  *validators.Validator
	logger     *zap.Logger
}

// NewAIAdminHandler creates a new AIAdminHandler
func NewAIAdminHandler(logRepo *repositories.AILogRepository, promptRepo *repositories.AIPromptTemplateRepository, validator *validators.Validator, logger *zap.Logger) *AIAdminHandler {
	return &AIAdminHandler{logRepo: logRepo, promptRepo: promptRepo, validator: validator, logger: logger}
}

// SetOpenRouter sets the OpenRouter service (called from main.go after creation)
func (h *AIAdminHandler) SetOpenRouter(svc *services.OpenRouterService) {
	h.openRouter = svc
}

// TestConnection handles POST /api/v4/admin/ai/test-connection
// It performs a minimal chat completion to verify the API key & base URL.
func (h *AIAdminHandler) TestConnection(c *fiber.Ctx) error {
	var req struct {
		APIKey  string `json:"api_key"`
		BaseURL string `json:"base_url"`
		Model   string `json:"model"`
	}
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}

	apiKey := req.APIKey
	baseURL := req.BaseURL
	if baseURL == "" {
		baseURL = "https://openrouter.ai/api/v1"
	}
	model := req.Model
	if model == "" {
		model = "openai/gpt-4o-mini"
	}

	if apiKey == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "API key không được để trống")
	}

	// Build a minimal chat completion request
	type msg struct {
		Role    string `json:"role"`
		Content string `json:"content"`
	}
	type chatReq struct {
		Model     string `json:"model"`
		Messages  []msg  `json:"messages"`
		MaxTokens int    `json:"max_tokens"`
	}

	payload := chatReq{
		Model: model,
		Messages: []msg{
			{Role: "user", Content: "Respond with only the word: OK"},
		},
		MaxTokens: 5,
	}

	body, _ := json.Marshal(payload)

	httpReq, err := http.NewRequestWithContext(c.Context(), http.MethodPost, baseURL+"/chat/completions", bytes.NewReader(body))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Không thể tạo request: "+err.Error())
	}
	httpReq.Header.Set("Authorization", "Bearer "+apiKey)
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("HTTP-Referer", "https://lichso.vn")
	httpReq.Header.Set("X-Title", "LichSo.vn")

	client := &http.Client{Timeout: 15 * time.Second}
	start := time.Now()
	resp, err := client.Do(httpReq)
	latency := time.Since(start).Milliseconds()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadGateway, "Không kết nối được tới OpenRouter: "+err.Error())
	}
	defer resp.Body.Close()

	raw, _ := io.ReadAll(resp.Body)

	if resp.StatusCode == http.StatusUnauthorized || resp.StatusCode == http.StatusForbidden {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, fmt.Sprintf("API key không hợp lệ (HTTP %d)", resp.StatusCode))
	}

	if resp.StatusCode != http.StatusOK {
		return utils.ErrorResponse(c, fiber.StatusBadGateway, fmt.Sprintf("OpenRouter trả về HTTP %d: %s", resp.StatusCode, string(raw)))
	}

	var result struct {
		Choices []struct {
			Message struct {
				Content string `json:"content"`
			} `json:"message"`
		} `json:"choices"`
		Model string `json:"model"`
	}
	if err := json.Unmarshal(raw, &result); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadGateway, "Phản hồi không hợp lệ từ OpenRouter")
	}

	reply := ""
	if len(result.Choices) > 0 {
		reply = result.Choices[0].Message.Content
	}

	return utils.SuccessResponse(c, "Kết nối thành công", fiber.Map{
		"model":      result.Model,
		"reply":      reply,
		"latency_ms": latency,
	})
}

// GetStats handles GET /api/v4/admin/ai/stats
func (h *AIAdminHandler) GetStats(c *fiber.Ctx) error {
	days := c.QueryInt("days", 30)
	to := time.Now()
	from := to.AddDate(0, 0, -days)

	stats, err := h.logRepo.Stats(from, to)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy thống kê AI")
	}

	byDay, err := h.logRepo.CostByDay(from, to)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy chi phí theo ngày")
	}

	stats["cost_by_day"] = byDay
	stats["from"] = from.Format("2006-01-02")
	stats["to"] = to.Format("2006-01-02")

	return utils.SuccessResponse(c, "Thống kê AI", stats)
}

// GetLogs handles GET /api/v4/admin/ai/logs
func (h *AIAdminHandler) GetLogs(c *fiber.Ctx) error {
	genType := c.Query("type")
	model := c.Query("model")
	page := c.QueryInt("page", 1)
	limit := c.QueryInt("limit", 50)

	var from, to time.Time
	if f := c.Query("from"); f != "" {
		from, _ = time.Parse("2006-01-02", f)
	}
	if t := c.Query("to"); t != "" {
		to, _ = time.Parse("2006-01-02", t)
		to = to.Add(24 * time.Hour)
	}

	logs, total, err := h.logRepo.ListWithFilter(genType, model, from, to, page, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy logs AI")
	}

	return utils.SuccessResponse(c, "Logs AI", fiber.Map{
		"data":  logs,
		"total": total,
		"page":  page,
		"limit": limit,
	})
}

// ListPrompts handles GET /api/admin/ai/prompts
func (h *AIAdminHandler) ListPrompts(c *fiber.Ctx) error {
	tplType := c.Query("type")
	templates, err := h.promptRepo.List(tplType)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy danh sách prompt")
	}
	return utils.SuccessResponse(c, "Danh sách prompt templates", templates)
}

// GetPrompt handles GET /api/admin/ai/prompts/:id
func (h *AIAdminHandler) GetPrompt(c *fiber.Ctx) error {
	id, err := c.ParamsInt("id")
	if err != nil || id <= 0 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "ID không hợp lệ")
	}

	tpl, err := h.promptRepo.GetByID(uint64(id))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, "Không tìm thấy prompt template")
	}
	return utils.SuccessResponse(c, "Prompt template", tpl)
}

// CreatePrompt handles POST /api/v4/admin/ai/prompts
func (h *AIAdminHandler) CreatePrompt(c *fiber.Ctx) error {
	var req dto.AIPromptTemplateRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}
	if errs := h.validator.ValidateStruct(&req); errs != nil {
		return c.Status(fiber.StatusUnprocessableEntity).JSON(fiber.Map{"errors": errs})
	}

	maxTokens := req.MaxTokens
	if maxTokens == 0 {
		maxTokens = 2048
	}
	temp := req.Temperature
	if temp == 0 {
		temp = 0.7
	}

	tpl := services.NewPromptTemplateFromRequest(req)
	if err := h.promptRepo.Create(tpl); err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi tạo prompt template")
	}
	return utils.CreatedResponse(c, "Đã tạo prompt template", tpl)
}

// UpdatePrompt handles PUT /api/v4/admin/ai/prompts/:id
func (h *AIAdminHandler) UpdatePrompt(c *fiber.Ctx) error {
	id, err := c.ParamsInt("id")
	if err != nil || id <= 0 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "ID không hợp lệ")
	}

	var req dto.AIPromptTemplateRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}

	tpl, err := h.promptRepo.GetByID(uint64(id))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, "Không tìm thấy prompt template")
	}

	tpl.Name = req.Name
	tpl.Type = req.Type
	tpl.SystemPrompt = req.SystemPrompt
	tpl.UserPrompt = req.UserPrompt
	tpl.IsActive = req.IsActive
	if req.Model != "" {
		tpl.Model = req.Model
	}
	if req.MaxTokens > 0 {
		tpl.MaxTokens = req.MaxTokens
	}
	if req.Temperature > 0 {
		tpl.Temperature = req.Temperature
	}

	if err := h.promptRepo.Update(tpl); err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi cập nhật prompt template")
	}

	return utils.SuccessResponse(c, "Đã cập nhật prompt template", tpl)
}

// DeletePrompt handles DELETE /api/v4/admin/ai/prompts/:id
func (h *AIAdminHandler) DeletePrompt(c *fiber.Ctx) error {
	id, err := c.ParamsInt("id")
	if err != nil || id <= 0 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "ID không hợp lệ")
	}

	if err := h.promptRepo.Delete(uint64(id)); err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi xoá prompt template")
	}

	return utils.SuccessResponse(c, "Đã xoá prompt template", nil)
}
