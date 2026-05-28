package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupPopupRoutes configures popup routes.
// Public: GET /api/popups (no auth)
// Admin: CRUD under /api/admin/popups (auth + permission required)
func SetupPopupRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	popupHandler *handlers.PopupHandler,
) {
	// ============================================
	// Public Popup Routes (no auth)
	// ============================================
	router.Get("/popups", popupHandler.GetActive)

	// ============================================
	// Admin Popup Routes (auth + content permission)
	// ============================================
	admin := router.Group("/admin/popups", authMiddleware.Authenticate())
	admin.Get("/", permMiddleware.RequirePermission("content.read"), popupHandler.ListAll)
	admin.Post("/", permMiddleware.RequirePermission("content.create"), popupHandler.Create)
	admin.Get("/:id", permMiddleware.RequirePermission("content.read"), popupHandler.GetByID)
	admin.Put("/:id", permMiddleware.RequirePermission("content.update"), popupHandler.Update)
	admin.Patch("/:id/toggle", permMiddleware.RequirePermission("content.update"), popupHandler.ToggleActive)
	admin.Delete("/:id", permMiddleware.RequirePermission("content.delete"), popupHandler.Delete)
}
