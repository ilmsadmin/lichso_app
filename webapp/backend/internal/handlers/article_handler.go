package handlers

import (
	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// ArticleHandler handles article-related HTTP requests.
type ArticleHandler struct {
	service   *services.ArticleService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewArticleHandler creates a new ArticleHandler.
func NewArticleHandler(service *services.ArticleService, validator *validators.Validator, logger *zap.Logger) *ArticleHandler {
	return &ArticleHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/articles
func (h *ArticleHandler) Create(c *fiber.Ctx) error {
	authorID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.CreateArticleRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.Create(authorID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã tạo bài viết", result)
}

// GetByID handles GET /api/articles/:id
func (h *ArticleHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid article ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết bài viết", result)
}

// GetBySlug handles GET /api/articles/slug/:slug
func (h *ArticleHandler) GetBySlug(c *fiber.Ctx) error {
	slug := c.Params("slug")
	if slug == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Slug is required")
	}

	result, err := h.service.GetBySlug(slug)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết bài viết", result)
}

// List handles GET /api/articles
func (h *ArticleHandler) List(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	var categoryID *uuid.UUID
	if catIDStr := c.Query("category_id"); catIDStr != "" {
		catID, err := uuid.Parse(catIDStr)
		if err == nil {
			categoryID = &catID
		}
	}

	var isFeatured *bool
	if featuredStr := c.Query("featured"); featuredStr != "" {
		featured := featuredStr == "true"
		isFeatured = &featured
	}

	// Filter by tag_id
	if tagIDStr := c.Query("tag_id"); tagIDStr != "" {
		tagID, err := uuid.Parse(tagIDStr)
		if err != nil {
			return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid tag ID")
		}
		result, total, err := h.service.ListByTag(tagID, pagination.Page, pagination.Limit)
		if err != nil {
			return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
		}
		return utils.PaginatedResponse(c, "Danh sách bài viết theo tag", result, pagination.Page, pagination.Limit, total)
	}

	result, total, err := h.service.List(pagination.Page, pagination.Limit, search, categoryID, isFeatured)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách bài viết", result, pagination.Page, pagination.Limit, total)
}

// ListAll handles GET /api/admin/articles (admin)
func (h *ArticleHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	status := c.Query("status")
	search := c.Query("search")

	var categoryID *uuid.UUID
	if catIDStr := c.Query("category_id"); catIDStr != "" {
		catID, err := uuid.Parse(catIDStr)
		if err == nil {
			categoryID = &catID
		}
	}

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, status, search, categoryID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách bài viết", result, pagination.Page, pagination.Limit, total)
}

// Search handles GET /api/articles/search
func (h *ArticleHandler) Search(c *fiber.Ctx) error {
	query := c.Query("q")
	if query == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Search query is required")
	}

	pagination := utils.ParsePagination(c)

	result, total, err := h.service.Search(query, pagination.Page, pagination.Limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Kết quả tìm kiếm", result, pagination.Page, pagination.Limit, total)
}

// RandomArticles handles GET /api/articles/random
func (h *ArticleHandler) RandomArticles(c *fiber.Ctx) error {
	limit := c.QueryInt("limit", 5)
	if limit < 1 {
		limit = 1
	}
	if limit > 20 {
		limit = 20
	}

	result, err := h.service.GetRandomArticles(limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Bài viết ngẫu nhiên", result)
}

// Update handles PUT /api/admin/articles/:id
func (h *ArticleHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid article ID")
	}

	var req dto.UpdateArticleRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật bài viết", result)
}

// Delete handles DELETE /api/admin/articles/:id
func (h *ArticleHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid article ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa bài viết", nil)
}

// ============================================
// Article Category Handler
// ============================================

// ArticleCategoryHandler handles article category HTTP requests.
type ArticleCategoryHandler struct {
	service   *services.ArticleCategoryService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewArticleCategoryHandler creates a new ArticleCategoryHandler.
func NewArticleCategoryHandler(service *services.ArticleCategoryService, validator *validators.Validator, logger *zap.Logger) *ArticleCategoryHandler {
	return &ArticleCategoryHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/categories
func (h *ArticleCategoryHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateArticleCategoryRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.Create(&req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã tạo danh mục", result)
}

// GetByID handles GET /api/categories/:id
func (h *ArticleCategoryHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid category ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết danh mục", result)
}

// GetBySlug handles GET /api/categories/slug/:slug
func (h *ArticleCategoryHandler) GetBySlug(c *fiber.Ctx) error {
	slug := c.Params("slug")
	if slug == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Slug is required")
	}

	cat, err := h.service.GetBySlug(slug)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết danh mục", cat)
}

// List handles GET /api/categories
func (h *ArticleCategoryHandler) List(c *fiber.Ctx) error {
	result, err := h.service.List()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách danh mục", result)
}

// ListAll handles GET /api/admin/categories
func (h *ArticleCategoryHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách danh mục", result, pagination.Page, pagination.Limit, total)
}

// Update handles PUT /api/admin/categories/:id
func (h *ArticleCategoryHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid category ID")
	}

	var req dto.UpdateArticleCategoryRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật danh mục", result)
}

// Delete handles DELETE /api/admin/categories/:id
func (h *ArticleCategoryHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid category ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa danh mục", nil)
}

// ============================================
// Article Tag Handler
// ============================================

// ArticleTagHandler handles article tag HTTP requests.
type ArticleTagHandler struct {
	service   *services.ArticleTagService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewArticleTagHandler creates a new ArticleTagHandler.
func NewArticleTagHandler(service *services.ArticleTagService, validator *validators.Validator, logger *zap.Logger) *ArticleTagHandler {
	return &ArticleTagHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/tags
func (h *ArticleTagHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateArticleTagRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.Create(&req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã tạo tag", result)
}

// GetByID handles GET /api/tags/:id
func (h *ArticleTagHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid tag ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết tag", result)
}

// GetBySlug handles GET /api/tags/slug/:slug
func (h *ArticleTagHandler) GetBySlug(c *fiber.Ctx) error {
	slug := c.Params("slug")
	if slug == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Slug is required")
	}

	result, err := h.service.GetBySlug(slug)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết tag", result)
}

// List handles GET /api/tags
func (h *ArticleTagHandler) List(c *fiber.Ctx) error {
	result, err := h.service.List()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách tag", result)
}

// ListAll handles GET /api/admin/tags
func (h *ArticleTagHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách tag", result, pagination.Page, pagination.Limit, total)
}

// Update handles PUT /api/admin/tags/:id
func (h *ArticleTagHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid tag ID")
	}

	var req dto.UpdateArticleTagRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật tag", result)
}

// Delete handles DELETE /api/admin/tags/:id
func (h *ArticleTagHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid tag ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa tag", nil)
}
