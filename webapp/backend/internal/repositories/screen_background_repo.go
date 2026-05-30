package repositories

import (
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// ScreenBackgroundRepository handles database operations for screen backgrounds.
type ScreenBackgroundRepository struct {
	db *gorm.DB
}

// NewScreenBackgroundRepository creates a new ScreenBackgroundRepository.
func NewScreenBackgroundRepository(db *gorm.DB) *ScreenBackgroundRepository {
	return &ScreenBackgroundRepository{db: db}
}

// FindAll returns all screen backgrounds ordered by screen_key.
func (r *ScreenBackgroundRepository) FindAll() ([]models.ScreenBackground, error) {
	var items []models.ScreenBackground
	err := r.db.Order("screen_key ASC").Find(&items).Error
	return items, err
}

// FindByKey returns a screen background by its screen_key.
func (r *ScreenBackgroundRepository) FindByKey(screenKey string) (*models.ScreenBackground, error) {
	var item models.ScreenBackground
	err := r.db.Where("screen_key = ?", screenKey).First(&item).Error
	if err != nil {
		return nil, err
	}
	return &item, nil
}

// Upsert creates or updates a screen background record.
func (r *ScreenBackgroundRepository) Upsert(item *models.ScreenBackground) error {
	return r.db.Save(item).Error
}

// ClearImage clears the image_url and image_path for a given screen_key.
func (r *ScreenBackgroundRepository) ClearImage(screenKey string) error {
	return r.db.Model(&models.ScreenBackground{}).
		Where("screen_key = ?", screenKey).
		Updates(map[string]interface{}{
			"image_url":  "",
			"image_path": "",
			"updated_at": gorm.Expr("NOW()"),
		}).Error
}

// FindAllActive returns all screen backgrounds that have an image set and are active.
func (r *ScreenBackgroundRepository) FindAllActive() ([]models.ScreenBackground, error) {
	var items []models.ScreenBackground
	err := r.db.
		Where("is_active = ? AND image_url != ''", true).
		Order("screen_key ASC").
		Find(&items).Error
	return items, err
}
