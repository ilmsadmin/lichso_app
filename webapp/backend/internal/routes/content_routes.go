package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
)

// SetupContentRoutes configures public content routes (no authentication required).
func SetupContentRoutes(
	router fiber.Router,
	articleHandler *handlers.ArticleHandler,
	categoryHandler *handlers.ArticleCategoryHandler,
	tagHandler *handlers.ArticleTagHandler,
	quoteHandler *handlers.QuoteHandler,
	famousPersonHandler *handlers.FamousPersonHandler,
	eventHandler *handlers.EventHandler,
	festivalHandler *handlers.FolkFestivalHandler,
) {
	router.Use(publicContentCache())

	// ============================================
	// Article Routes (Public)
	// ============================================
	articles := router.Group("/articles")
	articles.Get("/", articleHandler.List)
	articles.Get("/search", articleHandler.Search)
	articles.Get("/random", articleHandler.RandomArticles)
	articles.Get("/slug/:slug", articleHandler.GetBySlug)
	articles.Get("/:id", articleHandler.GetByID)

	// ============================================
	// Category Routes (Public)
	// ============================================
	categories := router.Group("/categories")
	categories.Get("/slug/:slug", categoryHandler.GetBySlug)
	categories.Get("/", categoryHandler.List)
	categories.Get("/:id", categoryHandler.GetByID)

	// ============================================
	// Tag Routes (Public)
	// ============================================
	tags := router.Group("/tags")
	tags.Get("/slug/:slug", tagHandler.GetBySlug)
	tags.Get("/", tagHandler.List)
	tags.Get("/:id", tagHandler.GetByID)

	// ============================================
	// Quote Routes (Public)
	// ============================================
	quotes := router.Group("/quotes")
	quotes.Get("/today", quoteHandler.GetToday)
	quotes.Get("/random", quoteHandler.GetRandom)
	quotes.Get("/", quoteHandler.List)
	quotes.Get("/:id", quoteHandler.GetByID)

	// ============================================
	// Famous People Routes (Public)
	// ============================================
	famousPeople := router.Group("/famous-people")
	famousPeople.Get("/birthday/:month/:day", famousPersonHandler.GetByBirthday)
	famousPeople.Get("/", famousPersonHandler.List)
	famousPeople.Get("/:id", famousPersonHandler.GetByID)

	// ============================================
	// Event Routes (Public)
	// ============================================
	events := router.Group("/events")
	events.Get("/date/:month/:day", eventHandler.GetByDate)
	events.Get("/slug/:slug", eventHandler.GetBySlug)
	events.Get("/", eventHandler.List)
	events.Get("/:id", eventHandler.GetByID)

	// ============================================
	// Folk Festival Routes (Public)
	// ============================================
	festivals := router.Group("/festivals")
	festivals.Get("/lunar/:month/:day", festivalHandler.GetByLunarDate)
	festivals.Get("/solar/:month/:day", festivalHandler.GetBySolarDate)
	festivals.Get("/slug/:slug", festivalHandler.GetBySlug)
	festivals.Get("/", festivalHandler.List)
	festivals.Get("/:id", festivalHandler.GetByID)
}

func publicContentCache() fiber.Handler {
	return func(c *fiber.Ctx) error {
		if c.Method() != fiber.MethodGet {
			return c.Next()
		}

		path := c.Path()
		switch {
		case path == "/api/articles" || path == "/api/articles/":
			c.Set(fiber.HeaderCacheControl, "public, max-age=600, stale-while-revalidate=3600")
		case path == "/api/categories" || path == "/api/categories/":
			c.Set(fiber.HeaderCacheControl, "public, max-age=600, stale-while-revalidate=3600")
		case hasPublicPrefix(path, "/api/articles/slug/"), hasPublicArticleIDPath(path):
			c.Set(fiber.HeaderCacheControl, "public, max-age=3600, stale-while-revalidate=86400")
		case hasPublicPrefix(path, "/api/categories/slug/"), hasPublicCategoryIDPath(path):
			c.Set(fiber.HeaderCacheControl, "public, max-age=1800, stale-while-revalidate=3600")
		case hasPublicPrefix(path, "/api/events/"),
			hasPublicPrefix(path, "/api/famous-people/"),
			hasPublicPrefix(path, "/api/festivals/"):
			c.Set(fiber.HeaderCacheControl, "public, max-age=1800, stale-while-revalidate=3600")
		case hasPublicPrefix(path, "/api/day-content/"),
			hasPublicPrefix(path, "/api/quotes/"):
			c.Set(fiber.HeaderCacheControl, "public, max-age=600, stale-while-revalidate=3600")
		}

		return c.Next()
	}
}

func hasPublicPrefix(path string, prefix string) bool {
	return len(path) >= len(prefix) && path[:len(prefix)] == prefix
}

func hasPublicArticleIDPath(path string) bool {
	return hasPublicPrefix(path, "/api/articles/") &&
		path != "/api/articles/search" &&
		path != "/api/articles/random" &&
		!hasPublicPrefix(path, "/api/articles/slug/")
}

func hasPublicCategoryIDPath(path string) bool {
	return hasPublicPrefix(path, "/api/categories/") &&
		!hasPublicPrefix(path, "/api/categories/slug/")
}
