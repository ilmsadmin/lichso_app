package dto

// ============================================
// Folk Festival DTOs
// ============================================

// CreateFolkFestivalRequest represents a request to create a folk festival.
type CreateFolkFestivalRequest struct {
	Name             string   `json:"name" validate:"required,max=500"`
	Slug             string   `json:"slug" validate:"omitempty,max=500"`
	AlternateName    string   `json:"alternate_name" validate:"omitempty,max=500"`
	CalendarType     string   `json:"calendar_type" validate:"required,oneof=lunar solar both"`
	LunarDay         *int     `json:"lunar_day" validate:"omitempty,min=1,max=30"`
	LunarMonth       *int     `json:"lunar_month" validate:"omitempty,min=1,max=12"`
	SolarDay         *int     `json:"solar_day" validate:"omitempty,min=1,max=31"`
	SolarMonth       *int     `json:"solar_month" validate:"omitempty,min=1,max=12"`
	DurationDays     int      `json:"duration_days" validate:"min=1"`
	FestivalType     string   `json:"festival_type" validate:"required,oneof=folk_festival religion national_holiday seasonal other"`
	Region           string   `json:"region" validate:"omitempty,max=255"`
	Country          string   `json:"country" validate:"omitempty,max=100"`
	ShortDescription string   `json:"short_description"`
	Traditions       []string `json:"traditions"`
	ImageURL         string   `json:"image_url" validate:"omitempty,max=500"`
	GalleryURLs      []string `json:"gallery_urls"`
	ArticleID        string   `json:"article_id" validate:"omitempty,uuid"`
	Importance       string   `json:"importance" validate:"omitempty,oneof=low medium high"`
	Tags             []string `json:"tags"`
}

// UpdateFolkFestivalRequest represents a request to update a folk festival.
type UpdateFolkFestivalRequest struct {
	Name             *string  `json:"name" validate:"omitempty,max=500"`
	Slug             *string  `json:"slug" validate:"omitempty,max=500"`
	AlternateName    *string  `json:"alternate_name" validate:"omitempty,max=500"`
	CalendarType     *string  `json:"calendar_type" validate:"omitempty,oneof=lunar solar both"`
	LunarDay         *int     `json:"lunar_day" validate:"omitempty,min=1,max=30"`
	LunarMonth       *int     `json:"lunar_month" validate:"omitempty,min=1,max=12"`
	SolarDay         *int     `json:"solar_day" validate:"omitempty,min=1,max=31"`
	SolarMonth       *int     `json:"solar_month" validate:"omitempty,min=1,max=12"`
	DurationDays     *int     `json:"duration_days" validate:"omitempty,min=1"`
	FestivalType     *string  `json:"festival_type" validate:"omitempty,oneof=folk_festival religion national_holiday seasonal other"`
	Region           *string  `json:"region" validate:"omitempty,max=255"`
	Country          *string  `json:"country" validate:"omitempty,max=100"`
	ShortDescription *string  `json:"short_description"`
	Traditions       []string `json:"traditions"`
	ImageURL         *string  `json:"image_url" validate:"omitempty,max=500"`
	GalleryURLs      []string `json:"gallery_urls"`
	ArticleID        *string  `json:"article_id" validate:"omitempty,uuid"`
	Importance       *string  `json:"importance" validate:"omitempty,oneof=low medium high"`
	Tags             []string `json:"tags"`
	IsActive         *bool    `json:"is_active"`
}

// FolkFestivalResponse represents a folk festival in API responses.
type FolkFestivalResponse struct {
	ID               string   `json:"id"`
	Name             string   `json:"name"`
	Slug             string   `json:"slug"`
	AlternateName    string   `json:"alternate_name,omitempty"`
	CalendarType     string   `json:"calendar_type"`
	LunarDay         *int     `json:"lunar_day,omitempty"`
	LunarMonth       *int     `json:"lunar_month,omitempty"`
	SolarDay         *int     `json:"solar_day,omitempty"`
	SolarMonth       *int     `json:"solar_month,omitempty"`
	DurationDays     int      `json:"duration_days"`
	FestivalType     string   `json:"festival_type"`
	Region           string   `json:"region,omitempty"`
	Country          string   `json:"country"`
	ShortDescription string   `json:"short_description,omitempty"`
	Traditions       []string `json:"traditions,omitempty"`
	ImageURL         string   `json:"image_url,omitempty"`
	GalleryURLs      []string `json:"gallery_urls,omitempty"`
	ArticleID        string   `json:"article_id,omitempty"`
	Importance       string   `json:"importance"`
	Tags             []string `json:"tags,omitempty"`
	IsActive         bool     `json:"is_active"`
	CreatedAt        string   `json:"created_at"`
}
