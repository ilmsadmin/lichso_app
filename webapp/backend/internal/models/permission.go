package models

import (
	"time"

	"github.com/google/uuid"
)

// Permission represents a permission in the system
type Permission struct {
	ID          uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name        string    `gorm:"type:varchar(100);uniqueIndex;not null" json:"name"`
	DisplayName string    `gorm:"type:varchar(200);not null" json:"display_name"`
	Module      string    `gorm:"type:varchar(50);not null" json:"module"`
	Action      string    `gorm:"type:varchar(50);not null" json:"action"`
	Description string    `gorm:"type:text;default:''" json:"description"`
	CreatedAt   time.Time `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`

	// Associations
	Roles []Role `gorm:"many2many:role_permissions;joinForeignKey:permission_id;joinReferences:role_id" json:"roles,omitempty"`
}

// TableName overrides the default table name
func (Permission) TableName() string {
	return "permissions"
}

// Permission modules
const (
	ModuleUsers       = "users"
	ModuleRoles       = "roles"
	ModulePermissions = "permissions"
	ModuleSettings    = "settings"
	ModuleDashboard   = "dashboard"
	ModuleLogs        = "logs"
)

// Permission actions
const (
	ActionCreate = "create"
	ActionRead   = "read"
	ActionUpdate = "update"
	ActionDelete = "delete"
	ActionExport = "export"
	ActionAssign = "assign"
	ActionStats  = "stats"
)

// PermissionResponse represents the permission data returned in API responses
type PermissionResponse struct {
	ID          uuid.UUID `json:"id"`
	Name        string    `json:"name"`
	DisplayName string    `json:"display_name"`
	Module      string    `json:"module"`
	Action      string    `json:"action"`
	Description string    `json:"description"`
	CreatedAt   time.Time `json:"created_at"`
}

// ToResponse converts Permission model to PermissionResponse
func (p *Permission) ToResponse() PermissionResponse {
	return PermissionResponse{
		ID:          p.ID,
		Name:        p.Name,
		DisplayName: p.DisplayName,
		Module:      p.Module,
		Action:      p.Action,
		Description: p.Description,
		CreatedAt:   p.CreatedAt,
	}
}

// GroupedPermissions groups permissions by module
type GroupedPermissions struct {
	Module      string               `json:"module"`
	Permissions []PermissionResponse `json:"permissions"`
}

// GroupPermissionsByModule groups a slice of permissions by their module
func GroupPermissionsByModule(permissions []Permission) []GroupedPermissions {
	moduleMap := make(map[string][]PermissionResponse)
	moduleOrder := make([]string, 0)

	for _, p := range permissions {
		resp := p.ToResponse()
		if _, exists := moduleMap[p.Module]; !exists {
			moduleOrder = append(moduleOrder, p.Module)
		}
		moduleMap[p.Module] = append(moduleMap[p.Module], resp)
	}

	grouped := make([]GroupedPermissions, 0, len(moduleOrder))
	for _, module := range moduleOrder {
		grouped = append(grouped, GroupedPermissions{
			Module:      module,
			Permissions: moduleMap[module],
		})
	}

	return grouped
}
