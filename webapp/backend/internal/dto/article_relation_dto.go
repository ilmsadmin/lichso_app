package dto

// ============================================
// Article Relation DTOs
// ============================================

// CreateArticleRelationRequest represents a request to create an article relation.
type CreateArticleRelationRequest struct {
	TargetArticleID string `json:"target_article_id" validate:"required,uuid"`
	RelationType    string `json:"relation_type" validate:"required,oneof=related series reference translation"`
	SortOrder       int    `json:"sort_order" validate:"min=0"`
	IsBidirectional bool   `json:"is_bidirectional"`
}

// BatchCreateArticleRelationsRequest represents a batch request.
type BatchCreateArticleRelationsRequest struct {
	Relations []CreateArticleRelationRequest `json:"relations" validate:"required,min=1,dive"`
}

// UpdateArticleRelationRequest represents a request to update an article relation.
type UpdateArticleRelationRequest struct {
	RelationType    *string `json:"relation_type" validate:"omitempty,oneof=related series reference translation"`
	SortOrder       *int    `json:"sort_order" validate:"omitempty,min=0"`
	IsBidirectional *bool   `json:"is_bidirectional"`
}

// ArticleRelationResponse represents an article relation in API responses.
type ArticleRelationResponse struct {
	ID              string               `json:"id"`
	SourceArticleID string               `json:"source_article_id"`
	TargetArticleID string               `json:"target_article_id"`
	RelationType    string               `json:"relation_type"`
	SortOrder       int                  `json:"sort_order"`
	IsBidirectional bool                 `json:"is_bidirectional"`
	TargetArticle   *ArticleListResponse `json:"target_article,omitempty"`
	CreatedAt       string               `json:"created_at"`
}

// RelatedArticlesResponse wraps the list of related articles by type.
type RelatedArticlesResponse struct {
	Related     []ArticleListResponse `json:"related,omitempty"`
	Series      []ArticleListResponse `json:"series,omitempty"`
	References  []ArticleListResponse `json:"references,omitempty"`
	RandomPicks []ArticleListResponse `json:"random_picks,omitempty"`
}
