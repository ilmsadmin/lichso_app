package services

import (
	"bytes"
	"context"
	"encoding/csv"
	"errors"
	"fmt"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

// UserService handles user management business logic
type UserService struct {
	userRepo        *repositories.UserRepository
	roleRepo        *repositories.RoleRepository
	deviceTokenRepo *repositories.DeviceTokenRepository
	rbacService     *RBACService
	cacheService    *CacheService
	logger          *zap.Logger
	mongoDB         *mongo.Database
}

// NewUserService creates a new UserService
func NewUserService(
	userRepo *repositories.UserRepository,
	roleRepo *repositories.RoleRepository,
	rbacService *RBACService,
	cacheService *CacheService,
	logger *zap.Logger,
	mongoDB *mongo.Database,
) *UserService {
	return &UserService{
		userRepo:     userRepo,
		roleRepo:     roleRepo,
		rbacService:  rbacService,
		cacheService: cacheService,
		logger:       logger,
		mongoDB:      mongoDB,
	}
}

// SetDeviceTokenRepo wires the device token repository (called after push notification setup).
func (s *UserService) SetDeviceTokenRepo(repo *repositories.DeviceTokenRepository) {
	s.deviceTokenRepo = repo
}

// ListUsers returns paginated enriched user rows for the admin user table.
func (s *UserService) ListUsers(pq utils.PaginationQuery) ([]models.UserAdminListItem, int64, error) {
	items, total, err := s.userRepo.FindAllPaginatedAdmin(pq)
	if err != nil {
		s.logger.Error("Failed to list users (admin)", zap.Error(err))
		return nil, 0, utils.ErrDatabaseFail
	}
	return items, total, nil
}

// GetUser returns a user by ID with roles
func (s *UserService) GetUser(userID uuid.UUID) (*models.UserResponse, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrUserNotFound
		}
		return nil, utils.ErrDatabaseFail
	}

	resp := user.ToResponse()
	return &resp, nil
}

// GetUserAdminDetail returns the fully enriched user detail for admin views.
func (s *UserService) GetUserAdminDetail(userID uuid.UUID) (*models.UserAdminDetail, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrUserNotFound
		}
		return nil, utils.ErrDatabaseFail
	}

	// Device tokens
	deviceTokens, _ := s.deviceTokenRepo.GetByUserID(userID)
	devices := make([]models.DeviceTokenBrief, len(deviceTokens))
	for i, dt := range deviceTokens {
		devices[i] = models.DeviceTokenBrief{
			Platform:   dt.Platform,
			AppVersion: dt.AppVersion,
			DeviceID:   dt.DeviceID,
			DeviceName: dt.DeviceName,
			LastSeen:   dt.LastSeen,
			CreatedAt:  dt.CreatedAt,
		}
	}

	// Aggregated stats (single SQL round-trip)
	stats, _ := s.userRepo.FindUserDetailStats(userID)

	return &models.UserAdminDetail{
		UserResponse: user.ToResponse(),
		ProviderID:   user.ProviderID,
		Devices:      devices,
		Stats:        stats,
	}, nil
}

// CreateUser creates a new user with optional role assignment (admin action)
func (s *UserService) CreateUser(req *dto.CreateUserRequest, actorID uuid.UUID, actorEmail, ip, ua string) (*models.UserResponse, error) {
	// Check if email exists
	exists, err := s.userRepo.EmailExists(req.Email)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}
	if exists {
		return nil, utils.ErrEmailExists
	}

	// Hash password
	hashedPassword, err := utils.HashPassword(req.Password)
	if err != nil {
		s.logger.Error("Failed to hash password", zap.Error(err))
		return nil, utils.ErrInternal
	}

	isActive := true
	if req.IsActive != nil {
		isActive = *req.IsActive
	}

	user := &models.User{
		ID:        uuid.New(),
		Email:     req.Email,
		Password:  hashedPassword,
		FirstName: req.FirstName,
		LastName:  req.LastName,
		Phone:     req.Phone,
		IsActive:  isActive,
	}

	if err := s.userRepo.Create(user); err != nil {
		s.logger.Error("Failed to create user", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Assign roles if provided
	if len(req.RoleIDs) > 0 {
		for _, roleID := range req.RoleIDs {
			_ = s.userRepo.AssignRole(user.ID, roleID, &actorID)
		}
	} else {
		// Assign default viewer role if no roles specified
		var viewerRole models.Role
		if err := s.userRepo.FindRoleByName(models.RoleViewer, &viewerRole); err == nil {
			_ = s.userRepo.AssignRole(user.ID, viewerRole.ID, &actorID)
		}
	}

	// Re-fetch with roles
	user, _ = s.userRepo.FindByID(user.ID)

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionUserCreate, models.ModuleUser,
		fmt.Sprintf("User created: %s (%s)", user.FullName(), user.Email), models.StatusSuccess, ip, ua)

	resp := user.ToResponse()
	return &resp, nil
}

// UpdateUser updates a user's information
func (s *UserService) UpdateUser(userID uuid.UUID, req *dto.UpdateUserRequest, actorID uuid.UUID, actorEmail, ip, ua string) (*models.UserResponse, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrUserNotFound
		}
		return nil, utils.ErrDatabaseFail
	}

	// Check email uniqueness if changing email
	if req.Email != "" && req.Email != user.Email {
		exists, err := s.userRepo.EmailExistsExcluding(req.Email, userID)
		if err != nil {
			return nil, utils.ErrDatabaseFail
		}
		if exists {
			return nil, utils.ErrEmailExists
		}
		user.Email = req.Email
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
	if req.IsActive != nil {
		user.IsActive = *req.IsActive
	}

	// Update password if provided
	if req.Password != nil && *req.Password != "" {
		hashedPassword, err := utils.HashPassword(*req.Password)
		if err != nil {
			s.logger.Error("Failed to hash password", zap.Error(err))
			return nil, utils.ErrInternal
		}
		user.Password = hashedPassword
	}

	if err := s.userRepo.Update(user); err != nil {
		s.logger.Error("Failed to update user", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Re-fetch with roles
	user, _ = s.userRepo.FindByID(userID)

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionUserUpdate, models.ModuleUser,
		fmt.Sprintf("User updated: %s (%s)", user.FullName(), user.Email), models.StatusSuccess, ip, ua)

	resp := user.ToResponse()
	return &resp, nil
}

// DeleteUser soft-deletes a user
func (s *UserService) DeleteUser(userID, actorID uuid.UUID, actorEmail, ip, ua string) error {
	// Cannot delete yourself
	if userID == actorID {
		return utils.ErrCannotDeleteSelf
	}

	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return utils.ErrUserNotFound
		}
		return utils.ErrDatabaseFail
	}

	// Cannot delete super_admin
	if user.IsSuperAdmin() {
		return utils.NewAppError(400, "Cannot delete a super admin user")
	}

	// Soft delete
	if err := s.userRepo.SoftDelete(userID); err != nil {
		s.logger.Error("Failed to delete user", zap.Error(err))
		return utils.ErrDatabaseFail
	}

	// Invalidate cache
	_ = s.cacheService.InvalidatePermissionCache(userID)
	_ = s.cacheService.ClearUserSession(userID)

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionUserDelete, models.ModuleUser,
		fmt.Sprintf("User deleted: %s (%s)", user.FullName(), user.Email), models.StatusSuccess, ip, ua)

	return nil
}

// ToggleUserStatus toggles a user's active status
func (s *UserService) ToggleUserStatus(userID uuid.UUID, isActive bool, actorID uuid.UUID, actorEmail, ip, ua string) (*models.UserResponse, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrUserNotFound
		}
		return nil, utils.ErrDatabaseFail
	}

	if err := s.userRepo.ToggleActive(userID, isActive); err != nil {
		s.logger.Error("Failed to toggle user status", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	action := models.ActionUserActivate
	desc := fmt.Sprintf("User activated: %s", user.Email)
	if !isActive {
		action = models.ActionUserDeactivate
		desc = fmt.Sprintf("User deactivated: %s", user.Email)
	}

	// Log activity
	s.logActivity(actorID.String(), actorEmail, action, models.ModuleUser,
		desc, models.StatusSuccess, ip, ua)

	// Re-fetch
	user, _ = s.userRepo.FindByID(userID)
	resp := user.ToResponse()
	return &resp, nil
}

// SetUserRoles replaces all roles for a user
func (s *UserService) SetUserRoles(userID uuid.UUID, roleIDs []uuid.UUID, actorID uuid.UUID, actorEmail, ip, ua string) (*models.UserResponse, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrUserNotFound
		}
		return nil, utils.ErrDatabaseFail
	}

	// Validate role IDs exist
	for _, roleID := range roleIDs {
		_, err := s.roleRepo.FindByID(roleID)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return nil, utils.NewAppError(400, fmt.Sprintf("Role %s not found", roleID.String()))
			}
			return nil, utils.ErrDatabaseFail
		}
	}

	// Remove all existing roles
	if err := s.userRepo.RemoveAllUserRoles(userID); err != nil {
		s.logger.Error("Failed to remove user roles", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Assign new roles
	for _, roleID := range roleIDs {
		if err := s.userRepo.AssignRole(userID, roleID, &actorID); err != nil {
			s.logger.Error("Failed to assign role", zap.Error(err))
		}
	}

	// Invalidate permission cache
	_ = s.rbacService.InvalidateUserPermissionCache(userID)

	// Log activity
	s.logActivity(actorID.String(), actorEmail, models.ActionRoleAssign, models.ModuleUser,
		fmt.Sprintf("Roles updated for user: %s (%s)", user.FullName(), user.Email), models.StatusSuccess, ip, ua)

	// Re-fetch
	user, _ = s.userRepo.FindByID(userID)
	resp := user.ToResponse()
	return &resp, nil
}

// ExportUsersCSV exports users to CSV format
func (s *UserService) ExportUsersCSV(pq utils.PaginationQuery) ([]byte, error) {
	// Override limit for export - get all matching users
	pq.Limit = 10000
	pq.Page = 1

	users, _, err := s.userRepo.FindAllPaginated(pq)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}

	var buf bytes.Buffer
	writer := csv.NewWriter(&buf)

	// Write header
	header := []string{"ID", "Email", "First Name", "Last Name", "Phone", "Active", "Roles", "Created At", "Last Login"}
	if err := writer.Write(header); err != nil {
		return nil, utils.ErrInternal
	}

	// Write rows
	for _, user := range users {
		roles := ""
		for i, role := range user.Roles {
			if i > 0 {
				roles += ", "
			}
			roles += role.DisplayName
		}

		lastLogin := ""
		if user.LastLogin != nil {
			lastLogin = user.LastLogin.Format("2006-01-02 15:04:05")
		}

		active := "Yes"
		if !user.IsActive {
			active = "No"
		}

		row := []string{
			user.ID.String(),
			user.Email,
			user.FirstName,
			user.LastName,
			user.Phone,
			active,
			roles,
			user.CreatedAt.Format("2006-01-02 15:04:05"),
			lastLogin,
		}
		if err := writer.Write(row); err != nil {
			return nil, utils.ErrInternal
		}
	}

	writer.Flush()
	if err := writer.Error(); err != nil {
		return nil, utils.ErrInternal
	}

	return buf.Bytes(), nil
}

// logActivity logs an activity to MongoDB
func (s *UserService) logActivity(userID, email, action, module, description, status, ip, ua string) {
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
