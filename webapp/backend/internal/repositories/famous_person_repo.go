package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// FamousPersonRepository handles famous person database operations.
type FamousPersonRepository struct {
	db *gorm.DB
}

// NewFamousPersonRepository creates a new FamousPersonRepository.
func NewFamousPersonRepository(db *gorm.DB) *FamousPersonRepository {
	return &FamousPersonRepository{db: db}
}

// Create creates a new famous person.
func (r *FamousPersonRepository) Create(person *models.FamousPerson) error {
	return r.db.Create(person).Error
}

// GetByID returns a famous person by ID with article preloaded.
func (r *FamousPersonRepository) GetByID(id uuid.UUID) (*models.FamousPerson, error) {
	var person models.FamousPerson
	err := r.db.Preload("Article").Where("id = ?", id).First(&person).Error
	return &person, err
}

// GetByBirthday returns famous people born on a specific month/day.
func (r *FamousPersonRepository) GetByBirthday(month, day int) ([]models.FamousPerson, error) {
	var people []models.FamousPerson
	err := r.db.Where("birth_month = ? AND birth_day = ? AND is_active = ?", month, day, true).
		Order("birth_year ASC NULLS LAST").Find(&people).Error
	return people, err
}

// List returns paginated famous people with optional filters.
func (r *FamousPersonRepository) List(page, pageSize int, category string, isVietnamese *bool) ([]models.FamousPerson, int64, error) {
	var people []models.FamousPerson
	var total int64

	query := r.db.Model(&models.FamousPerson{}).Where("is_active = ?", true)

	if category != "" {
		query = query.Where("category = ?", category)
	}
	if isVietnamese != nil {
		query = query.Where("is_vietnamese = ?", *isVietnamese)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("name ASC").
		Offset(offset).Limit(pageSize).Find(&people).Error

	return people, total, err
}

// ListAll returns all famous people (for admin) with pagination and search.
func (r *FamousPersonRepository) ListAll(page, pageSize int, search string) ([]models.FamousPerson, int64, error) {
	var people []models.FamousPerson
	var total int64

	query := r.db.Model(&models.FamousPerson{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(name ILIKE ? OR original_name ILIKE ? OR occupation ILIKE ?)", searchPattern, searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&people).Error

	return people, total, err
}

// Update updates a famous person.
func (r *FamousPersonRepository) Update(person *models.FamousPerson) error {
	return r.db.Save(person).Error
}

// Delete soft-deletes a famous person.
func (r *FamousPersonRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.FamousPerson{}).Error
}

// CountByDayInMonth returns a map of day -> birthday count for a given month.
func (r *FamousPersonRepository) CountByDayInMonth(month int) (map[int]int64, error) {
	type result struct {
		Day   int   `gorm:"column:day"`
		Count int64 `gorm:"column:count"`
	}
	var results []result
	err := r.db.Model(&models.FamousPerson{}).
		Select("birth_day as day, COUNT(*) as count").
		Where("birth_month = ? AND is_active = ?", month, true).
		Group("birth_day").
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
