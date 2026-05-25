package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// QuoteRepository handles quote database operations.
type QuoteRepository struct {
	db *gorm.DB
}

// NewQuoteRepository creates a new QuoteRepository.
func NewQuoteRepository(db *gorm.DB) *QuoteRepository {
	return &QuoteRepository{db: db}
}

// Create creates a new quote.
func (r *QuoteRepository) Create(quote *models.Quote) error {
	return r.db.Create(quote).Error
}

// GetByID returns a quote by ID.
func (r *QuoteRepository) GetByID(id uuid.UUID) (*models.Quote, error) {
	var quote models.Quote
	err := r.db.Where("id = ?", id).First(&quote).Error
	return &quote, err
}

// GetByDayOfYear returns active quotes for a specific day of the year.
func (r *QuoteRepository) GetByDayOfYear(dayOfYear int) ([]models.Quote, error) {
	var quotes []models.Quote
	err := r.db.Where("day_of_year = ? AND is_active = ?", dayOfYear, true).Find(&quotes).Error
	return quotes, err
}

// GetRandom returns a random active quote.
func (r *QuoteRepository) GetRandom() (*models.Quote, error) {
	var quote models.Quote
	err := r.db.Where("is_active = ?", true).Order("RANDOM()").First(&quote).Error
	return &quote, err
}

// List returns paginated quotes with optional author filter.
func (r *QuoteRepository) List(page, pageSize int, author string) ([]models.Quote, int64, error) {
	var quotes []models.Quote
	var total int64

	query := r.db.Model(&models.Quote{}).Where("is_active = ?", true)
	if author != "" {
		query = query.Where("author ILIKE ?", "%"+author+"%")
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&quotes).Error

	return quotes, total, err
}

// ListAll returns all quotes (for admin) with pagination and search.
func (r *QuoteRepository) ListAll(page, pageSize int, search string) ([]models.Quote, int64, error) {
	var quotes []models.Quote
	var total int64

	query := r.db.Model(&models.Quote{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(quote ILIKE ? OR author ILIKE ?)", searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&quotes).Error

	return quotes, total, err
}

// Update updates a quote.
func (r *QuoteRepository) Update(quote *models.Quote) error {
	return r.db.Save(quote).Error
}

// Delete soft-deletes a quote.
func (r *QuoteRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.Quote{}).Error
}

// CountByDayOfYearRange returns a map of day_of_year -> quote count within a range.
func (r *QuoteRepository) CountByDayOfYearRange(startDOY, endDOY int) (map[int]int64, error) {
	type result struct {
		DayOfYear int   `gorm:"column:day_of_year"`
		Count     int64 `gorm:"column:count"`
	}
	var results []result
	err := r.db.Model(&models.Quote{}).
		Select("day_of_year, COUNT(*) as count").
		Where("day_of_year BETWEEN ? AND ? AND is_active = ?", startDOY, endDOY, true).
		Group("day_of_year").
		Find(&results).Error
	if err != nil {
		return nil, err
	}
	m := make(map[int]int64)
	for _, r := range results {
		m[r.DayOfYear] = r.Count
	}
	return m, nil
}
