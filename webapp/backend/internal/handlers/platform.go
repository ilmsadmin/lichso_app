package handlers

import (
	"strings"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/models"
)

// clientPlatform resolves the requesting client's platform ("android"/"ios"),
// preferring the X-Client-Platform header and falling back to the User-Agent.
// Returns "" when the platform cannot be determined (e.g. web/admin).
func clientPlatform(c *fiber.Ctx) string {
	if p := models.NormalizePlatform(c.Get("X-Client-Platform")); p == models.PlatformAndroid || p == models.PlatformIOS {
		return p
	}

	ua := strings.ToLower(c.Get("User-Agent"))
	switch {
	case strings.Contains(ua, "android"):
		return models.PlatformAndroid
	case strings.Contains(ua, "iphone"), strings.Contains(ua, "ipad"), strings.Contains(ua, "ios"):
		return models.PlatformIOS
	default:
		return ""
	}
}
