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
// RBAC logic tests - Permission checking
// ============================================

func TestRBAC_SuperAdminBypass(t *testing.T) {
	// Super admin should be detected by HasRole
	user := &models.User{
		ID: uuid.New(),
		Roles: []models.Role{
			{Name: models.RoleSuperAdmin},
		},
	}
	assert.True(t, user.IsSuperAdmin())
	assert.True(t, user.HasRole(models.RoleSuperAdmin))
}

func TestRBAC_AdminHasRole(t *testing.T) {
	user := &models.User{
		ID: uuid.New(),
		Roles: []models.Role{
			{Name: models.RoleAdmin},
		},
	}
	assert.False(t, user.IsSuperAdmin())
	assert.True(t, user.HasRole(models.RoleAdmin))
	assert.True(t, user.IsAdmin())
}

func TestRBAC_EditorNotAdmin(t *testing.T) {
	user := &models.User{
		ID: uuid.New(),
		Roles: []models.Role{
			{Name: models.RoleEditor},
		},
	}
	assert.False(t, user.IsSuperAdmin())
	assert.False(t, user.IsAdmin())
	assert.True(t, user.HasRole(models.RoleEditor))
}

func TestRBAC_PermissionMatching(t *testing.T) {
	// Simulate permission matching logic from RBACService.CheckUserHasPermission
	permissions := []string{"users.read", "users.create", "roles.read", "dashboard.stats"}

	tests := []struct {
		permission string
		want       bool
	}{
		{"users.read", true},
		{"users.create", true},
		{"roles.read", true},
		{"dashboard.stats", true},
		{"users.delete", false},
		{"roles.create", false},
		{"settings.read", false},
		{"", false},
	}

	for _, tt := range tests {
		t.Run(tt.permission, func(t *testing.T) {
			found := false
			for _, p := range permissions {
				if p == tt.permission {
					found = true
					break
				}
			}
			assert.Equal(t, tt.want, found)
		})
	}
}

func TestRBAC_MultipleRoles(t *testing.T) {
	user := &models.User{
		Roles: []models.Role{
			{Name: models.RoleAdmin},
			{Name: models.RoleEditor},
		},
	}

	assert.True(t, user.HasRole(models.RoleAdmin))
	assert.True(t, user.HasRole(models.RoleEditor))
	assert.False(t, user.HasRole(models.RoleSuperAdmin))
	assert.False(t, user.HasRole(models.RoleViewer))
}

// ============================================
// Role DTO Tests
// ============================================

func TestRoleDTO_Fields(t *testing.T) {
	roleResp := models.RoleResponse{
		ID:          uuid.New(),
		Name:        "admin",
		DisplayName: "Administrator",
		Description: "System administrator role",
		IsSystem:    true,
		Level:       80,
		Permissions: []models.PermissionBrief{
			{
				ID:          uuid.New(),
				Name:        "users.read",
				DisplayName: "Read Users",
				Module:      "users",
				Action:      "read",
			},
		},
		UserCount: 5,
	}

	assert.Equal(t, "admin", roleResp.Name)
	assert.Equal(t, "Administrator", roleResp.DisplayName)
	assert.True(t, roleResp.IsSystem)
	assert.Equal(t, 80, roleResp.Level)
	assert.Len(t, roleResp.Permissions, 1)
	assert.Equal(t, int64(5), roleResp.UserCount)
}

func TestRoleBrief(t *testing.T) {
	brief := models.RoleBrief{
		ID:          uuid.New(),
		Name:        "editor",
		DisplayName: "Content Editor",
	}

	assert.Equal(t, "editor", brief.Name)
	assert.Equal(t, "Content Editor", brief.DisplayName)
}

// ============================================
// Role DTO Request Tests
// ============================================

func TestRoleDTO_Requests(t *testing.T) {
	t.Run("create role request", func(t *testing.T) {
		req := dto.CreateRoleRequest{
			Name:          "manager",
			DisplayName:   "Manager",
			Description:   "Manages a team",
			Level:         70,
			PermissionIDs: []uuid.UUID{uuid.New()},
		}
		assert.Equal(t, "manager", req.Name)
		assert.Equal(t, 70, req.Level)
		assert.Len(t, req.PermissionIDs, 1)
	})

	t.Run("update role request", func(t *testing.T) {
		desc := "Updated description"
		level := 75
		req := dto.UpdateRoleRequest{
			DisplayName: "Updated Name",
			Description: &desc,
			Level:       &level,
		}
		assert.Equal(t, "Updated Name", req.DisplayName)
		assert.Equal(t, 75, *req.Level)
		assert.Equal(t, "Updated description", *req.Description)
	})
}

// ============================================
// RBAC Error Tests
// ============================================

func TestRBAC_Errors(t *testing.T) {
	assert.Equal(t, 404, utils.ErrRoleNotFound.StatusCode())
	assert.Equal(t, 400, utils.ErrCannotDeleteSystemRole.StatusCode())
	assert.Equal(t, 403, utils.ErrForbidden.StatusCode())
	assert.Equal(t, 403, utils.ErrInsufficientRole.StatusCode())
}

// ============================================
// Assign/Remove Role Logic Tests
// ============================================

func TestRBAC_AssignRoleToUser_AlreadyAssigned(t *testing.T) {
	// Simulate the "already has role" check
	existingRoles := []string{"admin", "editor"}
	newRole := "admin"

	hasRole := false
	for _, r := range existingRoles {
		if r == newRole {
			hasRole = true
			break
		}
	}
	assert.True(t, hasRole, "should detect role already assigned")
}

func TestRBAC_RemoveRoleFromUser_NotAssigned(t *testing.T) {
	existingRoles := []string{"admin", "editor"}
	removeRole := "viewer"

	hasRole := false
	for _, r := range existingRoles {
		if r == removeRole {
			hasRole = true
			break
		}
	}
	assert.False(t, hasRole, "should detect role not assigned")
}
