package dto

import "github.com/google/uuid"

// ============================================
// User Request DTOs
// ============================================

// CreateUserRequest represents a request to create a user from admin panel
type CreateUserRequest struct {
	Email     string      `json:"email" validate:"required,email"`
	Password  string      `json:"password" validate:"required,min=8"`
	FirstName string      `json:"first_name" validate:"required,min=1,max=100"`
	LastName  string      `json:"last_name" validate:"required,min=1,max=100"`
	Phone     string      `json:"phone" validate:"omitempty,max=20"`
	IsActive  *bool       `json:"is_active"`
	RoleIDs   []uuid.UUID `json:"role_ids"`
}

// UpdateUserRequest represents a request to update a user from admin panel
type UpdateUserRequest struct {
	Email     string  `json:"email" validate:"omitempty,email"`
	FirstName string  `json:"first_name" validate:"omitempty,min=1,max=100"`
	LastName  string  `json:"last_name" validate:"omitempty,min=1,max=100"`
	Phone     string  `json:"phone" validate:"omitempty,max=20"`
	Avatar    string  `json:"avatar" validate:"omitempty,max=500"`
	IsActive  *bool   `json:"is_active"`
	Password  *string `json:"password" validate:"omitempty,min=8"`
}

// ToggleUserStatusRequest represents a request to toggle user active status
type ToggleUserStatusRequest struct {
	IsActive bool `json:"is_active"`
}

// UserRolesRequest represents a request to set user roles
type UserRolesRequest struct {
	RoleIDs []uuid.UUID `json:"role_ids" validate:"required"`
}
