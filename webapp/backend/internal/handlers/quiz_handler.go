package handlers

import (
	"strconv"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// QuizHandler handles all quiz-related HTTP requests.
type QuizHandler struct {
	service   *services.QuizService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewQuizHandler creates a new QuizHandler.
func NewQuizHandler(service *services.QuizService, validator *validators.Validator, logger *zap.Logger) *QuizHandler {
	return &QuizHandler{service: service, validator: validator, logger: logger}
}

// ============================================
// Public endpoints
// ============================================

// GetDailyQuestions handles GET /quiz/questions/daily?date=2026-05-25
func (h *QuizHandler) GetDailyQuestions(c *fiber.Ctx) error {
	date := time.Now()
	if ds := c.Query("date"); ds != "" {
		parsed, err := time.Parse("2006-01-02", ds)
		if err != nil {
			return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid date format, use YYYY-MM-DD")
		}
		date = parsed
	}

	questions, err := h.service.GetDailyQuestions(date)
	if err != nil {
		h.logger.Error("GetDailyQuestions failed", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Câu hỏi hàng ngày", questions)
}

// GetQuestions handles GET /quiz/questions?category=&difficulty=&limit=10
func (h *QuizHandler) GetQuestions(c *fiber.Ctx) error {
	category := c.Query("category")
	difficulty := c.Query("difficulty")
	limit, _ := strconv.Atoi(c.Query("limit", "10"))

	questions, err := h.service.GetQuestions(category, difficulty, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách câu hỏi", questions)
}

// GetLeaderboard handles GET /quiz/leaderboard?period=weekly&limit=50
func (h *QuizHandler) GetLeaderboard(c *fiber.Ctx) error {
	period := c.Query("period", "weekly")
	limit, _ := strconv.Atoi(c.Query("limit", "50"))

	scores, err := h.service.GetLeaderboard(period, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Bảng xếp hạng", scores)
}

// ============================================
// Auth-required endpoints
// ============================================

// StartSession handles POST /quiz/sessions
func (h *QuizHandler) StartSession(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req struct {
		SessionType string `json:"session_type"`
		Category    string `json:"category"`
	}
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	session, questions, err := h.service.StartSession(userID, req.SessionType, req.Category)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.CreatedResponse(c, "Đã bắt đầu phiên quiz", fiber.Map{
		"session":   session,
		"questions": questions,
	})
}

// SubmitAnswer handles POST /quiz/sessions/:id/submit
func (h *QuizHandler) SubmitAnswer(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	sessionID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid session ID")
	}

	var req struct {
		QuestionID int64  `json:"question_id"`
		Chosen     string `json:"chosen"`
		TimeMs     int    `json:"time_ms"`
	}
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}
	if req.QuestionID == 0 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "question_id is required")
	}

	result, err := h.service.SubmitAnswer(userID, sessionID, req.QuestionID, req.Chosen, req.TimeMs)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã ghi nhận câu trả lời", result)
}

// FinishSession handles POST /quiz/sessions/:id/finish
func (h *QuizHandler) FinishSession(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	sessionID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid session ID")
	}

	result, err := h.service.FinishSession(userID, sessionID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã hoàn thành phiên quiz", result)
}

// SyncOfflineSessions handles POST /quiz/sessions/sync
func (h *QuizHandler) SyncOfflineSessions(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req services.SyncOfflineQuizRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.SyncOfflineSessions(userID, req.Sessions)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã đồng bộ phiên quiz ngoại tuyến", result)
}

// GetSession handles GET /quiz/sessions/:id
func (h *QuizHandler) GetSession(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	sessionID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid session ID")
	}

	session, err := h.service.GetSession(userID, sessionID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết phiên quiz", session)
}

// GetMyHistory handles GET /quiz/history
func (h *QuizHandler) GetMyHistory(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	sessions, err := h.service.GetUserHistory(userID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Lịch sử quiz", sessions)
}

// GetMyRank handles GET /quiz/leaderboard/me?period=weekly
func (h *QuizHandler) GetMyRank(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	period := c.Query("period", "weekly")

	rank, err := h.service.GetMyRank(userID, period)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Xếp hạng của bạn", rank)
}

// ============================================
// Admin endpoints
// ============================================

// AdminListQuestions handles GET /admin/quiz/questions?search=&category=&difficulty=
func (h *QuizHandler) AdminListQuestions(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	category := c.Query("category")
	difficulty := c.Query("difficulty")
	search := c.Query("search")

	questions, total, err := h.service.ListQuestions(pagination.Page, pagination.Limit, category, difficulty, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách câu hỏi", questions, pagination.Page, pagination.Limit, total)
}

// AdminGenerateQuestions handles POST /admin/quiz/questions/generate
func (h *QuizHandler) AdminGenerateQuestions(c *fiber.Ctx) error {
	var req services.GenerateQuizQuestionsRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if req.Topic == "" && req.Text == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Cần cung cấp topic hoặc text làm nguồn sinh câu hỏi")
	}

	questions, err := h.service.GenerateQuizQuestions(req)
	if err != nil {
		h.logger.Error("AdminGenerateQuestions failed", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Đã sinh câu hỏi", questions)
}

// AdminGenerateTopics handles POST /admin/quiz/topics/generate
func (h *QuizHandler) AdminGenerateTopics(c *fiber.Ctx) error {
	var req services.GenerateQuizTopicsRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}
	if req.Category == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Cần chọn danh mục để gợi ý chủ đề")
	}

	topics, err := h.service.GenerateQuizTopics(req)
	if err != nil {
		h.logger.Error("AdminGenerateTopics failed", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Đã gợi ý chủ đề", topics)
}

// AdminGetQuestion handles GET /admin/quiz/questions/:id
func (h *QuizHandler) AdminGetQuestion(c *fiber.Ctx) error {
	id, err := strconv.ParseInt(c.Params("id"), 10, 64)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid question ID")
	}

	// Access repo via service.
	q, err := h.service.AdminGetQuestion(id)
	if err != nil {
		return utils.NotFoundResponse(c, "Question not found")
	}

	return utils.SuccessResponse(c, "Chi tiết câu hỏi", q)
}

// AdminCreateQuestion handles POST /admin/quiz/questions
func (h *QuizHandler) AdminCreateQuestion(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var q models.QuizQuestion
	if err := c.BodyParser(&q); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	q.CreatedBy = &userID

	if err := h.service.CreateQuestion(&q); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.CreatedResponse(c, "Đã tạo câu hỏi", q)
}

// AdminUpdateQuestion handles PUT /admin/quiz/questions/:id
func (h *QuizHandler) AdminUpdateQuestion(c *fiber.Ctx) error {
	id, err := strconv.ParseInt(c.Params("id"), 10, 64)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid question ID")
	}

	existing, err := h.service.AdminGetQuestion(id)
	if err != nil {
		return utils.NotFoundResponse(c, "Question not found")
	}

	if err := c.BodyParser(existing); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}
	existing.ID = id // ensure ID is not overwritten

	if err := h.service.UpdateQuestion(existing); err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật câu hỏi", existing)
}

// AdminDeleteQuestion handles DELETE /admin/quiz/questions/:id
func (h *QuizHandler) AdminDeleteQuestion(c *fiber.Ctx) error {
	id, err := strconv.ParseInt(c.Params("id"), 10, 64)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid question ID")
	}

	if err := h.service.DeleteQuestion(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.NoContentResponse(c)
}

// AdminSetDailySet handles POST /admin/quiz/daily-sets
func (h *QuizHandler) AdminSetDailySet(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req struct {
		Date        string  `json:"date"`
		QuestionIDs []int64 `json:"question_ids"`
	}
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	date := time.Now()
	if req.Date != "" {
		parsed, err := time.Parse("2006-01-02", req.Date)
		if err != nil {
			return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid date format, use YYYY-MM-DD")
		}
		date = parsed
	}

	if err := h.service.SetDailySet(userID, date, req.QuestionIDs); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật bộ câu hỏi ngày", fiber.Map{
		"date":         date.Format("2006-01-02"),
		"question_ids": req.QuestionIDs,
	})
}

// AdminRandomizeDailySet handles POST /admin/quiz/daily-sets/random
// Randomly picks `count` (default 20) active questions and saves them as the daily set for `date`.
func (h *QuizHandler) AdminRandomizeDailySet(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req struct {
		Date       string `json:"date"`
		Count      int    `json:"count"`
		Category   string `json:"category"`
		Difficulty string `json:"difficulty"`
	}
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	date := time.Now()
	if req.Date != "" {
		parsed, err := time.Parse("2006-01-02", req.Date)
		if err != nil {
			return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid date format, use YYYY-MM-DD")
		}
		date = parsed
	}

	count := req.Count
	if count <= 0 {
		count = 20
	}

	ids, err := h.service.RandomizeDailySet(userID, date, count, req.Category, req.Difficulty)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã tạo bộ câu hỏi ngẫu nhiên cho ngày", fiber.Map{
		"date":         date.Format("2006-01-02"),
		"count":        len(ids),
		"question_ids": ids,
	})
}

// AdminGetDailySets handles GET /admin/quiz/daily-sets
func (h *QuizHandler) AdminGetDailySets(c *fiber.Ctx) error {
	sets, err := h.service.GetDailySets()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách bộ câu hỏi ngày", sets)
}
