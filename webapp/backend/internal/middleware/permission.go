package middleware

import (
	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// PermissionMiddleware handles permission-based access control
type PermissionMiddleware struct {
	rbacService *services.RBACService
	logger      *zap.Logger
}

// NewPermissionMiddleware creates a new PermissionMiddleware
func NewPermissionMiddleware(rbacService *services.RBACService, logger *zap.Logger) *PermissionMiddleware {
	return &PermissionMiddleware{
		rbacService: rbacService,
		logger:      logger,
	}
}

// RequirePermission returns a middleware that checks if the user has a specific permission.
// Super admin bypasses all permission checks.
func (m *PermissionMiddleware) RequirePermission(permission string) fiber.Handler {
	return func(c *fiber.Ctx) error {
		userIDStr, ok := c.Locals("user_id").(string)
		if !ok || userIDStr == "" {
			return utils.ForbiddenResponse(c, "")
		}

		userID, err := uuid.Parse(userIDStr)
		if err != nil {
			return utils.ForbiddenResponse(c, "")
		}

		hasPermission, err := m.rbacService.CheckUserHasPermission(userID, permission)
		if err != nil {
			m.logger.Error("Failed to check permission",
				zap.String("user_id", userIDStr),
				zap.String("permission", permission),
				zap.Error(err),
			)
			return utils.ForbiddenResponse(c, "")
		}

		if !hasPermission {
			return utils.ForbiddenResponse(c, "You don't have the required permission: "+permission)
		}

		return c.Next()
	}
}

// RequireAnyPermission returns a middleware that checks if the user has at least one of the given permissions.
func (m *PermissionMiddleware) RequireAnyPermission(permissions ...string) fiber.Handler {
	return func(c *fiber.Ctx) error {
		userIDStr, ok := c.Locals("user_id").(string)
		if !ok || userIDStr == "" {
			return utils.ForbiddenResponse(c, "")
		}

		userID, err := uuid.Parse(userIDStr)
		if err != nil {
			return utils.ForbiddenResponse(c, "")
		}

		for _, perm := range permissions {
			hasPermission, err := m.rbacService.CheckUserHasPermission(userID, perm)
			if err != nil {
				m.logger.Error("Failed to check permission",
					zap.String("user_id", userIDStr),
					zap.String("permission", perm),
					zap.Error(err),
				)
				continue
			}
			if hasPermission {
				return c.Next()
			}
		}

		return utils.ForbiddenResponse(c, "You don't have any of the required permissions")
	}
}

// RequireRole returns a middleware that checks if the user has one of the given roles.
func (m *PermissionMiddleware) RequireRole(roles ...string) fiber.Handler {
	return func(c *fiber.Ctx) error {
		userRoles, ok := c.Locals("user_roles").([]string)
		if !ok {
			return utils.ForbiddenResponse(c, "")
		}

		// Super admin bypass
		for _, r := range userRoles {
			if r == "super_admin" {
				return c.Next()
			}
		}

		for _, required := range roles {
			for _, userRole := range userRoles {
				if userRole == required {
					return c.Next()
				}
			}
		}

		return utils.ForbiddenResponse(c, "Insufficient role privileges")
	}
}
