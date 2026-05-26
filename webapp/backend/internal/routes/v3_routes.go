package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupV3ContentRoutes configures V3 public content routes.
func SetupV3ContentRoutes(
	router fiber.Router,
	articleRelationHandler *handlers.ArticleRelationHandler,
	dailyContentHandler *handlers.DailyContentHandler,
	horoscopeHandler *handlers.HoroscopeHandler,
	goodDayHandler *handlers.GoodDayHandler,
) {
	// ============================================
	// Related Articles (Public)
	// ============================================
	articles := router.Group("/articles")
	articles.Get("/:id/related", articleRelationHandler.GetRelatedArticles)

	// ============================================
	// Day Content (Public)
	// ============================================
	dayContent := router.Group("/day-content")
	dayContent.Get("/today", dailyContentHandler.GetContentToday)
	dayContent.Get("/month/:year/:month", dailyContentHandler.GetMonthContentSummary)
	dayContent.Get("/:date", dailyContentHandler.GetContentForDate)

	// ============================================
	// Horoscope (Public) — Phase 23
	// ============================================
	horoscope := router.Group("/horoscope")
	horoscope.Get("/daily/:year/:month/:day", horoscopeHandler.GetDailyAll)
	horoscope.Get("/daily/:year/:month/:day/:zodiac", horoscopeHandler.GetDailyByZodiac)
	horoscope.Get("/birth-year/:year", horoscopeHandler.GetByBirthYear)
	horoscope.Get("/lunar-age/:year", horoscopeHandler.CalculateLunarAge)

	// ============================================
	// Good Days for Purpose (Public) — Phase 23
	// ============================================
	goodDays := router.Group("/good-days")
	goodDays.Get("/purposes", goodDayHandler.GetPurposes)
	goodDays.Get("/:year/:month/:purpose", goodDayHandler.GetGoodDaysForPurpose)
}

// SetupV3AdminRoutes configures V3 admin content management routes.
func SetupV3AdminRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	articleRelationHandler *handlers.ArticleRelationHandler,
	dailyContentHandler *handlers.DailyContentHandler,
	newsletterHandler *handlers.NewsletterHandler,
) {
	admin := router.Group("/admin", authMiddleware.Authenticate())

	// ============================================
	// Article Relations Management
	// ============================================
	articleRelations := admin.Group("/articles")
	articleRelations.Get("/:id/relations", permMiddleware.RequirePermission("content.read"), articleRelationHandler.GetRelations)
	articleRelations.Post("/:id/relations", permMiddleware.RequirePermission("content.create"), articleRelationHandler.Create)
	articleRelations.Post("/:id/relations/batch", permMiddleware.RequirePermission("content.create"), articleRelationHandler.BatchCreate)
	articleRelations.Put("/relations/:relationId", permMiddleware.RequirePermission("content.update"), articleRelationHandler.Update)
	articleRelations.Delete("/relations/:relationId", permMiddleware.RequirePermission("content.delete"), articleRelationHandler.Delete)

	// ============================================
	// Daily Content Schedule Management
	// ============================================
	dailyContent := admin.Group("/daily-content")
	dailyContent.Get("/stats", permMiddleware.RequirePermission("content.read"), dailyContentHandler.GetStats)
	dailyContent.Get("/", permMiddleware.RequirePermission("content.read"), dailyContentHandler.List)
	dailyContent.Get("/:id", permMiddleware.RequirePermission("content.read"), dailyContentHandler.GetByID)
	dailyContent.Post("/auto-fill", permMiddleware.RequirePermission("content.create"), dailyContentHandler.AutoFill)
	dailyContent.Post("/", permMiddleware.RequirePermission("content.create"), dailyContentHandler.Create)
	dailyContent.Put("/:id", permMiddleware.RequirePermission("content.update"), dailyContentHandler.Update)
	dailyContent.Delete("/:id", permMiddleware.RequirePermission("content.delete"), dailyContentHandler.Delete)

	// ============================================
	// Newsletter Admin Management — Phase 24
	// ============================================
	newsletter := admin.Group("/newsletter")
	newsletter.Get("/", permMiddleware.RequirePermission("content.read"), newsletterHandler.AdminList)
	newsletter.Get("/stats", permMiddleware.RequirePermission("content.read"), newsletterHandler.AdminGetStats)
	newsletter.Delete("/:id", permMiddleware.RequirePermission("content.delete"), newsletterHandler.AdminDelete)
}

// SetupV3UserRoutes configures V3 personal feature routes (require authentication).
func SetupV3UserRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	noteHandler *handlers.UserNoteHandler,
	countdownHandler *handlers.UserCountdownHandler,
	streakHandler *handlers.StreakHandler,
	newsletterHandler *handlers.NewsletterHandler,
) {
	authMw := authMiddleware.Authenticate()

	// ============================================
	// User Notes
	// ============================================
	notes := router.Group("/notes", authMw)
	notes.Get("/", noteHandler.List)
	notes.Get("/range", noteHandler.GetByDateRange)
	notes.Get("/date/:date", noteHandler.GetByDate)
	notes.Get("/:id", noteHandler.GetByID)
	notes.Post("/", noteHandler.Create)
	notes.Put("/:id", noteHandler.Update)
	notes.Delete("/:id", noteHandler.Delete)

	// ============================================
	// User Countdowns
	// ============================================
	countdowns := router.Group("/countdowns", authMw)
	countdowns.Get("/", countdownHandler.List)
	countdowns.Get("/active", countdownHandler.GetActive)
	countdowns.Get("/upcoming", countdownHandler.GetUpcoming)
	countdowns.Get("/:id", countdownHandler.GetByID)
	countdowns.Post("/", countdownHandler.Create)
	countdowns.Put("/:id", countdownHandler.Update)
	countdowns.Delete("/:id", countdownHandler.Delete)

	// ============================================
	// User Streaks & Achievements — Phase 24
	// ============================================
	streak := router.Group("/streak", authMw)
	streak.Post("/visit", streakHandler.RecordVisit)
	streak.Get("/", streakHandler.GetStreak)

	achievements := router.Group("/achievements", authMw)
	achievements.Get("/", streakHandler.GetAchievements)

	router.Get("/progress", authMw, streakHandler.GetProgress)

	// ============================================
	// Newsletter (User) — Phase 24
	// ============================================
	nl := router.Group("/newsletter", authMw)
	nl.Post("/subscribe", newsletterHandler.Subscribe)
	nl.Post("/unsubscribe", newsletterHandler.Unsubscribe)
	nl.Get("/status", newsletterHandler.GetSubscription)
	nl.Put("/preferences", newsletterHandler.UpdatePreferences)
}
