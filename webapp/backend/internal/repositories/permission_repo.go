package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// PermissionRepository handles database operations for permissions
type PermissionRepository struct {
	db *gorm.DB
}

// NewPermissionRepository creates a new PermissionRepository
func NewPermissionRepository(db *gorm.DB) *PermissionRepository {
	return &PermissionRepository{db: db}
}

// FindAll returns all permissions ordered by module and action
func (r *PermissionRepository) FindAll() ([]models.Permission, error) {
	var permissions []models.Permission
	err := r.db.Order("module ASC, action ASC").Find(&permissions).Error
	return permissions, err
}

// FindByID finds a permission by ID
func (r *PermissionRepository) FindByID(id uuid.UUID) (*models.Permission, error) {
	var permission models.Permission
	err := r.db.Where("id = ?", id).First(&permission).Error
	if err != nil {
		return nil, err
	}
	return &permission, nil
}

// FindByName finds a permission by name
func (r *PermissionRepository) FindByName(name string) (*models.Permission, error) {
	var permission models.Permission
	err := r.db.Where("name = ?", name).First(&permission).Error
	if err != nil {
		return nil, err
	}
	return &permission, nil
}

// FindByModule returns all permissions belonging to a module
func (r *PermissionRepository) FindByModule(module string) ([]models.Permission, error) {
	var permissions []models.Permission
	err := r.db.Where("module = ?", module).Order("action ASC").Find(&permissions).Error
	return permissions, err
}

// FindByIDs returns permissions matching the given IDs
func (r *PermissionRepository) FindByIDs(ids []uuid.UUID) ([]models.Permission, error) {
	var permissions []models.Permission
	err := r.db.Where("id IN ?", ids).Find(&permissions).Error
	return permissions, err
}

// GetAllGrouped returns permissions grouped by module
func (r *PermissionRepository) GetAllGrouped() ([]models.GroupedPermissions, error) {
	permissions, err := r.FindAll()
	if err != nil {
		return nil, err
	}
	return models.GroupPermissionsByModule(permissions), nil
}

// GetModules returns distinct module names
func (r *PermissionRepository) GetModules() ([]string, error) {
	var modules []string
	err := r.db.Model(&models.Permission{}).Distinct().Pluck("module", &modules).Error
	return modules, err
}
