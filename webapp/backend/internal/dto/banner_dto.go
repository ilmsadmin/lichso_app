package dto

import "time"

// ============================================
// Banner DTOs
// ============================================

// CreateBannerRequest represents a request to create a banner.
type CreateBannerRequest struct {
	Title     string     `json:"title" validate:"required,max=255"`
	Subtitle  string     `json:"subtitle"`
	ImageURL  string     `json:"image_url" validate:"omitempty,max=500"`
	IconURL   string     `json:"icon_url" validate:"omitempty,max=500"`
	IconKey   string     `json:"icon_key" validate:"omitempty,max=60"`
	CtaText   string     `json:"cta_text" validate:"omitempty,max=100"`
	CtaType   string     `json:"cta_type" validate:"omitempty,oneof=route url"`
	CtaRoute  string     `json:"cta_route" validate:"omitempty,max=255"`
	BgColor   string     `json:"bg_color" validate:"omitempty,max=20"`
	Locations []string   `json:"locations" validate:"required"`
	Platform  string     `json:"platform" validate:"omitempty,oneof=all android ios"`
	IsActive  *bool      `json:"is_active"`
	SortOrder *int       `json:"sort_order"`
	StartDate *time.Time `json:"start_date"`
	EndDate   *time.Time `json:"end_date"`
}

// UpdateBannerRequest represents a request to update a banner.
type UpdateBannerRequest struct {
	Title     *string    `json:"title" validate:"omitempty,max=255"`
	Subtitle  *string    `json:"subtitle"`
	ImageURL  *string    `json:"image_url" validate:"omitempty,max=500"`
	IconURL   *string    `json:"icon_url" validate:"omitempty,max=500"`
	IconKey   *string    `json:"icon_key" validate:"omitempty,max=60"`
	CtaText   *string    `json:"cta_text" validate:"omitempty,max=100"`
	CtaType   *string    `json:"cta_type" validate:"omitempty,oneof=route url"`
	CtaRoute  *string    `json:"cta_route" validate:"omitempty,max=255"`
	BgColor   *string    `json:"bg_color" validate:"omitempty,max=20"`
	Locations []string   `json:"locations" validate:"omitempty"`
	Platform  *string    `json:"platform" validate:"omitempty,oneof=all android ios"`
	IsActive  *bool      `json:"is_active"`
	SortOrder *int       `json:"sort_order"`
	StartDate *time.Time `json:"start_date"`
	EndDate   *time.Time `json:"end_date"`
}

// BannerResponse represents a banner in API responses.
type BannerResponse struct {
	ID        string  `json:"id"`
	Title     string  `json:"title"`
	Subtitle  string  `json:"subtitle,omitempty"`
	ImageURL  string  `json:"image_url,omitempty"`
	IconURL   string  `json:"icon_url,omitempty"`
	IconKey   string  `json:"icon_key,omitempty"`
	CtaText   string  `json:"cta_text,omitempty"`
	CtaType   string  `json:"cta_type,omitempty"`
	CtaRoute  string  `json:"cta_route,omitempty"`
	BgColor   string   `json:"bg_color,omitempty"`
	Locations []string `json:"locations"`
	Platform  string   `json:"platform"`
	IsActive  bool    `json:"is_active"`
	SortOrder int     `json:"sort_order"`
	StartDate *string `json:"start_date,omitempty"`
	EndDate   *string `json:"end_date,omitempty"`
	CreatedAt string  `json:"created_at"`
	UpdatedAt string  `json:"updated_at"`
}

// ReorderBannersRequest represents a request to reorder banners.
type ReorderBannersRequest struct {
	Orders []BannerOrder `json:"orders" validate:"required,min=1"`
}

// BannerOrder represents a single banner's sort order.
type BannerOrder struct {
	ID        string `json:"id" validate:"required"`
	SortOrder int    `json:"sort_order"`
}
