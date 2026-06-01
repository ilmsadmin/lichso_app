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
	// Build raw SQL for the upsert so we can use COALESCE on user_id.
	sql := `
		INSERT INTO device_tokens
			(user_id, token, platform, app_version, device_id, device_name, is_active, last_seen, created_at, updated_at)
		VALUES
			(?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW())
		ON CONFLICT (token) DO UPDATE SET
			user_id     = COALESCE(EXCLUDED.user_id, device_tokens.user_id),
			platform    = EXCLUDED.platform,
			app_version = EXCLUDED.app_version,
			device_id   = EXCLUDED.device_id,
			device_name = CASE WHEN EXCLUDED.device_name != '' THEN EXCLUDED.device_name ELSE device_tokens.device_name END,
			is_active   = EXCLUDED.is_active,
			last_seen   = NOW(),
			updated_at  = NOW()
		RETURNING id, created_at, updated_at, last_seen
	`
	var userID interface{}
	if token.UserID != nil {
		userID = token.UserID
	}
	return r.db.Raw(sql,
		userID, token.Token, token.Platform,
		token.AppVersion, token.DeviceID, token.DeviceName, token.IsActive,
	).Scan(token).Error
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
