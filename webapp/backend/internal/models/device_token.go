package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

const (
	PlatformAndroid = "android"
	PlatformIOS     = "ios"
	PlatformWeb     = "web"
)

// DeviceToken stores FCM push tokens per user device.
type DeviceToken struct {
	ID         uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID     *uuid.UUID     `gorm:"type:uuid;index" json:"user_id,omitempty"`
	Token      string         `gorm:"type:varchar(512);uniqueIndex;not null" json:"token"`
	Platform   string         `gorm:"type:varchar(20);not null;default:'android'" json:"platform"`
	AppVersion string         `gorm:"type:varchar(50);default:''" json:"app_version"`
	DeviceID   string         `gorm:"type:varchar(255);default:''" json:"device_id"`
	IsActive   bool           `gorm:"not null;default:true" json:"is_active"`
	LastSeen   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"last_seen"`
	CreatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt  gorm.DeletedAt `gorm:"type:timestamptz;index" json:"-"`
}

func (DeviceToken) TableName() string {
	return "device_tokens"
}
