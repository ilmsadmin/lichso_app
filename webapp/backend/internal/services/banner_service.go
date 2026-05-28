package services

import (
	"context"
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// BannerService handles banner business logic.
type BannerService struct {
	repo        *repositories.BannerRepository
	settingRepo *repositories.SettingRepository
	logger      *zap.Logger
}

// NewBannerService creates a new BannerService.
func NewBannerService(repo *repositories.BannerRepository, settingRepo *repositories.SettingRepository, logger *zap.Logger) *BannerService {
	return &BannerService{repo: repo, settingRepo: settingRepo, logger: logger}
}

// Create creates a new banner.
func (s *BannerService) Create(req *dto.CreateBannerRequest) (*dto.BannerResponse, error) {
	banner := &models.Banner{
		Title:    req.Title,
		Subtitle: req.Subtitle,
		ImageURL: req.ImageURL,
		IconURL:  req.IconURL,
		IconKey:  req.IconKey,
		CtaText:  req.CtaText,
		CtaType:  req.CtaType,
		CtaRoute: req.CtaRoute,
		BgColor:  req.BgColor,
		Type:     req.Type,
	}

	if banner.CtaType == "" {
		banner.CtaType = "route"
	}
	if banner.Type == "" {
		banner.Type = "feature"
	}

	if req.IsActive != nil {
		banner.IsActive = *req.IsActive
	}
	if req.SortOrder != nil {
		banner.SortOrder = *req.SortOrder
	}
	if req.StartDate != nil {
		banner.StartDate = req.StartDate
	}
	if req.EndDate != nil {
		banner.EndDate = req.EndDate
	}

	if err := s.repo.Create(banner); err != nil {
		s.logger.Error("Failed to create banner", zap.Error(err))
		return nil, fmt.Errorf("failed to create banner: %w", err)
	}

	return toBannerResponse(banner), nil
}

// GetByID returns a banner by ID.
func (s *BannerService) GetByID(id uuid.UUID) (*dto.BannerResponse, error) {
	banner, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("banner not found: %w", err)
	}
	return toBannerResponse(banner), nil
}

// GetActiveBanners returns active banners for public API.
// If use_server_banners setting is false, returns empty list so app uses local banners.
func (s *BannerService) GetActiveBanners() ([]dto.BannerResponse, error) {
	// Check if server banners are enabled
	ctx := context.Background()
	setting, err := s.settingRepo.FindByKey(ctx, models.SettingKeyUseServerBanners)
	if err == nil && setting != nil {
		// Setting exists — check its value
		if val, ok := setting.Value.(bool); ok && !val {
			// Server banners disabled → return empty list
			return []dto.BannerResponse{}, nil
		}
	}
	// Default: server banners enabled (or setting not found)

	banners, err := s.repo.ListActive()
	if err != nil {
		s.logger.Error("Failed to fetch active banners", zap.Error(err))
		return nil, fmt.Errorf("failed to fetch banners: %w", err)
	}

	result := make([]dto.BannerResponse, len(banners))
	for i, b := range banners {
		result[i] = *toBannerResponse(&b)
	}
	return result, nil
}

// ListAll returns all banners for admin.
func (s *BannerService) ListAll(page, pageSize int, search string) ([]dto.BannerResponse, int64, error) {
	banners, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch banners: %w", err)
	}

	result := make([]dto.BannerResponse, len(banners))
	for i, b := range banners {
		result[i] = *toBannerResponse(&b)
	}
	return result, total, nil
}

// Update updates a banner.
func (s *BannerService) Update(id uuid.UUID, req *dto.UpdateBannerRequest) (*dto.BannerResponse, error) {
	banner, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("banner not found: %w", err)
	}

	if req.Title != nil {
		banner.Title = *req.Title
	}
	if req.Subtitle != nil {
		banner.Subtitle = *req.Subtitle
	}
	if req.ImageURL != nil {
		banner.ImageURL = *req.ImageURL
	}
	if req.IconURL != nil {
		banner.IconURL = *req.IconURL
	}
	if req.IconKey != nil {
		banner.IconKey = *req.IconKey
	}
	if req.CtaText != nil {
		banner.CtaText = *req.CtaText
	}
	if req.CtaType != nil {
		banner.CtaType = *req.CtaType
	}
	if req.CtaRoute != nil {
		banner.CtaRoute = *req.CtaRoute
	}
	if req.BgColor != nil {
		banner.BgColor = *req.BgColor
	}
	if req.Type != nil {
		banner.Type = *req.Type
	}
	if req.IsActive != nil {
		banner.IsActive = *req.IsActive
	}
	if req.SortOrder != nil {
		banner.SortOrder = *req.SortOrder
	}
	if req.StartDate != nil {
		banner.StartDate = req.StartDate
	}
	if req.EndDate != nil {
		banner.EndDate = req.EndDate
	}

	if err := s.repo.Update(banner); err != nil {
		s.logger.Error("Failed to update banner", zap.Error(err))
		return nil, fmt.Errorf("failed to update banner: %w", err)
	}

	return toBannerResponse(banner), nil
}

// ToggleActive toggles a banner's active status.
func (s *BannerService) ToggleActive(id uuid.UUID) (*dto.BannerResponse, error) {
	banner, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("banner not found: %w", err)
	}

	banner.IsActive = !banner.IsActive
	if err := s.repo.Update(banner); err != nil {
		s.logger.Error("Failed to toggle banner", zap.Error(err))
		return nil, fmt.Errorf("failed to toggle banner: %w", err)
	}

	return toBannerResponse(banner), nil
}

// Delete soft-deletes a banner.
func (s *BannerService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete banner", zap.Error(err))
		return fmt.Errorf("failed to delete banner: %w", err)
	}
	return nil
}

// Reorder updates sort_order for multiple banners.
func (s *BannerService) Reorder(req *dto.ReorderBannersRequest) error {
	orders := make(map[uuid.UUID]int, len(req.Orders))
	for _, o := range req.Orders {
		id, err := uuid.Parse(o.ID)
		if err != nil {
			return fmt.Errorf("invalid banner ID: %s", o.ID)
		}
		orders[id] = o.SortOrder
	}

	if err := s.repo.UpdateSortOrders(orders); err != nil {
		s.logger.Error("Failed to reorder banners", zap.Error(err))
		return fmt.Errorf("failed to reorder banners: %w", err)
	}
	return nil
}

func toBannerResponse(b *models.Banner) *dto.BannerResponse {
	resp := &dto.BannerResponse{
		ID:        b.ID.String(),
		Title:     b.Title,
		Subtitle:  b.Subtitle,
		ImageURL:  b.ImageURL,
		IconURL:   b.IconURL,
		IconKey:   b.IconKey,
		CtaText:   b.CtaText,
		CtaType:   b.CtaType,
		CtaRoute:  b.CtaRoute,
		BgColor:   b.BgColor,
		Type:      b.Type,
		IsActive:  b.IsActive,
		SortOrder: b.SortOrder,
		CreatedAt: b.CreatedAt.Format(time.RFC3339),
		UpdatedAt: b.UpdatedAt.Format(time.RFC3339),
	}

	if b.StartDate != nil {
		s := b.StartDate.Format(time.RFC3339)
		resp.StartDate = &s
	}
	if b.EndDate != nil {
		e := b.EndDate.Format(time.RFC3339)
		resp.EndDate = &e
	}

	return resp
}
