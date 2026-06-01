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

// PopupHandler handles popup-related HTTP requests.
type PopupHandler struct {
	service   *services.PopupService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewPopupHandler creates a new PopupHandler.
func NewPopupHandler(service *services.PopupService, validator *validators.Validator, logger *zap.Logger) *PopupHandler {
	return &PopupHandler{service: service, validator: validator, logger: logger}
}

// GetActive handles GET /api/popups (public)
func (h *PopupHandler) GetActive(c *fiber.Ctx) error {
	popups, err := h.service.GetActivePopups(clientPlatform(c))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách popup hoạt động", popups)
}

// Create handles POST /api/admin/popups
func (h *PopupHandler) Create(c *fiber.Ctx) error {
	var req dto.CreatePopupRequest
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

	return utils.SuccessResponse(c, "Đã tạo popup", result)
}

// ListAll handles GET /api/admin/popups
func (h *PopupHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách tất cả popup", result, pagination.Page, pagination.Limit, total)
}

// GetByID handles GET /api/admin/popups/:id
func (h *PopupHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid popup ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết popup", result)
}

// Update handles PUT /api/admin/popups/:id
func (h *PopupHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid popup ID")
	}

	var req dto.UpdatePopupRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật popup", result)
}

// Delete handles DELETE /api/admin/popups/:id
func (h *PopupHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid popup ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa popup", nil)
}

// ToggleActive handles PATCH /api/admin/popups/:id/toggle
func (h *PopupHandler) ToggleActive(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid popup ID")
	}

	result, err := h.service.ToggleActive(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã thay đổi trạng thái popup", result)
}
