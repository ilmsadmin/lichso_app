package models

import (
	"time"

	"github.com/google/uuid"
)

// RolePermission represents the many-to-many relationship between roles and permissions
type RolePermission struct {
	ID           uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	RoleID       uuid.UUID `gorm:"type:uuid;not null;index" json:"role_id"`
	PermissionID uuid.UUID `gorm:"type:uuid;not null;index" json:"permission_id"`
	CreatedAt    time.Time `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`

	// Associations
	Role       Role       `gorm:"foreignKey:RoleID;references:ID" json:"role,omitempty"`
	Permission Permission `gorm:"foreignKey:PermissionID;references:ID" json:"permission,omitempty"`
}

// TableName overrides the default table name
func (RolePermission) TableName() string {
	return "role_permissions"
}
