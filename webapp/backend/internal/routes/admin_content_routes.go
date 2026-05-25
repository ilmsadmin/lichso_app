package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupAdminContentRoutes configures admin content management routes
// with authentication and permission middleware.
func SetupAdminContentRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	articleHandler *handlers.ArticleHandler,
	categoryHandler *handlers.ArticleCategoryHandler,
	tagHandler *handlers.ArticleTagHandler,
	quoteHandler *handlers.QuoteHandler,
	famousPersonHandler *handlers.FamousPersonHandler,
	eventHandler *handlers.EventHandler,
	festivalHandler *handlers.FolkFestivalHandler,
) {
	// All admin content routes require authentication
	admin := router.Group("/admin", authMiddleware.Authenticate())

	// ============================================
	// Article Management Routes
	// ============================================
	articles := admin.Group("/articles")
	articles.Get("/", permMiddleware.RequirePermission("content.read"), articleHandler.ListAll)
	articles.Get("/:id", permMiddleware.RequirePermission("content.read"), articleHandler.GetByID)
	articles.Post("/", permMiddleware.RequirePermission("content.create"), articleHandler.Create)
	articles.Put("/:id", permMiddleware.RequirePermission("content.update"), articleHandler.Update)
	articles.Delete("/:id", permMiddleware.RequirePermission("content.delete"), articleHandler.Delete)

	// ============================================
	// Category Management Routes
	// ============================================
	categories := admin.Group("/categories")
	categories.Get("/", permMiddleware.RequirePermission("content.read"), categoryHandler.ListAll)
	categories.Get("/:id", permMiddleware.RequirePermission("content.read"), categoryHandler.GetByID)
	categories.Post("/", permMiddleware.RequirePermission("content.create"), categoryHandler.Create)
	categories.Put("/:id", permMiddleware.RequirePermission("content.update"), categoryHandler.Update)
	categories.Delete("/:id", permMiddleware.RequirePermission("content.delete"), categoryHandler.Delete)

	// ============================================
	// Tag Management Routes
	// ============================================
	tags := admin.Group("/tags")
	tags.Get("/", permMiddleware.RequirePermission("content.read"), tagHandler.ListAll)
	tags.Get("/:id", permMiddleware.RequirePermission("content.read"), tagHandler.GetByID)
	tags.Post("/", permMiddleware.RequirePermission("content.create"), tagHandler.Create)
	tags.Put("/:id", permMiddleware.RequirePermission("content.update"), tagHandler.Update)
	tags.Delete("/:id", permMiddleware.RequirePermission("content.delete"), tagHandler.Delete)

	// ============================================
	// Quote Management Routes
	// ============================================
	quotes := admin.Group("/quotes")
	quotes.Get("/", permMiddleware.RequirePermission("content.read"), quoteHandler.ListAll)
	quotes.Get("/:id", permMiddleware.RequirePermission("content.read"), quoteHandler.GetByID)
	quotes.Post("/", permMiddleware.RequirePermission("content.create"), quoteHandler.Create)
	quotes.Put("/:id", permMiddleware.RequirePermission("content.update"), quoteHandler.Update)
	quotes.Delete("/:id", permMiddleware.RequirePermission("content.delete"), quoteHandler.Delete)

	// ============================================
	// Famous People Management Routes
	// ============================================
	famousPeople := admin.Group("/famous-people")
	famousPeople.Get("/", permMiddleware.RequirePermission("content.read"), famousPersonHandler.ListAll)
	famousPeople.Get("/:id", permMiddleware.RequirePermission("content.read"), famousPersonHandler.GetByID)
	famousPeople.Post("/", permMiddleware.RequirePermission("content.create"), famousPersonHandler.Create)
	famousPeople.Put("/:id", permMiddleware.RequirePermission("content.update"), famousPersonHandler.Update)
	famousPeople.Delete("/:id", permMiddleware.RequirePermission("content.delete"), famousPersonHandler.Delete)

	// ============================================
	// Event Management Routes
	// ============================================
	events := admin.Group("/events")
	events.Get("/", permMiddleware.RequirePermission("content.read"), eventHandler.ListAll)
	events.Get("/:id", permMiddleware.RequirePermission("content.read"), eventHandler.GetByID)
	events.Post("/", permMiddleware.RequirePermission("content.create"), eventHandler.Create)
	events.Put("/:id", permMiddleware.RequirePermission("content.update"), eventHandler.Update)
	events.Delete("/:id", permMiddleware.RequirePermission("content.delete"), eventHandler.Delete)

	// ============================================
	// Folk Festival Management Routes
	// ============================================
	festivals := admin.Group("/festivals")
	festivals.Get("/", permMiddleware.RequirePermission("content.read"), festivalHandler.ListAll)
	festivals.Get("/:id", permMiddleware.RequirePermission("content.read"), festivalHandler.GetByID)
	festivals.Post("/", permMiddleware.RequirePermission("content.create"), festivalHandler.Create)
	festivals.Put("/:id", permMiddleware.RequirePermission("content.update"), festivalHandler.Update)
	festivals.Delete("/:id", permMiddleware.RequirePermission("content.delete"), festivalHandler.Delete)
}
