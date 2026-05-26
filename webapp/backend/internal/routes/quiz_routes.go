package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupQuizRoutes registers all quiz routes: public, auth-required, and admin.
func SetupQuizRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	quizHandler *handlers.QuizHandler,
) {
	// ============================================
	// Quiz routes (public + auth via route-level middleware)
	// ============================================
	quiz := router.Group("/quiz")

	// Public — no auth required
	quiz.Get("/questions/daily", quizHandler.GetDailyQuestions)
	quiz.Get("/questions", quizHandler.GetQuestions)
	quiz.Get("/leaderboard", quizHandler.GetLeaderboard)

	// Auth-required — middleware applied per-route to avoid prefix conflict
	auth := authMiddleware.Authenticate()
	quiz.Post("/sessions", auth, quizHandler.StartSession)
	quiz.Post("/sessions/:id/submit", auth, quizHandler.SubmitAnswer)
	quiz.Post("/sessions/:id/finish", auth, quizHandler.FinishSession)
	quiz.Post("/sessions/sync", auth, quizHandler.SyncOfflineSessions)
	quiz.Get("/sessions/:id", auth, quizHandler.GetSession)
	quiz.Get("/history", auth, quizHandler.GetMyHistory)
	quiz.Get("/leaderboard/me", auth, quizHandler.GetMyRank)

	// ============================================
	// Admin quiz routes
	// ============================================
	adminQuiz := router.Group("/admin/quiz", authMiddleware.Authenticate())
	adminQuiz.Get("/questions", permMiddleware.RequirePermission("content.read"), quizHandler.AdminListQuestions)
	// /generate must be registered before /:id to avoid route conflict
	adminQuiz.Post("/questions/generate", permMiddleware.RequirePermission("content.create"), quizHandler.AdminGenerateQuestions)
	adminQuiz.Post("/topics/generate", permMiddleware.RequirePermission("content.create"), quizHandler.AdminGenerateTopics)
	adminQuiz.Get("/questions/:id", permMiddleware.RequirePermission("content.read"), quizHandler.AdminGetQuestion)
	adminQuiz.Post("/questions", permMiddleware.RequirePermission("content.create"), quizHandler.AdminCreateQuestion)
	adminQuiz.Put("/questions/:id", permMiddleware.RequirePermission("content.update"), quizHandler.AdminUpdateQuestion)
	adminQuiz.Delete("/questions/:id", permMiddleware.RequirePermission("content.delete"), quizHandler.AdminDeleteQuestion)
	adminQuiz.Post("/daily-sets", permMiddleware.RequirePermission("content.create"), quizHandler.AdminSetDailySet)
	adminQuiz.Get("/daily-sets", permMiddleware.RequirePermission("content.read"), quizHandler.AdminGetDailySets)
}

// SetupPointsRoutes registers routes for managing unified wallet points.
func SetupPointsRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	pointsHandler *handlers.PointsHandler,
) {
	points := router.Group("/points", authMiddleware.Authenticate())
	points.Get("/wallet", pointsHandler.GetWallet)
	points.Get("/transactions", pointsHandler.GetTransactions)
	points.Post("/spend", pointsHandler.SpendPoints)
	points.Post("/sync-guest", pointsHandler.SyncGuestPoints)
}
