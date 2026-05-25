package middleware

import (
	"strings"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// AuthMiddleware handles JWT authentication
type AuthMiddleware struct {
	cfg          *config.Config
	cacheService *services.CacheService
	logger       *zap.Logger
}

// NewAuthMiddleware creates a new AuthMiddleware
func NewAuthMiddleware(cfg *config.Config, cacheService *services.CacheService, logger *zap.Logger) *AuthMiddleware {
	return &AuthMiddleware{
		cfg:          cfg,
		cacheService: cacheService,
		logger:       logger,
	}
}

// Authenticate returns a middleware that verifies JWT access tokens
func (m *AuthMiddleware) Authenticate() fiber.Handler {
	return func(c *fiber.Ctx) error {
		// Extract token from Authorization header
		token := extractBearerToken(c)
		if token == "" {
			return utils.UnauthorizedResponse(c, "Missing or invalid authorization header")
		}

		// Validate token
		claims, err := utils.ValidateToken(token, m.cfg.JWT.Secret)
		if err != nil {
			m.logger.Debug("Token validation failed", zap.Error(err))
			return utils.UnauthorizedResponse(c, "Invalid or expired token")
		}

		// Check token type (must be access token)
		if claims.Type != utils.AccessToken {
			return utils.UnauthorizedResponse(c, "Invalid token type")
		}

		// Check if token is blacklisted
		if claims.ID != "" {
			isBlacklisted, err := m.cacheService.IsTokenBlacklisted(claims.ID)
			if err != nil {
				m.logger.Error("Failed to check token blacklist", zap.Error(err))
			}
			if isBlacklisted {
				return utils.UnauthorizedResponse(c, "Token has been revoked")
			}
		}

		// Calculate remaining TTL for potential blacklisting on logout
		var remainingTTL int64
		if claims.ExpiresAt != nil {
			remaining := time.Until(claims.ExpiresAt.Time)
			if remaining > 0 {
				remainingTTL = int64(remaining.Seconds())
			}
		}

		// Store user info in request context
		c.Locals("user_id", claims.UserID.String())
		c.Locals("user_email", claims.Email)
		c.Locals("user_roles", claims.Roles)
		c.Locals("token_jti", claims.ID)
		c.Locals("token_ttl", remainingTTL)

		return c.Next()
	}
}

// RequireRoles returns a middleware that checks if user has one of the required roles
func (m *AuthMiddleware) RequireRoles(roles ...string) fiber.Handler {
	return func(c *fiber.Ctx) error {
		userRoles, ok := c.Locals("user_roles").([]string)
		if !ok {
			return utils.ForbiddenResponse(c, "")
		}

		// Check if user has at least one of the required roles
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

// RequirePermission returns a middleware that checks if user has a specific permission
func (m *AuthMiddleware) RequirePermission(permission string) fiber.Handler {
	return func(c *fiber.Ctx) error {
		userIDStr, ok := c.Locals("user_id").(string)
		if !ok || userIDStr == "" {
			return utils.ForbiddenResponse(c, "")
		}

		// Check super_admin role (bypass permission check)
		userRoles, _ := c.Locals("user_roles").([]string)
		for _, role := range userRoles {
			if role == "super_admin" {
				return c.Next()
			}
		}

		// Check permission via Redis cache
		userID, err := uuid.Parse(userIDStr)
		if err != nil {
			return utils.ForbiddenResponse(c, "")
		}

		hasPermission, err := m.cacheService.HasPermission(userID, permission)
		if err != nil {
			m.logger.Error("Failed to check permission", zap.Error(err))
			return utils.ForbiddenResponse(c, "")
		}

		if !hasPermission {
			return utils.ForbiddenResponse(c, "You don't have the required permission: "+permission)
		}

		return c.Next()
	}
}

// extractBearerToken extracts the Bearer token from Authorization header
// or from the "token" query parameter (used by WebSocket connections)
func extractBearerToken(c *fiber.Ctx) string {
	auth := c.Get("Authorization")
	if auth != "" {
		parts := strings.SplitN(auth, " ", 2)
		if len(parts) == 2 && strings.EqualFold(parts[0], "bearer") {
			return parts[1]
		}
	}

	// Fallback: check query parameter (for WebSocket upgrade requests)
	if token := c.Query("token"); token != "" {
		return token
	}

	return ""
}
