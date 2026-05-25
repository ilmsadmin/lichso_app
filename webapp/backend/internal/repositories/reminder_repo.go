package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// ReminderRepository handles reminder database operations.
type ReminderRepository struct {
	db *gorm.DB
}

// NewReminderRepository creates a new ReminderRepository.
func NewReminderRepository(db *gorm.DB) *ReminderRepository {
	return &ReminderRepository{db: db}
}

// Create creates a new reminder.
func (r *ReminderRepository) Create(reminder *models.Reminder) error {
	return r.db.Create(reminder).Error
}

// GetByID returns a reminder by ID for a specific user.
func (r *ReminderRepository) GetByID(id, userID uuid.UUID) (*models.Reminder, error) {
	var reminder models.Reminder
	err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&reminder).Error
	return &reminder, err
}

// GetByUser returns all reminders for a user.
func (r *ReminderRepository) GetByUser(userID uuid.UUID) ([]models.Reminder, error) {
	var reminders []models.Reminder
	err := r.db.Where("user_id = ?", userID).Order("created_at DESC").Find(&reminders).Error
	return reminders, err
}

// GetActiveByUser returns active reminders for a user.
func (r *ReminderRepository) GetActiveByUser(userID uuid.UUID) ([]models.Reminder, error) {
	var reminders []models.Reminder
	err := r.db.Where("user_id = ? AND is_active = ?", userID, true).
		Order("created_at DESC").Find(&reminders).Error
	return reminders, err
}

// GetByLunarDate returns active reminders matching a lunar date (for all users).
func (r *ReminderRepository) GetByLunarDate(lunarDay, lunarMonth int) ([]models.Reminder, error) {
	var reminders []models.Reminder
	err := r.db.Where("is_lunar = ? AND lunar_day = ? AND lunar_month = ? AND is_active = ?",
		true, lunarDay, lunarMonth, true).Find(&reminders).Error
	return reminders, err
}

// GetBySolarDate returns active reminders matching a solar date (for all users).
func (r *ReminderRepository) GetBySolarDate(solarDay, solarMonth int) ([]models.Reminder, error) {
	var reminders []models.Reminder
	err := r.db.Where("is_lunar = ? AND solar_day = ? AND solar_month = ? AND is_active = ?",
		false, solarDay, solarMonth, true).Find(&reminders).Error
	return reminders, err
}

// Update updates a reminder.
func (r *ReminderRepository) Update(reminder *models.Reminder) error {
	return r.db.Save(reminder).Error
}

// MarkNotified updates the last_notified_at timestamp for a reminder.
func (r *ReminderRepository) MarkNotified(id uuid.UUID) error {
	now := time.Now()
	return r.db.Model(&models.Reminder{}).Where("id = ?", id).Update("last_notified_at", now).Error
}

// GetDueEmailReminders returns active reminders with notify_email=true that are due
// today (remind_before_days before the event), joined with the user email.
// solarDay/solarMonth and lunarDay/lunarMonth represent today's date.
func (r *ReminderRepository) GetDueEmailReminders(solarDay, solarMonth, lunarDay, lunarMonth int) ([]DueReminder, error) {
	type row struct {
		models.Reminder
		Email string
	}
	var rows []row

	err := r.db.Raw(`
		SELECT r.*, u.email
		FROM reminders r
		JOIN users u ON u.id = r.user_id
		WHERE r.is_active = true
		  AND r.notify_email = true
		  AND r.deleted_at IS NULL
		  AND (
		    (r.is_lunar = false AND r.solar_month = ? AND r.solar_day = ?)
		    OR
		    (r.is_lunar = true AND r.lunar_month = ? AND r.lunar_day = ?)
		  )
		  AND (r.last_notified_at IS NULL OR r.last_notified_at < NOW() - INTERVAL '20 hours')
	`, solarMonth, solarDay, lunarMonth, lunarDay).Scan(&rows).Error
	if err != nil {
		return nil, err
	}

	result := make([]DueReminder, len(rows))
	for i, row := range rows {
		result[i] = DueReminder{Reminder: row.Reminder, Email: row.Email}
	}
	return result, nil
}

// DueReminder holds a reminder with the owner's email address.
type DueReminder struct {
	models.Reminder
	Email  string
	UserID string
}

// GetDuePushReminders returns active reminders with notify_push=true that are due
// today, joined with the user's UUID string so push notifications can be sent.
func (r *ReminderRepository) GetDuePushReminders(solarDay, solarMonth, lunarDay, lunarMonth int) ([]DuePushReminder, error) {
	var rows []struct {
		models.Reminder
		UserIDStr string `gorm:"column:user_id_str"`
	}

	err := r.db.Raw(`
		SELECT r.*, r.user_id::text AS user_id_str
		FROM reminders r
		WHERE r.is_active = true
		  AND r.notify_push = true
		  AND r.deleted_at IS NULL
		  AND (
		    (r.is_lunar = false AND r.solar_month = ? AND r.solar_day = ?)
		    OR
		    (r.is_lunar = true AND r.lunar_month = ? AND r.lunar_day = ?)
		  )
		  AND (r.last_notified_at IS NULL OR r.last_notified_at < NOW() - INTERVAL '20 hours')
	`, solarMonth, solarDay, lunarMonth, lunarDay).Scan(&rows).Error
	if err != nil {
		return nil, err
	}

	result := make([]DuePushReminder, len(rows))
	for i, row := range rows {
		result[i] = DuePushReminder{Reminder: row.Reminder, UserIDStr: row.UserIDStr}
	}
	return result, nil
}

// DuePushReminder holds a reminder with the owner's user ID string for push notifications.
type DuePushReminder struct {
	models.Reminder
	UserIDStr string
}

// Delete deletes a reminder.
func (r *ReminderRepository) Delete(id, userID uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Reminder{}).Error
}
