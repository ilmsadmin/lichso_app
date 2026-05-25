package services

import (
	"fmt"
	"math"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// ============================================
// User Note Service
// ============================================

// UserNoteService handles user note business logic.
type UserNoteService struct {
	repo   *repositories.UserNoteRepository
	logger *zap.Logger
}

// NewUserNoteService creates a new UserNoteService.
func NewUserNoteService(repo *repositories.UserNoteRepository, logger *zap.Logger) *UserNoteService {
	return &UserNoteService{repo: repo, logger: logger}
}

// Create creates a new user note.
func (s *UserNoteService) Create(userID uuid.UUID, req *dto.CreateUserNoteRequest) (*dto.UserNoteResponse, error) {
	noteDate, err := time.Parse("2006-01-02", req.NoteDate)
	if err != nil {
		return nil, fmt.Errorf("invalid date format (YYYY-MM-DD): %w", err)
	}

	note := &models.UserNote{
		UserID:   userID,
		NoteDate: noteDate,
		Title:    req.Title,
		Content:  req.Content,
		IsPinned: req.IsPinned,
	}

	if req.Color != "" {
		note.Color = req.Color
	}

	if err := s.repo.Create(note); err != nil {
		s.logger.Error("Failed to create user note", zap.Error(err))
		return nil, fmt.Errorf("failed to create note: %w", err)
	}

	return toUserNoteResponse(note), nil
}

// GetByID returns a note by ID.
func (s *UserNoteService) GetByID(id, userID uuid.UUID) (*dto.UserNoteResponse, error) {
	note, err := s.repo.GetByID(id, userID)
	if err != nil {
		return nil, fmt.Errorf("note not found: %w", err)
	}
	return toUserNoteResponse(note), nil
}

// GetByDate returns all notes for a specific date.
func (s *UserNoteService) GetByDate(userID uuid.UUID, dateStr string) ([]dto.UserNoteResponse, error) {
	date, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		return nil, fmt.Errorf("invalid date format: %w", err)
	}

	notes, err := s.repo.GetByDate(userID, date)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch notes: %w", err)
	}

	results := make([]dto.UserNoteResponse, len(notes))
	for i, n := range notes {
		results[i] = *toUserNoteResponse(&n)
	}
	return results, nil
}

// GetByDateRange returns notes within a date range (e.g., for month view).
func (s *UserNoteService) GetByDateRange(userID uuid.UUID, startStr, endStr string) ([]dto.UserNoteResponse, error) {
	start, err := time.Parse("2006-01-02", startStr)
	if err != nil {
		return nil, fmt.Errorf("invalid start_date: %w", err)
	}
	end, err := time.Parse("2006-01-02", endStr)
	if err != nil {
		return nil, fmt.Errorf("invalid end_date: %w", err)
	}

	notes, err := s.repo.GetByDateRange(userID, start, end)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch notes: %w", err)
	}

	results := make([]dto.UserNoteResponse, len(notes))
	for i, n := range notes {
		results[i] = *toUserNoteResponse(&n)
	}
	return results, nil
}

// List returns all notes for a user with pagination.
func (s *UserNoteService) List(userID uuid.UUID, page, limit int) ([]dto.UserNoteResponse, int64, error) {
	notes, total, err := s.repo.List(userID, page, limit)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch notes: %w", err)
	}

	results := make([]dto.UserNoteResponse, len(notes))
	for i, n := range notes {
		results[i] = *toUserNoteResponse(&n)
	}
	return results, total, nil
}

// Update updates a user note.
func (s *UserNoteService) Update(id, userID uuid.UUID, req *dto.UpdateUserNoteRequest) (*dto.UserNoteResponse, error) {
	note, err := s.repo.GetByID(id, userID)
	if err != nil {
		return nil, fmt.Errorf("note not found: %w", err)
	}

	if req.NoteDate != nil {
		noteDate, err := time.Parse("2006-01-02", *req.NoteDate)
		if err == nil {
			note.NoteDate = noteDate
		}
	}
	if req.Title != nil {
		note.Title = *req.Title
	}
	if req.Content != nil {
		note.Content = *req.Content
	}
	if req.Color != nil {
		note.Color = *req.Color
	}
	if req.IsPinned != nil {
		note.IsPinned = *req.IsPinned
	}

	if err := s.repo.Update(note); err != nil {
		s.logger.Error("Failed to update note", zap.Error(err))
		return nil, fmt.Errorf("failed to update note: %w", err)
	}

	return toUserNoteResponse(note), nil
}

// Delete soft-deletes a user note.
func (s *UserNoteService) Delete(id, userID uuid.UUID) error {
	if err := s.repo.Delete(id, userID); err != nil {
		s.logger.Error("Failed to delete note", zap.Error(err))
		return fmt.Errorf("failed to delete note: %w", err)
	}
	return nil
}

func toUserNoteResponse(n *models.UserNote) *dto.UserNoteResponse {
	return &dto.UserNoteResponse{
		ID:        n.ID.String(),
		NoteDate:  n.NoteDate.Format("2006-01-02"),
		Title:     n.Title,
		Content:   n.Content,
		Color:     n.Color,
		IsPinned:  n.IsPinned,
		CreatedAt: n.CreatedAt.Format(time.RFC3339),
		UpdatedAt: n.UpdatedAt.Format(time.RFC3339),
	}
}

// ============================================
// User Countdown Service
// ============================================

// UserCountdownService handles user countdown business logic.
type UserCountdownService struct {
	repo   *repositories.UserCountdownRepository
	logger *zap.Logger
}

// NewUserCountdownService creates a new UserCountdownService.
func NewUserCountdownService(repo *repositories.UserCountdownRepository, logger *zap.Logger) *UserCountdownService {
	return &UserCountdownService{repo: repo, logger: logger}
}

// Create creates a new user countdown.
func (s *UserCountdownService) Create(userID uuid.UUID, req *dto.CreateUserCountdownRequest) (*dto.UserCountdownResponse, error) {
	targetDate, err := time.Parse("2006-01-02", req.TargetDate)
	if err != nil {
		return nil, fmt.Errorf("invalid target_date format (YYYY-MM-DD): %w", err)
	}

	countdown := &models.UserCountdown{
		UserID:           userID,
		Title:            req.Title,
		Description:      req.Description,
		TargetDate:       targetDate,
		IsRecurring:      req.IsRecurring,
		RecurringType:    req.RecurringType,
		NotifyBeforeDays: req.NotifyBeforeDays,
	}

	if req.TargetTime != "" {
		countdown.TargetTime = &req.TargetTime
	}
	if req.Color != "" {
		countdown.Color = req.Color
	}
	if req.Icon != "" {
		countdown.Icon = req.Icon
	}

	if err := s.repo.Create(countdown); err != nil {
		s.logger.Error("Failed to create countdown", zap.Error(err))
		return nil, fmt.Errorf("failed to create countdown: %w", err)
	}

	return toCountdownResponse(countdown), nil
}

// GetByID returns a countdown by ID.
func (s *UserCountdownService) GetByID(id, userID uuid.UUID) (*dto.UserCountdownResponse, error) {
	countdown, err := s.repo.GetByID(id, userID)
	if err != nil {
		return nil, fmt.Errorf("countdown not found: %w", err)
	}
	return toCountdownResponse(countdown), nil
}

// GetActive returns all active countdowns.
func (s *UserCountdownService) GetActive(userID uuid.UUID) ([]dto.UserCountdownResponse, error) {
	countdowns, err := s.repo.GetActive(userID)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch countdowns: %w", err)
	}

	results := make([]dto.UserCountdownResponse, len(countdowns))
	for i, c := range countdowns {
		results[i] = *toCountdownResponse(&c)
	}
	return results, nil
}

// GetUpcoming returns countdowns within N days from now.
func (s *UserCountdownService) GetUpcoming(userID uuid.UUID, withinDays int) ([]dto.UserCountdownResponse, error) {
	if withinDays <= 0 {
		withinDays = 30
	}

	countdowns, err := s.repo.GetUpcoming(userID, withinDays)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch upcoming countdowns: %w", err)
	}

	results := make([]dto.UserCountdownResponse, len(countdowns))
	for i, c := range countdowns {
		results[i] = *toCountdownResponse(&c)
	}
	return results, nil
}

// List returns all countdowns for a user with pagination.
func (s *UserCountdownService) List(userID uuid.UUID, page, limit int) ([]dto.UserCountdownResponse, int64, error) {
	countdowns, total, err := s.repo.List(userID, page, limit)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch countdowns: %w", err)
	}

	results := make([]dto.UserCountdownResponse, len(countdowns))
	for i, c := range countdowns {
		results[i] = *toCountdownResponse(&c)
	}
	return results, total, nil
}

// Update updates a user countdown.
func (s *UserCountdownService) Update(id, userID uuid.UUID, req *dto.UpdateUserCountdownRequest) (*dto.UserCountdownResponse, error) {
	countdown, err := s.repo.GetByID(id, userID)
	if err != nil {
		return nil, fmt.Errorf("countdown not found: %w", err)
	}

	if req.Title != nil {
		countdown.Title = *req.Title
	}
	if req.Description != nil {
		countdown.Description = *req.Description
	}
	if req.TargetDate != nil {
		targetDate, err := time.Parse("2006-01-02", *req.TargetDate)
		if err == nil {
			countdown.TargetDate = targetDate
		}
	}
	if req.TargetTime != nil {
		countdown.TargetTime = req.TargetTime
	}
	if req.Color != nil {
		countdown.Color = *req.Color
	}
	if req.Icon != nil {
		countdown.Icon = *req.Icon
	}
	if req.IsRecurring != nil {
		countdown.IsRecurring = *req.IsRecurring
	}
	if req.RecurringType != nil {
		countdown.RecurringType = *req.RecurringType
	}
	if req.NotifyBeforeDays != nil {
		countdown.NotifyBeforeDays = *req.NotifyBeforeDays
	}
	if req.IsActive != nil {
		countdown.IsActive = *req.IsActive
	}

	if err := s.repo.Update(countdown); err != nil {
		s.logger.Error("Failed to update countdown", zap.Error(err))
		return nil, fmt.Errorf("failed to update countdown: %w", err)
	}

	return toCountdownResponse(countdown), nil
}

// Delete soft-deletes a user countdown.
func (s *UserCountdownService) Delete(id, userID uuid.UUID) error {
	if err := s.repo.Delete(id, userID); err != nil {
		s.logger.Error("Failed to delete countdown", zap.Error(err))
		return fmt.Errorf("failed to delete countdown: %w", err)
	}
	return nil
}

func toCountdownResponse(c *models.UserCountdown) *dto.UserCountdownResponse {
	loc, _ := time.LoadLocation("Asia/Ho_Chi_Minh")
	now := time.Now().In(loc)
	daysRemaining := int(math.Ceil(c.TargetDate.Sub(now).Hours() / 24))

	resp := &dto.UserCountdownResponse{
		ID:               c.ID.String(),
		Title:            c.Title,
		Description:      c.Description,
		TargetDate:       c.TargetDate.Format("2006-01-02"),
		Color:            c.Color,
		Icon:             c.Icon,
		IsRecurring:      c.IsRecurring,
		RecurringType:    c.RecurringType,
		NotifyBeforeDays: c.NotifyBeforeDays,
		DaysRemaining:    daysRemaining,
		IsActive:         c.IsActive,
		CreatedAt:        c.CreatedAt.Format(time.RFC3339),
		UpdatedAt:        c.UpdatedAt.Format(time.RFC3339),
	}

	if c.TargetTime != nil {
		resp.TargetTime = *c.TargetTime
	}

	return resp
}
