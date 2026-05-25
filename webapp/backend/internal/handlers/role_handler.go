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

// RoleHandler handles role management HTTP requests
type RoleHandler struct {
	roleService *services.RoleService
	validator   *validators.Validator
	logger      *zap.Logger
}

// NewRoleHandler creates a new RoleHandler
func NewRoleHandler(roleService *services.RoleService, validator *validators.Validator, logger *zap.Logger) *RoleHandler {
	return &RoleHandler{
		roleService: roleService,
		validator:   validator,
		logger:      logger,
	}
}

// List handles GET /api/admin/roles
func (h *RoleHandler) List(c *fiber.Ctx) error {
	roles, err := h.roleService.ListRoles()
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list roles", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Roles retrieved successfully", roles)
}

// Get handles GET /api/admin/roles/:id
func (h *RoleHandler) Get(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid role ID")
	}

	role, err := h.roleService.GetRole(id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get role", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Role retrieved successfully", role)
}

// Create handles POST /api/admin/roles
func (h *RoleHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateRoleRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	role, err := h.roleService.CreateRole(&req, actorID, actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to create role", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "Role created successfully", role)
}

// Update handles PUT /api/admin/roles/:id
func (h *RoleHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid role ID")
	}

	var req dto.UpdateRoleRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	role, err := h.roleService.UpdateRole(id, &req, actorID, actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update role", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Role updated successfully", role)
}

// Delete handles DELETE /api/admin/roles/:id
func (h *RoleHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid role ID")
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	if err := h.roleService.DeleteRole(id, actorID, actorEmail, ip, ua); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to delete role", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Role deleted successfully", nil)
}

// AssignRole handles POST /api/admin/roles/assign
func (h *RoleHandler) AssignRole(c *fiber.Ctx) error {
	var req dto.AssignRoleRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	if err := h.roleService.AssignRole(req.UserID, req.RoleID, actorID, actorEmail, ip, ua); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to assign role", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Role assigned successfully", nil)
}

// UnassignRole handles POST /api/admin/roles/unassign
func (h *RoleHandler) UnassignRole(c *fiber.Ctx) error {
	var req dto.UnassignRoleRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	if err := h.roleService.UnassignRole(req.UserID, req.RoleID, actorID, actorEmail, ip, ua); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to unassign role", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Role unassigned successfully", nil)
}

// getActorInfo extracts actor information from request context
func getActorInfo(c *fiber.Ctx) (uuid.UUID, string) {
	userIDStr, _ := c.Locals("user_id").(string)
	userEmail, _ := c.Locals("user_email").(string)
	userID, _ := uuid.Parse(userIDStr)
	return userID, userEmail
}
