package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// ============================================
// User Note Repository
// ============================================

// UserNoteRepository handles user note database operations.
type UserNoteRepository struct {
	db *gorm.DB
}

// NewUserNoteRepository creates a new UserNoteRepository.
func NewUserNoteRepository(db *gorm.DB) *UserNoteRepository {
	return &UserNoteRepository{db: db}
}

// Create creates a new user note.
func (r *UserNoteRepository) Create(note *models.UserNote) error {
	return r.db.Create(note).Error
}

// GetByID returns a note by ID and user ID.
func (r *UserNoteRepository) GetByID(id, userID uuid.UUID) (*models.UserNote, error) {
	var note models.UserNote
	err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&note).Error
	return &note, err
}

// GetByDate returns all notes for a specific date and user.
func (r *UserNoteRepository) GetByDate(userID uuid.UUID, date time.Time) ([]models.UserNote, error) {
	var notes []models.UserNote
	err := r.db.Where("user_id = ? AND note_date = ?", userID, date).
		Order("is_pinned DESC, created_at DESC").
		Find(&notes).Error
	return notes, err
}

// GetByDateRange returns notes within a date range for a user.
func (r *UserNoteRepository) GetByDateRange(userID uuid.UUID, startDate, endDate time.Time) ([]models.UserNote, error) {
	var notes []models.UserNote
	err := r.db.Where("user_id = ? AND note_date >= ? AND note_date <= ?", userID, startDate, endDate).
		Order("note_date ASC, is_pinned DESC, created_at DESC").
		Find(&notes).Error
	return notes, err
}

// List returns all notes for a user with pagination.
func (r *UserNoteRepository) List(userID uuid.UUID, page, limit int) ([]models.UserNote, int64, error) {
	var notes []models.UserNote
	var total int64

	r.db.Model(&models.UserNote{}).Where("user_id = ?", userID).Count(&total)

	err := r.db.Where("user_id = ?", userID).
		Order("note_date DESC, is_pinned DESC, created_at DESC").
		Offset((page - 1) * limit).Limit(limit).
		Find(&notes).Error

	return notes, total, err
}

// Update updates a user note.
func (r *UserNoteRepository) Update(note *models.UserNote) error {
	return r.db.Save(note).Error
}

// Delete soft-deletes a user note.
func (r *UserNoteRepository) Delete(id, userID uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.UserNote{}).Error
}

// ============================================
// User Countdown Repository
// ============================================

// UserCountdownRepository handles user countdown database operations.
type UserCountdownRepository struct {
	db *gorm.DB
}

// NewUserCountdownRepository creates a new UserCountdownRepository.
func NewUserCountdownRepository(db *gorm.DB) *UserCountdownRepository {
	return &UserCountdownRepository{db: db}
}

// Create creates a new user countdown.
func (r *UserCountdownRepository) Create(countdown *models.UserCountdown) error {
	return r.db.Create(countdown).Error
}

// GetByID returns a countdown by ID and user ID.
func (r *UserCountdownRepository) GetByID(id, userID uuid.UUID) (*models.UserCountdown, error) {
	var countdown models.UserCountdown
	err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&countdown).Error
	return &countdown, err
}

// GetActive returns all active countdowns for a user.
func (r *UserCountdownRepository) GetActive(userID uuid.UUID) ([]models.UserCountdown, error) {
	var countdowns []models.UserCountdown
	err := r.db.Where("user_id = ? AND is_active = true", userID).
		Order("target_date ASC").
		Find(&countdowns).Error
	return countdowns, err
}

// GetUpcoming returns countdowns with target dates within N days from now.
func (r *UserCountdownRepository) GetUpcoming(userID uuid.UUID, withinDays int) ([]models.UserCountdown, error) {
	var countdowns []models.UserCountdown
	futureDate := time.Now().AddDate(0, 0, withinDays)
	err := r.db.Where("user_id = ? AND is_active = true AND target_date <= ? AND target_date >= CURRENT_DATE",
		userID, futureDate).
		Order("target_date ASC").
		Find(&countdowns).Error
	return countdowns, err
}

// List returns all countdowns for a user with pagination.
func (r *UserCountdownRepository) List(userID uuid.UUID, page, limit int) ([]models.UserCountdown, int64, error) {
	var countdowns []models.UserCountdown
	var total int64

	r.db.Model(&models.UserCountdown{}).Where("user_id = ?", userID).Count(&total)

	err := r.db.Where("user_id = ?", userID).
		Order("target_date ASC").
		Offset((page - 1) * limit).Limit(limit).
		Find(&countdowns).Error

	return countdowns, total, err
}

// Update updates a user countdown.
func (r *UserCountdownRepository) Update(countdown *models.UserCountdown) error {
	return r.db.Save(countdown).Error
}

// Delete soft-deletes a user countdown.
func (r *UserCountdownRepository) Delete(id, userID uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.UserCountdown{}).Error
}
