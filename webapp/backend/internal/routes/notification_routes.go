package routes

import (
	"github.com/gofiber/contrib/websocket"
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupNotificationRoutes configures notification REST and WebSocket routes
func SetupNotificationRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	notifHandler *handlers.NotificationHandler,
) {
	// REST routes (require authentication)
	notifs := router.Group("/notifications", authMiddleware.Authenticate())
	notifs.Get("/", notifHandler.List)
	notifs.Get("/unread-count", notifHandler.UnreadCount)
	notifs.Patch("/read-all", notifHandler.MarkAllAsRead)
	notifs.Patch("/:id/read", notifHandler.MarkAsRead)
	notifs.Delete("/all", notifHandler.DeleteAll)
	notifs.Delete("/:id", notifHandler.Delete)

	// WebSocket route
	// Auth middleware runs first, then upgrade check, then WS handler
	router.Use("/ws", authMiddleware.Authenticate())
	router.Use("/ws", func(c *fiber.Ctx) error {
		if websocket.IsWebSocketUpgrade(c) {
			return c.Next()
		}
		return fiber.ErrUpgradeRequired
	})
	router.Get("/ws", notifHandler.HandleWebSocket())
}
