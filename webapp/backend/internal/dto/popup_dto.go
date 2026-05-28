package dto

import "time"

// ============================================
// Popup DTOs
// ============================================

// CreatePopupRequest represents a request to create a popup.
type CreatePopupRequest struct {
	Title     string     `json:"title" validate:"required,max=255"`
	ImageURL  string     `json:"image_url" validate:"required,max=500"`
	CtaType   string     `json:"cta_type" validate:"omitempty,oneof=route url"`
	CtaRoute  string     `json:"cta_route" validate:"omitempty,max=255"`
	Position  string     `json:"position" validate:"omitempty,max=50"`
	IsActive  *bool      `json:"is_active"`
	StartDate *time.Time `json:"start_date"`
	EndDate   *time.Time `json:"end_date"`
}

// UpdatePopupRequest represents a request to update a popup.
type UpdatePopupRequest struct {
	Title     *string    `json:"title" validate:"omitempty,max=255"`
	ImageURL  *string    `json:"image_url" validate:"omitempty,max=500"`
	CtaType   *string    `json:"cta_type" validate:"omitempty,oneof=route url"`
	CtaRoute  *string    `json:"cta_route" validate:"omitempty,max=255"`
	Position  *string    `json:"position" validate:"omitempty,max=50"`
	IsActive  *bool      `json:"is_active"`
	StartDate *time.Time `json:"start_date"`
	EndDate   *time.Time `json:"end_date"`
}

// PopupResponse represents a popup in API responses.
type PopupResponse struct {
	ID        string  `json:"id"`
	Title     string  `json:"title"`
	ImageURL  string  `json:"image_url"`
	CtaType   string  `json:"cta_type,omitempty"`
	CtaRoute  string  `json:"cta_route,omitempty"`
	Position  string  `json:"position"`
	IsActive  bool    `json:"is_active"`
	StartDate *string `json:"start_date,omitempty"`
	EndDate   *string `json:"end_date,omitempty"`
	CreatedAt string  `json:"created_at"`
	UpdatedAt string  `json:"updated_at"`
}
