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

// QuoteHandler handles quote-related HTTP requests.
type QuoteHandler struct {
	service   *services.QuoteService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewQuoteHandler creates a new QuoteHandler.
func NewQuoteHandler(service *services.QuoteService, validator *validators.Validator, logger *zap.Logger) *QuoteHandler {
	return &QuoteHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/quotes
func (h *QuoteHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateQuoteRequest
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

	return utils.SuccessResponse(c, "Đã tạo câu nói", result)
}

// GetToday handles GET /api/quotes/today
func (h *QuoteHandler) GetToday(c *fiber.Ctx) error {
	result, err := h.service.GetToday()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Câu nói hôm nay", result)
}

// GetRandom handles GET /api/quotes/random
func (h *QuoteHandler) GetRandom(c *fiber.Ctx) error {
	result, err := h.service.GetRandom()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Câu nói ngẫu nhiên", result)
}

// GetByID handles GET /api/quotes/:id
func (h *QuoteHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid quote ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết câu nói", result)
}

// List handles GET /api/quotes
func (h *QuoteHandler) List(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	author := c.Query("author")

	result, total, err := h.service.List(pagination.Page, pagination.Limit, author)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách câu nói", result, pagination.Page, pagination.Limit, total)
}

// ListAll handles GET /api/admin/quotes
func (h *QuoteHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách câu nói", result, pagination.Page, pagination.Limit, total)
}

// Update handles PUT /api/admin/quotes/:id
func (h *QuoteHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid quote ID")
	}

	var req dto.UpdateQuoteRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật câu nói", result)
}

// Delete handles DELETE /api/admin/quotes/:id
func (h *QuoteHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid quote ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa câu nói", nil)
}
