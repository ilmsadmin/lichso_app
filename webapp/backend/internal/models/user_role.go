package models

import (
	"time"

	"github.com/google/uuid"
)

// UserRole represents the many-to-many relationship between users and roles
type UserRole struct {
	ID         uuid.UUID  `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID     uuid.UUID  `gorm:"type:uuid;not null;index" json:"user_id"`
	RoleID     uuid.UUID  `gorm:"type:uuid;not null;index" json:"role_id"`
	AssignedAt time.Time  `gorm:"type:timestamptz;not null;default:now()" json:"assigned_at"`
	AssignedBy *uuid.UUID `gorm:"type:uuid" json:"assigned_by,omitempty"`

	// Associations
	User         User  `gorm:"foreignKey:UserID;references:ID" json:"user,omitempty"`
	Role         Role  `gorm:"foreignKey:RoleID;references:ID" json:"role,omitempty"`
	AssignedUser *User `gorm:"foreignKey:AssignedBy;references:ID" json:"assigned_user,omitempty"`
}

// TableName overrides the default table name
func (UserRole) TableName() string {
	return "user_roles"
}
