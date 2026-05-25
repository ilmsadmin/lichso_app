package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// BookmarkRepository handles bookmark database operations.
type BookmarkRepository struct {
	db *gorm.DB
}

// NewBookmarkRepository creates a new BookmarkRepository.
func NewBookmarkRepository(db *gorm.DB) *BookmarkRepository {
	return &BookmarkRepository{db: db}
}

// Create creates a new bookmark.
func (r *BookmarkRepository) Create(bookmark *models.Bookmark) error {
	return r.db.Create(bookmark).Error
}

// GetByID returns a bookmark by ID for a specific user.
func (r *BookmarkRepository) GetByID(id, userID uuid.UUID) (*models.Bookmark, error) {
	var bookmark models.Bookmark
	err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&bookmark).Error
	return &bookmark, err
}

// GetByUserAndDate returns bookmarks for a user on a specific date.
func (r *BookmarkRepository) GetByUserAndDate(userID uuid.UUID, date time.Time) ([]models.Bookmark, error) {
	var bookmarks []models.Bookmark
	err := r.db.Where("user_id = ? AND solar_date = ?", userID, date).Find(&bookmarks).Error
	return bookmarks, err
}

// GetByUser returns all bookmarks for a user, ordered by date.
func (r *BookmarkRepository) GetByUser(userID uuid.UUID) ([]models.Bookmark, error) {
	var bookmarks []models.Bookmark
	err := r.db.Where("user_id = ?", userID).Order("solar_date DESC").Find(&bookmarks).Error
	return bookmarks, err
}

// GetByUserAndMonth returns bookmarks for a user in a specific month.
func (r *BookmarkRepository) GetByUserAndMonth(userID uuid.UUID, year, month int) ([]models.Bookmark, error) {
	var bookmarks []models.Bookmark
	startDate := time.Date(year, time.Month(month), 1, 0, 0, 0, 0, time.UTC)
	endDate := startDate.AddDate(0, 1, 0)
	err := r.db.Where("user_id = ? AND solar_date >= ? AND solar_date < ?", userID, startDate, endDate).
		Order("solar_date ASC").Find(&bookmarks).Error
	return bookmarks, err
}

// Update updates a bookmark.
func (r *BookmarkRepository) Update(bookmark *models.Bookmark) error {
	return r.db.Save(bookmark).Error
}

// Delete deletes a bookmark.
func (r *BookmarkRepository) Delete(id, userID uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Bookmark{}).Error
}

// ExistsByUserAndDate checks if a bookmark exists for a user on a date with the same title.
func (r *BookmarkRepository) ExistsByUserAndDate(userID uuid.UUID, date time.Time, title string) (bool, error) {
	var count int64
	err := r.db.Model(&models.Bookmark{}).
		Where("user_id = ? AND solar_date = ? AND title = ?", userID, date, title).
		Count(&count).Error
	return count > 0, err
}
