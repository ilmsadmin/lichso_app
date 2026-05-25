package middleware

import (
	"time"

	"github.com/gofiber/fiber/v2"
	"go.uber.org/zap"
)

// RequestLogger creates a request logging middleware using zap
func RequestLogger(logger *zap.Logger) fiber.Handler {
	return func(c *fiber.Ctx) error {
		start := time.Now()

		// Process request
		err := c.Next()

		// Calculate duration
		duration := time.Since(start)
		status := c.Response().StatusCode()

		// Build log fields
		fields := []zap.Field{
			zap.String("method", c.Method()),
			zap.String("path", c.Path()),
			zap.Int("status", status),
			zap.Duration("duration", duration),
			zap.String("ip", c.IP()),
			zap.String("user_agent", c.Get("User-Agent")),
			zap.String("request_id", c.GetRespHeader("X-Request-Id")),
		}

		// Add user ID if authenticated
		if userID, ok := c.Locals("user_id").(string); ok && userID != "" {
			fields = append(fields, zap.String("user_id", userID))
		}

		// Log based on status code
		switch {
		case status >= 500:
			logger.Error("Server error", fields...)
		case status >= 400:
			logger.Warn("Client error", fields...)
		default:
			logger.Info("Request", fields...)
		}

		return err
	}
}
