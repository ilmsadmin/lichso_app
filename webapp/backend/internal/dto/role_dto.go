package dto

import "github.com/google/uuid"

// ============================================
// Role Request DTOs
// ============================================

// CreateRoleRequest represents a request to create a new role
type CreateRoleRequest struct {
	Name          string      `json:"name" validate:"required,min=2,max=50"`
	DisplayName   string      `json:"display_name" validate:"required,min=2,max=100"`
	Description   string      `json:"description" validate:"max=500"`
	Level         int         `json:"level" validate:"min=0,max=100"`
	PermissionIDs []uuid.UUID `json:"permission_ids"`
}

// UpdateRoleRequest represents a request to update a role
type UpdateRoleRequest struct {
	Name          string       `json:"name" validate:"omitempty,min=2,max=50"`
	DisplayName   string       `json:"display_name" validate:"omitempty,min=2,max=100"`
	Description   *string      `json:"description" validate:"omitempty,max=500"`
	Level         *int         `json:"level" validate:"omitempty,min=0,max=100"`
	PermissionIDs *[]uuid.UUID `json:"permission_ids"`
}

// AssignRoleRequest represents a request to assign a role to a user
type AssignRoleRequest struct {
	UserID uuid.UUID `json:"user_id" validate:"required"`
	RoleID uuid.UUID `json:"role_id" validate:"required"`
}

// UnassignRoleRequest represents a request to remove a role from a user
type UnassignRoleRequest struct {
	UserID uuid.UUID `json:"user_id" validate:"required"`
	RoleID uuid.UUID `json:"role_id" validate:"required"`
}
