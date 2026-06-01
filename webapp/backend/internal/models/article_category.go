package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ArticleCategory represents a category for articles.
type ArticleCategory struct {
	ID          uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name        string         `gorm:"type:varchar(255);not null" json:"name"`
	Slug        string         `gorm:"type:varchar(255);not null;uniqueIndex" json:"slug"`
	Description string         `gorm:"type:text" json:"description,omitempty"`
	ParentID    *uuid.UUID     `gorm:"type:uuid;index" json:"parent_id,omitempty"`
	Icon        string         `gorm:"type:varchar(100)" json:"icon,omitempty"`
	ImageURL    string         `gorm:"type:varchar(500)" json:"image_url,omitempty"`
	SortOrder   int            `gorm:"default:0" json:"sort_order"`
	IsActive    bool           `gorm:"default:true" json:"is_active"`
	CreatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt   gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	Parent   *ArticleCategory  `gorm:"foreignKey:ParentID" json:"parent,omitempty"`
	Children []ArticleCategory `gorm:"foreignKey:ParentID" json:"children,omitempty"`
	Articles []Article         `gorm:"foreignKey:CategoryID" json:"articles,omitempty"`
}

func (ArticleCategory) TableName() string {
	return "article_categories"
}
