package models

import (
	"time"

	"github.com/google/uuid"
)

// Role represents a role in the system
type Role struct {
	ID          uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name        string    `gorm:"type:varchar(50);uniqueIndex;not null" json:"name"`
	DisplayName string    `gorm:"type:varchar(100);not null" json:"display_name"`
	Description string    `gorm:"type:text;default:''" json:"description"`
	IsSystem    bool      `gorm:"not null;default:false" json:"is_system"`
	Level       int       `gorm:"not null;default:0" json:"level"`
	CreatedAt   time.Time `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt   time.Time `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`

	// Associations
	Permissions []Permission `gorm:"many2many:role_permissions;joinForeignKey:role_id;joinReferences:permission_id" json:"permissions,omitempty"`
	Users       []User       `gorm:"many2many:user_roles;joinForeignKey:role_id;joinReferences:user_id" json:"users,omitempty"`
}

// TableName overrides the default table name
func (Role) TableName() string {
	return "roles"
}

// Default role names
const (
	RoleSuperAdmin = "super_admin"
	RoleAdmin      = "admin"
	RoleEditor     = "editor"
	RoleViewer     = "viewer"
)

// RoleResponse represents the role data returned in API responses
type RoleResponse struct {
	ID          uuid.UUID         `json:"id"`
	Name        string            `json:"name"`
	DisplayName string            `json:"display_name"`
	Description string            `json:"description"`
	IsSystem    bool              `json:"is_system"`
	Level       int               `json:"level"`
	CreatedAt   time.Time         `json:"created_at"`
	UpdatedAt   time.Time         `json:"updated_at"`
	Permissions []PermissionBrief `json:"permissions,omitempty"`
	UserCount   int64             `json:"user_count,omitempty"`
}

// PermissionBrief is a minimal permission representation for nested responses
type PermissionBrief struct {
	ID          uuid.UUID `json:"id"`
	Name        string    `json:"name"`
	DisplayName string    `json:"display_name"`
	Module      string    `json:"module"`
	Action      string    `json:"action"`
}

// ToResponse converts Role model to RoleResponse
func (r *Role) ToResponse() RoleResponse {
	resp := RoleResponse{
		ID:          r.ID,
		Name:        r.Name,
		DisplayName: r.DisplayName,
		Description: r.Description,
		IsSystem:    r.IsSystem,
		Level:       r.Level,
		CreatedAt:   r.CreatedAt,
		UpdatedAt:   r.UpdatedAt,
	}

	for _, perm := range r.Permissions {
		resp.Permissions = append(resp.Permissions, PermissionBrief{
			ID:          perm.ID,
			Name:        perm.Name,
			DisplayName: perm.DisplayName,
			Module:      perm.Module,
			Action:      perm.Action,
		})
	}

	return resp
}
