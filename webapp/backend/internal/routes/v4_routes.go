package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupV4PublicRoutes configures V4 AI public routes (no auth required).
// Currently all AI endpoints require at least a guest context (IP-based quota).
func SetupV4PublicRoutes(
	router fiber.Router,
	aiHoroscopeHandler *handlers.AIHoroscopeHandler,
) {
	ai := router.Group("/ai")

	// Horoscope — public (guest quota applies)
	horoscope := ai.Group("/horoscope")
	horoscope.Post("/read", aiHoroscopeHandler.ReadHoroscope)
	horoscope.Get("/quota", aiHoroscopeHandler.GetQuota)
}

// SetupV4AuthRoutes configures V4 AI routes that require authentication.
func SetupV4AuthRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	aiHoroscopeHandler *handlers.AIHoroscopeHandler,
	aiChatHandler *handlers.AIChatHandler,
) {
	ai := router.Group("/ai", authMiddleware.Authenticate())

	// Horoscope — authenticated (higher quota)
	horoscope := ai.Group("/horoscope")
	horoscope.Get("/history", aiHoroscopeHandler.GetHistory)

	// Chat — requires login
	chat := ai.Group("/chat")
	sessions := chat.Group("/sessions")
	sessions.Post("/", aiChatHandler.CreateSession)
	sessions.Get("/", aiChatHandler.ListSessions)
	sessions.Get("/:uuid", aiChatHandler.GetSession)
	sessions.Delete("/:uuid", aiChatHandler.DeleteSession)
	sessions.Post("/:uuid/messages", aiChatHandler.SendMessage)
}

// SetupV4AdminRoutes configures V4 admin routes for AI content management.
func SetupV4AdminRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	aiArticleHandler *handlers.AIArticleHandler,
	aiAdminHandler *handlers.AIAdminHandler,
) {
	admin := router.Group("/admin/ai", authMiddleware.Authenticate())

	// AI Article generation — requires content.create permission
	articles := admin.Group("/articles")
	articles.Get("/", permMiddleware.RequirePermission("content.read"), aiArticleHandler.ListAIArticles)
	articles.Post("/generate", permMiddleware.RequirePermission("content.create"), aiArticleHandler.GenerateArticle)
	articles.Post("/quick-draft", permMiddleware.RequirePermission("content.create"), aiArticleHandler.QuickDraft)
	articles.Get("/topics", permMiddleware.RequirePermission("content.read"), aiArticleHandler.SuggestTopics)
	articles.Get("/low-quality", permMiddleware.RequirePermission("content.read"), aiArticleHandler.ListLowQualityArticles)
	articles.Get("/low-quality/count", permMiddleware.RequirePermission("content.read"), aiArticleHandler.CountLowQualityArticles)
	articles.Post("/bulk-rewrite", permMiddleware.RequirePermission("content.create"), aiArticleHandler.BulkRewriteArticles)
	articles.Post("/:id/refine", permMiddleware.RequirePermission("content.create"), aiArticleHandler.RefineArticle)
	articles.Patch("/:id/status", permMiddleware.RequirePermission("content.create"), aiArticleHandler.UpdateArticleStatus)

	// AI Stats & logs — requires admin.read permission
	admin.Get("/stats", permMiddleware.RequirePermission("admin.read"), aiAdminHandler.GetStats)
	admin.Get("/logs", permMiddleware.RequirePermission("admin.read"), aiAdminHandler.GetLogs)

	// Test AI connection
	admin.Post("/test-connection", permMiddleware.RequirePermission("settings.update"), aiAdminHandler.TestConnection)

	// Prompt Templates CRUD
	prompts := admin.Group("/prompts")
	prompts.Get("/", permMiddleware.RequirePermission("admin.read"), aiAdminHandler.ListPrompts)
	prompts.Get("/:id", permMiddleware.RequirePermission("admin.read"), aiAdminHandler.GetPrompt)
	prompts.Post("/", permMiddleware.RequirePermission("admin.write"), aiAdminHandler.CreatePrompt)
	prompts.Put("/:id", permMiddleware.RequirePermission("admin.write"), aiAdminHandler.UpdatePrompt)
	prompts.Delete("/:id", permMiddleware.RequirePermission("admin.write"), aiAdminHandler.DeletePrompt)
}
