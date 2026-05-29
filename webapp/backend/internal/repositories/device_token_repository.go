package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type DeviceTokenRepository struct {
	db *gorm.DB
}

func NewDeviceTokenRepository(db *gorm.DB) *DeviceTokenRepository {
	return &DeviceTokenRepository{db: db}
}

// Upsert inserts or updates a device token (upsert by token value).
func (r *DeviceTokenRepository) Upsert(token *models.DeviceToken) error {
	return r.db.Clauses(clause.OnConflict{
		Columns: []clause.Column{{Name: "token"}},
		DoUpdates: clause.AssignmentColumns([]string{
			"user_id", "platform", "app_version", "device_id",
			"is_active", "last_seen", "updated_at",
		}),
	}).Create(token).Error
}

// DeactivateByToken marks a specific token as inactive (logout/unregister).
func (r *DeviceTokenRepository) DeactivateByToken(token string) error {
	return r.db.Model(&models.DeviceToken{}).
		Where("token = ?", token).
		Updates(map[string]interface{}{
			"is_active":  false,
			"updated_at": time.Now(),
		}).Error
}

// DeactivateByUserID marks all tokens for a user as inactive.
func (r *DeviceTokenRepository) DeactivateByUserID(userID uuid.UUID) error {
	return r.db.Model(&models.DeviceToken{}).
		Where("user_id = ?", userID).
		Updates(map[string]interface{}{
			"is_active":  false,
			"updated_at": time.Now(),
		}).Error
}

// GetActiveTokens returns all active FCM tokens (for broadcast campaigns).
func (r *DeviceTokenRepository) GetActiveTokens(platform string, limit, offset int) ([]string, error) {
	query := r.db.Model(&models.DeviceToken{}).
		Where("is_active = true AND deleted_at IS NULL").
		Select("token")

	if platform != "" {
		query = query.Where("platform = ?", platform)
	}
	if limit > 0 {
		query = query.Limit(limit).Offset(offset)
	}

	var tokens []string
	err := query.Pluck("token", &tokens).Error
	return tokens, err
}

// GetActiveTokensByUserIDs returns active tokens for a list of user IDs.
func (r *DeviceTokenRepository) GetActiveTokensByUserIDs(userIDs []uuid.UUID) ([]string, error) {
	var tokens []string
	err := r.db.Model(&models.DeviceToken{}).
		Where("user_id IN ? AND is_active = true AND deleted_at IS NULL", userIDs).
		Pluck("token", &tokens).Error
	return tokens, err
}

// GetByUserID returns all active device tokens for a single user.
func (r *DeviceTokenRepository) GetByUserID(userID uuid.UUID) ([]models.DeviceToken, error) {
	var tokens []models.DeviceToken
	err := r.db.Where("user_id = ? AND is_active = true AND deleted_at IS NULL", userID).
		Order("last_seen DESC").
		Find(&tokens).Error
	return tokens, err
}

// CountActive returns the count of active device tokens.
func (r *DeviceTokenRepository) CountActive() (int64, error) {
	var count int64
	err := r.db.Model(&models.DeviceToken{}).
		Where("is_active = true AND deleted_at IS NULL").
		Count(&count).Error
	return count, err
}
