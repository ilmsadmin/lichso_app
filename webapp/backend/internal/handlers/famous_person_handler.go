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

// FamousPersonHandler handles famous person HTTP requests.
type FamousPersonHandler struct {
	service   *services.FamousPersonService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewFamousPersonHandler creates a new FamousPersonHandler.
func NewFamousPersonHandler(service *services.FamousPersonService, validator *validators.Validator, logger *zap.Logger) *FamousPersonHandler {
	return &FamousPersonHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/admin/famous-people
func (h *FamousPersonHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateFamousPersonRequest
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

	return utils.SuccessResponse(c, "Đã tạo nhân vật nổi tiếng", result)
}

// GetByID handles GET /api/famous-people/:id
func (h *FamousPersonHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết nhân vật", result)
}

// GetByBirthday handles GET /api/famous-people/birthday/:month/:day
func (h *FamousPersonHandler) GetByBirthday(c *fiber.Ctx) error {
	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ")
	}
	day, err := strconv.Atoi(c.Params("day"))
	if err != nil || day < 1 || day > 31 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Ngày không hợp lệ")
	}

	result, err := h.service.GetByBirthday(month, day)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Nhân vật sinh nhật hôm nay", result)
}

// List handles GET /api/famous-people
func (h *FamousPersonHandler) List(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	category := c.Query("category")

	var isVietnamese *bool
	if vnStr := c.Query("vietnamese"); vnStr != "" {
		vn := vnStr == "true"
		isVietnamese = &vn
	}

	result, total, err := h.service.List(pagination.Page, pagination.Limit, category, isVietnamese)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách nhân vật nổi tiếng", result, pagination.Page, pagination.Limit, total)
}

// ListAll handles GET /api/admin/famous-people
func (h *FamousPersonHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách nhân vật nổi tiếng", result, pagination.Page, pagination.Limit, total)
}

// Update handles PUT /api/admin/famous-people/:id
func (h *FamousPersonHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid ID")
	}

	var req dto.UpdateFamousPersonRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật nhân vật", result)
}

// Delete handles DELETE /api/admin/famous-people/:id
func (h *FamousPersonHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa nhân vật", nil)
}
