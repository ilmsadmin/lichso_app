package handlers

import (
	"bufio"
	"context"
	"encoding/json"
	"fmt"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// ============================================
// AIArticleHandler
// ============================================

// AIArticleHandler handles AI article generation endpoints (admin only)
type AIArticleHandler struct {
	service   *services.AIArticleService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewAIArticleHandler creates a new AIArticleHandler
func NewAIArticleHandler(svc *services.AIArticleService, validator *validators.Validator, logger *zap.Logger) *AIArticleHandler {
	return &AIArticleHandler{service: svc, validator: validator, logger: logger}
}

// GenerateArticle handles POST /api/v4/ai/articles/generate
// Supports SSE streaming when ?stream=true or body.stream=true
func (h *AIArticleHandler) GenerateArticle(c *fiber.Ctx) error {
	var req dto.AIArticleGenerateRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}
	if errs := h.validator.ValidateStruct(&req); errs != nil {
		return c.Status(fiber.StatusUnprocessableEntity).JSON(fiber.Map{"errors": errs})
	}

	authorID := h.extractAuthorID(c)
	stream := c.QueryBool("stream", false)

	if stream {
		c.Set("Content-Type", "text/event-stream")
		c.Set("Cache-Control", "no-cache")
		c.Set("Connection", "keep-alive")
		c.Set("X-Accel-Buffering", "no")

		// IMPORTANT: capture a standalone context BEFORE entering SetBodyStreamWriter.
		// fasthttp recycles RequestCtx after the handler returns, so c.Context() inside
		// the stream goroutine is a nil/dangling pointer causing panic.
		streamCtx := context.Background()

		c.Context().SetBodyStreamWriter(func(w *bufio.Writer) {
			onChunk := func(delta string) error {
				chunk, _ := json.Marshal(fiber.Map{"delta": delta})
				_, err := fmt.Fprintf(w, "data: %s\n\n", chunk)
				if err != nil {
					return err
				}
				return w.Flush()
			}

			result, err := h.service.GenerateArticleStream(streamCtx, req, authorID, onChunk)
			if err != nil {
				errChunk, _ := json.Marshal(fiber.Map{"error": err.Error()})
				fmt.Fprintf(w, "data: %s\n\n", errChunk)
				w.Flush()
				return
			}

			done, _ := json.Marshal(fiber.Map{
				"done":   true,
				"result": result,
			})
			fmt.Fprintf(w, "data: %s\n\n", done)
			w.Flush()
		})
		return nil
	}

	result, err := h.service.GenerateArticle(c.Context(), req, authorID)
	if err != nil {
		h.logger.Error("GenerateArticle error", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi tạo bài viết AI")
	}
	return utils.CreatedResponse(c, "Đã tạo bài viết AI nháp", result)
}

// QuickDraft handles POST /api/v4/ai/articles/quick-draft
func (h *AIArticleHandler) QuickDraft(c *fiber.Ctx) error {
	var req dto.AIArticleQuickDraftRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}
	if errs := h.validator.ValidateStruct(&req); errs != nil {
		return c.Status(fiber.StatusUnprocessableEntity).JSON(fiber.Map{"errors": errs})
	}

	authorID := h.extractAuthorID(c)
	result, err := h.service.QuickDraft(c.Context(), req, authorID)
	if err != nil {
		h.logger.Error("QuickDraft error", zap.Error(err), zap.String("topic", req.Topic))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, fmt.Sprintf("Lỗi tạo nháp nhanh: %v", err))
	}
	return utils.CreatedResponse(c, "Bản nháp nhanh đã tạo", result)
}

// SuggestTopics handles GET /api/v4/ai/articles/topics
func (h *AIArticleHandler) SuggestTopics(c *fiber.Ctx) error {
	categoryID := c.Query("category_id", "")
	model := c.Query("model", "")
	topics, err := h.service.SuggestTopics(c.Context(), categoryID, model)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi gợi ý chủ đề")
	}
	return utils.SuccessResponse(c, "Gợi ý chủ đề", fiber.Map{"topics": topics})
}

// ListAIArticles handles GET /api/admin/ai/articles?status=ai_pending|draft|review
func (h *AIArticleHandler) ListAIArticles(c *fiber.Ctx) error {
	status := c.Query("status", "ai_pending")
	page := c.QueryInt("page", 1)
	pageSize := c.QueryInt("limit", 20)

	articles, total, err := h.service.ListAIArticles(c.Context(), status, page, pageSize)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy danh sách bài viết AI")
	}
	return utils.SuccessResponse(c, "Danh sách bài viết AI", fiber.Map{
		"data":  articles,
		"total": total,
		"page":  page,
		"limit": pageSize,
	})
}

// RefineArticle handles POST /api/admin/ai/articles/:id/refine
// Uses AI to expand and improve an existing ai_pending article
func (h *AIArticleHandler) RefineArticle(c *fiber.Ctx) error {
	articleID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "ID bài viết không hợp lệ")
	}

	var req dto.AIArticleRefineRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}

	authorID := h.extractAuthorID(c)
	result, err := h.service.RefineArticle(c.Context(), articleID, req, authorID)
	if err != nil {
		h.logger.Error("RefineArticle error", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi viết chi tiết bài viết: "+err.Error())
	}
	return utils.SuccessResponse(c, "Đã viết chi tiết bài viết", result)
}

// UpdateArticleStatus handles PATCH /api/admin/ai/articles/:id/status
func (h *AIArticleHandler) UpdateArticleStatus(c *fiber.Ctx) error {
	articleID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "ID bài viết không hợp lệ")
	}

	var req dto.AIArticleStatusRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}

	if err := h.service.UpdateArticleStatus(c.Context(), articleID, req.Status); err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi cập nhật trạng thái bài viết")
	}
	return utils.SuccessResponse(c, "Đã cập nhật trạng thái", fiber.Map{"status": req.Status})
}

// ListLowQualityArticles handles GET /api/admin/ai/articles/low-quality
// Returns articles from admin with content under N words (chưa chất lượng)
// Supports filters: max_words, status, search, category_id (same as admin/articles)
func (h *AIArticleHandler) ListLowQualityArticles(c *fiber.Ctx) error {
	maxWords := c.QueryInt("max_words", 500)
	page := c.QueryInt("page", 1)
	limit := c.QueryInt("limit", 20)
	status := c.Query("status")
	search := c.Query("search")

	var categoryID *uuid.UUID
	if catIDStr := c.Query("category_id"); catIDStr != "" {
		if catID, err := uuid.Parse(catIDStr); err == nil {
			categoryID = &catID
		}
	}

	articles, total, err := h.service.ListLowQualityArticles(c.Context(), maxWords, page, limit, status, search, categoryID)
	if err != nil {
		h.logger.Error("ListLowQualityArticles error", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy danh sách bài viết chưa chất lượng")
	}

	return utils.SuccessResponse(c, "Bài viết chưa chất lượng", fiber.Map{
		"data":      articles,
		"total":     total,
		"page":      page,
		"limit":     limit,
		"max_words": maxWords,
	})
}

// CountLowQualityArticles handles GET /api/admin/ai/articles/low-quality/count
func (h *AIArticleHandler) CountLowQualityArticles(c *fiber.Ctx) error {
	maxWords := c.QueryInt("max_words", 500)
	status := c.Query("status")

	count, err := h.service.CountLowQualityArticles(c.Context(), maxWords, status)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi đếm bài viết chưa chất lượng")
	}

	return utils.SuccessResponse(c, "Số bài viết chưa chất lượng", fiber.Map{
		"count":     count,
		"max_words": maxWords,
	})
}

// BulkRewriteArticles handles POST /api/admin/ai/articles/bulk-rewrite
// Rewrites selected low-quality articles using AI
func (h *AIArticleHandler) BulkRewriteArticles(c *fiber.Ctx) error {
	var req dto.AIBulkRewriteRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}
	if errs := h.validator.ValidateStruct(&req); errs != nil {
		return c.Status(fiber.StatusUnprocessableEntity).JSON(fiber.Map{"errors": errs})
	}

	if len(req.ArticleIDs) > 50 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tối đa 50 bài viết mỗi lần")
	}

	authorID := h.extractAuthorID(c)
	result, err := h.service.BulkRewriteLowQuality(c.Context(), req, authorID)
	if err != nil {
		h.logger.Error("BulkRewriteArticles error", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi viết lại bài viết hàng loạt")
	}

	return utils.SuccessResponse(c, "Kết quả viết lại bài viết chưa chất lượng", result)
}

// extractAuthorID parses the author UUID from auth locals
func (h *AIArticleHandler) extractAuthorID(c *fiber.Ctx) uuid.UUID {
	userIDStr, _ := c.Locals("user_id").(string)
	if id, err := uuid.Parse(userIDStr); err == nil {
		return id
	}
	return uuid.Nil
}
