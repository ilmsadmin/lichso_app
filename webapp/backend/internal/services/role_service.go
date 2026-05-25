package services

import (
	"context"
	"errors"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

// RoleService handles role business logic
type RoleService struct {
	roleRepo       *repositories.RoleRepository
	permissionRepo *repositories.PermissionRepository
	rbacService    *RBACService
	logger         *zap.Logger
	mongoDB        *mongo.Database
}

// NewRoleService creates a new RoleService
func NewRoleService(
	roleRepo *repositories.RoleRepository,
	permissionRepo *repositories.PermissionRepository,
	rbacService *RBACService,
	logger *zap.Logger,
	mongoDB *mongo.Database,
) *RoleService {
	return &RoleService{
		roleRepo:       roleRepo,
		permissionRepo: permissionRepo,
		rbacService:    rbacService,
		logger:         logger,
		mongoDB:        mongoDB,
	}
}

// ListRoles returns all roles with user counts
func (s *RoleService) ListRoles() ([]models.RoleResponse, error) {
	roles, err := s.roleRepo.FindAll()
	if err != nil {
		s.logger.Error("Failed to list roles", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	responses := make([]models.RoleResponse, len(roles))
	for i, role := range roles {
		resp := role.ToResponse()
		count, _ := s.roleRepo.GetUserCount(role.ID)
		resp.UserCount = count
		responses[i] = resp
	}

	return responses, nil
}

// GetRole returns a role by ID with permissions
func (s *RoleService) GetRole(roleID uuid.UUID) (*models.RoleResponse, error) {
	role, err := s.roleRepo.FindByID(roleID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrRoleNotFound
		}
		return nil, utils.ErrDatabaseFail
	}

	resp := role.ToResponse()
	count, _ := s.roleRepo.GetUserCount(role.ID)
	resp.UserCount = count

	return &resp, nil
}

// CreateRole creates a new role
func (s *RoleService) CreateRole(req *dto.CreateRoleRequest, actorID uuid.UUID, actorEmail, ip, ua string) (*models.RoleResponse, error) {
	// Check if name already exists
	exists, err := s.roleRepo.NameExists(req.Name)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}
	if exists {
		return nil, utils.ErrRoleExists
	}

	role := &models.Role{
		ID:          uuid.New(),
		Name:        req.Name,
		DisplayName: req.DisplayName,
		Description: req.Description,
		IsSystem:    false,
		Level:       req.Level,
	}

	if err := s.roleRepo.Create(role); err != nil {
		s.logger.Error("Failed to create role", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Set permissions if provided
	if len(req.PermissionIDs) > 0 {
		if err := s.roleRepo.SetPermissions(role.ID, req.PermissionIDs); err != nil {
			s.logger.Error("Failed to set role permissions", zap.Error(err))
			return nil, utils.ErrDatabaseFail
		}
	}

	// Re-fetch with permissions
	role, _ = s.roleRepo.FindByID(role.ID)

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionRoleCreate, models.ModuleRole,
		"Role created: "+role.Name, models.StatusSuccess, ip, ua)

	resp := role.ToResponse()
	return &resp, nil
}

// UpdateRole updates an existing role
func (s *RoleService) UpdateRole(roleID uuid.UUID, req *dto.UpdateRoleRequest, actorID uuid.UUID, actorEmail, ip, ua string) (*models.RoleResponse, error) {
	role, err := s.roleRepo.FindByID(roleID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrRoleNotFound
		}
		return nil, utils.ErrDatabaseFail
	}

	// Cannot update system role name
	if role.IsSystem && req.Name != "" && req.Name != role.Name {
		return nil, utils.NewAppError(400, "Cannot change the name of a system role")
	}

	// Check name uniqueness if changing name
	if req.Name != "" && req.Name != role.Name {
		exists, err := s.roleRepo.NameExists(req.Name, roleID)
		if err != nil {
			return nil, utils.ErrDatabaseFail
		}
		if exists {
			return nil, utils.ErrRoleExists
		}
		role.Name = req.Name
	}

	if req.DisplayName != "" {
		role.DisplayName = req.DisplayName
	}
	if req.Description != nil {
		role.Description = *req.Description
	}
	if req.Level != nil {
		role.Level = *req.Level
	}

	if err := s.roleRepo.Update(role); err != nil {
		s.logger.Error("Failed to update role", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Update permissions if provided
	if req.PermissionIDs != nil {
		if err := s.roleRepo.SetPermissions(roleID, *req.PermissionIDs); err != nil {
			s.logger.Error("Failed to update role permissions", zap.Error(err))
			return nil, utils.ErrDatabaseFail
		}

		// Invalidate permission cache for all users with this role
		if err := s.rbacService.InvalidateRoleUsersPermissionCache(roleID); err != nil {
			s.logger.Error("Failed to invalidate role users permission cache", zap.Error(err))
		}
	}

	// Re-fetch with permissions
	role, _ = s.roleRepo.FindByID(roleID)

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionRoleUpdate, models.ModuleRole,
		"Role updated: "+role.Name, models.StatusSuccess, ip, ua)

	resp := role.ToResponse()
	return &resp, nil
}

// DeleteRole deletes a role
func (s *RoleService) DeleteRole(roleID uuid.UUID, actorID uuid.UUID, actorEmail, ip, ua string) error {
	role, err := s.roleRepo.FindByID(roleID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return utils.ErrRoleNotFound
		}
		return utils.ErrDatabaseFail
	}

	// Cannot delete system roles
	if role.IsSystem {
		return utils.ErrCannotDeleteSystemRole
	}

	// Check if any users are assigned to this role
	count, _ := s.roleRepo.GetUserCount(roleID)
	if count > 0 {
		return utils.NewAppError(400, "Cannot delete role that is assigned to users. Remove all users from this role first.")
	}

	// Clear permissions first
	_ = s.roleRepo.SetPermissions(roleID, []uuid.UUID{})

	if err := s.roleRepo.Delete(roleID); err != nil {
		s.logger.Error("Failed to delete role", zap.Error(err))
		return utils.ErrDatabaseFail
	}

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionRoleDelete, models.ModuleRole,
		"Role deleted: "+role.Name, models.StatusSuccess, ip, ua)

	return nil
}

// AssignRole assigns a role to a user
func (s *RoleService) AssignRole(userID, roleID uuid.UUID, assignedBy uuid.UUID, actorEmail, ip, ua string) error {
	if err := s.rbacService.AssignRoleToUser(userID, roleID, &assignedBy); err != nil {
		return err
	}

	// Log activity
	s.logActivity(assignedBy.String(), actorEmail, models.ActionRoleAssign, models.ModuleRole,
		"Role assigned to user", models.StatusSuccess, ip, ua)

	return nil
}

// UnassignRole removes a role from a user
func (s *RoleService) UnassignRole(userID, roleID uuid.UUID, actorID uuid.UUID, actorEmail, ip, ua string) error {
	if err := s.rbacService.RemoveRoleFromUser(userID, roleID); err != nil {
		return err
	}

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionRoleUnassign, models.ModuleRole,
		"Role unassigned from user", models.StatusSuccess, ip, ua)

	return nil
}

// logActivity logs an activity to MongoDB
func (s *RoleService) logActivity(userID, email, action, module, description, status, ip, ua string) {
	log := models.NewActivityLog(userID, email, action, module, description).
		WithStatus(status).
		WithIPAndAgent(ip, ua)

	collection := s.mongoDB.Collection(models.ActivityLog{}.CollectionName())
	_, err := collection.InsertOne(context.Background(), log)
	if err != nil {
		s.logger.Error("Failed to log activity",
			zap.String("action", action),
			zap.Error(err),
		)
	}
}
