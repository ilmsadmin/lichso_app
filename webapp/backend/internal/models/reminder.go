package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ReminderType constants
const (
	ReminderTypeHoliday     = "holiday"
	ReminderTypeAnniversary = "anniversary"
	ReminderTypeBirthday    = "birthday"
	ReminderTypeGio         = "gio" // death anniversary
	ReminderTypeCustom      = "custom"
)

// Reminder represents a user's reminder for recurring dates.
type Reminder struct {
	ID               uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID           uuid.UUID      `gorm:"type:uuid;not null;index" json:"user_id"`
	Title            string         `gorm:"type:varchar(255);not null" json:"title"`
	Description      string         `gorm:"type:text;default:''" json:"description"`
	ReminderType     string         `gorm:"type:varchar(30);not null;default:'custom'" json:"reminder_type"`
	IsLunar          bool           `gorm:"default:false" json:"is_lunar"`
	SolarDay         *int           `gorm:"type:int" json:"solar_day,omitempty"`
	SolarMonth       *int           `gorm:"type:int" json:"solar_month,omitempty"`
	LunarDay         *int           `gorm:"type:int" json:"lunar_day,omitempty"`
	LunarMonth       *int           `gorm:"type:int" json:"lunar_month,omitempty"`
	IsRecurring      bool           `gorm:"default:true" json:"is_recurring"`
	RemindBeforeDays int            `gorm:"default:1" json:"remind_before_days"`
	NotifyEmail      bool           `gorm:"default:false" json:"notify_email"`
	NotifyPush       bool           `gorm:"default:true" json:"notify_push"`
	IsActive         bool           `gorm:"default:true" json:"is_active"`
	LastNotifiedAt   *time.Time     `gorm:"type:timestamptz" json:"last_notified_at,omitempty"`
	CreatedAt        time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt        time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt        gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Association
	User User `gorm:"foreignKey:UserID" json:"-"`
}

func (Reminder) TableName() string {
	return "reminders"
}
