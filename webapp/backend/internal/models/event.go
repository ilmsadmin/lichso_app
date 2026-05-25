package models

import (
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
	"gorm.io/gorm"
)

// EventType constants
const (
	EventTypeHistorical  = "historical_event"
	EventTypeNationalDay = "national_day"
	EventTypeWorldDay    = "world_day"
	EventTypeAnniversary = "anniversary"
	EventTypeCultural    = "cultural"
	EventTypeMilitary    = "military"
)

// EventImportance constants
const (
	EventImportanceLow    = "low"
	EventImportanceMedium = "medium"
	EventImportanceHigh   = "high"
)

// Event represents a historical event, national day, or international day.
type Event struct {
	ID               uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Title            string         `gorm:"type:varchar(500);not null" json:"title"`
	Slug             string         `gorm:"type:varchar(500);not null;uniqueIndex" json:"slug"`
	EventDate        *time.Time     `gorm:"type:date" json:"event_date,omitempty"`
	EventDay         int            `gorm:"type:int;not null" json:"event_day"`
	EventMonth       int            `gorm:"type:int;not null" json:"event_month"`
	EventYear        *int           `gorm:"type:int" json:"event_year,omitempty"`
	IsLunar          bool           `gorm:"default:false" json:"is_lunar"`
	IsRecurring      bool           `gorm:"default:true" json:"is_recurring"`
	EventType        string         `gorm:"type:varchar(50);not null;default:'historical_event'" json:"event_type"`
	Country          string         `gorm:"type:varchar(100)" json:"country,omitempty"`
	CountryCode      string         `gorm:"type:varchar(5)" json:"country_code,omitempty"`
	FlagEmoji        string         `gorm:"type:varchar(10)" json:"flag_emoji,omitempty"`
	ShortDescription string         `gorm:"type:text" json:"short_description,omitempty"`
	ImageURL         string         `gorm:"type:varchar(500)" json:"image_url,omitempty"`
	ArticleID        *uuid.UUID     `gorm:"type:uuid;index" json:"article_id,omitempty"`
	Importance       string         `gorm:"type:varchar(20);not null;default:'medium'" json:"importance"`
	Tags             pq.StringArray `gorm:"type:text[];default:'{}'" json:"tags,omitempty"`
	IsActive         bool           `gorm:"default:true" json:"is_active"`
	CreatedAt        time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt        time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt        gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	Article *Article `gorm:"foreignKey:ArticleID" json:"article,omitempty"`
}

func (Event) TableName() string {
	return "events"
}
