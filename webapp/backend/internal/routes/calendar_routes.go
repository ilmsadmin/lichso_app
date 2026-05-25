package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
)

// SetupCalendarRoutes configures calendar and feng shui routes.
// These are public routes (no authentication required).
func SetupCalendarRoutes(router fiber.Router, calHandler *handlers.CalendarHandler) {
	// ============================================
	// Calendar Routes
	// ============================================
	cal := router.Group("/calendar")

	cal.Get("/today", calHandler.GetToday)                  // Today's full info
	cal.Get("/date/:date", calHandler.GetDate)              // Specific date info (YYYY-MM-DD)
	cal.Get("/month/:year/:month", calHandler.GetMonth)     // Month calendar grid
	cal.Get("/convert", calHandler.Convert)                 // Convert solar↔lunar
	cal.Get("/good-days", calHandler.GetGoodDays)           // Good days in month
	cal.Get("/solar-terms/:year", calHandler.GetSolarTerms) // 24 solar terms for year

	// ============================================
	// Feng Shui Routes
	// ============================================
	fs := router.Group("/fengshui")

	fs.Get("/direction/:date", calHandler.GetFengshuiDirection)   // Direction for a date
	fs.Get("/hours/:date", calHandler.GetFengshuiHours)           // Hoang Dao hours
	fs.Get("/activities/:date", calHandler.GetFengshuiActivities) // Activities for a date
}
