package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Bookmark represents a user's bookmarked date.
type Bookmark struct {
	ID          uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID      uuid.UUID      `gorm:"type:uuid;not null;index" json:"user_id"`
	SolarDate   time.Time      `gorm:"type:date;not null" json:"solar_date"`
	Title       string         `gorm:"type:varchar(255);not null;default:''" json:"title"`
	Note        string         `gorm:"type:text;default:''" json:"note"`
	Color       string         `gorm:"type:varchar(20);default:'amber'" json:"color"`
	IsRecurring bool           `gorm:"default:false" json:"is_recurring"`
	CreatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt   gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Association
	User User `gorm:"foreignKey:UserID" json:"-"`
}

func (Bookmark) TableName() string {
	return "bookmarks"
}
