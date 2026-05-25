package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// DailyContentType constants
const (
	ContentTypeQuote        = "quote"
	ContentTypeEvent        = "event"
	ContentTypeArticle      = "article"
	ContentTypeFamousPerson = "famous_person"
	ContentTypeFolkFestival = "folk_festival"
	ContentTypeCustom       = "custom"
)

// ScheduleMode constants
const (
	ScheduleModeFixedDate       = "fixed_date"
	ScheduleModeRecurringAnnual = "recurring_annual"
	ScheduleModeDayOfYear       = "day_of_year"
	ScheduleModeLunarDate       = "lunar_date"
)

// DailyContentSchedule represents admin-managed content assigned to specific dates.
type DailyContentSchedule struct {
	ID uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`

	// Content reference
	ContentType   string     `gorm:"type:daily_content_type;not null" json:"content_type"`
	ContentID     *uuid.UUID `gorm:"type:uuid" json:"content_id,omitempty"`
	CustomTitle   string     `gorm:"type:varchar(500)" json:"custom_title,omitempty"`
	CustomContent string     `gorm:"type:text" json:"custom_content,omitempty"`
	CustomImage   string     `gorm:"type:varchar(500)" json:"custom_image,omitempty"`

	// Scheduling
	ScheduleMode   string     `gorm:"type:schedule_mode;not null;default:'fixed_date'" json:"schedule_mode"`
	FixedDate      *time.Time `gorm:"type:date" json:"fixed_date,omitempty"`
	DayOfYear      *int       `gorm:"type:int" json:"day_of_year,omitempty"`
	RecurringMonth *int       `gorm:"type:int" json:"recurring_month,omitempty"`
	RecurringDay   *int       `gorm:"type:int" json:"recurring_day,omitempty"`
	LunarMonth     *int       `gorm:"type:int" json:"lunar_month,omitempty"`
	LunarDay       *int       `gorm:"type:int" json:"lunar_day,omitempty"`
	YearFilter     *int       `gorm:"type:int" json:"year_filter,omitempty"`

	// Display settings
	DisplayPriority int    `gorm:"default:0" json:"display_priority"`
	DisplaySection  string `gorm:"type:varchar(100);not null;default:'main'" json:"display_section"`

	// Metadata
	IsActive  bool       `gorm:"default:true" json:"is_active"`
	StartDate *time.Time `gorm:"type:date" json:"start_date,omitempty"`
	EndDate   *time.Time `gorm:"type:date" json:"end_date,omitempty"`

	// Admin tracking
	CreatedBy *uuid.UUID     `gorm:"type:uuid" json:"created_by,omitempty"`
	UpdatedBy *uuid.UUID     `gorm:"type:uuid" json:"updated_by,omitempty"`
	CreatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations (for preloading)
	Creator *User `gorm:"foreignKey:CreatedBy" json:"creator,omitempty"`
	Updater *User `gorm:"foreignKey:UpdatedBy" json:"updater,omitempty"`
}

func (DailyContentSchedule) TableName() string {
	return "daily_content_schedules"
}
