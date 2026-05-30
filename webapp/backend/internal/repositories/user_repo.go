package repositories

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/utils"
	"gorm.io/gorm"
)

// UserRepository handles database operations for users
type UserRepository struct {
	db *gorm.DB
}

// NewUserRepository creates a new UserRepository
func NewUserRepository(db *gorm.DB) *UserRepository {
	return &UserRepository{db: db}
}

// GetDB returns the underlying GORM database instance
func (r *UserRepository) GetDB() *gorm.DB {
	return r.db
}

// FindByID finds a user by ID with roles preloaded
func (r *UserRepository) FindByID(id uuid.UUID) (*models.User, error) {
	var user models.User
	err := r.db.Preload("Roles").Where("id = ?", id).First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// FindByEmail finds a user by email with roles preloaded
func (r *UserRepository) FindByEmail(email string) (*models.User, error) {
	var user models.User
	err := r.db.Preload("Roles").Where("email = ?", email).First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// FindByProvider finds a user by provider and provider_id with roles preloaded
func (r *UserRepository) FindByProvider(provider, providerID string) (*models.User, error) {
	var user models.User
	err := r.db.Preload("Roles").Where("provider = ? AND provider_id = ?", provider, providerID).First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// Create creates a new user
func (r *UserRepository) Create(user *models.User) error {
	return r.db.Create(user).Error
}

// Update updates a user
func (r *UserRepository) Update(user *models.User) error {
	return r.db.Save(user).Error
}

// UpdateLastLogin updates the last login timestamp
func (r *UserRepository) UpdateLastLogin(userID uuid.UUID) error {
	return r.db.Model(&models.User{}).Where("id = ?", userID).
		Update("last_login", gorm.Expr("NOW()")).Error
}

// UpdatePassword updates a user's password
func (r *UserRepository) UpdatePassword(userID uuid.UUID, hashedPassword string) error {
	return r.db.Model(&models.User{}).Where("id = ?", userID).
		Updates(map[string]interface{}{
			"password":   hashedPassword,
			"updated_at": gorm.Expr("NOW()"),
		}).Error
}

// EmailExists checks if an email already exists
func (r *UserRepository) EmailExists(email string) (bool, error) {
	var count int64
	err := r.db.Model(&models.User{}).Where("email = ?", email).Count(&count).Error
	return count > 0, err
}

// GetUserRoleNames returns the role names for a user
func (r *UserRepository) GetUserRoleNames(userID uuid.UUID) ([]string, error) {
	var roles []models.Role
	err := r.db.Table("roles").
		Joins("JOIN user_roles ON user_roles.role_id = roles.id").
		Where("user_roles.user_id = ?", userID).
		Select("roles.name").
		Find(&roles).Error
	if err != nil {
		return nil, err
	}

	names := make([]string, len(roles))
	for i, role := range roles {
		names[i] = role.Name
	}
	return names, nil
}

// GetUserPermissions returns all permission names for a user through their roles
func (r *UserRepository) GetUserPermissions(userID uuid.UUID) ([]string, error) {
	var permissions []string
	err := r.db.Table("permissions").
		Joins("JOIN role_permissions ON role_permissions.permission_id = permissions.id").
		Joins("JOIN user_roles ON user_roles.role_id = role_permissions.role_id").
		Where("user_roles.user_id = ?", userID).
		Distinct().
		Pluck("permissions.name", &permissions).Error
	return permissions, err
}

// AssignRole assigns a role to a user
func (r *UserRepository) AssignRole(userID, roleID uuid.UUID, assignedBy *uuid.UUID) error {
	userRole := models.UserRole{
		UserID:     userID,
		RoleID:     roleID,
		AssignedBy: assignedBy,
	}
	return r.db.Create(&userRole).Error
}

// SaveRefreshToken stores a refresh token in the database
func (r *UserRepository) SaveRefreshToken(token *models.RefreshToken) error {
	return r.db.Create(token).Error
}

// FindRefreshToken finds a refresh token by its token string
func (r *UserRepository) FindRefreshToken(token string) (*models.RefreshToken, error) {
	var rt models.RefreshToken
	err := r.db.Where("token = ? AND revoked_at IS NULL", token).First(&rt).Error
	if err != nil {
		return nil, err
	}
	return &rt, nil
}

// RevokeRefreshToken revokes a refresh token
func (r *UserRepository) RevokeRefreshToken(tokenID uuid.UUID) error {
	return r.db.Model(&models.RefreshToken{}).Where("id = ?", tokenID).
		Update("revoked_at", gorm.Expr("NOW()")).Error
}

// RevokeAllUserRefreshTokens revokes all refresh tokens for a user
func (r *UserRepository) RevokeAllUserRefreshTokens(userID uuid.UUID) error {
	return r.db.Model(&models.RefreshToken{}).
		Where("user_id = ? AND revoked_at IS NULL", userID).
		Update("revoked_at", gorm.Expr("NOW()")).Error
}

// FindRoleByName finds a role by name
func (r *UserRepository) FindRoleByName(name string, role *models.Role) error {
	return r.db.Where("name = ?", name).First(role).Error
}

// FindAllPaginated returns users with pagination, search, filter, and sorting
func (r *UserRepository) FindAllPaginated(pq utils.PaginationQuery) ([]models.User, int64, error) {
	var users []models.User
	var total int64

	query := r.db.Model(&models.User{})

	// Search by email, first_name, last_name
	if pq.Search != "" {
		search := "%" + pq.Search + "%"
		query = query.Where("email ILIKE ? OR first_name ILIKE ? OR last_name ILIKE ?", search, search, search)
	}

	// Filter by active status
	switch pq.Status {
	case "active":
		query = query.Where("is_active = ?", true)
	case "inactive":
		query = query.Where("is_active = ?", false)
	}

	// Filter by role
	if pq.Role != "" {
		query = query.Where("id IN (SELECT user_id FROM user_roles JOIN roles ON roles.id = user_roles.role_id WHERE roles.name = ?)", pq.Role)
	}

	// Filter by device status
	switch pq.HasDevice {
	case "yes":
		query = query.Where("EXISTS (SELECT 1 FROM device_tokens dt WHERE dt.user_id = users.id AND dt.is_active = true AND dt.deleted_at IS NULL)")
	case "no":
		query = query.Where("NOT EXISTS (SELECT 1 FROM device_tokens dt WHERE dt.user_id = users.id AND dt.is_active = true AND dt.deleted_at IS NULL)")
	}

	// Count total
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Sort and paginate
	orderClause := fmt.Sprintf("%s %s", pq.SortBy, pq.SortOrder)
	err := query.Preload("Roles").
		Order(orderClause).
		Offset(pq.Offset()).
		Limit(pq.Limit).
		Find(&users).Error

	return users, total, err
}

// adminUserRow is a scan target for the enriched admin list query.
type adminUserRow struct {
	models.User
	DeviceCount   int    `gorm:"column:device_count"`
	Platforms     string `gorm:"column:platforms"`
	LatestVersion string `gorm:"column:latest_version"`
}

// FindAllPaginatedAdmin returns enriched user rows for the admin user table.
// Each row includes device count, platforms (android/ios), and latest app version.
func (r *UserRepository) FindAllPaginatedAdmin(pq utils.PaginationQuery) ([]models.UserAdminListItem, int64, error) {
	var total int64

	// Build base WHERE clause conditions
	whereClause := "u.deleted_at IS NULL"
	args := []interface{}{}

	if pq.Search != "" {
		s := "%" + pq.Search + "%"
		whereClause += " AND (u.email ILIKE ? OR u.first_name ILIKE ? OR u.last_name ILIKE ? OR u.phone ILIKE ?)"
		args = append(args, s, s, s, s)
	}
	switch pq.Status {
	case "active":
		whereClause += " AND u.is_active = true"
	case "inactive":
		whereClause += " AND u.is_active = false"
	}
	if pq.Role != "" {
		whereClause += " AND u.id IN (SELECT ur.user_id FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE r.name = ?)"
		args = append(args, pq.Role)
	}
	switch pq.HasDevice {
	case "yes":
		whereClause += " AND EXISTS (SELECT 1 FROM device_tokens dt WHERE dt.user_id = u.id AND dt.is_active = true AND dt.deleted_at IS NULL)"
	case "no":
		whereClause += " AND NOT EXISTS (SELECT 1 FROM device_tokens dt WHERE dt.user_id = u.id AND dt.is_active = true AND dt.deleted_at IS NULL)"
	}

	countSQL := "SELECT COUNT(*) FROM users u WHERE " + whereClause
	if err := r.db.Raw(countSQL, args...).Scan(&total).Error; err != nil {
		return nil, 0, err
	}

	orderClause := fmt.Sprintf("u.%s %s", pq.SortBy, pq.SortOrder)
	listSQL := fmt.Sprintf(`
		SELECT u.*,
			COALESCE(d.device_count, 0)  AS device_count,
			COALESCE(d.platforms, '')     AS platforms,
			COALESCE(d.latest_version,'') AS latest_version
		FROM users u
		LEFT JOIN (
			SELECT user_id,
				COUNT(*)                                    AS device_count,
				STRING_AGG(DISTINCT platform, ',')          AS platforms,
				MAX(app_version)                            AS latest_version
			FROM device_tokens
			WHERE is_active = true AND deleted_at IS NULL AND user_id IS NOT NULL
			GROUP BY user_id
		) d ON d.user_id = u.id
		WHERE %s
		ORDER BY %s
		LIMIT ? OFFSET ?
	`, whereClause, orderClause)

	queryArgs := append(args, pq.Limit, pq.Offset())
	var rows []adminUserRow
	if err := r.db.Raw(listSQL, queryArgs...).Scan(&rows).Error; err != nil {
		return nil, 0, err
	}

	if len(rows) == 0 {
		return []models.UserAdminListItem{}, total, nil
	}

	// Batch-load roles
	userIDs := make([]uuid.UUID, len(rows))
	for i, row := range rows {
		userIDs[i] = row.User.ID
	}

	type roleRow struct {
		UserID      uuid.UUID `gorm:"column:user_id"`
		RoleID      uuid.UUID `gorm:"column:role_id"`
		Name        string    `gorm:"column:name"`
		DisplayName string    `gorm:"column:display_name"`
	}
	var roleRows []roleRow
	r.db.Raw(`
		SELECT ur.user_id, r.id as role_id, r.name, r.display_name
		FROM user_roles ur JOIN roles r ON r.id = ur.role_id
		WHERE ur.user_id IN ?
	`, userIDs).Scan(&roleRows)

	rolesByUser := make(map[uuid.UUID][]models.RoleBrief)
	for _, rr := range roleRows {
		rolesByUser[rr.UserID] = append(rolesByUser[rr.UserID], models.RoleBrief{
			ID:          rr.RoleID,
			Name:        rr.Name,
			DisplayName: rr.DisplayName,
		})
	}

	items := make([]models.UserAdminListItem, len(rows))
	for i, row := range rows {
		resp := row.User.ToResponse()
		resp.Roles = rolesByUser[row.User.ID]
		items[i] = models.UserAdminListItem{
			UserResponse:  resp,
			DeviceCount:   row.DeviceCount,
			Platforms:     row.Platforms,
			LatestVersion: row.LatestVersion,
		}
	}
	return items, total, nil
}

// FindUserDetailStats returns aggregated stats for a single user.
func (r *UserRepository) FindUserDetailStats(userID uuid.UUID) (models.UserStats, error) {
	var stats models.UserStats
	r.db.Raw(`
		SELECT
			(SELECT COUNT(*) FROM bookmarks  WHERE user_id = ? AND deleted_at IS NULL) AS bookmark_count,
			(SELECT COUNT(*) FROM user_notes WHERE user_id = ? AND deleted_at IS NULL) AS note_count,
			(SELECT COUNT(*) FROM device_tokens WHERE user_id = ? AND is_active = true AND deleted_at IS NULL) AS device_count,
			COALESCE((SELECT current_streak FROM user_streaks WHERE user_id = ? LIMIT 1), 0) AS streak_days,
			COALESCE((SELECT balance FROM point_wallets WHERE user_id = ? LIMIT 1), 0) AS points
	`, userID, userID, userID, userID, userID).Scan(&stats)
	return stats, nil
}

// FindByIDWithRoles finds a user by ID with roles preloaded (including soft-deleted check)
func (r *UserRepository) FindByIDWithRoles(id uuid.UUID) (*models.User, error) {
	var user models.User
	err := r.db.Preload("Roles").Where("id = ?", id).First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// SoftDelete soft-deletes a user
func (r *UserRepository) SoftDelete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.User{}).Error
}

// ToggleActive toggles the is_active status of a user
func (r *UserRepository) ToggleActive(id uuid.UUID, isActive bool) error {
	return r.db.Model(&models.User{}).Where("id = ?", id).
		Updates(map[string]interface{}{
			"is_active":  isActive,
			"updated_at": gorm.Expr("NOW()"),
		}).Error
}

// RemoveAllUserRoles removes all roles from a user
func (r *UserRepository) RemoveAllUserRoles(userID uuid.UUID) error {
	return r.db.Where("user_id = ?", userID).Delete(&models.UserRole{}).Error
}

// EmailExistsExcluding checks if an email already exists, excluding a specific user
func (r *UserRepository) EmailExistsExcluding(email string, excludeID uuid.UUID) (bool, error) {
	var count int64
	err := r.db.Model(&models.User{}).Where("email = ? AND id != ?", email, excludeID).Count(&count).Error
	return count > 0, err
}

// CountAll returns total user count
func (r *UserRepository) CountAll() (int64, error) {
	var count int64
	err := r.db.Model(&models.User{}).Count(&count).Error
	return count, err
}

// CountActive returns active user count
func (r *UserRepository) CountActive() (int64, error) {
	var count int64
	err := r.db.Model(&models.User{}).Where("is_active = ?", true).Count(&count).Error
	return count, err
}

// CountCreatedSince returns the number of users created since the given time
func (r *UserRepository) CountCreatedSince(since time.Time) (int64, error) {
	var count int64
	err := r.db.Model(&models.User{}).Where("created_at >= ?", since).Count(&count).Error
	return count, err
}
