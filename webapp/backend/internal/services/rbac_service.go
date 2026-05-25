package services

import (
	"errors"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

// RBACService handles role-based access control logic
type RBACService struct {
	userRepo       *repositories.UserRepository
	roleRepo       *repositories.RoleRepository
	permissionRepo *repositories.PermissionRepository
	cacheService   *CacheService
	logger         *zap.Logger
}

// NewRBACService creates a new RBACService
func NewRBACService(
	userRepo *repositories.UserRepository,
	roleRepo *repositories.RoleRepository,
	permissionRepo *repositories.PermissionRepository,
	cacheService *CacheService,
	logger *zap.Logger,
) *RBACService {
	return &RBACService{
		userRepo:       userRepo,
		roleRepo:       roleRepo,
		permissionRepo: permissionRepo,
		cacheService:   cacheService,
		logger:         logger,
	}
}

// CheckUserHasPermission checks if a user has a specific permission
// Super admin always bypasses permission checks
func (s *RBACService) CheckUserHasPermission(userID uuid.UUID, permission string) (bool, error) {
	// Check if user is super admin (bypass)
	if isSuperAdmin, err := s.IsUserSuperAdmin(userID); err == nil && isSuperAdmin {
		return true, nil
	}

	// First try cache
	hasPerm, err := s.cacheService.HasPermission(userID, permission)
	if err == nil && hasPerm {
		return true, nil
	}

	// Fallback to DB and refresh cache
	permissions, err := s.userRepo.GetUserPermissions(userID)
	if err != nil {
		return false, err
	}

	// Cache the permissions for next time
	_ = s.cacheService.CacheUserPermissions(userID, permissions)

	for _, p := range permissions {
		if p == permission {
			return true, nil
		}
	}
	return false, nil
}

// CheckUserHasRole checks if a user has a specific role
func (s *RBACService) CheckUserHasRole(userID uuid.UUID, roleName string) (bool, error) {
	roleNames, err := s.userRepo.GetUserRoleNames(userID)
	if err != nil {
		return false, err
	}

	for _, r := range roleNames {
		if r == roleName {
			return true, nil
		}
	}
	return false, nil
}

// GetAllPermissionsOfUser returns all permission names for a user through their roles
func (s *RBACService) GetAllPermissionsOfUser(userID uuid.UUID) ([]string, error) {
	// Check super admin first
	if isSuperAdmin, err := s.IsUserSuperAdmin(userID); err == nil && isSuperAdmin {
		// Return all permissions for super admin
		allPerms, err := s.permissionRepo.FindAll()
		if err != nil {
			return nil, err
		}
		names := make([]string, len(allPerms))
		for i, p := range allPerms {
			names[i] = p.Name
		}
		return names, nil
	}

	// Try cache first
	cached, err := s.cacheService.GetUserPermissions(userID)
	if err == nil && len(cached) > 0 {
		return cached, nil
	}

	// Fallback to DB
	permissions, err := s.userRepo.GetUserPermissions(userID)
	if err != nil {
		return nil, err
	}

	// Cache for future use
	_ = s.cacheService.CacheUserPermissions(userID, permissions)

	return permissions, nil
}

// IsUserSuperAdmin checks if the user has super_admin role
func (s *RBACService) IsUserSuperAdmin(userID uuid.UUID) (bool, error) {
	return s.CheckUserHasRole(userID, models.RoleSuperAdmin)
}

// InvalidateUserPermissionCache invalidates the permission cache for a user
func (s *RBACService) InvalidateUserPermissionCache(userID uuid.UUID) error {
	return s.cacheService.InvalidatePermissionCache(userID)
}

// InvalidateRoleUsersPermissionCache invalidates permission cache for all users of a role
func (s *RBACService) InvalidateRoleUsersPermissionCache(roleID uuid.UUID) error {
	userIDs, err := s.roleRepo.GetUsersByRoleID(roleID)
	if err != nil {
		return err
	}

	for _, uid := range userIDs {
		if err := s.cacheService.InvalidatePermissionCache(uid); err != nil {
			s.logger.Error("Failed to invalidate permission cache",
				zap.String("user_id", uid.String()),
				zap.Error(err),
			)
		}
	}
	return nil
}

// AssignRoleToUser assigns a role to a user and invalidates cache
func (s *RBACService) AssignRoleToUser(userID, roleID uuid.UUID, assignedBy *uuid.UUID) error {
	// Check if role exists
	_, err := s.roleRepo.FindByID(roleID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return utils.ErrRoleNotFound
		}
		return utils.ErrDatabaseFail
	}

	// Check if already assigned
	hasRole, err := s.roleRepo.UserHasRole(userID, roleID)
	if err != nil {
		return utils.ErrDatabaseFail
	}
	if hasRole {
		return utils.NewAppError(409, "User already has this role")
	}

	// Assign
	if err := s.roleRepo.AssignUserRole(userID, roleID, assignedBy); err != nil {
		return utils.ErrDatabaseFail
	}

	// Invalidate cache
	_ = s.InvalidateUserPermissionCache(userID)

	return nil
}

// RemoveRoleFromUser removes a role from a user and invalidates cache
func (s *RBACService) RemoveRoleFromUser(userID, roleID uuid.UUID) error {
	// Check if actually assigned
	hasRole, err := s.roleRepo.UserHasRole(userID, roleID)
	if err != nil {
		return utils.ErrDatabaseFail
	}
	if !hasRole {
		return utils.NewAppError(404, "User does not have this role")
	}

	// Remove
	if err := s.roleRepo.RemoveUserRole(userID, roleID); err != nil {
		return utils.ErrDatabaseFail
	}

	// Invalidate cache
	_ = s.InvalidateUserPermissionCache(userID)

	return nil
}
