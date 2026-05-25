package handlers

import (
	"context"
	"strconv"

	"github.com/gofiber/contrib/websocket"
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// NotificationHandler handles notification HTTP and WebSocket requests
type NotificationHandler struct {
	notifService *services.NotificationService
	logger       *zap.Logger
}

// NewNotificationHandler creates a new NotificationHandler
func NewNotificationHandler(notifService *services.NotificationService, logger *zap.Logger) *NotificationHandler {
	return &NotificationHandler{
		notifService: notifService,
		logger:       logger,
	}
}

// List handles GET /api/notifications
func (h *NotificationHandler) List(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))

	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 20
	}

	notifications, total, err := h.notifService.GetByUserID(c.Context(), userID, page, limit)
	if err != nil {
		h.logger.Error("Failed to get notifications", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	totalPages := (total + int64(limit) - 1) / int64(limit)

	return utils.SuccessResponse(c, "Notifications retrieved", fiber.Map{
		"notifications": notifications,
		"meta": fiber.Map{
			"page":        page,
			"limit":       limit,
			"total":       total,
			"total_pages": totalPages,
		},
	})
}

// UnreadCount handles GET /api/notifications/unread-count
func (h *NotificationHandler) UnreadCount(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	count, err := h.notifService.GetUnreadCount(c.Context(), userID)
	if err != nil {
		h.logger.Error("Failed to get unread count", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Unread count retrieved", fiber.Map{
		"unread_count": count,
	})
}

// MarkAsRead handles PATCH /api/notifications/:id/read
func (h *NotificationHandler) MarkAsRead(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	notifID := c.Params("id")
	if notifID == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Notification ID is required")
	}

	if err := h.notifService.MarkAsRead(c.Context(), notifID, userID); err != nil {
		h.logger.Error("Failed to mark notification as read", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Notification marked as read", nil)
}

// MarkAllAsRead handles PATCH /api/notifications/read-all
func (h *NotificationHandler) MarkAllAsRead(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	if err := h.notifService.MarkAllAsRead(c.Context(), userID); err != nil {
		h.logger.Error("Failed to mark all notifications as read", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "All notifications marked as read", nil)
}

// Delete handles DELETE /api/notifications/:id
func (h *NotificationHandler) Delete(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	notifID := c.Params("id")
	if notifID == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Notification ID is required")
	}

	if err := h.notifService.Delete(c.Context(), notifID, userID); err != nil {
		h.logger.Error("Failed to delete notification", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Notification deleted", nil)
}

// DeleteAll handles DELETE /api/notifications
func (h *NotificationHandler) DeleteAll(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	if err := h.notifService.DeleteAll(c.Context(), userID); err != nil {
		h.logger.Error("Failed to delete all notifications", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "All notifications deleted", nil)
}

// HandleWebSocket handles the WebSocket connection at /api/ws
func (h *NotificationHandler) HandleWebSocket() fiber.Handler {
	return websocket.New(func(c *websocket.Conn) {
		userID := c.Locals("user_id").(string)
		hub := h.notifService.GetWSHub()

		// Register connection
		hub.Register(userID, c)
		defer hub.Unregister(userID, c)

		h.logger.Info("WebSocket connected",
			zap.String("user_id", userID),
		)

		// Send initial unread count
		ctx := context.Background()
		count, err := h.notifService.GetUnreadCount(ctx, userID)
		if err == nil {
			hub.SendToUser(userID, models.WSMessage{
				Type: "notification_count",
				Data: map[string]int64{"unread_count": count},
			})
		}

		// Read loop - keep connection alive and handle client messages
		for {
			_, _, err := c.ReadMessage()
			if err != nil {
				// Client disconnected
				h.logger.Debug("WebSocket read error (client disconnected)",
					zap.String("user_id", userID),
					zap.Error(err),
				)
				break
			}
			// We can handle client-sent messages here if needed in the future
			// For now, this is mainly a server-push channel
		}
	})
}
