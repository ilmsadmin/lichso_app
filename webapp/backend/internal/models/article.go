package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ArticleStatus constants
const (
	ArticleStatusDraft     = "draft"
	ArticleStatusAIPending = "ai_pending" // created by AI quick-draft, waiting for refinement
	ArticleStatusReview    = "review"
	ArticleStatusPublished = "published"
	ArticleStatusArchived  = "archived"
)

// Article represents a blog post or detailed content piece.
type Article struct {
	ID              uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Title           string         `gorm:"type:varchar(500);not null" json:"title"`
	Slug            string         `gorm:"type:varchar(500);not null;uniqueIndex" json:"slug"`
	Excerpt         string         `gorm:"type:text" json:"excerpt,omitempty"`
	Content         string         `gorm:"type:text;not null" json:"content"`
	FeaturedImage   string         `gorm:"type:varchar(500)" json:"featured_image,omitempty"`
	CategoryID      *uuid.UUID     `gorm:"type:uuid;index" json:"category_id,omitempty"`
	AuthorID        *uuid.UUID     `gorm:"type:uuid;index" json:"author_id,omitempty"`
	Status          string         `gorm:"type:article_status;not null;default:'draft'" json:"status"`
	PublishedAt     *time.Time     `gorm:"type:timestamptz" json:"published_at,omitempty"`
	ScheduledAt     *time.Time     `gorm:"type:timestamptz" json:"scheduled_at,omitempty"`
	MetaTitle       string         `gorm:"type:varchar(255)" json:"meta_title,omitempty"`
	MetaDescription string         `gorm:"type:varchar(500)" json:"meta_description,omitempty"`
	OGImage         string         `gorm:"type:varchar(500)" json:"og_image,omitempty"`
	ViewCount       int            `gorm:"default:0" json:"view_count"`
	ReadingTime     int            `gorm:"default:0" json:"reading_time"`
	IsFeatured      bool           `gorm:"default:false" json:"is_featured"`
	IsActive        bool           `gorm:"default:true" json:"is_active"`
	CreatedAt       time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt       time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt       gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	Category *ArticleCategory `gorm:"foreignKey:CategoryID" json:"category,omitempty"`
	Author   *User            `gorm:"foreignKey:AuthorID" json:"author,omitempty"`
	Tags     []ArticleTag     `gorm:"many2many:article_tag_relations;joinForeignKey:ArticleID;joinReferences:TagID" json:"tags,omitempty"`
}

func (Article) TableName() string {
	return "articles"
}

// ArticleTagRelation represents the many-to-many relation between articles and tags.
type ArticleTagRelation struct {
	ArticleID uuid.UUID `gorm:"type:uuid;primaryKey" json:"article_id"`
	TagID     uuid.UUID `gorm:"type:uuid;primaryKey" json:"tag_id"`
}

func (ArticleTagRelation) TableName() string {
	return "article_tag_relations"
}
