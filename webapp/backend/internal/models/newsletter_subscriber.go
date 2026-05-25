package models

import (
	"database/sql/driver"
	"encoding/json"
	"time"

	"github.com/google/uuid"
)

// JSONB is a custom type for PostgreSQL JSONB columns
type JSONB map[string]interface{}

// Value implements the driver.Valuer interface
func (j JSONB) Value() (driver.Value, error) {
	if j == nil {
		return "{}", nil
	}
	return json.Marshal(j)
}

// Scan implements the sql.Scanner interface
func (j *JSONB) Scan(value interface{}) error {
	if value == nil {
		*j = JSONB{}
		return nil
	}
	bytes, ok := value.([]byte)
	if !ok {
		return nil
	}
	return json.Unmarshal(bytes, j)
}

// NewsletterSubscriber represents a newsletter email subscriber
type NewsletterSubscriber struct {
	ID             uuid.UUID  `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	Email          string     `gorm:"type:varchar(255);uniqueIndex;not null" json:"email"`
	Name           string     `gorm:"type:varchar(200)" json:"name"`
	UserID         *uuid.UUID `gorm:"type:uuid;index" json:"user_id,omitempty"`
	Frequency      string     `gorm:"type:varchar(20);not null;default:'daily'" json:"frequency"` // daily, weekly, monthly
	Preferences    JSONB      `gorm:"type:jsonb;not null;default:'{}'" json:"preferences"`
	IsActive       bool       `gorm:"not null;default:true" json:"is_active"`
	ConfirmedAt    *time.Time `json:"confirmed_at,omitempty"`
	UnsubscribedAt *time.Time `json:"unsubscribed_at,omitempty"`
	LastSentAt     *time.Time `json:"last_sent_at,omitempty"`
	CreatedAt      time.Time  `gorm:"not null;default:now()" json:"created_at"`
	UpdatedAt      time.Time  `gorm:"not null;default:now()" json:"updated_at"`
}

// TableName returns the table name
func (NewsletterSubscriber) TableName() string {
	return "newsletter_subscribers"
}

// NewsletterPreferences represents user preferences for email digest
type NewsletterPreferences struct {
	IncludeHoroscope bool   `json:"include_horoscope"`
	Zodiac           string `json:"zodiac,omitempty"` // e.g. "ty", "suu"
	IncludeEvents    bool   `json:"include_events"`
	IncludeQuotes    bool   `json:"include_quotes"`
	IncludeArticles  bool   `json:"include_articles"`
	IncludeGoodDays  bool   `json:"include_good_days"`
}
