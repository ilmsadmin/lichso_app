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

// FolkFestivalHandler handles folk festival HTTP requests.
type FolkFestivalHandler struct {
	service   *services.FolkFestivalService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewFolkFestivalHandler creates a new FolkFestivalHandler.
func NewFolkFestivalHandler(service *services.FolkFestivalService, validator *validators.Validator, logger *zap.Logger) *FolkFestivalHandler {
	return &FolkFestivalHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/festivals
func (h *FolkFestivalHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateFolkFestivalRequest
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

	return utils.SuccessResponse(c, "Đã tạo lễ hội", result)
}

// GetByID handles GET /api/festivals/:id
func (h *FolkFestivalHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid festival ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết lễ hội", result)
}

// GetBySlug handles GET /api/festivals/slug/:slug
func (h *FolkFestivalHandler) GetBySlug(c *fiber.Ctx) error {
	slug := c.Params("slug")
	if slug == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Slug is required")
	}

	result, err := h.service.GetBySlug(slug)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết lễ hội", result)
}

// GetByLunarDate handles GET /api/festivals/lunar/:month/:day
func (h *FolkFestivalHandler) GetByLunarDate(c *fiber.Ctx) error {
	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ")
	}
	day, err := strconv.Atoi(c.Params("day"))
	if err != nil || day < 1 || day > 30 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Ngày không hợp lệ")
	}

	result, err := h.service.GetByLunarDate(month, day)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Lễ hội theo âm lịch", result)
}

// GetBySolarDate handles GET /api/festivals/solar/:month/:day
func (h *FolkFestivalHandler) GetBySolarDate(c *fiber.Ctx) error {
	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ")
	}
	day, err := strconv.Atoi(c.Params("day"))
	if err != nil || day < 1 || day > 31 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Ngày không hợp lệ")
	}

	result, err := h.service.GetBySolarDate(month, day)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Lễ hội theo dương lịch", result)
}

// List handles GET /api/festivals
func (h *FolkFestivalHandler) List(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	festivalType := c.Query("type")
	calendarType := c.Query("calendar")

	result, total, err := h.service.List(pagination.Page, pagination.Limit, festivalType, calendarType)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách lễ hội", result, pagination.Page, pagination.Limit, total)
}

// ListAll handles GET /api/admin/festivals
func (h *FolkFestivalHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách lễ hội", result, pagination.Page, pagination.Limit, total)
}

// Update handles PUT /api/admin/festivals/:id
func (h *FolkFestivalHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid festival ID")
	}

	var req dto.UpdateFolkFestivalRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật lễ hội", result)
}

// Delete handles DELETE /api/admin/festivals/:id
func (h *FolkFestivalHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid festival ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa lễ hội", nil)
}
