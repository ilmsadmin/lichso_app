package handlers

import (
	"context"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// SettingHandler handles settings HTTP requests
type SettingHandler struct {
	settingService *services.SettingService
	validator      *validators.Validator
	logger         *zap.Logger
}

// NewSettingHandler creates a new SettingHandler
func NewSettingHandler(settingService *services.SettingService, validator *validators.Validator, logger *zap.Logger) *SettingHandler {
	return &SettingHandler{
		settingService: settingService,
		validator:      validator,
		logger:         logger,
	}
}

// List handles GET /api/admin/settings
func (h *SettingHandler) List(c *fiber.Ctx) error {
	ctx := context.Background()

	settings, err := h.settingService.GetAllSettings(ctx)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list settings", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Settings retrieved successfully", settings)
}

// ListGrouped handles GET /api/admin/settings/grouped
func (h *SettingHandler) ListGrouped(c *fiber.Ctx) error {
	ctx := context.Background()

	grouped, err := h.settingService.GetGroupedSettings(ctx)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list grouped settings", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Grouped settings retrieved successfully", grouped)
}

// GetByGroup handles GET /api/admin/settings/group/:group
func (h *SettingHandler) GetByGroup(c *fiber.Ctx) error {
	ctx := context.Background()
	group := c.Params("group")

	if group == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Group parameter is required")
	}

	settings, err := h.settingService.GetSettingsByGroup(ctx, group)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get settings by group", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Settings retrieved successfully", settings)
}

// Get handles GET /api/admin/settings/:key
func (h *SettingHandler) Get(c *fiber.Ctx) error {
	ctx := context.Background()
	key := c.Params("key")

	setting, err := h.settingService.GetSetting(ctx, key)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get setting", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Setting retrieved successfully", setting)
}

// Update handles PUT /api/admin/settings/:key
func (h *SettingHandler) Update(c *fiber.Ctx) error {
	ctx := context.Background()
	key := c.Params("key")

	var req dto.UpdateSettingRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	req.Key = key

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	setting, err := h.settingService.UpdateSetting(ctx, &req, actorID.String(), actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update setting", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Setting updated successfully", setting)
}

// UpdateGroup handles PUT /api/admin/settings/group/:group
func (h *SettingHandler) UpdateGroup(c *fiber.Ctx) error {
	ctx := context.Background()
	group := c.Params("group")

	var req dto.UpdateSettingsGroupRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	req.Group = group

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	settings, err := h.settingService.UpdateSettingsGroup(ctx, &req, actorID.String(), actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update settings group", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Settings group updated successfully", settings)
}

// Create handles POST /api/admin/settings
func (h *SettingHandler) Create(c *fiber.Ctx) error {
	ctx := context.Background()

	var req dto.CreateSettingRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	actorID, _ := getActorInfo(c)

	setting, err := h.settingService.CreateSetting(ctx, &req, actorID.String())
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to create setting", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "Setting created successfully", setting)
}

// Delete handles DELETE /api/admin/settings/:key
func (h *SettingHandler) Delete(c *fiber.Ctx) error {
	ctx := context.Background()
	key := c.Params("key")

	if err := h.settingService.DeleteSetting(ctx, key); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to delete setting", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Setting deleted successfully", nil)
}
