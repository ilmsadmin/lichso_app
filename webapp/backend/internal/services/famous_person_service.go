package services

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// FamousPersonService handles famous person business logic.
type FamousPersonService struct {
	repo   *repositories.FamousPersonRepository
	logger *zap.Logger
}

// NewFamousPersonService creates a new FamousPersonService.
func NewFamousPersonService(repo *repositories.FamousPersonRepository, logger *zap.Logger) *FamousPersonService {
	return &FamousPersonService{repo: repo, logger: logger}
}

// Create creates a new famous person.
func (s *FamousPersonService) Create(req *dto.CreateFamousPersonRequest) (*dto.FamousPersonResponse, error) {
	person := &models.FamousPerson{
		Name:         req.Name,
		OriginalName: req.OriginalName,
		BirthDay:     req.BirthDay,
		BirthMonth:   req.BirthMonth,
		BirthYear:    req.BirthYear,
		Nationality:  req.Nationality,
		Occupation:   req.Occupation,
		Category:     req.Category,
		ShortBio:     req.ShortBio,
		ImageURL:     req.ImageURL,
		IsVietnamese: req.IsVietnamese,
		Tags:         pq.StringArray(req.Tags),
	}

	if req.BirthDate != "" {
		birthDate, err := time.Parse("2006-01-02", req.BirthDate)
		if err == nil {
			person.BirthDate = &birthDate
		}
	}
	if req.DeathDate != "" {
		deathDate, err := time.Parse("2006-01-02", req.DeathDate)
		if err == nil {
			person.DeathDate = &deathDate
		}
	}
	if req.ArticleID != "" {
		articleID, err := uuid.Parse(req.ArticleID)
		if err == nil {
			person.ArticleID = &articleID
		}
	}

	if err := s.repo.Create(person); err != nil {
		s.logger.Error("Failed to create famous person", zap.Error(err))
		return nil, fmt.Errorf("failed to create famous person: %w", err)
	}

	return toFamousPersonResponse(person), nil
}

// GetByID returns a famous person by ID.
func (s *FamousPersonService) GetByID(id uuid.UUID) (*dto.FamousPersonResponse, error) {
	person, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("famous person not found: %w", err)
	}
	return toFamousPersonResponse(person), nil
}

// GetByBirthday returns famous people born on a specific date.
func (s *FamousPersonService) GetByBirthday(month, day int) ([]dto.FamousPersonResponse, error) {
	people, err := s.repo.GetByBirthday(month, day)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch famous people: %w", err)
	}

	result := make([]dto.FamousPersonResponse, len(people))
	for i, p := range people {
		result[i] = *toFamousPersonResponse(&p)
	}
	return result, nil
}

// List returns paginated famous people.
func (s *FamousPersonService) List(page, pageSize int, category string, isVietnamese *bool) ([]dto.FamousPersonResponse, int64, error) {
	people, total, err := s.repo.List(page, pageSize, category, isVietnamese)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch famous people: %w", err)
	}

	result := make([]dto.FamousPersonResponse, len(people))
	for i, p := range people {
		result[i] = *toFamousPersonResponse(&p)
	}
	return result, total, nil
}

// ListAll returns all famous people (admin).
func (s *FamousPersonService) ListAll(page, pageSize int, search string) ([]dto.FamousPersonResponse, int64, error) {
	people, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch famous people: %w", err)
	}

	result := make([]dto.FamousPersonResponse, len(people))
	for i, p := range people {
		result[i] = *toFamousPersonResponse(&p)
	}
	return result, total, nil
}

// Update updates a famous person.
func (s *FamousPersonService) Update(id uuid.UUID, req *dto.UpdateFamousPersonRequest) (*dto.FamousPersonResponse, error) {
	person, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("famous person not found: %w", err)
	}

	if req.Name != nil {
		person.Name = *req.Name
	}
	if req.OriginalName != nil {
		person.OriginalName = *req.OriginalName
	}
	if req.BirthDate != nil {
		birthDate, err := time.Parse("2006-01-02", *req.BirthDate)
		if err == nil {
			person.BirthDate = &birthDate
		}
	}
	if req.BirthDay != nil {
		person.BirthDay = req.BirthDay
	}
	if req.BirthMonth != nil {
		person.BirthMonth = req.BirthMonth
	}
	if req.BirthYear != nil {
		person.BirthYear = req.BirthYear
	}
	if req.DeathDate != nil {
		deathDate, err := time.Parse("2006-01-02", *req.DeathDate)
		if err == nil {
			person.DeathDate = &deathDate
		}
	}
	if req.Nationality != nil {
		person.Nationality = *req.Nationality
	}
	if req.Occupation != nil {
		person.Occupation = *req.Occupation
	}
	if req.Category != nil {
		person.Category = *req.Category
	}
	if req.ShortBio != nil {
		person.ShortBio = *req.ShortBio
	}
	if req.ImageURL != nil {
		person.ImageURL = *req.ImageURL
	}
	if req.ArticleID != nil {
		articleID, err := uuid.Parse(*req.ArticleID)
		if err == nil {
			person.ArticleID = &articleID
		}
	}
	if req.IsVietnamese != nil {
		person.IsVietnamese = *req.IsVietnamese
	}
	if req.Tags != nil {
		person.Tags = pq.StringArray(req.Tags)
	}
	if req.IsActive != nil {
		person.IsActive = *req.IsActive
	}

	if err := s.repo.Update(person); err != nil {
		s.logger.Error("Failed to update famous person", zap.Error(err))
		return nil, fmt.Errorf("failed to update famous person: %w", err)
	}

	return toFamousPersonResponse(person), nil
}

// Delete soft-deletes a famous person.
func (s *FamousPersonService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete famous person", zap.Error(err))
		return fmt.Errorf("failed to delete famous person: %w", err)
	}
	return nil
}

func toFamousPersonResponse(p *models.FamousPerson) *dto.FamousPersonResponse {
	resp := &dto.FamousPersonResponse{
		ID:           p.ID.String(),
		Name:         p.Name,
		OriginalName: p.OriginalName,
		BirthDay:     p.BirthDay,
		BirthMonth:   p.BirthMonth,
		BirthYear:    p.BirthYear,
		Nationality:  p.Nationality,
		Occupation:   p.Occupation,
		Category:     p.Category,
		ShortBio:     p.ShortBio,
		ImageURL:     p.ImageURL,
		IsVietnamese: p.IsVietnamese,
		Tags:         []string(p.Tags),
		IsActive:     p.IsActive,
		CreatedAt:    p.CreatedAt.Format(time.RFC3339),
	}
	if p.BirthDate != nil {
		resp.BirthDate = p.BirthDate.Format("2006-01-02")
	}
	if p.DeathDate != nil {
		resp.DeathDate = p.DeathDate.Format("2006-01-02")
	}
	if p.ArticleID != nil {
		resp.ArticleID = p.ArticleID.String()
	}
	return resp
}
