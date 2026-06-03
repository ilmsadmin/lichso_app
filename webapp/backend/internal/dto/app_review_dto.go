package dto

import "time"

// SubmitAppReviewRequest is posted by Android/iOS after the user picks a star rating.
type SubmitAppReviewRequest struct {
	Stars      int    `json:"stars" validate:"required,min=1,max=5"`
	ReviewText string `json:"review_text" validate:"omitempty,max=5000"`
	ReviewFlow string `json:"review_flow" validate:"required,oneof=low_rating_feedback high_rating_prompt"`
}

// UpdateAppReviewRequest is used by admin to manage review workflow.
type UpdateAppReviewRequest struct {
	Status    *string `json:"status" validate:"omitempty,oneof=new reviewed resolved"`
	AdminNote *string `json:"admin_note" validate:"omitempty,max=5000"`
}

// AppReviewListParams contains admin filters.
type AppReviewListParams struct {
	Page      int
	Limit     int
	Search    string
	Status    string
	Platform  string
	Stars     int
	SortBy    string
	SortOrder string
}

// AppReviewUserDTO is a compact user payload for admin views.
type AppReviewUserDTO struct {
	ID       string `json:"id"`
	Email    string `json:"email"`
	FullName string `json:"full_name"`
	Provider string `json:"provider"`
}

// AppReviewResponse is shared by list/detail admin responses.
type AppReviewResponse struct {
	ID         string            `json:"id"`
	UserID     *string           `json:"user_id,omitempty"`
	Platform   string            `json:"platform"`
	AppVersion string            `json:"app_version"`
	DeviceID   string            `json:"device_id"`
	DeviceName string            `json:"device_name"`
	OSVersion  string            `json:"os_version"`
	Stars      int               `json:"stars"`
	ReviewText string            `json:"review_text"`
	ReviewFlow string            `json:"review_flow"`
	Status     string            `json:"status"`
	AdminNote  string            `json:"admin_note"`
	CreatedAt  time.Time         `json:"created_at"`
	UpdatedAt  time.Time         `json:"updated_at"`
	User       *AppReviewUserDTO `json:"user,omitempty"`
}
