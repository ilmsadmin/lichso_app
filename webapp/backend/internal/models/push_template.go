package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// PushTemplate is a reusable push notification template.
type PushTemplate struct {
	ID          uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name        string         `gorm:"type:varchar(255);not null" json:"name"`
	Title       string         `gorm:"type:varchar(255);not null" json:"title"`
	Body        string         `gorm:"type:text;not null" json:"body"`
	ImageURL    string         `gorm:"type:varchar(500);default:''" json:"image_url"`
	ClickAction string         `gorm:"type:varchar(500);default:''" json:"click_action"`
	DataPayload string         `gorm:"type:text;default:'{}'" json:"data_payload"`
	CreatedBy   *uuid.UUID     `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt   gorm.DeletedAt `gorm:"type:timestamptz;index" json:"-"`
}

func (PushTemplate) TableName() string { return "push_templates" }

// UserGroup is an admin-managed group of users for targeted push campaigns.
type UserGroup struct {
	ID          uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name        string         `gorm:"type:varchar(255);not null" json:"name"`
	Description string         `gorm:"type:text;default:''" json:"description"`
	MemberCount int            `gorm:"default:0" json:"member_count"`
	CreatedBy   *uuid.UUID     `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt   time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt   gorm.DeletedAt `gorm:"type:timestamptz;index" json:"-"`
}

func (UserGroup) TableName() string { return "user_groups" }

// UserGroupMember links a user to a group.
type UserGroupMember struct {
	GroupID uuid.UUID `gorm:"type:uuid;primaryKey" json:"group_id"`
	UserID  uuid.UUID `gorm:"type:uuid;primaryKey" json:"user_id"`
	AddedAt time.Time `gorm:"type:timestamptz;not null;default:now()" json:"added_at"`
}

func (UserGroupMember) TableName() string { return "user_group_members" }
