package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

type DeviceTokenRepository struct {
	db *gorm.DB
}

func NewDeviceTokenRepository(db *gorm.DB) *DeviceTokenRepository {
	return &DeviceTokenRepository{db: db}
}

// Upsert inserts or updates a device token (upsert by token value).
// IMPORTANT: user_id uses COALESCE so a NULL incoming value never overwrites
// an existing linked user — prevents cold-start (unauthenticated) registration
// from unlinking a token that was already associated with a user account.
func (r *DeviceTokenRepository) Upsert(token *models.DeviceToken) error {
	var existing models.DeviceToken
	var err error
	found := false

	// 1. Try to find by token first
	if token.Token != "" {
		err = r.db.Where("token = ? AND deleted_at IS NULL", token.Token).First(&existing).Error
		if err == nil {
			found = true
		} else if err != gorm.ErrRecordNotFound {
			return err
		}
	}

	// 2. If not found by token, try to find by device_id (if provided and non-empty)
	if !found && token.DeviceID != "" {
		err = r.db.Where("device_id = ? AND deleted_at IS NULL", token.DeviceID).
			Order("last_seen DESC, updated_at DESC").
			First(&existing).Error
		if err == nil {
			found = true
		} else if err != gorm.ErrRecordNotFound {
			return err
		}
	}

	if found {
		// Update existing record
		if token.UserID != nil {
			existing.UserID = token.UserID
		}
		// If Token has changed, update it
		if token.Token != "" {
			existing.Token = token.Token
		}
		existing.Platform = token.Platform
		existing.AppVersion = token.AppVersion
		existing.DeviceID = token.DeviceID
		if token.DeviceName != "" {
			existing.DeviceName = token.DeviceName
		}
		existing.IsActive = token.IsActive
		existing.LastSeen = time.Now()
		existing.UpdatedAt = time.Now()

		if err := r.db.Save(&existing).Error; err != nil {
			return err
		}
		// Update token struct fields with saved values (for caller compatibility)
		token.ID = existing.ID
		token.UserID = existing.UserID
		token.CreatedAt = existing.CreatedAt
		token.UpdatedAt = existing.UpdatedAt
		token.LastSeen = existing.LastSeen

		// Deactivate any other records with the same device_id to prevent duplicates
		if token.DeviceID != "" {
			err = r.db.Model(&models.DeviceToken{}).
				Where("device_id = ? AND id != ? AND deleted_at IS NULL", token.DeviceID, existing.ID).
				Updates(map[string]interface{}{
					"is_active":  false,
					"updated_at": time.Now(),
				}).Error
			if err != nil {
				return err
			}
		}
		return nil
	}

	// 3. Not found by token or device_id, insert a new record
	token.ID = uuid.New()
	token.LastSeen = time.Now()
	token.CreatedAt = time.Now()
	token.UpdatedAt = time.Now()
	return r.db.Create(token).Error
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
// When platform is non-empty ("android"/"ios"), results are restricted to that platform.
func (r *DeviceTokenRepository) GetActiveTokensByUserIDs(userIDs []uuid.UUID, platform string) ([]string, error) {
	var tokens []string
	query := r.db.Model(&models.DeviceToken{}).
		Where("user_id IN ? AND is_active = true AND deleted_at IS NULL", userIDs)
	if platform != "" {
		query = query.Where("platform = ?", platform)
	}
	err := query.Pluck("token", &tokens).Error
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
