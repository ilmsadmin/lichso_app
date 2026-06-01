package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Banner represents a promotional banner displayed in the mobile app.
type Banner struct {
	ID        uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Title     string         `gorm:"type:varchar(255);not null" json:"title"`
	Subtitle  string         `gorm:"type:text" json:"subtitle,omitempty"`
	ImageURL  string         `gorm:"type:varchar(500)" json:"image_url,omitempty"`
	IconURL   string         `gorm:"type:varchar(500)" json:"icon_url,omitempty"`
	IconKey   string         `gorm:"type:varchar(60)" json:"icon_key,omitempty"`
	CtaText   string         `gorm:"type:varchar(100)" json:"cta_text,omitempty"`
	CtaType   string         `gorm:"type:varchar(20);default:'route'" json:"cta_type,omitempty"`
	CtaRoute  string         `gorm:"type:varchar(255)" json:"cta_route,omitempty"`
	BgColor   string         `gorm:"type:varchar(20)" json:"bg_color,omitempty"`
	Locations []string       `gorm:"type:jsonb;serializer:json;default:'[\"home\"]'" json:"locations"`
	Platform  string         `gorm:"type:varchar(20);not null;default:'all'" json:"platform"`
	IsActive  bool           `gorm:"default:true" json:"is_active"`
	SortOrder int            `gorm:"default:0" json:"sort_order"`
	StartDate *time.Time     `gorm:"type:timestamptz" json:"start_date,omitempty"`
	EndDate   *time.Time     `gorm:"type:timestamptz" json:"end_date,omitempty"`
	CreatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`
}

func (Banner) TableName() string {
	return "banners"
}
