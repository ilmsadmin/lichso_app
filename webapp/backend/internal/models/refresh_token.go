package models

import (
	"time"

	"github.com/google/uuid"
)

// RefreshToken represents a refresh token stored in the database
type RefreshToken struct {
	ID        uuid.UUID  `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID    uuid.UUID  `gorm:"type:uuid;not null;index" json:"user_id"`
	Token     string     `gorm:"type:varchar(500);uniqueIndex;not null" json:"-"`
	UserAgent string     `gorm:"type:varchar(500);default:''" json:"user_agent"`
	IPAddress string     `gorm:"type:varchar(45);default:''" json:"ip_address"`
	ExpiresAt time.Time  `gorm:"type:timestamptz;not null" json:"expires_at"`
	CreatedAt time.Time  `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	RevokedAt *time.Time `gorm:"type:timestamptz" json:"revoked_at,omitempty"`

	// Associations
	User User `gorm:"foreignKey:UserID;references:ID" json:"user,omitempty"`
}

// TableName overrides the default table name
func (RefreshToken) TableName() string {
	return "refresh_tokens"
}

// IsExpired checks if the refresh token has expired
func (rt *RefreshToken) IsExpired() bool {
	return time.Now().After(rt.ExpiresAt)
}

// IsRevoked checks if the refresh token has been revoked
func (rt *RefreshToken) IsRevoked() bool {
	return rt.RevokedAt != nil
}

// IsValid checks if the refresh token is still valid (not expired and not revoked)
func (rt *RefreshToken) IsValid() bool {
	return !rt.IsExpired() && !rt.IsRevoked()
}
