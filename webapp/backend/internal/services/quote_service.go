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

// QuoteService handles quote business logic.
type QuoteService struct {
	repo   *repositories.QuoteRepository
	logger *zap.Logger
}

// NewQuoteService creates a new QuoteService.
func NewQuoteService(repo *repositories.QuoteRepository, logger *zap.Logger) *QuoteService {
	return &QuoteService{repo: repo, logger: logger}
}

// Create creates a new quote.
func (s *QuoteService) Create(req *dto.CreateQuoteRequest) (*dto.QuoteResponse, error) {
	quote := &models.Quote{
		Quote:             req.Quote,
		OriginalQuote:     req.OriginalQuote,
		OriginalLanguage:  req.OriginalLanguage,
		Author:            req.Author,
		AuthorBio:         req.AuthorBio,
		AuthorBirthYear:   req.AuthorBirthYear,
		AuthorDeathYear:   req.AuthorDeathYear,
		AuthorNationality: req.AuthorNationality,
		AuthorImageURL:    req.AuthorImageURL,
		Tags:              pq.StringArray(req.Tags),
		DayOfYear:         req.DayOfYear,
	}

	if quote.OriginalLanguage == "" {
		quote.OriginalLanguage = "vi"
	}

	if err := s.repo.Create(quote); err != nil {
		s.logger.Error("Failed to create quote", zap.Error(err))
		return nil, fmt.Errorf("failed to create quote: %w", err)
	}

	return toQuoteResponse(quote), nil
}

// GetByID returns a quote by ID.
func (s *QuoteService) GetByID(id uuid.UUID) (*dto.QuoteResponse, error) {
	quote, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("quote not found: %w", err)
	}
	return toQuoteResponse(quote), nil
}

// GetToday returns the quote of the day based on day of year.
func (s *QuoteService) GetToday() (*dto.QuoteResponse, error) {
	dayOfYear := time.Now().YearDay()
	quotes, err := s.repo.GetByDayOfYear(dayOfYear)
	if err != nil || len(quotes) == 0 {
		// Fallback to random
		quote, err := s.repo.GetRandom()
		if err != nil {
			return nil, fmt.Errorf("no quotes available: %w", err)
		}
		return toQuoteResponse(quote), nil
	}
	return toQuoteResponse(&quotes[0]), nil
}

// GetRandom returns a random quote.
func (s *QuoteService) GetRandom() (*dto.QuoteResponse, error) {
	quote, err := s.repo.GetRandom()
	if err != nil {
		return nil, fmt.Errorf("no quotes available: %w", err)
	}
	return toQuoteResponse(quote), nil
}

// List returns paginated quotes.
func (s *QuoteService) List(page, pageSize int, author string) ([]dto.QuoteResponse, int64, error) {
	quotes, total, err := s.repo.List(page, pageSize, author)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch quotes: %w", err)
	}

	result := make([]dto.QuoteResponse, len(quotes))
	for i, q := range quotes {
		result[i] = *toQuoteResponse(&q)
	}
	return result, total, nil
}

// ListAll returns all quotes (admin).
func (s *QuoteService) ListAll(page, pageSize int, search string) ([]dto.QuoteResponse, int64, error) {
	quotes, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch quotes: %w", err)
	}

	result := make([]dto.QuoteResponse, len(quotes))
	for i, q := range quotes {
		result[i] = *toQuoteResponse(&q)
	}
	return result, total, nil
}

// Update updates a quote.
func (s *QuoteService) Update(id uuid.UUID, req *dto.UpdateQuoteRequest) (*dto.QuoteResponse, error) {
	quote, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("quote not found: %w", err)
	}

	if req.Quote != nil {
		quote.Quote = *req.Quote
	}
	if req.OriginalQuote != nil {
		quote.OriginalQuote = *req.OriginalQuote
	}
	if req.OriginalLanguage != nil {
		quote.OriginalLanguage = *req.OriginalLanguage
	}
	if req.Author != nil {
		quote.Author = *req.Author
	}
	if req.AuthorBio != nil {
		quote.AuthorBio = *req.AuthorBio
	}
	if req.AuthorBirthYear != nil {
		quote.AuthorBirthYear = req.AuthorBirthYear
	}
	if req.AuthorDeathYear != nil {
		quote.AuthorDeathYear = req.AuthorDeathYear
	}
	if req.AuthorNationality != nil {
		quote.AuthorNationality = *req.AuthorNationality
	}
	if req.AuthorImageURL != nil {
		quote.AuthorImageURL = *req.AuthorImageURL
	}
	if req.Tags != nil {
		quote.Tags = pq.StringArray(req.Tags)
	}
	if req.DayOfYear != nil {
		quote.DayOfYear = req.DayOfYear
	}
	if req.IsActive != nil {
		quote.IsActive = *req.IsActive
	}

	if err := s.repo.Update(quote); err != nil {
		s.logger.Error("Failed to update quote", zap.Error(err))
		return nil, fmt.Errorf("failed to update quote: %w", err)
	}

	return toQuoteResponse(quote), nil
}

// Delete soft-deletes a quote.
func (s *QuoteService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete quote", zap.Error(err))
		return fmt.Errorf("failed to delete quote: %w", err)
	}
	return nil
}

func toQuoteResponse(q *models.Quote) *dto.QuoteResponse {
	return &dto.QuoteResponse{
		ID:                q.ID.String(),
		Quote:             q.Quote,
		OriginalQuote:     q.OriginalQuote,
		OriginalLanguage:  q.OriginalLanguage,
		Author:            q.Author,
		AuthorBio:         q.AuthorBio,
		AuthorBirthYear:   q.AuthorBirthYear,
		AuthorDeathYear:   q.AuthorDeathYear,
		AuthorNationality: q.AuthorNationality,
		AuthorImageURL:    q.AuthorImageURL,
		Tags:              []string(q.Tags),
		DayOfYear:         q.DayOfYear,
		IsActive:          q.IsActive,
		CreatedAt:         q.CreatedAt.Format(time.RFC3339),
	}
}
