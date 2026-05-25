package models

import (
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
)

// ============================================
// User model tests
// ============================================

func TestUser_FullName(t *testing.T) {
	tests := []struct {
		name      string
		firstName string
		lastName  string
		want      string
	}{
		{"both names", "John", "Doe", "John Doe"},
		{"first name only", "Jane", "", "Jane"},
		{"last name only", "", "Smith", "Smith"},
		{"no name", "", "", ""},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			user := &User{FirstName: tt.firstName, LastName: tt.lastName}
			assert.Equal(t, tt.want, user.FullName())
		})
	}
}

func TestUser_HasRole(t *testing.T) {
	user := &User{
		Roles: []Role{
			{Name: "admin"},
			{Name: "editor"},
		},
	}

	assert.True(t, user.HasRole("admin"))
	assert.True(t, user.HasRole("editor"))
	assert.False(t, user.HasRole("viewer"))
	assert.False(t, user.HasRole("super_admin"))
}

func TestUser_IsSuperAdmin(t *testing.T) {
	t.Run("super admin", func(t *testing.T) {
		user := &User{Roles: []Role{{Name: RoleSuperAdmin}}}
		assert.True(t, user.IsSuperAdmin())
	})

	t.Run("regular admin", func(t *testing.T) {
		user := &User{Roles: []Role{{Name: RoleAdmin}}}
		assert.False(t, user.IsSuperAdmin())
	})

	t.Run("no roles", func(t *testing.T) {
		user := &User{}
		assert.False(t, user.IsSuperAdmin())
	})
}

func TestUser_IsAdmin(t *testing.T) {
	tests := []struct {
		name     string
		roles    []Role
		expected bool
	}{
		{"super_admin", []Role{{Name: RoleSuperAdmin}}, true},
		{"admin", []Role{{Name: RoleAdmin}}, true},
		{"editor", []Role{{Name: RoleEditor}}, false},
		{"viewer", []Role{{Name: RoleViewer}}, false},
		{"no roles", []Role{}, false},
		{"admin + editor", []Role{{Name: RoleAdmin}, {Name: RoleEditor}}, true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			user := &User{Roles: tt.roles}
			assert.Equal(t, tt.expected, user.IsAdmin())
		})
	}
}

func TestUser_TableName(t *testing.T) {
	user := User{}
	assert.Equal(t, "users", user.TableName())
}

func TestUser_ToResponse(t *testing.T) {
	now := time.Now()
	userID := uuid.New()
	roleID := uuid.New()

	user := &User{
		ID:        userID,
		Email:     "test@example.com",
		FirstName: "Test",
		LastName:  "User",
		Avatar:    "https://avatar.url",
		Phone:     "+1234567890",
		IsActive:  true,
		LastLogin: &now,
		CreatedAt: now,
		UpdatedAt: now,
		Roles: []Role{
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
	assert.Equal(t, "Test", resp.FirstName)
	assert.Equal(t, "User", resp.LastName)
	assert.Equal(t, "Test User", resp.FullName)
	assert.Equal(t, "https://avatar.url", resp.Avatar)
	assert.Equal(t, "+1234567890", resp.Phone)
	assert.True(t, resp.IsActive)
	assert.NotNil(t, resp.LastLogin)
	assert.Len(t, resp.Roles, 1)
	assert.Equal(t, "admin", resp.Roles[0].Name)
	assert.Equal(t, "Administrator", resp.Roles[0].DisplayName)
}

func TestUser_ToResponse_NoRoles(t *testing.T) {
	user := &User{
		ID:       uuid.New(),
		Email:    "noroles@example.com",
		IsActive: true,
	}

	resp := user.ToResponse()
	assert.Nil(t, resp.Roles)
}

// ============================================
// Role model tests
// ============================================

func TestRole_TableName(t *testing.T) {
	role := Role{}
	assert.Equal(t, "roles", role.TableName())
}

func TestRole_Constants(t *testing.T) {
	assert.Equal(t, "super_admin", RoleSuperAdmin)
	assert.Equal(t, "admin", RoleAdmin)
	assert.Equal(t, "editor", RoleEditor)
	assert.Equal(t, "viewer", RoleViewer)
}

// ============================================
// Permission model tests
// ============================================

func TestPermission_TableName(t *testing.T) {
	p := Permission{}
	assert.Equal(t, "permissions", p.TableName())
}

func TestPermission_ModuleConstants(t *testing.T) {
	assert.Equal(t, "users", ModuleUsers)
	assert.Equal(t, "roles", ModuleRoles)
	assert.Equal(t, "permissions", ModulePermissions)
	assert.Equal(t, "settings", ModuleSettings)
	assert.Equal(t, "dashboard", ModuleDashboard)
	assert.Equal(t, "logs", ModuleLogs)
}

func TestPermission_ActionConstants(t *testing.T) {
	assert.Equal(t, "create", ActionCreate)
	assert.Equal(t, "read", ActionRead)
	assert.Equal(t, "update", ActionUpdate)
	assert.Equal(t, "delete", ActionDelete)
	assert.Equal(t, "export", ActionExport)
	assert.Equal(t, "assign", ActionAssign)
	assert.Equal(t, "stats", ActionStats)
}

// ============================================
// RefreshToken model tests
// ============================================

func TestRefreshToken_TableName(t *testing.T) {
	rt := RefreshToken{}
	assert.Equal(t, "refresh_tokens", rt.TableName())
}

func TestRefreshToken_IsExpired(t *testing.T) {
	t.Run("not expired", func(t *testing.T) {
		rt := &RefreshToken{ExpiresAt: time.Now().Add(1 * time.Hour)}
		assert.False(t, rt.IsExpired())
	})

	t.Run("expired", func(t *testing.T) {
		rt := &RefreshToken{ExpiresAt: time.Now().Add(-1 * time.Hour)}
		assert.True(t, rt.IsExpired())
	})
}

func TestRefreshToken_IsRevoked(t *testing.T) {
	t.Run("not revoked", func(t *testing.T) {
		rt := &RefreshToken{RevokedAt: nil}
		assert.False(t, rt.IsRevoked())
	})

	t.Run("revoked", func(t *testing.T) {
		now := time.Now()
		rt := &RefreshToken{RevokedAt: &now}
		assert.True(t, rt.IsRevoked())
	})
}

func TestRefreshToken_IsValid(t *testing.T) {
	t.Run("valid token", func(t *testing.T) {
		rt := &RefreshToken{
			ExpiresAt: time.Now().Add(1 * time.Hour),
			RevokedAt: nil,
		}
		assert.True(t, rt.IsValid())
	})

	t.Run("expired token", func(t *testing.T) {
		rt := &RefreshToken{
			ExpiresAt: time.Now().Add(-1 * time.Hour),
			RevokedAt: nil,
		}
		assert.False(t, rt.IsValid())
	})

	t.Run("revoked token", func(t *testing.T) {
		now := time.Now()
		rt := &RefreshToken{
			ExpiresAt: time.Now().Add(1 * time.Hour),
			RevokedAt: &now,
		}
		assert.False(t, rt.IsValid())
	})

	t.Run("expired and revoked", func(t *testing.T) {
		now := time.Now()
		rt := &RefreshToken{
			ExpiresAt: time.Now().Add(-1 * time.Hour),
			RevokedAt: &now,
		}
		assert.False(t, rt.IsValid())
	})
}

// ============================================
// ActivityLog model tests
// ============================================

func TestActivityLog_CollectionName(t *testing.T) {
	al := ActivityLog{}
	assert.Equal(t, "activity_logs", al.CollectionName())
}

func TestActivityLog_ActionConstants(t *testing.T) {
	actions := []string{
		ActionUserLogin, ActionUserLogout, ActionUserCreate, ActionUserUpdate,
		ActionUserDelete, ActionUserPasswordChange, ActionUserPasswordReset,
		ActionUserActivate, ActionUserDeactivate, ActionRoleCreate, ActionRoleUpdate,
		ActionRoleDelete, ActionRoleAssign, ActionRoleUnassign,
		ActionPermissionAssign, ActionPermissionUnassign, ActionSettingsUpdate,
		ActionLoginFailed,
	}

	for _, a := range actions {
		assert.NotEmpty(t, a)
	}
}

func TestActivityLog_StatusConstants(t *testing.T) {
	assert.Equal(t, "success", StatusSuccess)
	assert.Equal(t, "failure", StatusFailure)
}

func TestNewActivityLog(t *testing.T) {
	userID := uuid.New().String()
	email := "test@example.com"

	log := NewActivityLog(userID, email, ActionUserLogin, ModuleAuth, "User logged in")

	assert.Equal(t, userID, log.UserID)
	assert.Equal(t, email, log.UserEmail)
	assert.Equal(t, ActionUserLogin, log.Action)
	assert.Equal(t, ModuleAuth, log.Module)
	assert.Equal(t, "User logged in", log.Description)
	assert.False(t, log.CreatedAt.IsZero())
}

func TestActivityLog_WithStatus(t *testing.T) {
	log := NewActivityLog("1", "test@test.com", ActionUserLogin, ModuleAuth, "test")
	log = log.WithStatus(StatusSuccess)
	assert.Equal(t, StatusSuccess, log.Status)
}

func TestActivityLog_WithIPAndAgent(t *testing.T) {
	log := NewActivityLog("1", "test@test.com", ActionUserLogin, ModuleAuth, "test")
	log = log.WithIPAndAgent("127.0.0.1", "Mozilla/5.0")
	assert.Equal(t, "127.0.0.1", log.IPAddress)
	assert.Equal(t, "Mozilla/5.0", log.UserAgent)
}

func TestActivityLog_WithMetadata(t *testing.T) {
	log := NewActivityLog("1", "test@test.com", ActionUserLogin, ModuleAuth, "test")
	metadata := map[string]interface{}{"key": "value", "count": 42}
	log = log.WithMetadata(metadata)
	assert.Equal(t, "value", log.Metadata["key"])
	assert.Equal(t, 42, log.Metadata["count"])
}

func TestActivityLog_ToResponse(t *testing.T) {
	log := NewActivityLog("user-1", "test@test.com", ActionUserLogin, ModuleAuth, "logged in")
	log = log.WithStatus(StatusSuccess).WithIPAndAgent("10.0.0.1", "Chrome")

	resp := log.ToResponse()
	assert.Equal(t, "user-1", resp.UserID)
	assert.Equal(t, "test@test.com", resp.UserEmail)
	assert.Equal(t, ActionUserLogin, resp.Action)
	assert.Equal(t, ModuleAuth, resp.Module)
	assert.Equal(t, StatusSuccess, resp.Status)
	assert.Equal(t, "10.0.0.1", resp.IPAddress)
}

// ============================================
// Permission response and grouping tests
// ============================================

func TestPermission_ToResponse(t *testing.T) {
	id := uuid.New()
	now := time.Now()
	p := &Permission{
		ID:          id,
		Name:        "users.create",
		DisplayName: "Create Users",
		Module:      ModuleUsers,
		Action:      ActionCreate,
		Description: "Can create new users",
		CreatedAt:   now,
	}

	resp := p.ToResponse()
	assert.Equal(t, id, resp.ID)
	assert.Equal(t, "users.create", resp.Name)
	assert.Equal(t, "Create Users", resp.DisplayName)
	assert.Equal(t, ModuleUsers, resp.Module)
	assert.Equal(t, ActionCreate, resp.Action)
	assert.Equal(t, "Can create new users", resp.Description)
}

func TestGroupPermissionsByModule(t *testing.T) {
	permissions := []Permission{
		{ID: uuid.New(), Name: "users.read", Module: "users", Action: "read"},
		{ID: uuid.New(), Name: "users.create", Module: "users", Action: "create"},
		{ID: uuid.New(), Name: "roles.read", Module: "roles", Action: "read"},
		{ID: uuid.New(), Name: "settings.read", Module: "settings", Action: "read"},
		{ID: uuid.New(), Name: "roles.create", Module: "roles", Action: "create"},
	}

	grouped := GroupPermissionsByModule(permissions)

	assert.Len(t, grouped, 3)
	// Module order should preserve first-seen order
	assert.Equal(t, "users", grouped[0].Module)
	assert.Len(t, grouped[0].Permissions, 2)
	assert.Equal(t, "roles", grouped[1].Module)
	assert.Len(t, grouped[1].Permissions, 2)
	assert.Equal(t, "settings", grouped[2].Module)
	assert.Len(t, grouped[2].Permissions, 1)
}

func TestGroupPermissionsByModule_Empty(t *testing.T) {
	grouped := GroupPermissionsByModule([]Permission{})
	assert.Empty(t, grouped)
}

// ============================================
// Module constants tests
// ============================================

func TestActivityLog_ModuleConstants(t *testing.T) {
	assert.Equal(t, "auth", ModuleAuth)
	assert.Equal(t, "users", ModuleUser)
	assert.Equal(t, "roles", ModuleRole)
	assert.Equal(t, "permissions", ModulePerm)
	assert.Equal(t, "settings", ModuleSetting)
	assert.Equal(t, "system", ModuleSystem)
}
