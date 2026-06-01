package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupSurveyRoutes configures survey routes.
// Public / Client:
//   GET /api/surveys/active          -> Fetch current active survey
//   POST /api/surveys/active/responses -> Submit answers (uses token if authenticated)
// Admin:
//   CRUD operations under /api/admin/surveys (auth + permission content.* required)
func SetupSurveyRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	surveyHandler *handlers.SurveyHandler,
) {
	// ============================================
	// Client Survey Routes
	// ============================================
	// The mobile client passes an optional token. If authenticated, the middleware parses it.
	// Since client needs to save user ID, let's attach the optional auth handler if we want,
	// or rely on our custom handler parsing it. The authMiddleware.Authenticate() will block
	// requests without a token, so we can make it optional by letting the handler check it
	// (or just use standard authenticate if every mobile user HAS to be authenticated guest).
	// Because of client interceptor, a token is always supplied. So we can use auth to be secure.
	auth := authMiddleware.Authenticate()
	optionalAuth := authMiddleware.OptionalAuthenticate()
	
	clientGroup := router.Group("/surveys")
	clientGroup.Get("/active", surveyHandler.GetActive)
	clientGroup.Post("/active/responses", optionalAuth, surveyHandler.SubmitResponse)

	// ============================================
	// Admin Survey Routes
	// ============================================
	admin := router.Group("/admin/surveys", auth)
	admin.Get("/", permMiddleware.RequirePermission("content.read"), surveyHandler.ListAll)
	admin.Post("/", permMiddleware.RequirePermission("content.create"), surveyHandler.Create)
	admin.Get("/:id", permMiddleware.RequirePermission("content.read"), surveyHandler.GetByID)
	admin.Put("/:id", permMiddleware.RequirePermission("content.update"), surveyHandler.Update)
	admin.Patch("/:id/toggle", permMiddleware.RequirePermission("content.update"), surveyHandler.ToggleActive)
	admin.Delete("/:id", permMiddleware.RequirePermission("content.delete"), surveyHandler.Delete)
	admin.Get("/:id/stats", permMiddleware.RequirePermission("content.read"), surveyHandler.GetStats)
}
