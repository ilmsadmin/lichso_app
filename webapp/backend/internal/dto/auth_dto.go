package dto

import "github.com/zplus/lichso/internal/models"

// ============================================
// Auth Request DTOs
// ============================================

// LoginRequest represents a login request
type LoginRequest struct {
	Email    string `json:"email" validate:"required,email"`
	Password string `json:"password" validate:"required,min=6"`
}

// RegisterRequest represents a registration request
type RegisterRequest struct {
	Email     string `json:"email" validate:"required,email"`
	Password  string `json:"password" validate:"required,min=8"`
	FirstName string `json:"first_name" validate:"required,min=1,max=100"`
	LastName  string `json:"last_name" validate:"required,min=1,max=100"`
}

// RefreshTokenRequest represents a token refresh request
type RefreshTokenRequest struct {
	RefreshToken string `json:"refresh_token" validate:"required"`
}

// ForgotPasswordRequest represents a forgot password request
type ForgotPasswordRequest struct {
	Email string `json:"email" validate:"required,email"`
}

// ResetPasswordRequest represents a password reset request
type ResetPasswordRequest struct {
	Token    string `json:"token" validate:"required"`
	Password string `json:"password" validate:"required,min=8"`
}

// ChangePasswordRequest represents a change password request
type ChangePasswordRequest struct {
	CurrentPassword string `json:"current_password" validate:"required"`
	NewPassword     string `json:"new_password" validate:"required,min=8"`
}

// GoogleLoginRequest represents a Google OAuth login request
type GoogleLoginRequest struct {
	IDToken string `json:"id_token" validate:"required"`
	// DeviceID lets the backend reconcile an existing anonymous guest user
	// (provider=guest, provider_id=device_id) with the Google account on sign-in.
	DeviceID string `json:"device_id"`
}

// AppleLoginRequest represents an Apple OAuth login request
type AppleLoginRequest struct {
	IDToken   string `json:"id_token" validate:"required"`
	DeviceID  string `json:"device_id"`
	FirstName string `json:"first_name"`
	LastName  string `json:"last_name"`
}

// GuestLoginRequest creates or resumes an anonymous guest session keyed by device id.
type GuestLoginRequest struct {
	DeviceID    string `json:"device_id" validate:"required,min=4"`
	DeviceName  string `json:"device_name"`
	DisplayName string `json:"display_name"`
}

// UpdateProfileRequest represents a request to update the current user's profile.
// LastName is optional so single-token display names (e.g. guests) are accepted.
type UpdateProfileRequest struct {
	FirstName string `json:"first_name" validate:"required,min=1,max=100"`
	LastName  string `json:"last_name" validate:"omitempty,max=100"`
}

// ============================================
// Auth Response DTOs
// ============================================

// LoginResponse represents a login response
type LoginResponse struct {
	AccessToken  string           `json:"access_token"`
	RefreshToken string           `json:"refresh_token"`
	ExpiresIn    int64            `json:"expires_in"`
	TokenType    string           `json:"token_type"`
	User         AuthUserResponse `json:"user"`
}

// AuthUserResponse represents user data in auth responses
type AuthUserResponse struct {
	ID          string   `json:"id"`
	Email       string   `json:"email"`
	FirstName   string   `json:"first_name"`
	LastName    string   `json:"last_name"`
	FullName    string   `json:"full_name"`
	Avatar      string   `json:"avatar"`
	Roles       []string `json:"roles"`
	Permissions []string `json:"permissions"`
}

// RefreshTokenResponse represents a token refresh response
type RefreshTokenResponse struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
	ExpiresIn    int64  `json:"expires_in"`
}

// GetMeResponse represents the response for GET /api/auth/me
type GetMeResponse struct {
	User AuthUserResponse `json:"user"`
}

// ToAuthUserResponse converts a User model to AuthUserResponse
func ToAuthUserResponse(user *models.User, permissions []string) AuthUserResponse {
	roles := make([]string, len(user.Roles))
	for i, role := range user.Roles {
		roles[i] = role.Name
	}

	return AuthUserResponse{
		ID:          user.ID.String(),
		Email:       user.Email,
		FirstName:   user.FirstName,
		LastName:    user.LastName,
		FullName:    user.FullName(),
		Avatar:      user.Avatar,
		Roles:       roles,
		Permissions: permissions,
	}
}
