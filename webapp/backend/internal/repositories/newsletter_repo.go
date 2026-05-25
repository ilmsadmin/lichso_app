package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// NewsletterRepository handles data access for newsletter subscribers
type NewsletterRepository struct {
	db *gorm.DB
}

// NewNewsletterRepository creates a new NewsletterRepository
func NewNewsletterRepository(db *gorm.DB) *NewsletterRepository {
	return &NewsletterRepository{db: db}
}

// Create creates a new subscriber
func (r *NewsletterRepository) Create(sub *models.NewsletterSubscriber) error {
	return r.db.Create(sub).Error
}

// FindByEmail finds a subscriber by email
func (r *NewsletterRepository) FindByEmail(email string) (*models.NewsletterSubscriber, error) {
	var sub models.NewsletterSubscriber
	err := r.db.Where("email = ?", email).First(&sub).Error
	if err != nil {
		return nil, err
	}
	return &sub, nil
}

// FindByID finds a subscriber by ID
func (r *NewsletterRepository) FindByID(id uuid.UUID) (*models.NewsletterSubscriber, error) {
	var sub models.NewsletterSubscriber
	err := r.db.Where("id = ?", id).First(&sub).Error
	if err != nil {
		return nil, err
	}
	return &sub, nil
}

// Update updates a subscriber
func (r *NewsletterRepository) Update(sub *models.NewsletterSubscriber) error {
	return r.db.Save(sub).Error
}

// Delete hard-deletes a subscriber
func (r *NewsletterRepository) Delete(id uuid.UUID) error {
	return r.db.Delete(&models.NewsletterSubscriber{}, "id = ?", id).Error
}

// ListActive returns all active subscribers with pagination
func (r *NewsletterRepository) ListActive(page, limit int) ([]models.NewsletterSubscriber, int64, error) {
	var subs []models.NewsletterSubscriber
	var total int64

	query := r.db.Model(&models.NewsletterSubscriber{}).Where("is_active = ?", true)
	query.Count(&total)

	offset := (page - 1) * limit
	err := query.Order("created_at DESC").Offset(offset).Limit(limit).Find(&subs).Error
	return subs, total, err
}

// ListByFrequency returns active subscribers by frequency
func (r *NewsletterRepository) ListByFrequency(frequency string) ([]models.NewsletterSubscriber, error) {
	var subs []models.NewsletterSubscriber
	err := r.db.Where("is_active = ? AND frequency = ?", true, frequency).Find(&subs).Error
	return subs, err
}

// CountActive returns the count of active subscribers
func (r *NewsletterRepository) CountActive() (int64, error) {
	var count int64
	err := r.db.Model(&models.NewsletterSubscriber{}).Where("is_active = ?", true).Count(&count).Error
	return count, err
}

// ListAll returns all subscribers with pagination (for admin)
func (r *NewsletterRepository) ListAll(page, limit int, search string) ([]models.NewsletterSubscriber, int64, error) {
	var subs []models.NewsletterSubscriber
	var total int64

	query := r.db.Model(&models.NewsletterSubscriber{})
	if search != "" {
		query = query.Where("email ILIKE ? OR name ILIKE ?", "%"+search+"%", "%"+search+"%")
	}
	query.Count(&total)

	offset := (page - 1) * limit
	err := query.Order("created_at DESC").Offset(offset).Limit(limit).Find(&subs).Error
	return subs, total, err
}
