package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// PopupRepository handles popup database operations.
type PopupRepository struct {
	db *gorm.DB
}

// NewPopupRepository creates a new PopupRepository.
func NewPopupRepository(db *gorm.DB) *PopupRepository {
	return &PopupRepository{db: db}
}

// Create creates a new popup.
func (r *PopupRepository) Create(popup *models.Popup) error {
	return r.db.Create(popup).Error
}

// GetByID returns a popup by ID.
func (r *PopupRepository) GetByID(id uuid.UUID) (*models.Popup, error) {
	var popup models.Popup
	err := r.db.Where("id = ?", id).First(&popup).Error
	return &popup, err
}

// ListActive returns active popups within valid date range.
func (r *PopupRepository) ListActive() ([]models.Popup, error) {
	var popups []models.Popup
	now := time.Now()
	err := r.db.Where("is_active = ?", true).
		Where("(start_date IS NULL OR start_date <= ?)", now).
		Where("(end_date IS NULL OR end_date >= ?)", now).
		Order("created_at DESC").
		Find(&popups).Error
	return popups, err
}

// ListAll returns all popups (for admin) with pagination and search.
func (r *PopupRepository) ListAll(page, pageSize int, search string) ([]models.Popup, int64, error) {
	var popups []models.Popup
	var total int64

	query := r.db.Model(&models.Popup{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(title ILIKE ? OR position ILIKE ?)", searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&popups).Error

	return popups, total, err
}

// Update updates a popup.
func (r *PopupRepository) Update(popup *models.Popup) error {
	return r.db.Save(popup).Error
}

// Delete soft-deletes a popup.
func (r *PopupRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.Popup{}).Error
}
