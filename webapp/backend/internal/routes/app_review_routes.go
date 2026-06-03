package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupAppReviewRoutes registers client submission and admin management endpoints.
func SetupAppReviewRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	appReviewHandler *handlers.AppReviewHandler,
) {
	client := router.Group("/app-reviews", authMiddleware.OptionalAuthenticate())
	client.Post("/", appReviewHandler.Submit)

	admin := router.Group("/admin/app-reviews", authMiddleware.Authenticate())
	admin.Get("/", permMiddleware.RequirePermission("content.read"), appReviewHandler.List)
	admin.Get("/:id", permMiddleware.RequirePermission("content.read"), appReviewHandler.GetByID)
	admin.Patch("/:id", permMiddleware.RequirePermission("content.update"), appReviewHandler.Update)
}
