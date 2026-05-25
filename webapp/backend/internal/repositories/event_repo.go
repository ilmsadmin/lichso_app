package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// EventRepository handles event database operations.
type EventRepository struct {
	db *gorm.DB
}

// NewEventRepository creates a new EventRepository.
func NewEventRepository(db *gorm.DB) *EventRepository {
	return &EventRepository{db: db}
}

// Create creates a new event.
func (r *EventRepository) Create(event *models.Event) error {
	return r.db.Create(event).Error
}

// GetByID returns an event by ID with article preloaded.
func (r *EventRepository) GetByID(id uuid.UUID) (*models.Event, error) {
	var event models.Event
	err := r.db.Preload("Article").Where("id = ?", id).First(&event).Error
	return &event, err
}

// GetBySlug returns an event by slug.
func (r *EventRepository) GetBySlug(slug string) (*models.Event, error) {
	var event models.Event
	err := r.db.Preload("Article").Where("slug = ? AND is_active = ?", slug, true).First(&event).Error
	return &event, err
}

// GetByDate returns solar events matching (month, day) UNION lunar events matching (lunarMonth, lunarDay).
func (r *EventRepository) GetByDate(solarMonth, solarDay, lunarMonth, lunarDay int) ([]models.Event, error) {
	var events []models.Event
	err := r.db.Where(
		"is_active = ? AND ((is_lunar = FALSE AND event_month = ? AND event_day = ?) OR (is_lunar = TRUE AND event_month = ? AND event_day = ?))",
		true, solarMonth, solarDay, lunarMonth, lunarDay,
	).Order("importance DESC, event_year ASC NULLS LAST").Find(&events).Error
	return events, err
}

// List returns paginated events with optional filters.
func (r *EventRepository) List(page, pageSize int, eventType string, importance string) ([]models.Event, int64, error) {
	var events []models.Event
	var total int64

	query := r.db.Model(&models.Event{}).Where("is_active = ?", true)

	if eventType != "" {
		query = query.Where("event_type = ?", eventType)
	}
	if importance != "" {
		query = query.Where("importance = ?", importance)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("event_month ASC, event_day ASC").
		Offset(offset).Limit(pageSize).Find(&events).Error

	return events, total, err
}

// ListAll returns all events (for admin) with pagination and search.
func (r *EventRepository) ListAll(page, pageSize int, search string) ([]models.Event, int64, error) {
	var events []models.Event
	var total int64

	query := r.db.Model(&models.Event{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(title ILIKE ? OR short_description ILIKE ? OR country ILIKE ?)", searchPattern, searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&events).Error

	return events, total, err
}

// Update updates an event.
func (r *EventRepository) Update(event *models.Event) error {
	return r.db.Save(event).Error
}

// Delete soft-deletes an event.
func (r *EventRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.Event{}).Error
}

// SlugExists checks if an event slug already exists.
func (r *EventRepository) SlugExists(slug string, excludeID *uuid.UUID) (bool, error) {
	var count int64
	query := r.db.Model(&models.Event{}).Where("slug = ?", slug)
	if excludeID != nil {
		query = query.Where("id != ?", *excludeID)
	}
	err := query.Count(&count).Error
	return count > 0, err
}

// CountByDayInMonth returns a map of day -> event count for a given month.
func (r *EventRepository) CountByDayInMonth(month int) (map[int]int64, error) {
	type result struct {
		Day   int   `gorm:"column:day"`
		Count int64 `gorm:"column:count"`
	}
	var results []result
	err := r.db.Model(&models.Event{}).
		Select("event_day as day, COUNT(*) as count").
		Where("event_month = ? AND is_active = ?", month, true).
		Group("event_day").
		Find(&results).Error
	if err != nil {
		return nil, err
	}
	m := make(map[int]int64)
	for _, r := range results {
		m[r.Day] = r.Count
	}
	return m, nil
}
