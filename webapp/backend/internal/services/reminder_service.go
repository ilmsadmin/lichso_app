package services

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// ReminderService handles reminder business logic.
type ReminderService struct {
	repo   *repositories.ReminderRepository
	logger *zap.Logger
}

// NewReminderService creates a new ReminderService.
func NewReminderService(repo *repositories.ReminderRepository, logger *zap.Logger) *ReminderService {
	return &ReminderService{repo: repo, logger: logger}
}

// Create creates a new reminder.
func (s *ReminderService) Create(userID uuid.UUID, req *dto.CreateReminderRequest) (*dto.ReminderResponse, error) {
	reminder := &models.Reminder{
		UserID:           userID,
		Title:            req.Title,
		Description:      req.Description,
		ReminderType:     req.ReminderType,
		IsLunar:          req.IsLunar,
		SolarDay:         req.SolarDay,
		SolarMonth:       req.SolarMonth,
		LunarDay:         req.LunarDay,
		LunarMonth:       req.LunarMonth,
		IsRecurring:      req.IsRecurring,
		RemindBeforeDays: req.RemindBeforeDays,
		NotifyEmail:      req.NotifyEmail,
		NotifyPush:       req.NotifyPush,
		IsActive:         true,
	}

	// Validate: lunar reminders need lunar fields, solar need solar fields
	if req.IsLunar {
		if req.LunarDay == nil || req.LunarMonth == nil {
			return nil, fmt.Errorf("lunar_day and lunar_month are required for lunar reminders")
		}
	} else {
		if req.SolarDay == nil || req.SolarMonth == nil {
			return nil, fmt.Errorf("solar_day and solar_month are required for solar reminders")
		}
	}

	if err := s.repo.Create(reminder); err != nil {
		s.logger.Error("Failed to create reminder", zap.Error(err))
		return nil, fmt.Errorf("failed to create reminder: %w", err)
	}

	return toReminderResponse(reminder), nil
}

// GetByUser returns all reminders for a user.
func (s *ReminderService) GetByUser(userID uuid.UUID) ([]dto.ReminderResponse, error) {
	reminders, err := s.repo.GetByUser(userID)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch reminders: %w", err)
	}

	result := make([]dto.ReminderResponse, len(reminders))
	for i, r := range reminders {
		result[i] = *toReminderResponse(&r)
	}
	return result, nil
}

// GetActiveByUser returns active reminders for a user.
func (s *ReminderService) GetActiveByUser(userID uuid.UUID) ([]dto.ReminderResponse, error) {
	reminders, err := s.repo.GetActiveByUser(userID)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch reminders: %w", err)
	}

	result := make([]dto.ReminderResponse, len(reminders))
	for i, r := range reminders {
		result[i] = *toReminderResponse(&r)
	}
	return result, nil
}

// Update updates a reminder.
func (s *ReminderService) Update(userID, reminderID uuid.UUID, req *dto.UpdateReminderRequest) (*dto.ReminderResponse, error) {
	reminder, err := s.repo.GetByID(reminderID, userID)
	if err != nil {
		return nil, fmt.Errorf("reminder not found: %w", err)
	}

	if req.Title != nil {
		reminder.Title = *req.Title
	}
	if req.Description != nil {
		reminder.Description = *req.Description
	}
	if req.ReminderType != nil {
		reminder.ReminderType = *req.ReminderType
	}
	if req.IsLunar != nil {
		reminder.IsLunar = *req.IsLunar
	}
	if req.SolarDay != nil {
		reminder.SolarDay = req.SolarDay
	}
	if req.SolarMonth != nil {
		reminder.SolarMonth = req.SolarMonth
	}
	if req.LunarDay != nil {
		reminder.LunarDay = req.LunarDay
	}
	if req.LunarMonth != nil {
		reminder.LunarMonth = req.LunarMonth
	}
	if req.IsRecurring != nil {
		reminder.IsRecurring = *req.IsRecurring
	}
	if req.RemindBeforeDays != nil {
		reminder.RemindBeforeDays = *req.RemindBeforeDays
	}
	if req.NotifyEmail != nil {
		reminder.NotifyEmail = *req.NotifyEmail
	}
	if req.NotifyPush != nil {
		reminder.NotifyPush = *req.NotifyPush
	}
	if req.IsActive != nil {
		reminder.IsActive = *req.IsActive
	}

	if err := s.repo.Update(reminder); err != nil {
		s.logger.Error("Failed to update reminder", zap.Error(err))
		return nil, fmt.Errorf("failed to update reminder: %w", err)
	}

	return toReminderResponse(reminder), nil
}

// Delete deletes a reminder.
func (s *ReminderService) Delete(userID, reminderID uuid.UUID) error {
	if err := s.repo.Delete(reminderID, userID); err != nil {
		s.logger.Error("Failed to delete reminder", zap.Error(err))
		return fmt.Errorf("failed to delete reminder: %w", err)
	}
	return nil
}

func toReminderResponse(r *models.Reminder) *dto.ReminderResponse {
	return &dto.ReminderResponse{
		ID:               r.ID.String(),
		Title:            r.Title,
		Description:      r.Description,
		ReminderType:     r.ReminderType,
		IsLunar:          r.IsLunar,
		SolarDay:         r.SolarDay,
		SolarMonth:       r.SolarMonth,
		LunarDay:         r.LunarDay,
		LunarMonth:       r.LunarMonth,
		IsRecurring:      r.IsRecurring,
		RemindBeforeDays: r.RemindBeforeDays,
		NotifyEmail:      r.NotifyEmail,
		NotifyPush:       r.NotifyPush,
		IsActive:         r.IsActive,
		CreatedAt:        r.CreatedAt.Format(time.RFC3339),
	}
}
