package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

const (
	CampaignStatusDraft     = "draft"
	CampaignStatusScheduled = "scheduled"
	CampaignStatusSending   = "sending"
	CampaignStatusSent      = "sent"
	CampaignStatusFailed    = "failed"

	CampaignTargetAll   = "all"
	CampaignTargetUsers = "users"
	CampaignTargetGroup = "group"
)

// PushCampaign represents an admin-managed push notification broadcast.
type PushCampaign struct {
	ID          uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Title       string         `gorm:"type:varchar(255);not null" json:"title"`
	Body        string         `gorm:"type:text;not null" json:"body"`
	ImageURL    string         `gorm:"type:varchar(500);default:''" json:"image_url"`
	ClickAction string         `gorm:"type:varchar(500);default:''" json:"click_action"`
	// JSON object stored as text: {"key":"value"} for app deep-link data
	DataPayload string         `gorm:"type:text;default:'{}'" json:"data_payload"`
	TargetType  string         `gorm:"type:varchar(20);not null;default:'all'" json:"target_type"`
	// Comma-separated user IDs when target_type = "users"
	TargetUsers string         `gorm:"type:text;default:''" json:"target_users"`
	// Group ID when target_type = "group"
	TargetGroupID *uuid.UUID   `gorm:"type:uuid" json:"target_group_id,omitempty"`
	// Optional reference to a reusable template
	TemplateID *uuid.UUID      `gorm:"type:uuid" json:"template_id,omitempty"`
	Status      string         `gorm:"type:varchar(20);not null;default:'draft'" json:"status"`
	ScheduledAt *time.Time     `gorm:"type:timestamptz" json:"scheduled_at,omitempty"`
	SentAt      *time.Time     `gorm:"type:timestamptz" json:"sent_at,omitempty"`
	SentCount   int            `gorm:"default:0" json:"sent_count"`
	FailCount   int            `gorm:"default:0" json:"fail_count"`
	CreatedBy   *uuid.UUID     `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt   gorm.DeletedAt `gorm:"type:timestamptz;index" json:"-"`
}

func (PushCampaign) TableName() string {
	return "push_campaigns"
}
