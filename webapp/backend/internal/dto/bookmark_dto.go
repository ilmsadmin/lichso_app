package dto

// ============================================
// Bookmark DTOs
// ============================================

// CreateBookmarkRequest represents a request to create a bookmark.
type CreateBookmarkRequest struct {
	SolarDate   string `json:"solar_date" validate:"required"` // YYYY-MM-DD
	Title       string `json:"title" validate:"required,max=255"`
	Note        string `json:"note" validate:"max=1000"`
	Color       string `json:"color" validate:"omitempty,oneof=amber jade red gold blue purple"`
	IsRecurring bool   `json:"is_recurring"`
}

// UpdateBookmarkRequest represents a request to update a bookmark.
type UpdateBookmarkRequest struct {
	Title       *string `json:"title" validate:"omitempty,max=255"`
	Note        *string `json:"note" validate:"omitempty,max=1000"`
	Color       *string `json:"color" validate:"omitempty,oneof=amber jade red gold blue purple"`
	IsRecurring *bool   `json:"is_recurring"`
}

// BookmarkResponse represents a bookmark in API responses.
type BookmarkResponse struct {
	ID          string `json:"id"`
	SolarDate   string `json:"solar_date"`
	Title       string `json:"title"`
	Note        string `json:"note"`
	Color       string `json:"color"`
	IsRecurring bool   `json:"is_recurring"`
	CreatedAt   string `json:"created_at"`
}

// ============================================
// Reminder DTOs
// ============================================

// CreateReminderRequest represents a request to create a reminder.
type CreateReminderRequest struct {
	Title            string `json:"title" validate:"required,max=255"`
	Description      string `json:"description" validate:"max=1000"`
	ReminderType     string `json:"reminder_type" validate:"required,oneof=holiday anniversary birthday gio custom"`
	IsLunar          bool   `json:"is_lunar"`
	SolarDay         *int   `json:"solar_day" validate:"omitempty,min=1,max=31"`
	SolarMonth       *int   `json:"solar_month" validate:"omitempty,min=1,max=12"`
	LunarDay         *int   `json:"lunar_day" validate:"omitempty,min=1,max=30"`
	LunarMonth       *int   `json:"lunar_month" validate:"omitempty,min=1,max=12"`
	IsRecurring      bool   `json:"is_recurring"`
	RemindBeforeDays int    `json:"remind_before_days" validate:"min=0,max=30"`
	NotifyEmail      bool   `json:"notify_email"`
	NotifyPush       bool   `json:"notify_push"`
}

// UpdateReminderRequest represents a request to update a reminder.
type UpdateReminderRequest struct {
	Title            *string `json:"title" validate:"omitempty,max=255"`
	Description      *string `json:"description" validate:"omitempty,max=1000"`
	ReminderType     *string `json:"reminder_type" validate:"omitempty,oneof=holiday anniversary birthday gio custom"`
	IsLunar          *bool   `json:"is_lunar"`
	SolarDay         *int    `json:"solar_day" validate:"omitempty,min=1,max=31"`
	SolarMonth       *int    `json:"solar_month" validate:"omitempty,min=1,max=12"`
	LunarDay         *int    `json:"lunar_day" validate:"omitempty,min=1,max=30"`
	LunarMonth       *int    `json:"lunar_month" validate:"omitempty,min=1,max=12"`
	IsRecurring      *bool   `json:"is_recurring"`
	RemindBeforeDays *int    `json:"remind_before_days" validate:"omitempty,min=0,max=30"`
	NotifyEmail      *bool   `json:"notify_email"`
	NotifyPush       *bool   `json:"notify_push"`
	IsActive         *bool   `json:"is_active"`
}

// ReminderResponse represents a reminder in API responses.
type ReminderResponse struct {
	ID               string `json:"id"`
	Title            string `json:"title"`
	Description      string `json:"description"`
	ReminderType     string `json:"reminder_type"`
	IsLunar          bool   `json:"is_lunar"`
	SolarDay         *int   `json:"solar_day,omitempty"`
	SolarMonth       *int   `json:"solar_month,omitempty"`
	LunarDay         *int   `json:"lunar_day,omitempty"`
	LunarMonth       *int   `json:"lunar_month,omitempty"`
	IsRecurring      bool   `json:"is_recurring"`
	RemindBeforeDays int    `json:"remind_before_days"`
	NotifyEmail      bool   `json:"notify_email"`
	NotifyPush       bool   `json:"notify_push"`
	IsActive         bool   `json:"is_active"`
	CreatedAt        string `json:"created_at"`
}

// ============================================
// Export DTOs
// ============================================

// ExportCalendarRequest for exporting calendar data.
type ExportCalendarRequest struct {
	Year   int    `query:"year" validate:"required,min=1900,max=2100"`
	Month  int    `query:"month" validate:"required,min=1,max=12"`
	Format string `query:"format" validate:"required,oneof=ical pdf"`
}
