package handlers

import (
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// ArticleRelationHandler handles article relation HTTP requests.
type ArticleRelationHandler struct {
	service   *services.ArticleRelationService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewArticleRelationHandler creates a new ArticleRelationHandler.
func NewArticleRelationHandler(service *services.ArticleRelationService, validator *validators.Validator, logger *zap.Logger) *ArticleRelationHandler {
	return &ArticleRelationHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/articles/:id/relations
func (h *ArticleRelationHandler) Create(c *fiber.Ctx) error {
	articleID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid article ID")
	}

	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.CreateArticleRelationRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.Create(articleID, userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.CreatedResponse(c, "Đã thêm bài viết liên quan", result)
}

// BatchCreate handles POST /api/admin/articles/:id/relations/batch
func (h *ArticleRelationHandler) BatchCreate(c *fiber.Ctx) error {
	articleID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid article ID")
	}

	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.BatchCreateArticleRelationsRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	results, err := h.service.BatchCreate(articleID, userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.CreatedResponse(c, "Đã thêm các bài viết liên quan", results)
}

// GetRelations handles GET /api/admin/articles/:id/relations
func (h *ArticleRelationHandler) GetRelations(c *fiber.Ctx) error {
	articleID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid article ID")
	}

	relationType := c.Query("type", "")

	results, err := h.service.GetRelations(articleID, relationType)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách bài viết liên quan", results)
}

// GetRelatedArticles handles GET /api/articles/:id/related (public)
func (h *ArticleRelationHandler) GetRelatedArticles(c *fiber.Ctx) error {
	articleID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid article ID")
	}

	limit, _ := strconv.Atoi(c.Query("limit", "10"))
	if limit <= 0 || limit > 50 {
		limit = 10
	}

	result, err := h.service.GetRelatedArticles(articleID, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Bài viết liên quan", result)
}

// Update handles PUT /api/admin/articles/relations/:relationId
func (h *ArticleRelationHandler) Update(c *fiber.Ctx) error {
	relationID, err := uuid.Parse(c.Params("relationId"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid relation ID")
	}

	var req dto.UpdateArticleRelationRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(relationID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật quan hệ bài viết", result)
}

// Delete handles DELETE /api/admin/articles/relations/:relationId
func (h *ArticleRelationHandler) Delete(c *fiber.Ctx) error {
	relationID, err := uuid.Parse(c.Params("relationId"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid relation ID")
	}

	if err := h.service.Delete(relationID); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa quan hệ bài viết", nil)
}
