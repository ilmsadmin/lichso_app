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

// PopupService handles popup business logic.
type PopupService struct {
	repo   *repositories.PopupRepository
	logger *zap.Logger
}

// NewPopupService creates a new PopupService.
func NewPopupService(repo *repositories.PopupRepository, logger *zap.Logger) *PopupService {
	return &PopupService{repo: repo, logger: logger}
}

// Create creates a new popup.
func (s *PopupService) Create(req *dto.CreatePopupRequest) (*dto.PopupResponse, error) {
	popup := &models.Popup{
		Title:    req.Title,
		ImageURL: req.ImageURL,
		CtaType:  req.CtaType,
		CtaRoute: req.CtaRoute,
		Position: req.Position,
		Platform: req.Platform,
	}

	if popup.CtaType == "" {
		popup.CtaType = "route"
	}
	if popup.Position == "" {
		popup.Position = "center"
	}
	if popup.Platform == "" {
		popup.Platform = models.PlatformAll
	}

	if req.IsActive != nil {
		popup.IsActive = *req.IsActive
	}
	if req.StartDate != nil {
		popup.StartDate = req.StartDate
	}
	if req.EndDate != nil {
		popup.EndDate = req.EndDate
	}

	if err := s.repo.Create(popup); err != nil {
		s.logger.Error("Failed to create popup", zap.Error(err))
		return nil, fmt.Errorf("failed to create popup: %w", err)
	}

	return toPopupResponse(popup), nil
}

// GetByID returns a popup by ID.
func (s *PopupService) GetByID(id uuid.UUID) (*dto.PopupResponse, error) {
	popup, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("popup not found: %w", err)
	}
	return toPopupResponse(popup), nil
}

// GetActivePopups returns active popups for public API, filtered by client platform
// ("android"/"ios"; empty returns only platform-agnostic popups).
func (s *PopupService) GetActivePopups(platform string) ([]dto.PopupResponse, error) {
	popups, err := s.repo.ListActive(platform)
	if err != nil {
		s.logger.Error("Failed to fetch active popups", zap.Error(err))
		return nil, fmt.Errorf("failed to fetch active popups: %w", err)
	}

	result := make([]dto.PopupResponse, len(popups))
	for i, p := range popups {
		result[i] = *toPopupResponse(&p)
	}
	return result, nil
}

// ListAll returns all popups for admin.
func (s *PopupService) ListAll(page, pageSize int, search string) ([]dto.PopupResponse, int64, error) {
	popups, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch popups: %w", err)
	}

	result := make([]dto.PopupResponse, len(popups))
	for i, p := range popups {
		result[i] = *toPopupResponse(&p)
	}
	return result, total, nil
}

// Update updates a popup.
func (s *PopupService) Update(id uuid.UUID, req *dto.UpdatePopupRequest) (*dto.PopupResponse, error) {
	popup, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("popup not found: %w", err)
	}

	if req.Title != nil {
		popup.Title = *req.Title
	}
	if req.ImageURL != nil {
		popup.ImageURL = *req.ImageURL
	}
	if req.CtaType != nil {
		popup.CtaType = *req.CtaType
	}
	if req.CtaRoute != nil {
		popup.CtaRoute = *req.CtaRoute
	}
	if req.Position != nil {
		popup.Position = *req.Position
	}
	if req.Platform != nil {
		popup.Platform = *req.Platform
	}
	if req.IsActive != nil {
		popup.IsActive = *req.IsActive
	}
	if req.StartDate != nil {
		popup.StartDate = req.StartDate
	}
	if req.EndDate != nil {
		popup.EndDate = req.EndDate
	}

	if err := s.repo.Update(popup); err != nil {
		s.logger.Error("Failed to update popup", zap.Error(err))
		return nil, fmt.Errorf("failed to update popup: %w", err)
	}

	return toPopupResponse(popup), nil
}

// ToggleActive toggles a popup's active status.
func (s *PopupService) ToggleActive(id uuid.UUID) (*dto.PopupResponse, error) {
	popup, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("popup not found: %w", err)
	}

	popup.IsActive = !popup.IsActive
	if err := s.repo.Update(popup); err != nil {
		s.logger.Error("Failed to toggle popup", zap.Error(err))
		return nil, fmt.Errorf("failed to toggle popup: %w", err)
	}

	return toPopupResponse(popup), nil
}

// Delete soft-deletes a popup.
func (s *PopupService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete popup", zap.Error(err))
		return fmt.Errorf("failed to delete popup: %w", err)
	}
	return nil
}

func toPopupResponse(p *models.Popup) *dto.PopupResponse {
	resp := &dto.PopupResponse{
		ID:        p.ID.String(),
		Title:     p.Title,
		ImageURL:  p.ImageURL,
		CtaType:   p.CtaType,
		CtaRoute:  p.CtaRoute,
		Position:  p.Position,
		Platform:  p.Platform,
		IsActive:  p.IsActive,
		CreatedAt: p.CreatedAt.Format(time.RFC3339),
		UpdatedAt: p.UpdatedAt.Format(time.RFC3339),
	}

	if p.StartDate != nil {
		s := p.StartDate.Format(time.RFC3339)
		resp.StartDate = &s
	}
	if p.EndDate != nil {
		e := p.EndDate.Format(time.RFC3339)
		resp.EndDate = &e
	}

	return resp
}
