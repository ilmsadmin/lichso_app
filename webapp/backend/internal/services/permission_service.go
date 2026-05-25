package services

import (
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// PermissionService handles permission business logic
type PermissionService struct {
	permissionRepo *repositories.PermissionRepository
	logger         *zap.Logger
}

// NewPermissionService creates a new PermissionService
func NewPermissionService(
	permissionRepo *repositories.PermissionRepository,
	logger *zap.Logger,
) *PermissionService {
	return &PermissionService{
		permissionRepo: permissionRepo,
		logger:         logger,
	}
}

// ListPermissions returns all permissions
func (s *PermissionService) ListPermissions() ([]models.PermissionResponse, error) {
	permissions, err := s.permissionRepo.FindAll()
	if err != nil {
		s.logger.Error("Failed to list permissions", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	responses := make([]models.PermissionResponse, len(permissions))
	for i, perm := range permissions {
		responses[i] = perm.ToResponse()
	}

	return responses, nil
}

// ListPermissionsGrouped returns permissions grouped by module
func (s *PermissionService) ListPermissionsGrouped() ([]models.GroupedPermissions, error) {
	grouped, err := s.permissionRepo.GetAllGrouped()
	if err != nil {
		s.logger.Error("Failed to list grouped permissions", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	return grouped, nil
}

// GetModules returns the list of available permission modules
func (s *PermissionService) GetModules() ([]string, error) {
	modules, err := s.permissionRepo.GetModules()
	if err != nil {
		s.logger.Error("Failed to get modules", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	return modules, nil
}
