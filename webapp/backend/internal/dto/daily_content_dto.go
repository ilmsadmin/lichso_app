package dto

// ============================================
// Daily Content Schedule DTOs
// ============================================

// CreateDailyContentRequest represents a request to create a daily content schedule.
type CreateDailyContentRequest struct {
	ContentType     string `json:"content_type" validate:"required,oneof=quote event article famous_person folk_festival custom"`
	ContentID       string `json:"content_id" validate:"omitempty,uuid"`
	CustomTitle     string `json:"custom_title" validate:"omitempty,max=500"`
	CustomContent   string `json:"custom_content"`
	CustomImage     string `json:"custom_image" validate:"omitempty,max=500"`
	ScheduleMode    string `json:"schedule_mode" validate:"required,oneof=fixed_date recurring_annual day_of_year lunar_date"`
	FixedDate       string `json:"fixed_date"`
	DayOfYear       *int   `json:"day_of_year" validate:"omitempty,min=1,max=366"`
	RecurringMonth  *int   `json:"recurring_month" validate:"omitempty,min=1,max=12"`
	RecurringDay    *int   `json:"recurring_day" validate:"omitempty,min=1,max=31"`
	LunarMonth      *int   `json:"lunar_month" validate:"omitempty,min=1,max=12"`
	LunarDay        *int   `json:"lunar_day" validate:"omitempty,min=1,max=30"`
	YearFilter      *int   `json:"year_filter"`
	DisplayPriority int    `json:"display_priority" validate:"min=0"`
	DisplaySection  string `json:"display_section" validate:"omitempty,max=100"`
	StartDate       string `json:"start_date"`
	EndDate         string `json:"end_date"`
}

// UpdateDailyContentRequest represents a request to update a daily content schedule.
type UpdateDailyContentRequest struct {
	ContentType     *string `json:"content_type" validate:"omitempty,oneof=quote event article famous_person folk_festival custom"`
	ContentID       *string `json:"content_id" validate:"omitempty,uuid"`
	CustomTitle     *string `json:"custom_title" validate:"omitempty,max=500"`
	CustomContent   *string `json:"custom_content"`
	CustomImage     *string `json:"custom_image" validate:"omitempty,max=500"`
	ScheduleMode    *string `json:"schedule_mode" validate:"omitempty,oneof=fixed_date recurring_annual day_of_year lunar_date"`
	FixedDate       *string `json:"fixed_date"`
	DayOfYear       *int    `json:"day_of_year" validate:"omitempty,min=1,max=366"`
	RecurringMonth  *int    `json:"recurring_month" validate:"omitempty,min=1,max=12"`
	RecurringDay    *int    `json:"recurring_day" validate:"omitempty,min=1,max=31"`
	LunarMonth      *int    `json:"lunar_month" validate:"omitempty,min=1,max=12"`
	LunarDay        *int    `json:"lunar_day" validate:"omitempty,min=1,max=30"`
	YearFilter      *int    `json:"year_filter"`
	DisplayPriority *int    `json:"display_priority" validate:"omitempty,min=0"`
	DisplaySection  *string `json:"display_section" validate:"omitempty,max=100"`
	IsActive        *bool   `json:"is_active"`
	StartDate       *string `json:"start_date"`
	EndDate         *string `json:"end_date"`
}

// DailyContentResponse represents a daily content schedule in API responses.
type DailyContentResponse struct {
	ID              string      `json:"id"`
	ContentType     string      `json:"content_type"`
	ContentID       string      `json:"content_id,omitempty"`
	CustomTitle     string      `json:"custom_title,omitempty"`
	CustomContent   string      `json:"custom_content,omitempty"`
	CustomImage     string      `json:"custom_image,omitempty"`
	ScheduleMode    string      `json:"schedule_mode"`
	FixedDate       string      `json:"fixed_date,omitempty"`
	DayOfYear       *int        `json:"day_of_year,omitempty"`
	RecurringMonth  *int        `json:"recurring_month,omitempty"`
	RecurringDay    *int        `json:"recurring_day,omitempty"`
	LunarMonth      *int        `json:"lunar_month,omitempty"`
	LunarDay        *int        `json:"lunar_day,omitempty"`
	YearFilter      *int        `json:"year_filter,omitempty"`
	DisplayPriority int         `json:"display_priority"`
	DisplaySection  string      `json:"display_section"`
	IsActive        bool        `json:"is_active"`
	StartDate       string      `json:"start_date,omitempty"`
	EndDate         string      `json:"end_date,omitempty"`
	Content         interface{} `json:"content,omitempty"` // Resolved content object
	CreatedAt       string      `json:"created_at"`
	UpdatedAt       string      `json:"updated_at"`
}

// DayContentResponse represents all content for a specific day.
type DayContentResponse struct {
	Date      string                 `json:"date"`
	Quotes    []interface{}          `json:"quotes,omitempty"`
	Events    []interface{}          `json:"events,omitempty"`
	Articles  []ArticleListResponse  `json:"articles,omitempty"`
	People    []interface{}          `json:"famous_people,omitempty"`
	Festivals []interface{}          `json:"festivals,omitempty"`
	Custom    []DailyContentResponse `json:"custom,omitempty"`
}

// DayContentSummary represents lightweight content counts for a single day.
type DayContentSummary struct {
	Day       int `json:"day"`
	Quotes    int `json:"quotes"`
	Events    int `json:"events"`
	Articles  int `json:"articles"`
	People    int `json:"famous_people"`
	Festivals int `json:"festivals"`
	Custom    int `json:"custom"`
	Total     int `json:"total"`
}

// MonthContentSummaryResponse represents content counts for all days in a month.
type MonthContentSummaryResponse struct {
	Year  int                 `json:"year"`
	Month int                 `json:"month"`
	Days  []DayContentSummary `json:"days"`
}

// AutoFillRequest represents a request to auto-fill content for a date range.
type AutoFillRequest struct {
	StartDate    string   `json:"start_date" validate:"required"`
	EndDate      string   `json:"end_date" validate:"required"`
	ContentTypes []string `json:"content_types" validate:"required,min=1,dive,oneof=quote event famous_person folk_festival"`
	SkipExisting bool     `json:"skip_existing"`
}

// AutoFillResult represents the result of an auto-fill operation.
type AutoFillResult struct {
	TotalDays    int `json:"total_days"`
	FilledDays   int `json:"filled_days"`
	SkippedDays  int `json:"skipped_days"`
	ItemsCreated int `json:"items_created"`
}

// DailyContentStatsResponse represents coverage statistics for daily content.
type DailyContentStatsResponse struct {
	TotalSchedules  int64            `json:"total_schedules"`
	ActiveSchedules int64            `json:"active_schedules"`
	ByType          map[string]int64 `json:"by_type"`
	ByMode          map[string]int64 `json:"by_mode"`
	CoverageSummary CoverageSummary  `json:"coverage_summary"`
}

// CoverageSummary represents how many days in the year have content.
type CoverageSummary struct {
	Year         int     `json:"year"`
	TotalDays    int     `json:"total_days"`
	CoveredDays  int     `json:"covered_days"`
	EmptyDays    int     `json:"empty_days"`
	CoverageRate float64 `json:"coverage_rate"`
}
