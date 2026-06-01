package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Popup represents a floating draggable popup displayed in the mobile app.
type Popup struct {
	ID        uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Title     string         `gorm:"type:varchar(255);not null" json:"title"`
	ImageURL  string         `gorm:"type:varchar(500);not null" json:"image_url"`
	CtaType   string         `gorm:"type:varchar(20);default:'route'" json:"cta_type,omitempty"`
	CtaRoute  string         `gorm:"type:varchar(255)" json:"cta_route,omitempty"`
	Position  string         `gorm:"type:varchar(50);default:'center'" json:"position"`
	Platform  string         `gorm:"type:varchar(20);not null;default:'all'" json:"platform"`
	IsActive  bool           `gorm:"default:true" json:"is_active"`
	StartDate *time.Time     `gorm:"type:timestamptz" json:"start_date,omitempty"`
	EndDate   *time.Time     `gorm:"type:timestamptz" json:"end_date,omitempty"`
	CreatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`
}

func (Popup) TableName() string {
	return "popups"
}
