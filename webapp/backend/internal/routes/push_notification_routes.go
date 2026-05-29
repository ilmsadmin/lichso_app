package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupPushNotificationRoutes registers push notification routes.
func SetupPushNotificationRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	handler *handlers.PushNotificationHandler,
) {
	// ── Device token registration (optional auth — works for guests too) ──
	push := router.Group("/push")
	push.Post("/register", handler.RegisterToken)
	push.Delete("/register", handler.UnregisterToken)

	// ── Admin campaign management ─────────────────────────────────────────
	admin := router.Group("/admin/push", authMiddleware.Authenticate())
	admin.Get("/stats", permMiddleware.RequirePermission("content.read"), handler.AdminGetStats)
	admin.Get("/campaigns", permMiddleware.RequirePermission("content.read"), handler.AdminListCampaigns)
	admin.Post("/campaigns", permMiddleware.RequirePermission("content.create"), handler.AdminCreateCampaign)
	admin.Get("/campaigns/:id", permMiddleware.RequirePermission("content.read"), handler.AdminGetCampaign)
	admin.Put("/campaigns/:id", permMiddleware.RequirePermission("content.update"), handler.AdminUpdateCampaign)
	admin.Delete("/campaigns/:id", permMiddleware.RequirePermission("content.delete"), handler.AdminDeleteCampaign)
	admin.Post("/campaigns/:id/send", permMiddleware.RequirePermission("content.create"), handler.AdminSendCampaign)
}
