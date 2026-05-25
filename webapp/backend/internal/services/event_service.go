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

// EventService handles event business logic.
type EventService struct {
	repo   *repositories.EventRepository
	logger *zap.Logger
}

// NewEventService creates a new EventService.
func NewEventService(repo *repositories.EventRepository, logger *zap.Logger) *EventService {
	return &EventService{repo: repo, logger: logger}
}

// Create creates a new event.
func (s *EventService) Create(req *dto.CreateEventRequest) (*dto.EventResponse, error) {
	slug := req.Slug
	if slug == "" {
		slug = utils.GenerateSlug(req.Title)
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
		importance = models.EventImportanceMedium
	}

	event := &models.Event{
		Title:            req.Title,
		Slug:             slug,
		EventDay:         req.EventDay,
		EventMonth:       req.EventMonth,
		EventYear:        req.EventYear,
		IsLunar:          req.IsLunar,
		IsRecurring:      req.IsRecurring,
		EventType:        req.EventType,
		Country:          req.Country,
		CountryCode:      req.CountryCode,
		FlagEmoji:        req.FlagEmoji,
		ShortDescription: req.ShortDescription,
		ImageURL:         req.ImageURL,
		Importance:       importance,
		Tags:             pq.StringArray(req.Tags),
	}

	if req.EventDate != "" {
		eventDate, err := time.Parse("2006-01-02", req.EventDate)
		if err == nil {
			event.EventDate = &eventDate
		}
	}
	if req.ArticleID != "" {
		articleID, err := uuid.Parse(req.ArticleID)
		if err == nil {
			event.ArticleID = &articleID
		}
	}

	if err := s.repo.Create(event); err != nil {
		s.logger.Error("Failed to create event", zap.Error(err))
		return nil, fmt.Errorf("failed to create event: %w", err)
	}

	return toEventResponse(event), nil
}

// GetByID returns an event by ID.
func (s *EventService) GetByID(id uuid.UUID) (*dto.EventResponse, error) {
	event, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("event not found: %w", err)
	}
	return toEventResponse(event), nil
}

// GetBySlug returns an event by slug.
func (s *EventService) GetBySlug(slug string) (*dto.EventResponse, error) {
	event, err := s.repo.GetBySlug(slug)
	if err != nil {
		return nil, fmt.Errorf("event not found: %w", err)
	}
	return toEventResponse(event), nil
}

// GetByDate returns solar-calendar events for a specific date AND
// lunar-calendar events whose lunar day/month matches the provided lunar date.
func (s *EventService) GetByDate(solarMonth, solarDay, lunarMonth, lunarDay int) ([]dto.EventResponse, error) {
	events, err := s.repo.GetByDate(solarMonth, solarDay, lunarMonth, lunarDay)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch events: %w", err)
	}

	result := make([]dto.EventResponse, len(events))
	for i, e := range events {
		result[i] = *toEventResponse(&e)
	}
	return result, nil
}

// List returns paginated events.
func (s *EventService) List(page, pageSize int, eventType string, importance string) ([]dto.EventResponse, int64, error) {
	events, total, err := s.repo.List(page, pageSize, eventType, importance)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch events: %w", err)
	}

	result := make([]dto.EventResponse, len(events))
	for i, e := range events {
		result[i] = *toEventResponse(&e)
	}
	return result, total, nil
}

// ListAll returns all events (admin).
func (s *EventService) ListAll(page, pageSize int, search string) ([]dto.EventResponse, int64, error) {
	events, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch events: %w", err)
	}

	result := make([]dto.EventResponse, len(events))
	for i, e := range events {
		result[i] = *toEventResponse(&e)
	}
	return result, total, nil
}

// Update updates an event.
func (s *EventService) Update(id uuid.UUID, req *dto.UpdateEventRequest) (*dto.EventResponse, error) {
	event, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("event not found: %w", err)
	}

	if req.Title != nil {
		event.Title = *req.Title
	}
	if req.Slug != nil {
		exists, err := s.repo.SlugExists(*req.Slug, &id)
		if err == nil && !exists {
			event.Slug = *req.Slug
		}
	}
	if req.EventDate != nil {
		eventDate, err := time.Parse("2006-01-02", *req.EventDate)
		if err == nil {
			event.EventDate = &eventDate
		}
	}
	if req.EventDay != nil {
		event.EventDay = *req.EventDay
	}
	if req.EventMonth != nil {
		event.EventMonth = *req.EventMonth
	}
	if req.EventYear != nil {
		event.EventYear = req.EventYear
	}
	if req.IsLunar != nil {
		event.IsLunar = *req.IsLunar
	}
	if req.IsRecurring != nil {
		event.IsRecurring = *req.IsRecurring
	}
	if req.EventType != nil {
		event.EventType = *req.EventType
	}
	if req.Country != nil {
		event.Country = *req.Country
	}
	if req.CountryCode != nil {
		event.CountryCode = *req.CountryCode
	}
	if req.FlagEmoji != nil {
		event.FlagEmoji = *req.FlagEmoji
	}
	if req.ShortDescription != nil {
		event.ShortDescription = *req.ShortDescription
	}
	if req.ImageURL != nil {
		event.ImageURL = *req.ImageURL
	}
	if req.ArticleID != nil {
		articleID, err := uuid.Parse(*req.ArticleID)
		if err == nil {
			event.ArticleID = &articleID
		}
	}
	if req.Importance != nil {
		event.Importance = *req.Importance
	}
	if req.Tags != nil {
		event.Tags = pq.StringArray(req.Tags)
	}
	if req.IsActive != nil {
		event.IsActive = *req.IsActive
	}

	if err := s.repo.Update(event); err != nil {
		s.logger.Error("Failed to update event", zap.Error(err))
		return nil, fmt.Errorf("failed to update event: %w", err)
	}

	return toEventResponse(event), nil
}

// Delete soft-deletes an event.
func (s *EventService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete event", zap.Error(err))
		return fmt.Errorf("failed to delete event: %w", err)
	}
	return nil
}

func toEventResponse(e *models.Event) *dto.EventResponse {
	resp := &dto.EventResponse{
		ID:               e.ID.String(),
		Title:            e.Title,
		Slug:             e.Slug,
		EventDay:         e.EventDay,
		EventMonth:       e.EventMonth,
		EventYear:        e.EventYear,
		IsLunar:          e.IsLunar,
		IsRecurring:      e.IsRecurring,
		EventType:        e.EventType,
		Country:          e.Country,
		CountryCode:      e.CountryCode,
		FlagEmoji:        e.FlagEmoji,
		ShortDescription: e.ShortDescription,
		ImageURL:         e.ImageURL,
		Importance:       e.Importance,
		Tags:             []string(e.Tags),
		IsActive:         e.IsActive,
		CreatedAt:        e.CreatedAt.Format(time.RFC3339),
	}
	if e.EventDate != nil {
		resp.EventDate = e.EventDate.Format("2006-01-02")
	}
	if e.ArticleID != nil {
		resp.ArticleID = e.ArticleID.String()
	}
	return resp
}
