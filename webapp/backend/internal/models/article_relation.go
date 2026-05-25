package models

import (
	"time"

	"github.com/google/uuid"
)

// ArticleRelationType constants
const (
	RelationTypeRelated     = "related"
	RelationTypeSeries      = "series"
	RelationTypeReference   = "reference"
	RelationTypeTranslation = "translation"
)

// ArticleRelation represents a relationship between two articles.
type ArticleRelation struct {
	ID              uuid.UUID  `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	SourceArticleID uuid.UUID  `gorm:"type:uuid;not null;index" json:"source_article_id"`
	TargetArticleID uuid.UUID  `gorm:"type:uuid;not null;index" json:"target_article_id"`
	RelationType    string     `gorm:"type:article_relation_type;not null;default:'related'" json:"relation_type"`
	SortOrder       int        `gorm:"default:0" json:"sort_order"`
	IsBidirectional bool       `gorm:"default:true" json:"is_bidirectional"`
	CreatedBy       *uuid.UUID `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt       time.Time  `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`

	// Associations
	SourceArticle *Article `gorm:"foreignKey:SourceArticleID" json:"source_article,omitempty"`
	TargetArticle *Article `gorm:"foreignKey:TargetArticleID" json:"target_article,omitempty"`
	Creator       *User    `gorm:"foreignKey:CreatedBy" json:"creator,omitempty"`
}

func (ArticleRelation) TableName() string {
	return "article_relations"
}
