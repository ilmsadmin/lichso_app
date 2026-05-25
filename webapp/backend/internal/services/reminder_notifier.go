package services

import (
	"context"
	"fmt"
	"time"

	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/services/lunar"
	"go.uber.org/zap"
)

// reminderTypeLabels maps reminder type codes to Vietnamese display labels
var reminderTypeLabels = map[string]string{
	"holiday":     "Ngày lễ",
	"anniversary": "Kỷ niệm",
	"birthday":    "Sinh nhật",
	"gio":         "Ngày giỗ",
	"custom":      "Nhắc nhở",
}

// reminderTypeEmojis maps reminder type codes to emojis for push notifications
var reminderTypeEmojis = map[string]string{
	"holiday":     "🎊",
	"anniversary": "💕",
	"birthday":    "🎂",
	"gio":         "🕯️",
	"custom":      "📌",
}

// ReminderNotifier is a background service that sends daily reminders via email and push (WebSocket)
type ReminderNotifier struct {
	repo                *repositories.ReminderRepository
	emailService        *EmailService
	notificationService *NotificationService
	logger              *zap.Logger
}

// NewReminderNotifier creates a new ReminderNotifier
func NewReminderNotifier(repo *repositories.ReminderRepository, emailService *EmailService, notificationService *NotificationService, logger *zap.Logger) *ReminderNotifier {
	return &ReminderNotifier{
		repo:                repo,
		emailService:        emailService,
		notificationService: notificationService,
		logger:              logger,
	}
}

// Start runs the reminder notifier in a goroutine.
// It fires once immediately on startup, then every 24 hours aligned to 07:00 Vietnam time.
func (n *ReminderNotifier) Start(ctx context.Context) {
	n.logger.Info("ReminderNotifier started")

	// Fire once at startup (in case server restarted after 7 AM)
	n.runOnce()

	for {
		next := nextRunTime()
		n.logger.Info("ReminderNotifier sleeping until next run", zap.Time("next_run", next))

		select {
		case <-ctx.Done():
			n.logger.Info("ReminderNotifier stopped")
			return
		case <-time.After(time.Until(next)):
			n.runOnce()
		}
	}
}

// nextRunTime returns the next 07:00 AM Vietnam time (UTC+7)
func nextRunTime() time.Time {
	loc, err := time.LoadLocation("Asia/Ho_Chi_Minh")
	if err != nil {
		// Fallback: UTC+7 fixed offset
		loc = time.FixedZone("ICT", 7*3600)
	}

	now := time.Now().In(loc)
	next := time.Date(now.Year(), now.Month(), now.Day(), 7, 0, 0, 0, loc)
	if !next.After(now) {
		next = next.Add(24 * time.Hour)
	}
	return next
}

// runOnce checks for due reminders today and sends notification emails and push notifications
func (n *ReminderNotifier) runOnce() {
	loc, err := time.LoadLocation("Asia/Ho_Chi_Minh")
	if err != nil {
		loc = time.FixedZone("ICT", 7*3600)
	}

	now := time.Now().In(loc)
	solarDay := now.Day()
	solarMonth := int(now.Month())
	solarYear := now.Year()

	// Convert today's solar date to lunar
	lunarDate := lunar.SolarToLunar(solarDay, solarMonth, solarYear, 7.0)
	lunarDay := lunarDate.Day
	lunarMonth := lunarDate.Month

	n.logger.Info("ReminderNotifier running",
		zap.String("solar_date", fmt.Sprintf("%02d/%02d/%d", solarDay, solarMonth, solarYear)),
		zap.String("lunar_date", fmt.Sprintf("%02d/%02d", lunarDay, lunarMonth)),
	)

	ctx := context.Background()

	// ── Push (in-app WebSocket) notifications ──────────────────────────────
	n.sendPushNotifications(ctx, solarDay, solarMonth, lunarDay, lunarMonth)

	// ── Email notifications ────────────────────────────────────────────────
	dueReminders, err := n.repo.GetDueEmailReminders(solarDay, solarMonth, lunarDay, lunarMonth)
	if err != nil {
		n.logger.Error("Failed to get due email reminders", zap.Error(err))
		return
	}

	n.logger.Info("Found due email reminders", zap.Int("count", len(dueReminders)))

	for _, r := range dueReminders {
		// Build date label
		var dateLabel string
		if r.IsLunar {
			dateLabel = fmt.Sprintf("ngày %02d tháng %02d âm lịch", lunarDay, lunarMonth)
		} else {
			dateLabel = fmt.Sprintf("ngày %02d/%02d dương lịch", solarDay, solarMonth)
		}

		// Get type label
		typeLabel, ok := reminderTypeLabels[string(r.ReminderType)]
		if !ok {
			typeLabel = "Nhắc nhở"
		}

		// Use display name or email prefix as fallback
		userName := r.Email
		if idx := len(r.Email); idx > 0 {
			// Use part before @ as friendly name
			for i, ch := range r.Email {
				if ch == '@' {
					userName = r.Email[:i]
					break
				}
			}
		}

		// Send the email
		sendErr := n.emailService.SendReminderEmail(
			r.Email,
			userName,
			r.Title,
			r.Description,
			typeLabel,
			dateLabel,
			r.IsLunar,
		)
		if sendErr != nil {
			n.logger.Error("Failed to send reminder email",
				zap.String("reminder_id", r.ID.String()),
				zap.String("email", r.Email),
				zap.Error(sendErr),
			)
			continue
		}

		// Mark as notified so we don't resend
		if markErr := n.repo.MarkNotified(r.ID); markErr != nil {
			n.logger.Error("Failed to mark reminder as notified",
				zap.String("reminder_id", r.ID.String()),
				zap.Error(markErr),
			)
		} else {
			n.logger.Info("Reminder email sent and marked",
				zap.String("reminder_id", r.ID.String()),
				zap.String("email", r.Email),
				zap.String("title", r.Title),
			)
		}
	}
}

// sendPushNotifications sends in-app (WebSocket) notifications for due push reminders
func (n *ReminderNotifier) sendPushNotifications(ctx context.Context, solarDay, solarMonth, lunarDay, lunarMonth int) {
	if n.notificationService == nil {
		return
	}

	dueReminders, err := n.repo.GetDuePushReminders(solarDay, solarMonth, lunarDay, lunarMonth)
	if err != nil {
		n.logger.Error("Failed to get due push reminders", zap.Error(err))
		return
	}

	n.logger.Info("Found due push reminders", zap.Int("count", len(dueReminders)))

	for _, r := range dueReminders {
		typeLabel, ok := reminderTypeLabels[string(r.ReminderType)]
		if !ok {
			typeLabel = "Nhắc nhở"
		}
		emoji, ok := reminderTypeEmojis[string(r.ReminderType)]
		if !ok {
			emoji = "🔔"
		}

		var dateLabel string
		if r.IsLunar {
			dateLabel = fmt.Sprintf("ngày %02d tháng %02d âm lịch", lunarDay, lunarMonth)
		} else {
			dateLabel = fmt.Sprintf("ngày %02d/%02d dương lịch", solarDay, solarMonth)
		}

		title := fmt.Sprintf("%s %s: %s", emoji, typeLabel, r.Title)
		message := fmt.Sprintf("Nhắc nhở hôm nay, %s.", dateLabel)
		if r.Description != "" {
			message = fmt.Sprintf("%s — %s", message, r.Description)
		}

		sendErr := n.notificationService.SendWithRef(
			ctx,
			r.UserIDStr,
			title,
			message,
			models.NotificationTypeWarning,
			"reminder",
			r.ID.String(),
		)
		if sendErr != nil {
			n.logger.Error("Failed to send push notification for reminder",
				zap.String("reminder_id", r.ID.String()),
				zap.String("user_id", r.UserIDStr),
				zap.Error(sendErr),
			)
		} else {
			n.logger.Info("Push notification sent for reminder",
				zap.String("reminder_id", r.ID.String()),
				zap.String("user_id", r.UserIDStr),
				zap.String("title", r.Title),
			)
		}
	}
}
