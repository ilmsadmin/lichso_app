package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupBannerRoutes configures banner routes.
// Public: GET /api/banners (no auth)
// Admin: CRUD under /api/admin/banners (auth + permission required)
func SetupBannerRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	bannerHandler *handlers.BannerHandler,
) {
	// ============================================
	// Public Banner Routes (no auth)
	// ============================================
	router.Get("/banners", bannerHandler.GetActive)

	// ============================================
	// Admin Banner Routes (auth + content permission)
	// ============================================
	admin := router.Group("/admin/banners", authMiddleware.Authenticate())
	admin.Get("/", permMiddleware.RequirePermission("content.read"), bannerHandler.ListAll)
	admin.Post("/", permMiddleware.RequirePermission("content.create"), bannerHandler.Create)
	admin.Put("/reorder", permMiddleware.RequirePermission("content.update"), bannerHandler.Reorder)
	admin.Get("/:id", permMiddleware.RequirePermission("content.read"), bannerHandler.GetByID)
	admin.Put("/:id", permMiddleware.RequirePermission("content.update"), bannerHandler.Update)
	admin.Patch("/:id/toggle", permMiddleware.RequirePermission("content.update"), bannerHandler.ToggleActive)
	admin.Delete("/:id", permMiddleware.RequirePermission("content.delete"), bannerHandler.Delete)
}
