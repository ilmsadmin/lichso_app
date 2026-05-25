package handlers

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// PermissionHandler handles permission management HTTP requests
type PermissionHandler struct {
	permissionService *services.PermissionService
	logger            *zap.Logger
}

// NewPermissionHandler creates a new PermissionHandler
func NewPermissionHandler(permissionService *services.PermissionService, logger *zap.Logger) *PermissionHandler {
	return &PermissionHandler{
		permissionService: permissionService,
		logger:            logger,
	}
}

// List handles GET /api/admin/permissions
func (h *PermissionHandler) List(c *fiber.Ctx) error {
	permissions, err := h.permissionService.ListPermissions()
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list permissions", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Permissions retrieved successfully", permissions)
}

// ListGrouped handles GET /api/admin/permissions/grouped
func (h *PermissionHandler) ListGrouped(c *fiber.Ctx) error {
	grouped, err := h.permissionService.ListPermissionsGrouped()
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list grouped permissions", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Grouped permissions retrieved successfully", grouped)
}

// Modules handles GET /api/admin/permissions/modules
func (h *PermissionHandler) Modules(c *fiber.Ctx) error {
	modules, err := h.permissionService.GetModules()
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get modules", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Modules retrieved successfully", modules)
}
