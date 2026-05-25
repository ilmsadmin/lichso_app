package services

import (
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/utils"
)

// ============================================
// Helper functions for tests
// ============================================

func createTestUser(email, firstName, lastName string) *models.User {
	return &models.User{
		ID:        uuid.New(),
		Email:     email,
		Password:  "$2a$12$hashedpassword",
		FirstName: firstName,
		LastName:  lastName,
		IsActive:  true,
	}
}

func createTestUserWithRoles(email, firstName, lastName string, roles []models.Role) *models.User {
	user := createTestUser(email, firstName, lastName)
	user.Roles = roles
	return user
}

func createTestRole(name, displayName string, level int) models.Role {
	return models.Role{
		ID:          uuid.New(),
		Name:        name,
		DisplayName: displayName,
		Level:       level,
		IsSystem:    name == models.RoleSuperAdmin || name == models.RoleAdmin,
	}
}

// ============================================
// Auth DTO Tests
// ============================================

func TestToAuthUserResponse(t *testing.T) {
	tests := []struct {
		name        string
		email       string
		firstName   string
		lastName    string
		roles       []models.Role
		permissions []string
		wantFull    string
		wantRoles   []string
	}{
		{
			name:      "full name with roles and permissions",
			email:     "admin@example.com",
			firstName: "John",
			lastName:  "Doe",
			roles: []models.Role{
				createTestRole("admin", "Admin", 80),
			},
			permissions: []string{"users.read", "users.create"},
			wantFull:    "John Doe",
			wantRoles:   []string{"admin"},
		},
		{
			name:        "first name only, no roles",
			email:       "user@example.com",
			firstName:   "Jane",
			lastName:    "",
			roles:       []models.Role{},
			permissions: []string{},
			wantFull:    "Jane",
			wantRoles:   []string{},
		},
		{
			name:        "no name, nil permissions",
			email:       "test@example.com",
			firstName:   "",
			lastName:    "",
			roles:       nil,
			permissions: nil,
			wantFull:    "",
			wantRoles:   []string{},
		},
		{
			name:      "multiple roles",
			email:     "multi@example.com",
			firstName: "Multi",
			lastName:  "Role",
			roles: []models.Role{
				createTestRole("admin", "Admin", 80),
				createTestRole("editor", "Editor", 60),
			},
			permissions: []string{"users.read", "roles.read"},
			wantFull:    "Multi Role",
			wantRoles:   []string{"admin", "editor"},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			user := createTestUserWithRoles(tt.email, tt.firstName, tt.lastName, tt.roles)
			resp := dto.ToAuthUserResponse(user, tt.permissions)

			assert.Equal(t, user.ID.String(), resp.ID)
			assert.Equal(t, tt.email, resp.Email)
			assert.Equal(t, tt.firstName, resp.FirstName)
			assert.Equal(t, tt.lastName, resp.LastName)
			assert.Equal(t, tt.wantFull, resp.FullName)
			assert.Equal(t, len(tt.wantRoles), len(resp.Roles))
			for _, wantRole := range tt.wantRoles {
				assert.Contains(t, resp.Roles, wantRole)
			}
		})
	}
}

func TestLoginRequest_Fields(t *testing.T) {
	tests := []struct {
		name     string
		email    string
		password string
	}{
		{"valid credentials", "test@example.com", "password123"},
		{"with special chars", "user+tag@example.com", "P@$$w0rd!"},
		{"long email", "verylongemail@subdomain.example.com", "securepass"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			req := dto.LoginRequest{
				Email:    tt.email,
				Password: tt.password,
			}
			assert.Equal(t, tt.email, req.Email)
			assert.Equal(t, tt.password, req.Password)
		})
	}
}

func TestRegisterRequest_Fields(t *testing.T) {
	req := dto.RegisterRequest{
		Email:     "new@example.com",
		Password:  "securePassword123",
		FirstName: "New",
		LastName:  "User",
	}

	assert.Equal(t, "new@example.com", req.Email)
	assert.Equal(t, "securePassword123", req.Password)
	assert.Equal(t, "New", req.FirstName)
	assert.Equal(t, "User", req.LastName)
}

func TestChangePasswordRequest_Fields(t *testing.T) {
	req := dto.ChangePasswordRequest{
		CurrentPassword: "oldpass",
		NewPassword:     "newpass123",
	}

	assert.Equal(t, "oldpass", req.CurrentPassword)
	assert.Equal(t, "newpass123", req.NewPassword)
	assert.NotEqual(t, req.CurrentPassword, req.NewPassword)
}

func TestLoginResponse_Structure(t *testing.T) {
	resp := dto.LoginResponse{
		AccessToken:  "access-token-xxx",
		RefreshToken: "refresh-token-xxx",
		ExpiresIn:    900,
		TokenType:    "Bearer",
		User: dto.AuthUserResponse{
			ID:          "user-id",
			Email:       "test@example.com",
			Roles:       []string{"admin"},
			Permissions: []string{"users.read"},
		},
	}

	assert.Equal(t, "Bearer", resp.TokenType)
	assert.Equal(t, int64(900), resp.ExpiresIn)
	assert.NotEmpty(t, resp.AccessToken)
	assert.NotEmpty(t, resp.RefreshToken)
	assert.Equal(t, "test@example.com", resp.User.Email)
	assert.Contains(t, resp.User.Roles, "admin")
	assert.Contains(t, resp.User.Permissions, "users.read")
}

func TestRefreshTokenResponse_Structure(t *testing.T) {
	resp := dto.RefreshTokenResponse{
		AccessToken:  "new-access-token",
		RefreshToken: "new-refresh-token",
		ExpiresIn:    900,
	}

	assert.NotEmpty(t, resp.AccessToken)
	assert.NotEmpty(t, resp.RefreshToken)
	assert.Equal(t, int64(900), resp.ExpiresIn)
}

func TestGetMeResponse_Structure(t *testing.T) {
	resp := dto.GetMeResponse{
		User: dto.AuthUserResponse{
			ID:          uuid.New().String(),
			Email:       "me@example.com",
			FirstName:   "Me",
			LastName:    "User",
			FullName:    "Me User",
			Roles:       []string{"viewer"},
			Permissions: []string{"users.read"},
		},
	}

	assert.Equal(t, "me@example.com", resp.User.Email)
	assert.Equal(t, "Me User", resp.User.FullName)
	assert.Contains(t, resp.User.Roles, "viewer")
}

// ============================================
// Auth Business Logic Tests (Password verification flow)
// ============================================

func TestAuthService_PasswordVerificationFlow(t *testing.T) {
	t.Run("correct password passes verification", func(t *testing.T) {
		password := "securePassword123"
		hashed, err := utils.HashPassword(password)
		assert.NoError(t, err)

		assert.True(t, utils.VerifyPassword(hashed, password))
	})

	t.Run("wrong password fails verification", func(t *testing.T) {
		password := "securePassword123"
		hashed, err := utils.HashPassword(password)
		assert.NoError(t, err)

		assert.False(t, utils.VerifyPassword(hashed, "wrongPassword"))
	})

	t.Run("empty password fails", func(t *testing.T) {
		hashed, err := utils.HashPassword("realpassword")
		assert.NoError(t, err)

		assert.False(t, utils.VerifyPassword(hashed, ""))
	})
}

func TestAuthService_TokenGenerationFlow(t *testing.T) {
	jwtCfg := &utils.JWTConfig{
		Secret:             "test-secret-key-for-unit-testing",
		AccessTokenExpiry:  15 * time.Minute,
		RefreshTokenExpiry: 7 * 24 * time.Hour,
		Issuer:             "zplus-test",
	}
	userID := uuid.New()
	email := "test@example.com"
	roles := []string{"admin", "editor"}

	t.Run("generates valid token pair", func(t *testing.T) {
		pair, refreshJTI, err := utils.GenerateTokenPair(jwtCfg, userID, email, roles)
		assert.NoError(t, err)
		assert.NotEmpty(t, pair.AccessToken)
		assert.NotEmpty(t, pair.RefreshToken)
		assert.Equal(t, "Bearer", pair.TokenType)
		assert.Greater(t, pair.ExpiresIn, int64(0))
		assert.NotEmpty(t, refreshJTI)

		// Validate access token claims
		claims, err := utils.ValidateToken(pair.AccessToken, jwtCfg.Secret)
		assert.NoError(t, err)
		assert.Equal(t, userID, claims.UserID)
		assert.Equal(t, email, claims.Email)
		assert.Equal(t, roles, claims.Roles)
		assert.Equal(t, utils.AccessToken, claims.Type)
	})

	t.Run("access token is valid", func(t *testing.T) {
		pair, _, err := utils.GenerateTokenPair(jwtCfg, userID, email, roles)
		assert.NoError(t, err)

		claims, err := utils.ValidateToken(pair.AccessToken, jwtCfg.Secret)
		assert.NoError(t, err)
		assert.Equal(t, userID, claims.UserID)
		assert.Equal(t, utils.AccessToken, claims.Type)
	})

	t.Run("refresh token is valid", func(t *testing.T) {
		pair, _, err := utils.GenerateTokenPair(jwtCfg, userID, email, roles)
		assert.NoError(t, err)

		claims, err := utils.ValidateToken(pair.RefreshToken, jwtCfg.Secret)
		assert.NoError(t, err)
		assert.Equal(t, userID, claims.UserID)
		assert.Equal(t, utils.RefreshToken, claims.Type)
	})

	t.Run("wrong secret fails validation", func(t *testing.T) {
		pair, _, err := utils.GenerateTokenPair(jwtCfg, userID, email, roles)
		assert.NoError(t, err)

		_, err = utils.ValidateToken(pair.AccessToken, "wrong-secret")
		assert.Error(t, err)
	})
}

// ============================================
// Error type tests
// ============================================

func TestAppError_Codes(t *testing.T) {
	tests := []struct {
		name     string
		err      *utils.AppError
		wantCode int
	}{
		{"invalid credentials", utils.ErrInvalidCredentials, 401},
		{"user not found", utils.ErrUserNotFound, 404},
		{"email exists", utils.ErrEmailExists, 409},
		{"cannot delete self", utils.ErrCannotDeleteSelf, 400},
		{"user inactive", utils.ErrUserInactive, 403},
		{"forbidden", utils.ErrForbidden, 403},
		{"database fail", utils.ErrDatabaseFail, 500},
		{"internal", utils.ErrInternal, 500},
		{"too many requests", utils.ErrTooManyRequests, 429},
		{"password mismatch", utils.ErrPasswordMismatch, 400},
		{"token expired", utils.ErrTokenExpired, 401},
		{"token invalid", utils.ErrTokenInvalid, 401},
		{"refresh token expired", utils.ErrRefreshTokenExpired, 401},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			assert.Equal(t, tt.wantCode, tt.err.StatusCode())
			assert.NotEmpty(t, tt.err.Error())
		})
	}
}

func TestAppError_NewAppError(t *testing.T) {
	t.Run("without detail", func(t *testing.T) {
		err := utils.NewAppError(400, "Bad request")
		assert.Equal(t, 400, err.Code)
		assert.Equal(t, "Bad request", err.Message)
		assert.Empty(t, err.Detail)
	})

	t.Run("with detail", func(t *testing.T) {
		err := utils.NewAppError(400, "Bad request", "email is invalid")
		assert.Equal(t, 400, err.Code)
		assert.Equal(t, "Bad request", err.Message)
		assert.Equal(t, "email is invalid", err.Detail)
		assert.Contains(t, err.Error(), "email is invalid")
	})
}
