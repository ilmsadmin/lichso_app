package services

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// FolkFestivalService handles folk festival business logic.
type FolkFestivalService struct {
	repo   *repositories.FolkFestivalRepository
	logger *zap.Logger
}

// NewFolkFestivalService creates a new FolkFestivalService.
func NewFolkFestivalService(repo *repositories.FolkFestivalRepository, logger *zap.Logger) *FolkFestivalService {
	return &FolkFestivalService{repo: repo, logger: logger}
}

// Create creates a new folk festival.
func (s *FolkFestivalService) Create(req *dto.CreateFolkFestivalRequest) (*dto.FolkFestivalResponse, error) {
	slug := req.Slug
	if slug == "" {
		slug = utils.GenerateSlug(req.Name)
	}

	exists, err := s.repo.SlugExists(slug, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to check slug: %w", err)
	}
	if exists {
		slug = fmt.Sprintf("%s-%d", slug, time.Now().UnixMilli())
	}

	importance := req.Importance
	if importance == "" {
		importance = "medium"
	}

	country := req.Country
	if country == "" {
		country = "Việt Nam"
	}

	festival := &models.FolkFestival{
		Name:             req.Name,
		Slug:             slug,
		AlternateName:    req.AlternateName,
		CalendarType:     req.CalendarType,
		LunarDay:         req.LunarDay,
		LunarMonth:       req.LunarMonth,
		SolarDay:         req.SolarDay,
		SolarMonth:       req.SolarMonth,
		DurationDays:     req.DurationDays,
		FestivalType:     req.FestivalType,
		Region:           req.Region,
		Country:          country,
		ShortDescription: req.ShortDescription,
		Traditions:       pq.StringArray(req.Traditions),
		ImageURL:         req.ImageURL,
		GalleryURLs:      pq.StringArray(req.GalleryURLs),
		Importance:       importance,
		Tags:             pq.StringArray(req.Tags),
	}

	if req.ArticleID != "" {
		articleID, err := uuid.Parse(req.ArticleID)
		if err == nil {
			festival.ArticleID = &articleID
		}
	}

	if festival.DurationDays < 1 {
		festival.DurationDays = 1
	}

	if err := s.repo.Create(festival); err != nil {
		s.logger.Error("Failed to create folk festival", zap.Error(err))
		return nil, fmt.Errorf("failed to create folk festival: %w", err)
	}

	return toFolkFestivalResponse(festival), nil
}

// GetByID returns a folk festival by ID.
func (s *FolkFestivalService) GetByID(id uuid.UUID) (*dto.FolkFestivalResponse, error) {
	festival, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("folk festival not found: %w", err)
	}
	return toFolkFestivalResponse(festival), nil
}

// GetBySlug returns a folk festival by slug.
func (s *FolkFestivalService) GetBySlug(slug string) (*dto.FolkFestivalResponse, error) {
	festival, err := s.repo.GetBySlug(slug)
	if err != nil {
		return nil, fmt.Errorf("folk festival not found: %w", err)
	}
	return toFolkFestivalResponse(festival), nil
}

// GetByLunarDate returns festivals for a lunar date.
func (s *FolkFestivalService) GetByLunarDate(month, day int) ([]dto.FolkFestivalResponse, error) {
	festivals, err := s.repo.GetByLunarDate(month, day)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch festivals: %w", err)
	}

	result := make([]dto.FolkFestivalResponse, len(festivals))
	for i, f := range festivals {
		result[i] = *toFolkFestivalResponse(&f)
	}
	return result, nil
}

// GetBySolarDate returns festivals for a solar date.
func (s *FolkFestivalService) GetBySolarDate(month, day int) ([]dto.FolkFestivalResponse, error) {
	festivals, err := s.repo.GetBySolarDate(month, day)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch festivals: %w", err)
	}

	result := make([]dto.FolkFestivalResponse, len(festivals))
	for i, f := range festivals {
		result[i] = *toFolkFestivalResponse(&f)
	}
	return result, nil
}

// List returns paginated folk festivals.
func (s *FolkFestivalService) List(page, pageSize int, festivalType string, calendarType string) ([]dto.FolkFestivalResponse, int64, error) {
	festivals, total, err := s.repo.List(page, pageSize, festivalType, calendarType)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch festivals: %w", err)
	}

	result := make([]dto.FolkFestivalResponse, len(festivals))
	for i, f := range festivals {
		result[i] = *toFolkFestivalResponse(&f)
	}
	return result, total, nil
}

// ListAll returns all folk festivals (admin).
func (s *FolkFestivalService) ListAll(page, pageSize int, search string) ([]dto.FolkFestivalResponse, int64, error) {
	festivals, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch festivals: %w", err)
	}

	result := make([]dto.FolkFestivalResponse, len(festivals))
	for i, f := range festivals {
		result[i] = *toFolkFestivalResponse(&f)
	}
	return result, total, nil
}

// Update updates a folk festival.
func (s *FolkFestivalService) Update(id uuid.UUID, req *dto.UpdateFolkFestivalRequest) (*dto.FolkFestivalResponse, error) {
	festival, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("folk festival not found: %w", err)
	}

	if req.Name != nil {
		festival.Name = *req.Name
	}
	if req.Slug != nil {
		exists, err := s.repo.SlugExists(*req.Slug, &id)
		if err == nil && !exists {
			festival.Slug = *req.Slug
		}
	}
	if req.AlternateName != nil {
		festival.AlternateName = *req.AlternateName
	}
	if req.CalendarType != nil {
		festival.CalendarType = *req.CalendarType
	}
	if req.LunarDay != nil {
		festival.LunarDay = req.LunarDay
	}
	if req.LunarMonth != nil {
		festival.LunarMonth = req.LunarMonth
	}
	if req.SolarDay != nil {
		festival.SolarDay = req.SolarDay
	}
	if req.SolarMonth != nil {
		festival.SolarMonth = req.SolarMonth
	}
	if req.DurationDays != nil {
		festival.DurationDays = *req.DurationDays
	}
	if req.FestivalType != nil {
		festival.FestivalType = *req.FestivalType
	}
	if req.Region != nil {
		festival.Region = *req.Region
	}
	if req.Country != nil {
		festival.Country = *req.Country
	}
	if req.ShortDescription != nil {
		festival.ShortDescription = *req.ShortDescription
	}
	if req.Traditions != nil {
		festival.Traditions = pq.StringArray(req.Traditions)
	}
	if req.ImageURL != nil {
		festival.ImageURL = *req.ImageURL
	}
	if req.GalleryURLs != nil {
		festival.GalleryURLs = pq.StringArray(req.GalleryURLs)
	}
	if req.ArticleID != nil {
		articleID, err := uuid.Parse(*req.ArticleID)
		if err == nil {
			festival.ArticleID = &articleID
		}
	}
	if req.Importance != nil {
		festival.Importance = *req.Importance
	}
	if req.Tags != nil {
		festival.Tags = pq.StringArray(req.Tags)
	}
	if req.IsActive != nil {
		festival.IsActive = *req.IsActive
	}

	if err := s.repo.Update(festival); err != nil {
		s.logger.Error("Failed to update folk festival", zap.Error(err))
		return nil, fmt.Errorf("failed to update folk festival: %w", err)
	}

	return toFolkFestivalResponse(festival), nil
}

// Delete soft-deletes a folk festival.
func (s *FolkFestivalService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete folk festival", zap.Error(err))
		return fmt.Errorf("failed to delete folk festival: %w", err)
	}
	return nil
}

func toFolkFestivalResponse(f *models.FolkFestival) *dto.FolkFestivalResponse {
	resp := &dto.FolkFestivalResponse{
		ID:               f.ID.String(),
		Name:             f.Name,
		Slug:             f.Slug,
		AlternateName:    f.AlternateName,
		CalendarType:     f.CalendarType,
		LunarDay:         f.LunarDay,
		LunarMonth:       f.LunarMonth,
		SolarDay:         f.SolarDay,
		SolarMonth:       f.SolarMonth,
		DurationDays:     f.DurationDays,
		FestivalType:     f.FestivalType,
		Region:           f.Region,
		Country:          f.Country,
		ShortDescription: f.ShortDescription,
		Traditions:       []string(f.Traditions),
		ImageURL:         f.ImageURL,
		GalleryURLs:      []string(f.GalleryURLs),
		Importance:       f.Importance,
		Tags:             []string(f.Tags),
		IsActive:         f.IsActive,
		CreatedAt:        f.CreatedAt.Format(time.RFC3339),
	}
	if f.ArticleID != nil {
		resp.ArticleID = f.ArticleID.String()
	}
	return resp
}
