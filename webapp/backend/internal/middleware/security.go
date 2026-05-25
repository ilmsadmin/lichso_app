package middleware

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/config"
)

// SecurityHeaders returns a middleware that sets security-related HTTP headers.
// In production, strict headers like HSTS and CSP are applied.
func SecurityHeaders(cfg *config.Config) fiber.Handler {
	return func(c *fiber.Ctx) error {
		// Prevent MIME type sniffing
		c.Set("X-Content-Type-Options", "nosniff")

		// Prevent clickjacking
		c.Set("X-Frame-Options", "DENY")

		// XSS protection (legacy, but still useful for older browsers)
		c.Set("X-XSS-Protection", "1; mode=block")

		// Referrer policy
		c.Set("Referrer-Policy", "strict-origin-when-cross-origin")

		// Permissions policy (restrict browser features)
		c.Set("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()")

		// Don't expose server internals
		c.Set("X-Permitted-Cross-Domain-Policies", "none")

		if cfg.IsProduction() {
			// HSTS - force HTTPS (1 year, includeSubDomains, preload)
			c.Set("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")

			// Content Security Policy for API responses
			c.Set("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")

			// Prevent caching of API responses with sensitive data
			c.Set("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate")
			c.Set("Pragma", "no-cache")
			c.Set("Expires", "0")
		}

		return c.Next()
	}
}
