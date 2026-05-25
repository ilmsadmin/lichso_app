package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// RoleRepository handles database operations for roles
type RoleRepository struct {
	db *gorm.DB
}

// NewRoleRepository creates a new RoleRepository
func NewRoleRepository(db *gorm.DB) *RoleRepository {
	return &RoleRepository{db: db}
}

// FindAll returns all roles with optional permission preloading
func (r *RoleRepository) FindAll() ([]models.Role, error) {
	var roles []models.Role
	err := r.db.Preload("Permissions").Order("level DESC, name ASC").Find(&roles).Error
	return roles, err
}

// FindByID finds a role by ID with permissions preloaded
func (r *RoleRepository) FindByID(id uuid.UUID) (*models.Role, error) {
	var role models.Role
	err := r.db.Preload("Permissions").Where("id = ?", id).First(&role).Error
	if err != nil {
		return nil, err
	}
	return &role, nil
}

// FindByName finds a role by name
func (r *RoleRepository) FindByName(name string) (*models.Role, error) {
	var role models.Role
	err := r.db.Preload("Permissions").Where("name = ?", name).First(&role).Error
	if err != nil {
		return nil, err
	}
	return &role, nil
}

// Create creates a new role
func (r *RoleRepository) Create(role *models.Role) error {
	return r.db.Create(role).Error
}

// Update updates a role
func (r *RoleRepository) Update(role *models.Role) error {
	return r.db.Save(role).Error
}

// Delete deletes a role by ID
func (r *RoleRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.Role{}).Error
}

// NameExists checks if a role name already exists (excluding the given ID)
func (r *RoleRepository) NameExists(name string, excludeID ...uuid.UUID) (bool, error) {
	var count int64
	query := r.db.Model(&models.Role{}).Where("name = ?", name)
	if len(excludeID) > 0 {
		query = query.Where("id != ?", excludeID[0])
	}
	err := query.Count(&count).Error
	return count > 0, err
}

// GetUserCount returns the number of users assigned to a role
func (r *RoleRepository) GetUserCount(roleID uuid.UUID) (int64, error) {
	var count int64
	err := r.db.Model(&models.UserRole{}).Where("role_id = ?", roleID).Count(&count).Error
	return count, err
}

// SetPermissions replaces all permissions for a role
func (r *RoleRepository) SetPermissions(roleID uuid.UUID, permissionIDs []uuid.UUID) error {
	return r.db.Transaction(func(tx *gorm.DB) error {
		// Remove existing role_permissions
		if err := tx.Where("role_id = ?", roleID).Delete(&models.RolePermission{}).Error; err != nil {
			return err
		}

		// Insert new role_permissions
		for _, permID := range permissionIDs {
			rp := models.RolePermission{
				RoleID:       roleID,
				PermissionID: permID,
			}
			if err := tx.Create(&rp).Error; err != nil {
				return err
			}
		}
		return nil
	})
}

// GetRolePermissionIDs returns all permission IDs for a role
func (r *RoleRepository) GetRolePermissionIDs(roleID uuid.UUID) ([]uuid.UUID, error) {
	var ids []uuid.UUID
	err := r.db.Model(&models.RolePermission{}).
		Where("role_id = ?", roleID).
		Pluck("permission_id", &ids).Error
	return ids, err
}

// AssignUserRole assigns a role to a user
func (r *RoleRepository) AssignUserRole(userID, roleID uuid.UUID, assignedBy *uuid.UUID) error {
	userRole := models.UserRole{
		UserID:     userID,
		RoleID:     roleID,
		AssignedBy: assignedBy,
	}
	return r.db.Create(&userRole).Error
}

// RemoveUserRole removes a role from a user
func (r *RoleRepository) RemoveUserRole(userID, roleID uuid.UUID) error {
	return r.db.Where("user_id = ? AND role_id = ?", userID, roleID).
		Delete(&models.UserRole{}).Error
}

// UserHasRole checks if a user has a specific role
func (r *RoleRepository) UserHasRole(userID, roleID uuid.UUID) (bool, error) {
	var count int64
	err := r.db.Model(&models.UserRole{}).
		Where("user_id = ? AND role_id = ?", userID, roleID).
		Count(&count).Error
	return count > 0, err
}

// GetUsersByRoleID returns all user IDs assigned to a given role
func (r *RoleRepository) GetUsersByRoleID(roleID uuid.UUID) ([]uuid.UUID, error) {
	var ids []uuid.UUID
	err := r.db.Model(&models.UserRole{}).
		Where("role_id = ?", roleID).
		Pluck("user_id", &ids).Error
	return ids, err
}

// CountAll returns the total number of roles
func (r *RoleRepository) CountAll() (int64, error) {
	var count int64
	err := r.db.Model(&models.Role{}).Count(&count).Error
	return count, err
}
