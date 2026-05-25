package services

import (
	"context"

	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.uber.org/zap"
)

// NotificationService handles notification business logic
type NotificationService struct {
	notifRepo    *repositories.NotificationRepository
	wsHub        *WSHub
	emailService *EmailService
	logger       *zap.Logger
}

// NewNotificationService creates a new NotificationService
func NewNotificationService(
	notifRepo *repositories.NotificationRepository,
	wsHub *WSHub,
	logger *zap.Logger,
) *NotificationService {
	return &NotificationService{
		notifRepo: notifRepo,
		wsHub:     wsHub,
		logger:    logger,
	}
}

// SetEmailService sets the email service (called after initialization)
func (s *NotificationService) SetEmailService(emailService *EmailService) {
	s.emailService = emailService
}

// Send creates a notification and pushes it via WebSocket if the user is online
func (s *NotificationService) Send(ctx context.Context, userID, title, message, notifType string, link ...string) error {
	notif := models.NewNotification(userID, title, message, notifType)
	if len(link) > 0 && link[0] != "" {
		notif.WithLink(link[0])
	}

	if err := s.notifRepo.Create(ctx, notif); err != nil {
		s.logger.Error("Failed to create notification",
			zap.String("user_id", userID),
			zap.Error(err),
		)
		return err
	}

	// Push via WebSocket
	s.wsHub.SendToUser(userID, models.WSMessage{
		Type: "notification",
		Data: notif.ToResponse(),
	})

	// Also send updated unread count
	s.sendUnreadCount(ctx, userID)

	return nil
}

// SendWithRef creates a notification with a reference to a source entity (reminder, bookmark, etc.)
func (s *NotificationService) SendWithRef(ctx context.Context, userID, title, message, notifType, refType, refID string, link ...string) error {
	notif := models.NewNotification(userID, title, message, notifType)
	notif.RefType = refType
	notif.RefID = refID
	if len(link) > 0 && link[0] != "" {
		notif.WithLink(link[0])
	}

	if err := s.notifRepo.Create(ctx, notif); err != nil {
		s.logger.Error("Failed to create notification",
			zap.String("user_id", userID),
			zap.Error(err),
		)
		return err
	}

	// Push via WebSocket
	s.wsHub.SendToUser(userID, models.WSMessage{
		Type: "notification",
		Data: notif.ToResponse(),
	})

	s.sendUnreadCount(ctx, userID)
	return nil
}

// SendWithEmail creates a notification, pushes via WebSocket, and sends email
func (s *NotificationService) SendWithEmail(ctx context.Context, userID, userEmail, userName, title, message, notifType string, link ...string) error {
	// Send in-app notification first
	if err := s.Send(ctx, userID, title, message, notifType, link...); err != nil {
		return err
	}

	// Send email notification asynchronously
	if s.emailService != nil && s.emailService.IsEnabled() && userEmail != "" {
		go func() {
			if err := s.emailService.SendNotificationEmail(userEmail, userName, title, message, notifType, link...); err != nil {
				s.logger.Error("Failed to send notification email",
					zap.String("user_id", userID),
					zap.String("email", userEmail),
					zap.Error(err),
				)
			}
		}()
	}

	return nil
}

// SendToMultiple sends a notification to multiple users
func (s *NotificationService) SendToMultiple(ctx context.Context, userIDs []string, title, message, notifType string, link ...string) {
	for _, uid := range userIDs {
		_ = s.Send(ctx, uid, title, message, notifType, link...)
	}
}

// GetByUserID returns notifications for a user with pagination
func (s *NotificationService) GetByUserID(ctx context.Context, userID string, page, limit int) ([]models.NotificationResponse, int64, error) {
	notifications, total, err := s.notifRepo.FindByUserID(ctx, userID, page, limit)
	if err != nil {
		return nil, 0, err
	}

	responses := make([]models.NotificationResponse, len(notifications))
	for i, n := range notifications {
		responses[i] = n.ToResponse()
	}

	return responses, total, nil
}

// GetUnreadCount returns the unread notification count for a user
func (s *NotificationService) GetUnreadCount(ctx context.Context, userID string) (int64, error) {
	return s.notifRepo.CountUnread(ctx, userID)
}

// MarkAsRead marks a notification as read
func (s *NotificationService) MarkAsRead(ctx context.Context, notifID, userID string) error {
	objID, err := primitive.ObjectIDFromHex(notifID)
	if err != nil {
		return err
	}

	if err := s.notifRepo.MarkAsRead(ctx, objID, userID); err != nil {
		return err
	}

	// Push updated unread count via WebSocket
	s.sendUnreadCount(ctx, userID)

	return nil
}

// MarkAllAsRead marks all notifications as read for a user
func (s *NotificationService) MarkAllAsRead(ctx context.Context, userID string) error {
	if err := s.notifRepo.MarkAllAsRead(ctx, userID); err != nil {
		return err
	}

	// Push updated unread count via WebSocket
	s.wsHub.SendToUser(userID, models.WSMessage{
		Type: "notification_read_all",
		Data: nil,
	})
	s.sendUnreadCount(ctx, userID)

	return nil
}

// Delete deletes a notification
func (s *NotificationService) Delete(ctx context.Context, notifID, userID string) error {
	objID, err := primitive.ObjectIDFromHex(notifID)
	if err != nil {
		return err
	}

	if err := s.notifRepo.Delete(ctx, objID, userID); err != nil {
		return err
	}

	s.sendUnreadCount(ctx, userID)
	return nil
}

// DeleteAll deletes all notifications for a user
func (s *NotificationService) DeleteAll(ctx context.Context, userID string) error {
	if err := s.notifRepo.DeleteAllByUser(ctx, userID); err != nil {
		return err
	}

	s.sendUnreadCount(ctx, userID)
	return nil
}

// sendUnreadCount pushes the current unread count to a user via WebSocket
func (s *NotificationService) sendUnreadCount(ctx context.Context, userID string) {
	count, err := s.notifRepo.CountUnread(ctx, userID)
	if err != nil {
		s.logger.Error("Failed to get unread count", zap.Error(err))
		return
	}

	s.wsHub.SendToUser(userID, models.WSMessage{
		Type: "notification_count",
		Data: map[string]int64{"unread_count": count},
	})
}

// GetWSHub returns the WebSocket hub (for handler to use)
func (s *NotificationService) GetWSHub() *WSHub {
	return s.wsHub
}
