package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ScreenBackground represents a background image setting for a specific app screen.
type ScreenBackground struct {
	ID         uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	ScreenKey  string         `gorm:"type:varchar(50);not null;uniqueIndex" json:"screen_key"`
	ScreenName string         `gorm:"type:varchar(100);not null" json:"screen_name"`
	ImageURL   string         `gorm:"type:varchar(500)" json:"image_url,omitempty"`
	ImagePath  string         `gorm:"type:varchar(500)" json:"-"`
	IsActive   bool           `gorm:"default:true" json:"is_active"`
	UpdatedBy  string         `gorm:"type:varchar(255)" json:"updated_by,omitempty"`
	CreatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt  gorm.DeletedAt `gorm:"type:timestamptz;index" json:"-"`
}

func (ScreenBackground) TableName() string {
	return "screen_backgrounds"
}

// ScreenBackgroundResponse is the public API response for a screen background.
type ScreenBackgroundResponse struct {
	ID         string    `json:"id"`
	ScreenKey  string    `json:"screen_key"`
	ScreenName string    `json:"screen_name"`
	ImageURL   string    `json:"image_url,omitempty"`
	IsActive   bool      `json:"is_active"`
	UpdatedBy  string    `json:"updated_by,omitempty"`
	UpdatedAt  time.Time `json:"updated_at"`
}

// ToResponse converts ScreenBackground to ScreenBackgroundResponse.
func (s *ScreenBackground) ToResponse() ScreenBackgroundResponse {
	return ScreenBackgroundResponse{
		ID:         s.ID.String(),
		ScreenKey:  s.ScreenKey,
		ScreenName: s.ScreenName,
		ImageURL:   s.ImageURL,
		IsActive:   s.IsActive,
		UpdatedBy:  s.UpdatedBy,
		UpdatedAt:  s.UpdatedAt,
	}
}
