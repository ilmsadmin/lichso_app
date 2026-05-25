package middleware

import (
	"fmt"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
)

// RateLimitConfig holds rate limit configuration for a route
type RateLimitConfig struct {
	Max    int // Maximum number of requests
	Window int // Window in seconds
}

// RateLimiter creates a rate limiting middleware using Redis
func RateLimiter(cacheService *services.CacheService, cfg RateLimitConfig) fiber.Handler {
	return func(c *fiber.Ctx) error {
		// Create a unique key based on IP and path
		key := fmt.Sprintf("%s:%s", c.IP(), c.Path())

		// Increment counter
		count, err := cacheService.IncrementRateLimit(key, time.Duration(cfg.Window)*time.Second)
		if err != nil {
			// If Redis fails, allow the request
			return c.Next()
		}

		// Set rate limit headers
		c.Set("X-RateLimit-Limit", fmt.Sprintf("%d", cfg.Max))
		c.Set("X-RateLimit-Remaining", fmt.Sprintf("%d", max(0, cfg.Max-int(count))))

		// Check if limit exceeded
		if int(count) > cfg.Max {
			return utils.TooManyRequestsResponse(c)
		}

		return c.Next()
	}
}
