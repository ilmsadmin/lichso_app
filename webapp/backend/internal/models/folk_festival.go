package models

import (
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
	"gorm.io/gorm"
)

// FestivalType constants
const (
	FestivalTypeFolk     = "folk_festival"
	FestivalTypeReligion = "religion"
	FestivalTypeNational = "national_holiday"
	FestivalTypeSeasonal = "seasonal"
	FestivalTypeOther    = "other"
)

// CalendarType constants
const (
	CalendarTypeLunar = "lunar"
	CalendarTypeSolar = "solar"
	CalendarTypeBoth  = "both"
)

// FolkFestival represents a traditional folk festival.
type FolkFestival struct {
	ID               uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name             string         `gorm:"type:varchar(500);not null" json:"name"`
	Slug             string         `gorm:"type:varchar(500);not null;uniqueIndex" json:"slug"`
	AlternateName    string         `gorm:"type:varchar(500)" json:"alternate_name,omitempty"`
	CalendarType     string         `gorm:"type:varchar(10);not null;default:'lunar'" json:"calendar_type"`
	LunarDay         *int           `gorm:"type:int" json:"lunar_day,omitempty"`
	LunarMonth       *int           `gorm:"type:int" json:"lunar_month,omitempty"`
	SolarDay         *int           `gorm:"type:int" json:"solar_day,omitempty"`
	SolarMonth       *int           `gorm:"type:int" json:"solar_month,omitempty"`
	DurationDays     int            `gorm:"default:1" json:"duration_days"`
	FestivalType     string         `gorm:"type:varchar(50);not null;default:'folk_festival'" json:"festival_type"`
	Region           string         `gorm:"type:varchar(255)" json:"region,omitempty"`
	Country          string         `gorm:"type:varchar(100);default:'Việt Nam'" json:"country"`
	ShortDescription string         `gorm:"type:text" json:"short_description,omitempty"`
	Traditions       pq.StringArray `gorm:"type:text[];default:'{}'" json:"traditions,omitempty"`
	ImageURL         string         `gorm:"type:varchar(500)" json:"image_url,omitempty"`
	GalleryURLs      pq.StringArray `gorm:"type:text[];default:'{}'" json:"gallery_urls,omitempty"`
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

func (FolkFestival) TableName() string {
	return "folk_festivals"
}
