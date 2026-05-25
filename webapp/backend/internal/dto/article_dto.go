package dto

// ============================================
// Article DTOs
// ============================================

// CreateArticleRequest represents a request to create an article.
type CreateArticleRequest struct {
	Title           string   `json:"title" validate:"required,max=500"`
	Slug            string   `json:"slug" validate:"omitempty,max=500"`
	Excerpt         string   `json:"excerpt" validate:"max=1000"`
	Content         string   `json:"content" validate:"required"`
	FeaturedImage   string   `json:"featured_image" validate:"omitempty,max=500"`
	CategoryID      string   `json:"category_id" validate:"omitempty,uuid"`
	Status          string   `json:"status" validate:"omitempty,oneof=draft review published archived"`
	MetaTitle       string   `json:"meta_title" validate:"omitempty,max=255"`
	MetaDescription string   `json:"meta_description" validate:"omitempty,max=500"`
	OGImage         string   `json:"og_image" validate:"omitempty,max=500"`
	IsFeatured      bool     `json:"is_featured"`
	TagIDs          []string `json:"tag_ids" validate:"omitempty,dive,uuid"`
}

// UpdateArticleRequest represents a request to update an article.
type UpdateArticleRequest struct {
	Title           *string  `json:"title" validate:"omitempty,max=500"`
	Slug            *string  `json:"slug" validate:"omitempty,max=500"`
	Excerpt         *string  `json:"excerpt" validate:"omitempty,max=1000"`
	Content         *string  `json:"content"`
	FeaturedImage   *string  `json:"featured_image" validate:"omitempty,max=500"`
	CategoryID      *string  `json:"category_id" validate:"omitempty,uuid"`
	Status          *string  `json:"status" validate:"omitempty,oneof=draft review published archived"`
	MetaTitle       *string  `json:"meta_title" validate:"omitempty,max=255"`
	MetaDescription *string  `json:"meta_description" validate:"omitempty,max=500"`
	OGImage         *string  `json:"og_image" validate:"omitempty,max=500"`
	IsFeatured      *bool    `json:"is_featured"`
	IsActive        *bool    `json:"is_active"`
	TagIDs          []string `json:"tag_ids" validate:"omitempty,dive,uuid"`
}

// ArticleResponse represents an article in API responses.
type ArticleResponse struct {
	ID              string                   `json:"id"`
	Title           string                   `json:"title"`
	Slug            string                   `json:"slug"`
	Excerpt         string                   `json:"excerpt,omitempty"`
	Content         string                   `json:"content"`
	FeaturedImage   string                   `json:"featured_image,omitempty"`
	CategoryID      string                   `json:"category_id,omitempty"`
	Category        *ArticleCategoryResponse `json:"category,omitempty"`
	AuthorID        string                   `json:"author_id,omitempty"`
	Status          string                   `json:"status"`
	PublishedAt     string                   `json:"published_at,omitempty"`
	MetaTitle       string                   `json:"meta_title,omitempty"`
	MetaDescription string                   `json:"meta_description,omitempty"`
	OGImage         string                   `json:"og_image,omitempty"`
	ViewCount       int                      `json:"view_count"`
	ReadingTime     int                      `json:"reading_time"`
	IsFeatured      bool                     `json:"is_featured"`
	IsActive        bool                     `json:"is_active"`
	Tags            []ArticleTagResponse     `json:"tags,omitempty"`
	CreatedAt       string                   `json:"created_at"`
	UpdatedAt       string                   `json:"updated_at"`
}

// ArticleListResponse represents an article summary in list responses.
type ArticleListResponse struct {
	ID            string                   `json:"id"`
	Title         string                   `json:"title"`
	Slug          string                   `json:"slug"`
	Excerpt       string                   `json:"excerpt,omitempty"`
	FeaturedImage string                   `json:"featured_image,omitempty"`
	Category      *ArticleCategoryResponse `json:"category,omitempty"`
	Status        string                   `json:"status"`
	PublishedAt   string                   `json:"published_at,omitempty"`
	ViewCount     int                      `json:"view_count"`
	ReadingTime   int                      `json:"reading_time"`
	IsFeatured    bool                     `json:"is_featured"`
	Tags          []ArticleTagResponse     `json:"tags,omitempty"`
	CreatedAt     string                   `json:"created_at"`
}

// ============================================
// Article Category DTOs
// ============================================

// CreateArticleCategoryRequest represents a request to create an article category.
type CreateArticleCategoryRequest struct {
	Name        string `json:"name" validate:"required,max=255"`
	Slug        string `json:"slug" validate:"omitempty,max=255"`
	Description string `json:"description" validate:"max=1000"`
	ParentID    string `json:"parent_id" validate:"omitempty,uuid"`
	Icon        string `json:"icon" validate:"omitempty,max=100"`
	SortOrder   int    `json:"sort_order" validate:"min=0"`
}

// UpdateArticleCategoryRequest represents a request to update an article category.
type UpdateArticleCategoryRequest struct {
	Name        *string `json:"name" validate:"omitempty,max=255"`
	Slug        *string `json:"slug" validate:"omitempty,max=255"`
	Description *string `json:"description" validate:"omitempty,max=1000"`
	ParentID    *string `json:"parent_id" validate:"omitempty,uuid"`
	Icon        *string `json:"icon" validate:"omitempty,max=100"`
	SortOrder   *int    `json:"sort_order" validate:"omitempty,min=0"`
	IsActive    *bool   `json:"is_active"`
}

// ArticleCategoryResponse represents an article category in API responses.
type ArticleCategoryResponse struct {
	ID          string                    `json:"id"`
	Name        string                    `json:"name"`
	Slug        string                    `json:"slug"`
	Description string                    `json:"description,omitempty"`
	ParentID    string                    `json:"parent_id,omitempty"`
	Icon        string                    `json:"icon,omitempty"`
	SortOrder   int                       `json:"sort_order"`
	IsActive    bool                      `json:"is_active"`
	Children    []ArticleCategoryResponse `json:"children,omitempty"`
	CreatedAt   string                    `json:"created_at"`
}

// ============================================
// Article Tag DTOs
// ============================================

// CreateArticleTagRequest represents a request to create an article tag.
type CreateArticleTagRequest struct {
	Name        string `json:"name" validate:"required,max=255"`
	Slug        string `json:"slug" validate:"omitempty,max=255"`
	Description string `json:"description" validate:"max=1000"`
}

// UpdateArticleTagRequest represents a request to update an article tag.
type UpdateArticleTagRequest struct {
	Name        *string `json:"name" validate:"omitempty,max=255"`
	Slug        *string `json:"slug" validate:"omitempty,max=255"`
	Description *string `json:"description" validate:"omitempty,max=1000"`
}

// ArticleTagResponse represents an article tag in API responses.
type ArticleTagResponse struct {
	ID           string `json:"id"`
	Name         string `json:"name"`
	Slug         string `json:"slug"`
	Description  string `json:"description,omitempty"`
	ArticleCount int    `json:"article_count"`
	CreatedAt    string `json:"created_at"`
}
