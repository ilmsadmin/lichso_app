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

	// ── Admin: campaigns ──────────────────────────────────────────────────
	admin := router.Group("/admin/push", authMiddleware.Authenticate())
	admin.Get("/stats", permMiddleware.RequirePermission("content.read"), handler.AdminGetStats)
	admin.Get("/campaigns", permMiddleware.RequirePermission("content.read"), handler.AdminListCampaigns)
	admin.Post("/campaigns", permMiddleware.RequirePermission("content.create"), handler.AdminCreateCampaign)
	admin.Get("/campaigns/:id", permMiddleware.RequirePermission("content.read"), handler.AdminGetCampaign)
	admin.Put("/campaigns/:id", permMiddleware.RequirePermission("content.update"), handler.AdminUpdateCampaign)
	admin.Delete("/campaigns/:id", permMiddleware.RequirePermission("content.delete"), handler.AdminDeleteCampaign)
	admin.Post("/campaigns/:id/send", permMiddleware.RequirePermission("content.create"), handler.AdminSendCampaign)

	// ── Admin: notification templates ─────────────────────────────────────
	admin.Get("/templates", permMiddleware.RequirePermission("content.read"), handler.AdminListTemplates)
	admin.Post("/templates", permMiddleware.RequirePermission("content.create"), handler.AdminCreateTemplate)
	admin.Put("/templates/:id", permMiddleware.RequirePermission("content.update"), handler.AdminUpdateTemplate)
	admin.Delete("/templates/:id", permMiddleware.RequirePermission("content.delete"), handler.AdminDeleteTemplate)

	// ── Admin: user groups ────────────────────────────────────────────────
	admin.Get("/groups", permMiddleware.RequirePermission("content.read"), handler.AdminListGroups)
	admin.Post("/groups", permMiddleware.RequirePermission("content.create"), handler.AdminCreateGroup)
	admin.Put("/groups/:id", permMiddleware.RequirePermission("content.update"), handler.AdminUpdateGroup)
	admin.Delete("/groups/:id", permMiddleware.RequirePermission("content.delete"), handler.AdminDeleteGroup)
	admin.Get("/groups/:id/members", permMiddleware.RequirePermission("content.read"), handler.AdminGetGroupMembers)
	admin.Post("/groups/:id/members", permMiddleware.RequirePermission("content.update"), handler.AdminAddGroupMember)
	admin.Delete("/groups/:id/members/:userID", permMiddleware.RequirePermission("content.update"), handler.AdminRemoveGroupMember)
}
