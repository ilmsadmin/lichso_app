package dto

// ============================================
// Quote DTOs
// ============================================

// CreateQuoteRequest represents a request to create a quote.
type CreateQuoteRequest struct {
	Quote             string   `json:"quote" validate:"required"`
	OriginalQuote     string   `json:"original_quote"`
	OriginalLanguage  string   `json:"original_language" validate:"omitempty,max=10"`
	Author            string   `json:"author" validate:"required,max=255"`
	AuthorBio         string   `json:"author_bio"`
	AuthorBirthYear   *int     `json:"author_birth_year"`
	AuthorDeathYear   *int     `json:"author_death_year"`
	AuthorNationality string   `json:"author_nationality" validate:"omitempty,max=100"`
	AuthorImageURL    string   `json:"author_image_url" validate:"omitempty,max=500"`
	Tags              []string `json:"tags"`
	DayOfYear         *int     `json:"day_of_year" validate:"omitempty,min=1,max=366"`
}

// UpdateQuoteRequest represents a request to update a quote.
type UpdateQuoteRequest struct {
	Quote             *string  `json:"quote"`
	OriginalQuote     *string  `json:"original_quote"`
	OriginalLanguage  *string  `json:"original_language" validate:"omitempty,max=10"`
	Author            *string  `json:"author" validate:"omitempty,max=255"`
	AuthorBio         *string  `json:"author_bio"`
	AuthorBirthYear   *int     `json:"author_birth_year"`
	AuthorDeathYear   *int     `json:"author_death_year"`
	AuthorNationality *string  `json:"author_nationality" validate:"omitempty,max=100"`
	AuthorImageURL    *string  `json:"author_image_url" validate:"omitempty,max=500"`
	Tags              []string `json:"tags"`
	DayOfYear         *int     `json:"day_of_year" validate:"omitempty,min=1,max=366"`
	IsActive          *bool    `json:"is_active"`
}

// QuoteResponse represents a quote in API responses.
type QuoteResponse struct {
	ID                string   `json:"id"`
	Quote             string   `json:"quote"`
	OriginalQuote     string   `json:"original_quote,omitempty"`
	OriginalLanguage  string   `json:"original_language"`
	Author            string   `json:"author"`
	AuthorBio         string   `json:"author_bio,omitempty"`
	AuthorBirthYear   *int     `json:"author_birth_year,omitempty"`
	AuthorDeathYear   *int     `json:"author_death_year,omitempty"`
	AuthorNationality string   `json:"author_nationality,omitempty"`
	AuthorImageURL    string   `json:"author_image_url,omitempty"`
	Tags              []string `json:"tags,omitempty"`
	DayOfYear         *int     `json:"day_of_year,omitempty"`
	IsActive          bool     `json:"is_active"`
	CreatedAt         string   `json:"created_at"`
}
