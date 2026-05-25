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

// EventHandler handles event-related HTTP requests.
type EventHandler struct {
	service   *services.EventService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewEventHandler creates a new EventHandler.
func NewEventHandler(service *services.EventService, validator *validators.Validator, logger *zap.Logger) *EventHandler {
	return &EventHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/events
func (h *EventHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateEventRequest
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

	return utils.SuccessResponse(c, "Đã tạo sự kiện", result)
}

// GetByID handles GET /api/events/:id
func (h *EventHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid event ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết sự kiện", result)
}

// GetBySlug handles GET /api/events/slug/:slug
func (h *EventHandler) GetBySlug(c *fiber.Ctx) error {
	slug := c.Params("slug")
	if slug == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Slug is required")
	}

	result, err := h.service.GetBySlug(slug)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết sự kiện", result)
}

// GetByDate handles GET /api/events/date/:month/:day?lunar_month=&lunar_day=
func (h *EventHandler) GetByDate(c *fiber.Ctx) error {
	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ")
	}
	day, err := strconv.Atoi(c.Params("day"))
	if err != nil || day < 1 || day > 31 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Ngày không hợp lệ")
	}

	// Lunar date params — fall back to solar if not provided
	lunarMonth, _ := strconv.Atoi(c.Query("lunar_month", "0"))
	lunarDay, _ := strconv.Atoi(c.Query("lunar_day", "0"))
	if lunarMonth < 1 || lunarMonth > 13 {
		lunarMonth = month
	}
	if lunarDay < 1 || lunarDay > 30 {
		lunarDay = day
	}

	result, err := h.service.GetByDate(month, day, lunarMonth, lunarDay)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Sự kiện trong ngày", result)
}

// List handles GET /api/events
func (h *EventHandler) List(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	eventType := c.Query("type")
	importance := c.Query("importance")

	result, total, err := h.service.List(pagination.Page, pagination.Limit, eventType, importance)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách sự kiện", result, pagination.Page, pagination.Limit, total)
}

// ListAll handles GET /api/admin/events
func (h *EventHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách sự kiện", result, pagination.Page, pagination.Limit, total)
}

// Update handles PUT /api/admin/events/:id
func (h *EventHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid event ID")
	}

	var req dto.UpdateEventRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật sự kiện", result)
}

// Delete handles DELETE /api/admin/events/:id
func (h *EventHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid event ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa sự kiện", nil)
}
