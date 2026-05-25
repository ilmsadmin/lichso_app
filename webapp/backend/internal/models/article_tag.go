package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ArticleTag represents a tag that can be applied to articles.
type ArticleTag struct {
	ID           uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name         string         `gorm:"type:varchar(255);not null" json:"name"`
	Slug         string         `gorm:"type:varchar(255);not null;uniqueIndex" json:"slug"`
	Description  string         `gorm:"type:text" json:"description,omitempty"`
	ArticleCount int            `gorm:"default:0" json:"article_count"`
	CreatedAt    time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt    time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt    gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	Articles []Article `gorm:"many2many:article_tag_relations;joinForeignKey:TagID;joinReferences:ArticleID" json:"articles,omitempty"`
}

func (ArticleTag) TableName() string {
	return "article_tags"
}
