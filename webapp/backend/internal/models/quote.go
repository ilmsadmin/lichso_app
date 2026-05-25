package models

import (
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
	"gorm.io/gorm"
)

// Quote represents a famous quote displayed daily.
type Quote struct {
	ID                uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Quote             string         `gorm:"type:text;not null" json:"quote"`
	OriginalQuote     string         `gorm:"type:text" json:"original_quote,omitempty"`
	OriginalLanguage  string         `gorm:"type:varchar(10);default:'vi'" json:"original_language"`
	Author            string         `gorm:"type:varchar(255);not null" json:"author"`
	AuthorBio         string         `gorm:"type:text" json:"author_bio,omitempty"`
	AuthorBirthYear   *int           `gorm:"type:int" json:"author_birth_year,omitempty"`
	AuthorDeathYear   *int           `gorm:"type:int" json:"author_death_year,omitempty"`
	AuthorNationality string         `gorm:"type:varchar(100)" json:"author_nationality,omitempty"`
	AuthorImageURL    string         `gorm:"type:varchar(500)" json:"author_image_url,omitempty"`
	Tags              pq.StringArray `gorm:"type:text[];default:'{}'" json:"tags,omitempty"`
	DayOfYear         *int           `gorm:"type:int" json:"day_of_year,omitempty"`
	IsActive          bool           `gorm:"default:true" json:"is_active"`
	CreatedAt         time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt         time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt         gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`
}

func (Quote) TableName() string {
	return "quotes"
}
