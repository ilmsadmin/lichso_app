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

// BookmarkService handles bookmark business logic.
type BookmarkService struct {
	repo   *repositories.BookmarkRepository
	logger *zap.Logger
}

// NewBookmarkService creates a new BookmarkService.
func NewBookmarkService(repo *repositories.BookmarkRepository, logger *zap.Logger) *BookmarkService {
	return &BookmarkService{repo: repo, logger: logger}
}

// Create creates a new bookmark.
func (s *BookmarkService) Create(userID uuid.UUID, req *dto.CreateBookmarkRequest) (*dto.BookmarkResponse, error) {
	solarDate, err := time.Parse("2006-01-02", req.SolarDate)
	if err != nil {
		return nil, fmt.Errorf("invalid date format: %w", err)
	}

	// Check duplicate
	exists, err := s.repo.ExistsByUserAndDate(userID, solarDate, req.Title)
	if err != nil {
		s.logger.Error("Failed to check bookmark existence", zap.Error(err))
		return nil, fmt.Errorf("failed to check bookmark: %w", err)
	}
	if exists {
		return nil, fmt.Errorf("bookmark already exists for this date and title")
	}

	color := req.Color
	if color == "" {
		color = "amber"
	}

	bookmark := &models.Bookmark{
		UserID:      userID,
		SolarDate:   solarDate,
		Title:       req.Title,
		Note:        req.Note,
		Color:       color,
		IsRecurring: req.IsRecurring,
	}

	if err := s.repo.Create(bookmark); err != nil {
		s.logger.Error("Failed to create bookmark", zap.Error(err))
		return nil, fmt.Errorf("failed to create bookmark: %w", err)
	}

	return toBookmarkResponse(bookmark), nil
}

// GetByUser returns all bookmarks for a user.
func (s *BookmarkService) GetByUser(userID uuid.UUID) ([]dto.BookmarkResponse, error) {
	bookmarks, err := s.repo.GetByUser(userID)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch bookmarks: %w", err)
	}

	result := make([]dto.BookmarkResponse, len(bookmarks))
	for i, b := range bookmarks {
		result[i] = *toBookmarkResponse(&b)
	}
	return result, nil
}

// GetByMonth returns bookmarks for a specific month.
func (s *BookmarkService) GetByMonth(userID uuid.UUID, year, month int) ([]dto.BookmarkResponse, error) {
	bookmarks, err := s.repo.GetByUserAndMonth(userID, year, month)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch bookmarks: %w", err)
	}

	result := make([]dto.BookmarkResponse, len(bookmarks))
	for i, b := range bookmarks {
		result[i] = *toBookmarkResponse(&b)
	}
	return result, nil
}

// GetByDate returns bookmarks for a specific date.
func (s *BookmarkService) GetByDate(userID uuid.UUID, dateStr string) ([]dto.BookmarkResponse, error) {
	date, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		return nil, fmt.Errorf("invalid date format: %w", err)
	}

	bookmarks, err := s.repo.GetByUserAndDate(userID, date)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch bookmarks: %w", err)
	}

	result := make([]dto.BookmarkResponse, len(bookmarks))
	for i, b := range bookmarks {
		result[i] = *toBookmarkResponse(&b)
	}
	return result, nil
}

// Update updates a bookmark.
func (s *BookmarkService) Update(userID, bookmarkID uuid.UUID, req *dto.UpdateBookmarkRequest) (*dto.BookmarkResponse, error) {
	bookmark, err := s.repo.GetByID(bookmarkID, userID)
	if err != nil {
		return nil, fmt.Errorf("bookmark not found: %w", err)
	}

	if req.Title != nil {
		bookmark.Title = *req.Title
	}
	if req.Note != nil {
		bookmark.Note = *req.Note
	}
	if req.Color != nil {
		bookmark.Color = *req.Color
	}
	if req.IsRecurring != nil {
		bookmark.IsRecurring = *req.IsRecurring
	}

	if err := s.repo.Update(bookmark); err != nil {
		s.logger.Error("Failed to update bookmark", zap.Error(err))
		return nil, fmt.Errorf("failed to update bookmark: %w", err)
	}

	return toBookmarkResponse(bookmark), nil
}

// Delete deletes a bookmark.
func (s *BookmarkService) Delete(userID, bookmarkID uuid.UUID) error {
	if err := s.repo.Delete(bookmarkID, userID); err != nil {
		s.logger.Error("Failed to delete bookmark", zap.Error(err))
		return fmt.Errorf("failed to delete bookmark: %w", err)
	}
	return nil
}

func toBookmarkResponse(b *models.Bookmark) *dto.BookmarkResponse {
	return &dto.BookmarkResponse{
		ID:          b.ID.String(),
		SolarDate:   b.SolarDate.Format("2006-01-02"),
		Title:       b.Title,
		Note:        b.Note,
		Color:       b.Color,
		IsRecurring: b.IsRecurring,
		CreatedAt:   b.CreatedAt.Format(time.RFC3339),
	}
}
