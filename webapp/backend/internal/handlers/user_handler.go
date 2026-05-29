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

// UserHandler handles user management HTTP requests
type UserHandler struct {
	userService *services.UserService
	validator   *validators.Validator
	logger      *zap.Logger
}

// NewUserHandler creates a new UserHandler
func NewUserHandler(userService *services.UserService, validator *validators.Validator, logger *zap.Logger) *UserHandler {
	return &UserHandler{
		userService: userService,
		validator:   validator,
		logger:      logger,
	}
}

// List handles GET /api/admin/users
func (h *UserHandler) List(c *fiber.Ctx) error {
	pq := utils.ParsePagination(c)

	users, total, err := h.userService.ListUsers(pq)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list users", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.PaginatedResponse(c, "Users retrieved successfully", users, pq.Page, pq.Limit, total)
}

// Get handles GET /api/admin/users/:id
func (h *UserHandler) Get(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	user, err := h.userService.GetUser(id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get user", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "User retrieved successfully", user)
}

// GetDetail handles GET /api/admin/users/:id/detail — enriched admin view
func (h *UserHandler) GetDetail(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	detail, err := h.userService.GetUserAdminDetail(id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get user detail", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "User detail retrieved", detail)
}

// Create handles POST /api/admin/users
func (h *UserHandler) Create(c *fiber.Ctx) error {
	var req dto.CreateUserRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	if validationErrors := validators.ValidateCreateUser(&req); validationErrors != nil {
		return utils.ValidationErrorResponse(c, validationErrors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	user, err := h.userService.CreateUser(&req, actorID, actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to create user", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "User created successfully", user)
}

// Update handles PUT /api/admin/users/:id
func (h *UserHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	var req dto.UpdateUserRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	user, err := h.userService.UpdateUser(id, &req, actorID, actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update user", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "User updated successfully", user)
}

// Delete handles DELETE /api/admin/users/:id
func (h *UserHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	if err := h.userService.DeleteUser(id, actorID, actorEmail, ip, ua); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to delete user", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "User deleted successfully", nil)
}

// ToggleStatus handles PATCH /api/admin/users/:id/status
func (h *UserHandler) ToggleStatus(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	var req dto.ToggleUserStatusRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	user, err := h.userService.ToggleUserStatus(id, req.IsActive, actorID, actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to toggle user status", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "User status updated successfully", user)
}

// SetRoles handles PUT /api/admin/users/:id/roles
func (h *UserHandler) SetRoles(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	var req dto.UserRolesRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if validationErrors := validators.ValidateUserRoles(req.RoleIDs); validationErrors != nil {
		return utils.ValidationErrorResponse(c, validationErrors)
	}

	actorID, actorEmail := getActorInfo(c)
	ip := c.IP()
	ua := c.Get("User-Agent")

	user, err := h.userService.SetUserRoles(id, req.RoleIDs, actorID, actorEmail, ip, ua)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to set user roles", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "User roles updated successfully", user)
}

// Export handles GET /api/admin/users/export
func (h *UserHandler) Export(c *fiber.Ctx) error {
	pq := utils.ParsePagination(c)

	csvData, err := h.userService.ExportUsersCSV(pq)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to export users", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	c.Set("Content-Type", "text/csv")
	c.Set("Content-Disposition", "attachment; filename=users.csv")
	return c.Send(csvData)
}
