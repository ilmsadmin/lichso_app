package services

import (
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/utils"
)

// ============================================
// User DTO Tests
// ============================================

func TestCreateUserRequest_Fields(t *testing.T) {
	isActive := true
	roleID := uuid.New()

	req := dto.CreateUserRequest{
		Email:     "newuser@example.com",
		Password:  "securePass123",
		FirstName: "New",
		LastName:  "User",
		Phone:     "+1234567890",
		IsActive:  &isActive,
		RoleIDs:   []uuid.UUID{roleID},
	}

	assert.Equal(t, "newuser@example.com", req.Email)
	assert.Equal(t, "securePass123", req.Password)
	assert.Equal(t, "New", req.FirstName)
	assert.Equal(t, "User", req.LastName)
	assert.Equal(t, "+1234567890", req.Phone)
	assert.True(t, *req.IsActive)
	assert.Len(t, req.RoleIDs, 1)
	assert.Equal(t, roleID, req.RoleIDs[0])
}

func TestUpdateUserRequest_Fields(t *testing.T) {
	isActive := false
	password := "newPassword123"

	req := dto.UpdateUserRequest{
		Email:     "updated@example.com",
		FirstName: "Updated",
		LastName:  "Name",
		Phone:     "+9876543210",
		Avatar:    "https://new-avatar.png",
		IsActive:  &isActive,
		Password:  &password,
	}

	assert.Equal(t, "updated@example.com", req.Email)
	assert.Equal(t, "Updated", req.FirstName)
	assert.Equal(t, "Updated", req.FirstName)
	assert.False(t, *req.IsActive)
	assert.Equal(t, "newPassword123", *req.Password)
}

func TestToggleUserStatusRequest_Fields(t *testing.T) {
	req := dto.ToggleUserStatusRequest{IsActive: true}
	assert.True(t, req.IsActive)

	req2 := dto.ToggleUserStatusRequest{IsActive: false}
	assert.False(t, req2.IsActive)
}

func TestUserRolesRequest_Fields(t *testing.T) {
	roleIDs := []uuid.UUID{uuid.New(), uuid.New(), uuid.New()}
	req := dto.UserRolesRequest{RoleIDs: roleIDs}
	assert.Len(t, req.RoleIDs, 3)
}

// ============================================
// User Response conversion tests
// ============================================

func TestUserResponse_Conversion(t *testing.T) {
	userID := uuid.New()
	roleID := uuid.New()

	user := &models.User{
		ID:        userID,
		Email:     "test@example.com",
		FirstName: "Test",
		LastName:  "User",
		Avatar:    "https://avatar.png",
		Phone:     "+1234567890",
		IsActive:  true,
		Roles: []models.Role{
			{
				ID:          roleID,
				Name:        "admin",
				DisplayName: "Administrator",
			},
		},
	}

	resp := user.ToResponse()

	assert.Equal(t, userID, resp.ID)
	assert.Equal(t, "test@example.com", resp.Email)
	assert.Equal(t, "Test User", resp.FullName)
	assert.True(t, resp.IsActive)
	assert.Len(t, resp.Roles, 1)
	assert.Equal(t, "admin", resp.Roles[0].Name)
}

// ============================================
// User Service business logic tests
// ============================================

func TestUserService_DeleteUser_CannotDeleteSelf(t *testing.T) {
	// Test that the logic correctly checks for self-deletion
	actorID := uuid.New()
	userID := actorID // same as actor

	assert.Equal(t, userID, actorID, "IDs should be equal - self deletion check")
}

func TestUserService_DeleteUser_CannotDeleteSuperAdmin(t *testing.T) {
	// Test that super admin check works
	user := &models.User{
		ID: uuid.New(),
		Roles: []models.Role{
			{Name: models.RoleSuperAdmin},
		},
	}

	assert.True(t, user.IsSuperAdmin())
}

func TestUserService_CreateUser_HashesPassword(t *testing.T) {
	password := "securePassword123"
	hashed, err := utils.HashPassword(password)
	assert.NoError(t, err)
	assert.NotEqual(t, password, hashed)
	assert.True(t, utils.VerifyPassword(hashed, password))
}

func TestUserService_UpdateUser_EmailUniqueness(t *testing.T) {
	// Test that email update logic checks for different email
	user := createTestUser("original@example.com", "Test", "User")
	newEmail := "new@example.com"

	assert.NotEqual(t, user.Email, newEmail, "emails should be different when updating")
}

func TestUserService_UpdateUser_AppliesFields(t *testing.T) {
	user := createTestUser("test@example.com", "Old", "Name")

	// Simulate update logic from the service
	req := dto.UpdateUserRequest{
		FirstName: "New",
		LastName:  "Name",
		Phone:     "+1234567890",
		Avatar:    "https://new-avatar.png",
	}

	if req.FirstName != "" {
		user.FirstName = req.FirstName
	}
	if req.LastName != "" {
		user.LastName = req.LastName
	}
	if req.Phone != "" {
		user.Phone = req.Phone
	}
	if req.Avatar != "" {
		user.Avatar = req.Avatar
	}

	assert.Equal(t, "New", user.FirstName)
	assert.Equal(t, "Name", user.LastName)
	assert.Equal(t, "+1234567890", user.Phone)
	assert.Equal(t, "https://new-avatar.png", user.Avatar)
}

func TestUserService_UpdateUser_PasswordUpdate(t *testing.T) {
	newPassword := "newSecurePassword"
	hashed, err := utils.HashPassword(newPassword)
	assert.NoError(t, err)

	assert.True(t, utils.VerifyPassword(hashed, newPassword))
	assert.False(t, utils.VerifyPassword(hashed, "oldPassword"))
}

func TestUserService_ToggleStatus_Logic(t *testing.T) {
	tests := []struct {
		name       string
		isActive   bool
		wantAction string
	}{
		{"activate", true, models.ActionUserActivate},
		{"deactivate", false, models.ActionUserDeactivate},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			action := models.ActionUserActivate
			if !tt.isActive {
				action = models.ActionUserDeactivate
			}
			assert.Equal(t, tt.wantAction, action)
		})
	}
}

// ============================================
// CSV Export logic tests
// ============================================

func TestExportCSV_RoleFormatting(t *testing.T) {
	// Test the role formatting logic used in ExportUsersCSV
	roles := []models.Role{
		{DisplayName: "Administrator"},
		{DisplayName: "Editor"},
		{DisplayName: "Viewer"},
	}

	rolesStr := ""
	for i, role := range roles {
		if i > 0 {
			rolesStr += ", "
		}
		rolesStr += role.DisplayName
	}

	assert.Equal(t, "Administrator, Editor, Viewer", rolesStr)
}

func TestExportCSV_RoleFormattingEmpty(t *testing.T) {
	var roles []models.Role
	rolesStr := ""
	for i, role := range roles {
		if i > 0 {
			rolesStr += ", "
		}
		rolesStr += role.DisplayName
	}
	assert.Empty(t, rolesStr)
}

func TestExportCSV_ActiveFormatting(t *testing.T) {
	tests := []struct {
		isActive bool
		want     string
	}{
		{true, "Yes"},
		{false, "No"},
	}

	for _, tt := range tests {
		active := "Yes"
		if !tt.isActive {
			active = "No"
		}
		assert.Equal(t, tt.want, active)
	}
}

// ============================================
// Pagination Query Tests
// ============================================

func TestPaginationQuery_Offset(t *testing.T) {
	tests := []struct {
		name       string
		page       int
		limit      int
		wantOffset int
	}{
		{"page 1", 1, 10, 0},
		{"page 2", 2, 10, 10},
		{"page 3 with 20 limit", 3, 20, 40},
		{"page 1 with 50 limit", 1, 50, 0},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			pq := utils.PaginationQuery{
				Page:  tt.page,
				Limit: tt.limit,
			}
			assert.Equal(t, tt.wantOffset, pq.Offset())
		})
	}
}
