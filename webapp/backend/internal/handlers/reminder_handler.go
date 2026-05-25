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

// ReminderHandler handles reminder-related HTTP requests.
type ReminderHandler struct {
	service   *services.ReminderService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewReminderHandler creates a new ReminderHandler.
func NewReminderHandler(service *services.ReminderService, validator *validators.Validator, logger *zap.Logger) *ReminderHandler {
	return &ReminderHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/reminders
func (h *ReminderHandler) Create(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.CreateReminderRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.Create(userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã tạo nhắc nhở", result)
}

// GetAll handles GET /api/reminders
func (h *ReminderHandler) GetAll(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	activeOnly := c.Query("active") == "true"

	var result []dto.ReminderResponse
	if activeOnly {
		result, err = h.service.GetActiveByUser(userID)
	} else {
		result, err = h.service.GetByUser(userID)
	}
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách nhắc nhở", result)
}

// Update handles PUT /api/reminders/:id
func (h *ReminderHandler) Update(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	reminderID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid reminder ID")
	}

	var req dto.UpdateReminderRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(userID, reminderID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật nhắc nhở", result)
}

// Delete handles DELETE /api/reminders/:id
func (h *ReminderHandler) Delete(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	reminderID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid reminder ID")
	}

	if err := h.service.Delete(userID, reminderID); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa nhắc nhở", nil)
}
