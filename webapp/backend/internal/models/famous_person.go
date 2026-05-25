package models

import (
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
	"gorm.io/gorm"
)

// FamousPerson category constants
const (
	FamousPersonCategoryPolitics   = "chinh_tri"
	FamousPersonCategoryScience    = "khoa_hoc"
	FamousPersonCategoryArt        = "nghe_thuat"
	FamousPersonCategoryMusic      = "am_nhac"
	FamousPersonCategorySport      = "the_thao"
	FamousPersonCategoryLiterature = "van_hoc"
	FamousPersonCategoryHistory    = "lich_su"
	FamousPersonCategoryFilm       = "dien_anh"
	FamousPersonCategoryBusiness   = "kinh_doanh"
	FamousPersonCategoryOther      = "khac"
)

// FamousPerson represents a notable historical or contemporary figure.
type FamousPerson struct {
	ID           uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name         string         `gorm:"type:varchar(255);not null" json:"name"`
	OriginalName string         `gorm:"type:varchar(255)" json:"original_name,omitempty"`
	BirthDate    *time.Time     `gorm:"type:date" json:"birth_date,omitempty"`
	BirthDay     *int           `gorm:"type:int" json:"birth_day,omitempty"`
	BirthMonth   *int           `gorm:"type:int" json:"birth_month,omitempty"`
	BirthYear    *int           `gorm:"type:int" json:"birth_year,omitempty"`
	DeathDate    *time.Time     `gorm:"type:date" json:"death_date,omitempty"`
	Nationality  string         `gorm:"type:varchar(100)" json:"nationality,omitempty"`
	Occupation   string         `gorm:"type:varchar(500)" json:"occupation,omitempty"`
	Category     string         `gorm:"type:varchar(50);not null;default:'khac'" json:"category"`
	ShortBio     string         `gorm:"type:text" json:"short_bio,omitempty"`
	ImageURL     string         `gorm:"type:varchar(500)" json:"image_url,omitempty"`
	ArticleID    *uuid.UUID     `gorm:"type:uuid;index" json:"article_id,omitempty"`
	IsVietnamese bool           `gorm:"default:false" json:"is_vietnamese"`
	Tags         pq.StringArray `gorm:"type:text[];default:'{}'" json:"tags,omitempty"`
	IsActive     bool           `gorm:"default:true" json:"is_active"`
	CreatedAt    time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt    time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt    gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	Article *Article `gorm:"foreignKey:ArticleID" json:"article,omitempty"`
}

func (FamousPerson) TableName() string {
	return "famous_people"
}
