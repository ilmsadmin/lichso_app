package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupBookmarkRoutes configures bookmark routes (require authentication).
func SetupBookmarkRoutes(router fiber.Router, authMiddleware *middleware.AuthMiddleware, bookmarkHandler *handlers.BookmarkHandler) {
	bm := router.Group("/bookmarks", authMiddleware.Authenticate())

	bm.Post("/", bookmarkHandler.Create)
	bm.Get("/", bookmarkHandler.GetAll)
	bm.Get("/month/:year/:month", bookmarkHandler.GetByMonth)
	bm.Get("/date/:date", bookmarkHandler.GetByDate)
	bm.Put("/:id", bookmarkHandler.Update)
	bm.Delete("/:id", bookmarkHandler.Delete)
}

// SetupReminderRoutes configures reminder routes (require authentication).
func SetupReminderRoutes(router fiber.Router, authMiddleware *middleware.AuthMiddleware, reminderHandler *handlers.ReminderHandler) {
	rm := router.Group("/reminders", authMiddleware.Authenticate())

	rm.Post("/", reminderHandler.Create)
	rm.Get("/", reminderHandler.GetAll)
	rm.Put("/:id", reminderHandler.Update)
	rm.Delete("/:id", reminderHandler.Delete)
}

// SetupExportRoutes configures calendar export routes (public, no auth).
func SetupExportRoutes(router fiber.Router, exportHandler *handlers.ExportHandler) {
	ex := router.Group("/export")

	ex.Get("/ical", exportHandler.ExportICal)
	ex.Get("/text", exportHandler.ExportText)
}
