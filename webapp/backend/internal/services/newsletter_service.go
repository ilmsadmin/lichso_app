package services

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// NewsletterService handles newsletter subscription business logic
type NewsletterService struct {
	repo   *repositories.NewsletterRepository
	logger *zap.Logger
}

// NewNewsletterService creates a new NewsletterService
func NewNewsletterService(repo *repositories.NewsletterRepository, logger *zap.Logger) *NewsletterService {
	return &NewsletterService{repo: repo, logger: logger}
}

// Subscribe adds a new subscriber or reactivates an existing one
func (s *NewsletterService) Subscribe(email, name, frequency string, userID *uuid.UUID) (*models.NewsletterSubscriber, error) {
	// Check if already subscribed
	existing, err := s.repo.FindByEmail(email)
	if err == nil && existing != nil {
		if existing.IsActive {
			return nil, utils.NewAppError(409, "Email đã được đăng ký newsletter")
		}
		// Reactivate
		existing.IsActive = true
		existing.UnsubscribedAt = nil
		existing.Name = name
		if frequency != "" {
			existing.Frequency = frequency
		}
		if userID != nil {
			existing.UserID = userID
		}
		now := time.Now()
		existing.ConfirmedAt = &now
		existing.UpdatedAt = now
		if err := s.repo.Update(existing); err != nil {
			return nil, utils.ErrDatabaseFail
		}
		return existing, nil
	}

	if frequency == "" {
		frequency = "daily"
	}

	now := time.Now()
	sub := &models.NewsletterSubscriber{
		Email:       email,
		Name:        name,
		UserID:      userID,
		Frequency:   frequency,
		Preferences: models.JSONB{"include_horoscope": true, "include_events": true, "include_quotes": true, "include_articles": true, "include_good_days": true},
		IsActive:    true,
		ConfirmedAt: &now,
	}

	if err := s.repo.Create(sub); err != nil {
		s.logger.Error("Failed to create newsletter subscriber", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	return sub, nil
}

// Unsubscribe deactivates a subscriber by email
func (s *NewsletterService) Unsubscribe(email string) error {
	sub, err := s.repo.FindByEmail(email)
	if err != nil {
		return utils.NewAppError(404, "Email không tìm thấy")
	}

	now := time.Now()
	sub.IsActive = false
	sub.UnsubscribedAt = &now
	sub.UpdatedAt = now

	return s.repo.Update(sub)
}

// UpdatePreferences updates a subscriber's preferences
func (s *NewsletterService) UpdatePreferences(email string, frequency string, preferences models.JSONB) (*models.NewsletterSubscriber, error) {
	sub, err := s.repo.FindByEmail(email)
	if err != nil {
		return nil, utils.NewAppError(404, "Email không tìm thấy")
	}

	if frequency != "" {
		sub.Frequency = frequency
	}
	if preferences != nil {
		sub.Preferences = preferences
	}
	sub.UpdatedAt = time.Now()

	if err := s.repo.Update(sub); err != nil {
		return nil, utils.ErrDatabaseFail
	}
	return sub, nil
}

// GetByEmail returns a subscriber by email
func (s *NewsletterService) GetByEmail(email string) (*models.NewsletterSubscriber, error) {
	sub, err := s.repo.FindByEmail(email)
	if err != nil {
		return nil, utils.NewAppError(404, "Email không tìm thấy")
	}
	return sub, nil
}

// ListActive returns all active subscribers (for admin)
func (s *NewsletterService) ListActive(page, limit int) ([]models.NewsletterSubscriber, int64, error) {
	return s.repo.ListActive(page, limit)
}

// ListAll returns all subscribers with search (for admin)
func (s *NewsletterService) ListAll(page, limit int, search string) ([]models.NewsletterSubscriber, int64, error) {
	return s.repo.ListAll(page, limit, search)
}

// GetStats returns newsletter statistics
func (s *NewsletterService) GetStats() (map[string]interface{}, error) {
	count, err := s.repo.CountActive()
	if err != nil {
		return nil, err
	}

	dailySubs, _ := s.repo.ListByFrequency("daily")
	weeklySubs, _ := s.repo.ListByFrequency("weekly")
	monthlySubs, _ := s.repo.ListByFrequency("monthly")

	return map[string]interface{}{
		"total_active":  count,
		"daily_count":   len(dailySubs),
		"weekly_count":  len(weeklySubs),
		"monthly_count": len(monthlySubs),
		"last_updated":  time.Now().Format("2006-01-02 15:04:05"),
	}, nil
}

// Delete permanently deletes a subscriber (admin)
func (s *NewsletterService) Delete(id string) error {
	uid, err := uuid.Parse(id)
	if err != nil {
		return utils.NewAppError(400, fmt.Sprintf("Invalid ID: %s", id))
	}
	return s.repo.Delete(uid)
}
