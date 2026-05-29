package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

type PushCampaignRepository struct {
	db *gorm.DB
}

func NewPushCampaignRepository(db *gorm.DB) *PushCampaignRepository {
	return &PushCampaignRepository{db: db}
}

func (r *PushCampaignRepository) Create(c *models.PushCampaign) error {
	return r.db.Create(c).Error
}

func (r *PushCampaignRepository) GetByID(id uuid.UUID) (*models.PushCampaign, error) {
	var c models.PushCampaign
	err := r.db.Where("id = ? AND deleted_at IS NULL", id).First(&c).Error
	if err != nil {
		return nil, err
	}
	return &c, nil
}

func (r *PushCampaignRepository) List(page, limit int, status string) ([]models.PushCampaign, int64, error) {
	var campaigns []models.PushCampaign
	var total int64

	query := r.db.Model(&models.PushCampaign{}).Where("deleted_at IS NULL")
	if status != "" {
		query = query.Where("status = ?", status)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * limit
	err := query.Order("created_at DESC").Limit(limit).Offset(offset).Find(&campaigns).Error
	return campaigns, total, err
}

func (r *PushCampaignRepository) Update(c *models.PushCampaign) error {
	return r.db.Save(c).Error
}

func (r *PushCampaignRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.PushCampaign{}).Error
}

// UpdateStatus updates only the status and delivery counters.
func (r *PushCampaignRepository) UpdateStatus(id uuid.UUID, status string, sentCount, failCount int, sentAt interface{}) error {
	updates := map[string]interface{}{
		"status":     status,
		"sent_count": sentCount,
		"fail_count": failCount,
	}
	if sentAt != nil {
		updates["sent_at"] = sentAt
	}
	return r.db.Model(&models.PushCampaign{}).Where("id = ?", id).Updates(updates).Error
}
