package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupMediaRoutes configures file manager routes
func SetupMediaRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	mediaHandler *handlers.MediaHandler,
	uploadPath string,
) {
	// Serve uploaded files (public access for is_public files)
	router.Static("/uploads", uploadPath, fiber.Static{
		Browse: false,
	})

	// Admin media management routes (require authentication)
	media := router.Group("/admin/media", authMiddleware.Authenticate())

	// File operations
	media.Post("/upload", permMiddleware.RequirePermission("settings.update"), mediaHandler.Upload)
	media.Post("/upload-multiple", permMiddleware.RequirePermission("settings.update"), mediaHandler.UploadMultiple)
	media.Post("/delete-multiple", permMiddleware.RequirePermission("settings.delete"), mediaHandler.DeleteMultiple)

	// Read operations
	media.Get("/folders", permMiddleware.RequirePermission("settings.read"), mediaHandler.GetFolders)
	media.Get("/stats", permMiddleware.RequirePermission("settings.read"), mediaHandler.GetStats)
	media.Get("/", permMiddleware.RequirePermission("settings.read"), mediaHandler.List)
	media.Get("/:id", permMiddleware.RequirePermission("settings.read"), mediaHandler.Get)

	// Update & Delete
	media.Put("/:id", permMiddleware.RequirePermission("settings.update"), mediaHandler.Update)
	media.Delete("/:id", permMiddleware.RequirePermission("settings.delete"), mediaHandler.Delete)
}
