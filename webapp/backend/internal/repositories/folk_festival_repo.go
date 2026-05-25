package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// FolkFestivalRepository handles folk festival database operations.
type FolkFestivalRepository struct {
	db *gorm.DB
}

// NewFolkFestivalRepository creates a new FolkFestivalRepository.
func NewFolkFestivalRepository(db *gorm.DB) *FolkFestivalRepository {
	return &FolkFestivalRepository{db: db}
}

// Create creates a new folk festival.
func (r *FolkFestivalRepository) Create(festival *models.FolkFestival) error {
	return r.db.Create(festival).Error
}

// GetByID returns a folk festival by ID with article preloaded.
func (r *FolkFestivalRepository) GetByID(id uuid.UUID) (*models.FolkFestival, error) {
	var festival models.FolkFestival
	err := r.db.Preload("Article").Where("id = ?", id).First(&festival).Error
	return &festival, err
}

// GetBySlug returns a folk festival by slug.
func (r *FolkFestivalRepository) GetBySlug(slug string) (*models.FolkFestival, error) {
	var festival models.FolkFestival
	err := r.db.Preload("Article").Where("slug = ? AND is_active = ?", slug, true).First(&festival).Error
	return &festival, err
}

// GetByLunarDate returns folk festivals for a specific lunar month/day.
func (r *FolkFestivalRepository) GetByLunarDate(month, day int) ([]models.FolkFestival, error) {
	var festivals []models.FolkFestival
	err := r.db.Where("lunar_month = ? AND lunar_day = ? AND calendar_type IN (?, ?) AND is_active = ?",
		month, day, "lunar", "both", true).
		Order("importance DESC").Find(&festivals).Error
	return festivals, err
}

// GetBySolarDate returns folk festivals for a specific solar month/day.
func (r *FolkFestivalRepository) GetBySolarDate(month, day int) ([]models.FolkFestival, error) {
	var festivals []models.FolkFestival
	err := r.db.Where("solar_month = ? AND solar_day = ? AND calendar_type IN (?, ?) AND is_active = ?",
		month, day, "solar", "both", true).
		Order("importance DESC").Find(&festivals).Error
	return festivals, err
}

// List returns paginated folk festivals with optional filters.
func (r *FolkFestivalRepository) List(page, pageSize int, festivalType string, calendarType string) ([]models.FolkFestival, int64, error) {
	var festivals []models.FolkFestival
	var total int64

	query := r.db.Model(&models.FolkFestival{}).Where("is_active = ?", true)

	if festivalType != "" {
		query = query.Where("festival_type = ?", festivalType)
	}
	if calendarType != "" {
		query = query.Where("calendar_type = ?", calendarType)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("lunar_month ASC NULLS LAST, lunar_day ASC NULLS LAST, name ASC").
		Offset(offset).Limit(pageSize).Find(&festivals).Error

	return festivals, total, err
}

// ListAll returns all folk festivals (for admin) with pagination and search.
func (r *FolkFestivalRepository) ListAll(page, pageSize int, search string) ([]models.FolkFestival, int64, error) {
	var festivals []models.FolkFestival
	var total int64

	query := r.db.Model(&models.FolkFestival{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(name ILIKE ? OR alternate_name ILIKE ? OR short_description ILIKE ? OR region ILIKE ?)", searchPattern, searchPattern, searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&festivals).Error

	return festivals, total, err
}

// Update updates a folk festival.
func (r *FolkFestivalRepository) Update(festival *models.FolkFestival) error {
	return r.db.Save(festival).Error
}

// Delete soft-deletes a folk festival.
func (r *FolkFestivalRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.FolkFestival{}).Error
}

// SlugExists checks if a festival slug already exists.
func (r *FolkFestivalRepository) SlugExists(slug string, excludeID *uuid.UUID) (bool, error) {
	var count int64
	query := r.db.Model(&models.FolkFestival{}).Where("slug = ?", slug)
	if excludeID != nil {
		query = query.Where("id != ?", *excludeID)
	}
	err := query.Count(&count).Error
	return count > 0, err
}

// CountByDayInSolarMonth returns a map of day -> festival count for a given solar month.
func (r *FolkFestivalRepository) CountByDayInSolarMonth(month int) (map[int]int64, error) {
	type result struct {
		Day   int   `gorm:"column:day"`
		Count int64 `gorm:"column:count"`
	}
	var results []result
	err := r.db.Model(&models.FolkFestival{}).
		Select("solar_day as day, COUNT(*) as count").
		Where("solar_month = ? AND calendar_type IN (?, ?) AND is_active = ?", month, "solar", "both", true).
		Group("solar_day").
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
