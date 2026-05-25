package services

import (
	"context"
	"time"

	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"
)

// SettingService handles settings business logic
type SettingService struct {
	settingRepo *repositories.SettingRepository
	mongoDB     *mongo.Database
	logger      *zap.Logger
}

// NewSettingService creates a new SettingService
func NewSettingService(
	settingRepo *repositories.SettingRepository,
	mongoDB *mongo.Database,
	logger *zap.Logger,
) *SettingService {
	return &SettingService{
		settingRepo: settingRepo,
		mongoDB:     mongoDB,
		logger:      logger,
	}
}

// GetAllSettings returns all settings
func (s *SettingService) GetAllSettings(ctx context.Context) ([]models.SettingResponse, error) {
	settings, err := s.settingRepo.FindAll(ctx)
	if err != nil {
		s.logger.Error("Failed to get all settings", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	responses := make([]models.SettingResponse, len(settings))
	for i, setting := range settings {
		responses[i] = setting.ToResponse()
	}

	return responses, nil
}

// GetSettingsByGroup returns settings for a specific group
func (s *SettingService) GetSettingsByGroup(ctx context.Context, group string) ([]models.SettingResponse, error) {
	settings, err := s.settingRepo.FindByGroup(ctx, group)
	if err != nil {
		s.logger.Error("Failed to get settings by group", zap.Error(err), zap.String("group", group))
		return nil, utils.ErrDatabaseFail
	}

	responses := make([]models.SettingResponse, len(settings))
	for i, setting := range settings {
		responses[i] = setting.ToResponse()
	}

	return responses, nil
}

// GetGroupedSettings returns all settings grouped by group
func (s *SettingService) GetGroupedSettings(ctx context.Context) (map[string][]models.SettingResponse, error) {
	grouped, err := s.settingRepo.GetGrouped(ctx)
	if err != nil {
		s.logger.Error("Failed to get grouped settings", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	result := make(map[string][]models.SettingResponse)
	for group, settings := range grouped {
		responses := make([]models.SettingResponse, len(settings))
		for i, setting := range settings {
			responses[i] = setting.ToResponse()
		}
		result[group] = responses
	}

	return result, nil
}

// GetSetting returns a single setting by key
func (s *SettingService) GetSetting(ctx context.Context, key string) (*models.SettingResponse, error) {
	setting, err := s.settingRepo.FindByKey(ctx, key)
	if err != nil {
		s.logger.Error("Failed to get setting", zap.Error(err), zap.String("key", key))
		return nil, utils.NewAppError(404, "Setting not found")
	}

	resp := setting.ToResponse()
	return &resp, nil
}

// UpdateSetting updates a single setting
func (s *SettingService) UpdateSetting(ctx context.Context, req *dto.UpdateSettingRequest, actorID, actorEmail, ip, ua string) (*models.SettingResponse, error) {
	// Get existing setting to preserve group and description
	existing, err := s.settingRepo.FindByKey(ctx, req.Key)
	if err != nil {
		s.logger.Error("Setting not found for update", zap.Error(err), zap.String("key", req.Key))
		return nil, utils.NewAppError(404, "Setting not found")
	}

	existing.Value = req.Value
	existing.UpdatedBy = actorID
	existing.UpdatedAt = time.Now()

	if err := s.settingRepo.Upsert(ctx, existing); err != nil {
		s.logger.Error("Failed to update setting", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Log activity
	s.logActivity(ctx, actorID, actorEmail, ip, ua, req.Key, existing.Group)

	resp := existing.ToResponse()
	return &resp, nil
}

// UpdateSettingsGroup updates multiple settings in a group at once
func (s *SettingService) UpdateSettingsGroup(ctx context.Context, req *dto.UpdateSettingsGroupRequest, actorID, actorEmail, ip, ua string) ([]models.SettingResponse, error) {
	settings := make([]models.Setting, len(req.Settings))
	for i, item := range req.Settings {
		settings[i] = models.Setting{
			Key:       item.Key,
			Value:     item.Value,
			Group:     req.Group,
			UpdatedBy: actorID,
			UpdatedAt: time.Now(),
		}
	}

	if err := s.settingRepo.BulkUpsert(ctx, settings); err != nil {
		s.logger.Error("Failed to bulk update settings", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Log activity
	activityLog := models.NewActivityLog(actorID, actorEmail, models.ActionSettingsUpdate, models.ModuleSetting,
		"Updated "+req.Group+" settings").
		WithStatus(models.StatusSuccess).
		WithIPAndAgent(ip, ua).
		WithMetadata(map[string]interface{}{
			"group":          req.Group,
			"settings_count": len(req.Settings),
		})

	collection := s.mongoDB.Collection(models.ActivityLog{}.CollectionName())
	_, err := collection.InsertOne(ctx, activityLog)
	if err != nil {
		s.logger.Error("Failed to log activity", zap.Error(err))
	}

	// Return updated settings
	return s.GetSettingsByGroup(ctx, req.Group)
}

// CreateSetting creates a new setting
func (s *SettingService) CreateSetting(ctx context.Context, req *dto.CreateSettingRequest, actorID string) (*models.SettingResponse, error) {
	// Check if key already exists
	existing, _ := s.settingRepo.FindByKey(ctx, req.Key)
	if existing != nil {
		return nil, utils.NewAppError(409, "Setting with this key already exists")
	}

	setting := &models.Setting{
		Key:         req.Key,
		Value:       req.Value,
		Group:       req.Group,
		Description: req.Description,
		UpdatedBy:   actorID,
		UpdatedAt:   time.Now(),
	}

	if err := s.settingRepo.Upsert(ctx, setting); err != nil {
		s.logger.Error("Failed to create setting", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	resp := setting.ToResponse()
	return &resp, nil
}

// DeleteSetting removes a setting by key
func (s *SettingService) DeleteSetting(ctx context.Context, key string) error {
	if err := s.settingRepo.Delete(ctx, key); err != nil {
		s.logger.Error("Failed to delete setting", zap.Error(err))
		return utils.ErrDatabaseFail
	}
	return nil
}

// logActivity creates an activity log entry for settings changes
func (s *SettingService) logActivity(ctx context.Context, actorID, actorEmail, ip, ua, key, group string) {
	log := models.NewActivityLog(actorID, actorEmail, models.ActionSettingsUpdate, models.ModuleSetting,
		"Updated setting: "+key).
		WithStatus(models.StatusSuccess).
		WithIPAndAgent(ip, ua).
		WithMetadata(map[string]interface{}{
			"key":   key,
			"group": group,
		})

	collection := s.mongoDB.Collection(models.ActivityLog{}.CollectionName())
	_, err := collection.InsertOne(ctx, log)
	if err != nil {
		s.logger.Error("Failed to log activity",
			zap.String("action", models.ActionSettingsUpdate),
			zap.Error(err),
		)
	}
}
