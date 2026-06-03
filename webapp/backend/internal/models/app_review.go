package models

import (
	"strings"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

const (
	AppReviewFlowLowRatingFeedback = "low_rating_feedback"
	AppReviewFlowHighRatingPrompt  = "high_rating_prompt"

	AppReviewStatusNew      = "new"
	AppReviewStatusReviewed = "reviewed"
	AppReviewStatusResolved = "resolved"
)

// NormalizeAppReviewStatus converts a raw status to a supported canonical value.
func NormalizeAppReviewStatus(raw string) string {
	switch strings.ToLower(strings.TrimSpace(raw)) {
	case AppReviewStatusNew:
		return AppReviewStatusNew
	case AppReviewStatusReviewed:
		return AppReviewStatusReviewed
	case AppReviewStatusResolved:
		return AppReviewStatusResolved
	default:
		return ""
	}
}

// NormalizeAppReviewFlow converts a raw review flow to a supported canonical value.
func NormalizeAppReviewFlow(raw string) string {
	switch strings.ToLower(strings.TrimSpace(raw)) {
	case AppReviewFlowLowRatingFeedback:
		return AppReviewFlowLowRatingFeedback
	case AppReviewFlowHighRatingPrompt:
		return AppReviewFlowHighRatingPrompt
	default:
		return ""
	}
}

// AppReview stores a star rating and optional written feedback sent from mobile apps.
type AppReview struct {
	ID         uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID     *uuid.UUID     `gorm:"type:uuid;index" json:"user_id,omitempty"`
	Platform   string         `gorm:"type:varchar(20);not null" json:"platform"`
	AppVersion string         `gorm:"type:varchar(50);not null;default:''" json:"app_version"`
	DeviceID   string         `gorm:"type:varchar(255);not null;default:''" json:"device_id"`
	DeviceName string         `gorm:"type:varchar(255);not null;default:''" json:"device_name"`
	OSVersion  string         `gorm:"type:varchar(100);not null;default:''" json:"os_version"`
	Stars      int            `gorm:"type:smallint;not null" json:"stars"`
	ReviewText string         `gorm:"type:text;not null;default:''" json:"review_text"`
	ReviewFlow string         `gorm:"type:varchar(40);not null" json:"review_flow"`
	Status     string         `gorm:"type:varchar(20);not null;default:'new'" json:"status"`
	AdminNote  string         `gorm:"type:text;not null;default:''" json:"admin_note"`
	CreatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt  gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	User *User `gorm:"foreignKey:UserID" json:"user,omitempty"`
}

func (AppReview) TableName() string {
	return "app_reviews"
}
