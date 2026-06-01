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

// SurveyHandler handles HTTP request mapping for survey features.
type SurveyHandler struct {
	service   *services.SurveyService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewSurveyHandler creates a new SurveyHandler.
func NewSurveyHandler(service *services.SurveyService, validator *validators.Validator, logger *zap.Logger) *SurveyHandler {
	return &SurveyHandler{service: service, validator: validator, logger: logger}
}

// GetActive handles GET /api/surveys/active (Public/Client)
func (h *SurveyHandler) GetActive(c *fiber.Ctx) error {
	survey, err := h.service.GetLatestActive()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}
	if survey == nil {
		return utils.SuccessResponse(c, "No active survey available", nil)
	}
	return utils.SuccessResponse(c, "Active survey", survey)
}

// SubmitResponse handles POST /api/surveys/active/responses (Client)
func (h *SurveyHandler) SubmitResponse(c *fiber.Ctx) error {
	var req dto.SubmitSurveyResponseRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	activeSurvey, err := h.service.GetLatestActive()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}
	if activeSurvey == nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, "No active survey to submit responses to")
	}

	surveyID, err := uuid.Parse(activeSurvey.ID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Invalid active survey ID format")
	}

	var userID *uuid.UUID
	if userIDStr, ok := c.Locals("user_id").(string); ok && userIDStr != "" {
		if parsed, err := uuid.Parse(userIDStr); err == nil {
			userID = &parsed
		}
	}

	if err := h.service.SubmitResponse(surveyID, &req, userID); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Khảo sát đã được gửi thành công", nil)
}

// Create handles POST /api/admin/surveys
func (h *SurveyHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateSurveyRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	var actorID *uuid.UUID
	if userIDStr, ok := c.Locals("user_id").(string); ok && userIDStr != "" {
		if parsed, err := uuid.Parse(userIDStr); err == nil {
			actorID = &parsed
		}
	}

	result, err := h.service.Create(&req, actorID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã tạo khảo sát thành công", result)
}

// ListAll handles GET /api/admin/surveys
func (h *SurveyHandler) ListAll(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	search := c.Query("search")

	result, total, err := h.service.ListAll(pagination.Page, pagination.Limit, search)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách khảo sát", result, pagination.Page, pagination.Limit, total)
}

// GetByID handles GET /api/admin/surveys/:id
func (h *SurveyHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid survey ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết khảo sát", result)
}

// Update handles PUT /api/admin/surveys/:id
func (h *SurveyHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid survey ID")
	}

	var req dto.UpdateSurveyRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật khảo sát thành công", result)
}

// Delete handles DELETE /api/admin/surveys/:id
func (h *SurveyHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid survey ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa khảo sát thành công", nil)
}

// ToggleActive handles PATCH /api/admin/surveys/:id/toggle
func (h *SurveyHandler) ToggleActive(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid survey ID")
	}

	result, err := h.service.ToggleActive(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã thay đổi trạng thái kích hoạt", result)
}

// GetStats handles GET /api/admin/surveys/:id/stats
func (h *SurveyHandler) GetStats(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid survey ID")
	}

	result, err := h.service.GetSurveyStats(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Thống kê khảo sát", result)
}
