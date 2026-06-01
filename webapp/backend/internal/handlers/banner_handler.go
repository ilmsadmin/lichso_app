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

// BannerHandler handles banner-related HTTP requests.
type BannerHandler struct {
	service   *services.BannerService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewBannerHandler creates a new BannerHandler.
func NewBannerHandler(service *services.BannerService, validator *validators.Validator, logger *zap.Logger) *BannerHandler {
	return &BannerHandler{service: service, validator: validator, logger: logger}
}

// GetActive handles GET /api/banners (public)
func (h *BannerHandler) GetActive(c *fiber.Ctx) error {
	location := c.Query("location")
	if location == "" {
		location = c.Query("type") // fallback for old clients
	}
	banners, err := h.service.GetActiveBanners(clientPlatform(c), location)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách banner", banners)
}

// Create handles POST /api/admin/banners
func (h *BannerHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateBannerRequest
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

	return utils.SuccessResponse(c, "Đã tạo banner", result)
}

// ListAll handles GET /api/admin/banners
func (h *BannerHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách banner", result, pagination.Page, pagination.Limit, total)
}

// GetByID handles GET /api/admin/banners/:id
func (h *BannerHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid banner ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết banner", result)
}

// Update handles PUT /api/admin/banners/:id
func (h *BannerHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid banner ID")
	}

	var req dto.UpdateBannerRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật banner", result)
}

// Delete handles DELETE /api/admin/banners/:id
func (h *BannerHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid banner ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa banner", nil)
}

// ToggleActive handles PATCH /api/admin/banners/:id/toggle
func (h *BannerHandler) ToggleActive(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid banner ID")
	}

	result, err := h.service.ToggleActive(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã thay đổi trạng thái banner", result)
}

// Reorder handles PUT /api/admin/banners/reorder
func (h *BannerHandler) Reorder(c *fiber.Ctx) error {
	var req dto.ReorderBannersRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	if err := h.service.Reorder(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã sắp xếp lại banner", nil)
}
