package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// UserNote represents a personal note attached to a specific date.
type UserNote struct {
	ID        uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID    uuid.UUID      `gorm:"type:uuid;not null;index" json:"user_id"`
	NoteDate  time.Time      `gorm:"type:date;not null" json:"note_date"`
	Title     string         `gorm:"type:varchar(255);not null" json:"title"`
	Content   string         `gorm:"type:text" json:"content,omitempty"`
	Color     string         `gorm:"type:varchar(20);default:'#3b82f6'" json:"color"`
	IsPinned  bool           `gorm:"default:false" json:"is_pinned"`
	CreatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	User *User `gorm:"foreignKey:UserID" json:"user,omitempty"`
}

func (UserNote) TableName() string {
	return "user_notes"
}

// UserCountdown represents a countdown timer to an important date.
type UserCountdown struct {
	ID               uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID           uuid.UUID      `gorm:"type:uuid;not null;index" json:"user_id"`
	Title            string         `gorm:"type:varchar(255);not null" json:"title"`
	Description      string         `gorm:"type:text" json:"description,omitempty"`
	TargetDate       time.Time      `gorm:"type:date;not null" json:"target_date"`
	TargetTime       *string        `gorm:"type:time" json:"target_time,omitempty"`
	Color            string         `gorm:"type:varchar(20);default:'#ef4444'" json:"color"`
	Icon             string         `gorm:"type:varchar(50);default:'🎯'" json:"icon"`
	IsRecurring      bool           `gorm:"default:false" json:"is_recurring"`
	RecurringType    string         `gorm:"type:varchar(20)" json:"recurring_type,omitempty"`
	NotifyBeforeDays int            `gorm:"default:1" json:"notify_before_days"`
	IsActive         bool           `gorm:"default:true" json:"is_active"`
	CreatedAt        time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt        time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt        gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	User *User `gorm:"foreignKey:UserID" json:"user,omitempty"`
}

func (UserCountdown) TableName() string {
	return "user_countdowns"
}
