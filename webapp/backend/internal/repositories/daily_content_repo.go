package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// DailyContentRepository handles daily content schedule database operations.
type DailyContentRepository struct {
	db *gorm.DB
}

// NewDailyContentRepository creates a new DailyContentRepository.
func NewDailyContentRepository(db *gorm.DB) *DailyContentRepository {
	return &DailyContentRepository{db: db}
}

// Create creates a new daily content schedule.
func (r *DailyContentRepository) Create(schedule *models.DailyContentSchedule) error {
	return r.db.Create(schedule).Error
}

// GetByID returns a schedule by ID.
func (r *DailyContentRepository) GetByID(id uuid.UUID) (*models.DailyContentSchedule, error) {
	var schedule models.DailyContentSchedule
	err := r.db.Where("id = ?", id).First(&schedule).Error
	return &schedule, err
}

// GetByDate returns all active content scheduled for a specific date.
// This handles all schedule modes: fixed_date, recurring_annual, day_of_year.
func (r *DailyContentRepository) GetByDate(date time.Time) ([]models.DailyContentSchedule, error) {
	var schedules []models.DailyContentSchedule
	dayOfYear := date.YearDay()
	month := int(date.Month())
	day := date.Day()
	year := date.Year()

	err := r.db.Where("is_active = true AND deleted_at IS NULL").
		Where("(start_date IS NULL OR start_date <= ?) AND (end_date IS NULL OR end_date >= ?)", date, date).
		Where(`
			(schedule_mode = 'fixed_date' AND fixed_date = ?) OR
			(schedule_mode = 'recurring_annual' AND recurring_month = ? AND recurring_day = ?) OR
			(schedule_mode = 'day_of_year' AND day_of_year = ?) OR
			(schedule_mode = 'lunar_date')
		`, date, month, day, dayOfYear).
		Where("year_filter IS NULL OR year_filter = ?", year).
		Order("display_section ASC, display_priority DESC, created_at ASC").
		Find(&schedules).Error

	return schedules, err
}

// GetByLunarDate returns content scheduled for a specific lunar date.
func (r *DailyContentRepository) GetByLunarDate(lunarMonth, lunarDay int) ([]models.DailyContentSchedule, error) {
	var schedules []models.DailyContentSchedule
	err := r.db.Where("is_active = true AND deleted_at IS NULL").
		Where("schedule_mode = 'lunar_date' AND lunar_month = ? AND lunar_day = ?", lunarMonth, lunarDay).
		Order("display_priority DESC").
		Find(&schedules).Error
	return schedules, err
}

// GetByContentType returns schedules filtered by content type.
func (r *DailyContentRepository) GetByContentType(contentType string, page, limit int) ([]models.DailyContentSchedule, int64, error) {
	var schedules []models.DailyContentSchedule
	var total int64

	query := r.db.Model(&models.DailyContentSchedule{})
	if contentType != "" {
		query = query.Where("content_type = ?", contentType)
	}

	query.Count(&total)

	err := query.Order("created_at DESC").
		Offset((page - 1) * limit).Limit(limit).
		Find(&schedules).Error

	return schedules, total, err
}

// List returns all schedules with pagination (admin).
func (r *DailyContentRepository) List(page, limit int) ([]models.DailyContentSchedule, int64, error) {
	var schedules []models.DailyContentSchedule
	var total int64

	r.db.Model(&models.DailyContentSchedule{}).Count(&total)

	err := r.db.Order("created_at DESC").
		Offset((page - 1) * limit).Limit(limit).
		Find(&schedules).Error

	return schedules, total, err
}

// Update updates a daily content schedule.
func (r *DailyContentRepository) Update(schedule *models.DailyContentSchedule) error {
	return r.db.Save(schedule).Error
}

// Delete soft-deletes a daily content schedule.
func (r *DailyContentRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.DailyContentSchedule{}).Error
}

// CountByDayInMonth returns a map of day -> count of scheduled content for a given month.
// This counts recurring_annual items for the given month and day_of_year items within the month's DOY range.
func (r *DailyContentRepository) CountByDayInMonth(year, month int, startDOY, endDOY int) (map[int]int64, error) {
	type result struct {
		Day   int   `gorm:"column:day"`
		Count int64 `gorm:"column:count"`
	}
	// Recurring annual
	var recurringResults []result
	err := r.db.Model(&models.DailyContentSchedule{}).
		Select("recurring_day as day, COUNT(*) as count").
		Where("is_active = true AND deleted_at IS NULL").
		Where("schedule_mode = 'recurring_annual' AND recurring_month = ?", month).
		Where("year_filter IS NULL OR year_filter = ?", year).
		Group("recurring_day").
		Find(&recurringResults).Error
	if err != nil {
		return nil, err
	}

	m := make(map[int]int64)
	for _, r := range recurringResults {
		m[r.Day] = r.Count
	}

	// Day of year
	type doyResult struct {
		DayOfYear int   `gorm:"column:day_of_year"`
		Count     int64 `gorm:"column:count"`
	}
	var doyResults []doyResult
	err = r.db.Model(&models.DailyContentSchedule{}).
		Select("day_of_year, COUNT(*) as count").
		Where("is_active = true AND deleted_at IS NULL").
		Where("schedule_mode = 'day_of_year' AND day_of_year BETWEEN ? AND ?", startDOY, endDOY).
		Where("year_filter IS NULL OR year_filter = ?", year).
		Group("day_of_year").
		Find(&doyResults).Error
	if err != nil {
		return nil, err
	}

	// Convert DOY to day in month
	startOfMonth := time.Date(year, time.Month(month), 1, 0, 0, 0, 0, time.UTC)
	for _, r := range doyResults {
		dayInMonth := r.DayOfYear - startOfMonth.YearDay() + 1
		if dayInMonth >= 1 {
			m[dayInMonth] += r.Count
		}
	}

	// Fixed date in this month
	monthStart := time.Date(year, time.Month(month), 1, 0, 0, 0, 0, time.UTC)
	monthEnd := monthStart.AddDate(0, 1, -1)
	var fixedResults []result
	err = r.db.Model(&models.DailyContentSchedule{}).
		Select("EXTRACT(DAY FROM fixed_date)::int as day, COUNT(*) as count").
		Where("is_active = true AND deleted_at IS NULL").
		Where("schedule_mode = 'fixed_date' AND fixed_date BETWEEN ? AND ?", monthStart, monthEnd).
		Group("EXTRACT(DAY FROM fixed_date)").
		Find(&fixedResults).Error
	if err != nil {
		return nil, err
	}
	for _, r := range fixedResults {
		m[r.Day] += r.Count
	}

	return m, nil
}

// CountByType returns the count of schedules grouped by content_type.
func (r *DailyContentRepository) CountByType() (map[string]int64, error) {
	type result struct {
		ContentType string `gorm:"column:content_type"`
		Count       int64  `gorm:"column:count"`
	}
	var results []result
	err := r.db.Model(&models.DailyContentSchedule{}).
		Select("content_type, COUNT(*) as count").
		Where("deleted_at IS NULL").
		Group("content_type").
		Find(&results).Error
	if err != nil {
		return nil, err
	}
	m := make(map[string]int64)
	for _, r := range results {
		m[r.ContentType] = r.Count
	}
	return m, nil
}

// CountByMode returns the count of schedules grouped by schedule_mode.
func (r *DailyContentRepository) CountByMode() (map[string]int64, error) {
	type result struct {
		ScheduleMode string `gorm:"column:schedule_mode"`
		Count        int64  `gorm:"column:count"`
	}
	var results []result
	err := r.db.Model(&models.DailyContentSchedule{}).
		Select("schedule_mode, COUNT(*) as count").
		Where("deleted_at IS NULL").
		Group("schedule_mode").
		Find(&results).Error
	if err != nil {
		return nil, err
	}
	m := make(map[string]int64)
	for _, r := range results {
		m[r.ScheduleMode] = r.Count
	}
	return m, nil
}

// CountActive returns the count of active vs total schedules.
func (r *DailyContentRepository) CountActive() (total int64, active int64, err error) {
	err = r.db.Model(&models.DailyContentSchedule{}).Where("deleted_at IS NULL").Count(&total).Error
	if err != nil {
		return
	}
	err = r.db.Model(&models.DailyContentSchedule{}).Where("deleted_at IS NULL AND is_active = true").Count(&active).Error
	return
}

// HasContentForDays checks which days in a recurring_annual month already have scheduled content.
// Returns a set of days that already have schedules.
func (r *DailyContentRepository) HasContentForDays(month int, days []int) (map[int]bool, error) {
	type result struct {
		Day int `gorm:"column:recurring_day"`
	}
	var results []result
	err := r.db.Model(&models.DailyContentSchedule{}).
		Select("DISTINCT recurring_day").
		Where("deleted_at IS NULL AND is_active = true AND schedule_mode = 'recurring_annual' AND recurring_month = ? AND recurring_day IN ?", month, days).
		Find(&results).Error
	if err != nil {
		return nil, err
	}
	m := make(map[int]bool)
	for _, r := range results {
		m[r.Day] = true
	}
	return m, nil
}
