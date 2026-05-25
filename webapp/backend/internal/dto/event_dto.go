package dto

// ============================================
// Event DTOs
// ============================================

// CreateEventRequest represents a request to create an event.
type CreateEventRequest struct {
	Title            string   `json:"title" validate:"required,max=500"`
	Slug             string   `json:"slug" validate:"omitempty,max=500"`
	EventDate        string   `json:"event_date"` // YYYY-MM-DD
	EventDay         int      `json:"event_day" validate:"required,min=1,max=31"`
	EventMonth       int      `json:"event_month" validate:"required,min=1,max=12"`
	EventYear        *int     `json:"event_year"`
	IsLunar          bool     `json:"is_lunar"`
	IsRecurring      bool     `json:"is_recurring"`
	EventType        string   `json:"event_type" validate:"required,oneof=historical_event national_day world_day anniversary cultural military"`
	Country          string   `json:"country" validate:"omitempty,max=100"`
	CountryCode      string   `json:"country_code" validate:"omitempty,max=5"`
	FlagEmoji        string   `json:"flag_emoji" validate:"omitempty,max=10"`
	ShortDescription string   `json:"short_description"`
	ImageURL         string   `json:"image_url" validate:"omitempty,max=500"`
	ArticleID        string   `json:"article_id" validate:"omitempty,uuid"`
	Importance       string   `json:"importance" validate:"omitempty,oneof=low medium high"`
	Tags             []string `json:"tags"`
}

// UpdateEventRequest represents a request to update an event.
type UpdateEventRequest struct {
	Title            *string  `json:"title" validate:"omitempty,max=500"`
	Slug             *string  `json:"slug" validate:"omitempty,max=500"`
	EventDate        *string  `json:"event_date"`
	EventDay         *int     `json:"event_day" validate:"omitempty,min=1,max=31"`
	EventMonth       *int     `json:"event_month" validate:"omitempty,min=1,max=12"`
	EventYear        *int     `json:"event_year"`
	IsLunar          *bool    `json:"is_lunar"`
	IsRecurring      *bool    `json:"is_recurring"`
	EventType        *string  `json:"event_type" validate:"omitempty,oneof=historical_event national_day world_day anniversary cultural military"`
	Country          *string  `json:"country" validate:"omitempty,max=100"`
	CountryCode      *string  `json:"country_code" validate:"omitempty,max=5"`
	FlagEmoji        *string  `json:"flag_emoji" validate:"omitempty,max=10"`
	ShortDescription *string  `json:"short_description"`
	ImageURL         *string  `json:"image_url" validate:"omitempty,max=500"`
	ArticleID        *string  `json:"article_id" validate:"omitempty,uuid"`
	Importance       *string  `json:"importance" validate:"omitempty,oneof=low medium high"`
	Tags             []string `json:"tags"`
	IsActive         *bool    `json:"is_active"`
}

// EventResponse represents an event in API responses.
type EventResponse struct {
	ID               string   `json:"id"`
	Title            string   `json:"title"`
	Slug             string   `json:"slug"`
	EventDate        string   `json:"event_date,omitempty"`
	EventDay         int      `json:"event_day"`
	EventMonth       int      `json:"event_month"`
	EventYear        *int     `json:"event_year,omitempty"`
	IsLunar          bool     `json:"is_lunar"`
	IsRecurring      bool     `json:"is_recurring"`
	EventType        string   `json:"event_type"`
	Country          string   `json:"country,omitempty"`
	CountryCode      string   `json:"country_code,omitempty"`
	FlagEmoji        string   `json:"flag_emoji,omitempty"`
	ShortDescription string   `json:"short_description,omitempty"`
	ImageURL         string   `json:"image_url,omitempty"`
	ArticleID        string   `json:"article_id,omitempty"`
	Importance       string   `json:"importance"`
	Tags             []string `json:"tags,omitempty"`
	IsActive         bool     `json:"is_active"`
	CreatedAt        string   `json:"created_at"`
}
