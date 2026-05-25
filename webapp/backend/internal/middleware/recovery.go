package middleware

import (
	"fmt"
	"runtime/debug"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// Recovery creates a panic recovery middleware
func Recovery(logger *zap.Logger) fiber.Handler {
	return func(c *fiber.Ctx) (err error) {
		defer func() {
			if r := recover(); r != nil {
				// Log the panic with stack trace
				logger.Error("Panic recovered",
					zap.String("error", fmt.Sprintf("%v", r)),
					zap.String("stack", string(debug.Stack())),
					zap.String("method", c.Method()),
					zap.String("path", c.Path()),
					zap.String("ip", c.IP()),
				)

				err = utils.InternalErrorResponse(c)
			}
		}()

		return c.Next()
	}
}
