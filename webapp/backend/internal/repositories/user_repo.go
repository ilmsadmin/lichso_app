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

// MergeGuestInto reassigns a guest user's data to a destination account in a
// single transaction. Used when a returning Google account already exists and a
// separate guest row was created on a new device. Multi-row tables are simply
// reassigned; composite-unique tables move only non-conflicting rows; aggregate
// single-row tables (scores/wallet/streak) are summed into the destination.
func (r *UserRepository) MergeGuestInto(srcID, dstID uuid.UUID) error {
	if srcID == dstID {
		return nil
	}
	return r.db.Transaction(func(tx *gorm.DB) error {
		// Plain reassign — only keyed by user_id, no other per-user uniqueness.
		plain := []string{
			"device_tokens", "bookmarks", "user_notes", "user_countdowns",
			"reminders", "refresh_tokens", "quiz_sessions", "quiz_assist_usages",
			"point_transactions", "user_achievements", "newsletter_subscribers",
		}
		for _, table := range plain {
			if err := tx.Exec(
				fmt.Sprintf("UPDATE %s SET user_id = ? WHERE user_id = ?", table), dstID, srcID,
			).Error; err != nil {
				return fmt.Errorf("merge %s: %w", table, err)
			}
		}

		// Composite-unique (user_id + key): move rows the destination doesn't
		// already have, then drop the guest's leftovers.
		composite := []struct{ table, key string }{
			{"quiz_category_masteries", "category"},
			{"user_badges", "badge_key"},
		}
		for _, c := range composite {
			move := fmt.Sprintf(
				"UPDATE %[1]s s SET user_id = ? WHERE s.user_id = ? AND NOT EXISTS "+
					"(SELECT 1 FROM %[1]s d WHERE d.user_id = ? AND d.%[2]s = s.%[2]s)",
				c.table, c.key)
			if err := tx.Exec(move, dstID, srcID, dstID).Error; err != nil {
				return fmt.Errorf("merge %s: %w", c.table, err)
			}
			if err := tx.Exec(
				fmt.Sprintf("DELETE FROM %s WHERE user_id = ?", c.table), srcID,
			).Error; err != nil {
				return fmt.Errorf("cleanup %s: %w", c.table, err)
			}
		}

		// Aggregate single-row tables: insert-or-sum into the destination.
		if err := tx.Exec(`
			INSERT INTO quiz_scores (user_id, display_name, avatar_url, total_score, week_score, month_score, best_streak, cur_streak, xp, updated_at)
			SELECT ?, display_name, avatar_url, total_score, week_score, month_score, best_streak, cur_streak, xp, NOW()
			FROM quiz_scores WHERE user_id = ?
			ON CONFLICT (user_id) DO UPDATE SET
				total_score = quiz_scores.total_score + EXCLUDED.total_score,
				week_score  = quiz_scores.week_score  + EXCLUDED.week_score,
				month_score = quiz_scores.month_score + EXCLUDED.month_score,
				best_streak = GREATEST(quiz_scores.best_streak, EXCLUDED.best_streak),
				cur_streak  = GREATEST(quiz_scores.cur_streak, EXCLUDED.cur_streak),
				xp          = quiz_scores.xp + EXCLUDED.xp,
				updated_at  = NOW()`, dstID, srcID).Error; err != nil {
			return fmt.Errorf("merge quiz_scores: %w", err)
		}
		if err := tx.Exec("DELETE FROM quiz_scores WHERE user_id = ?", srcID).Error; err != nil {
			return fmt.Errorf("cleanup quiz_scores: %w", err)
		}

		if err := tx.Exec(`
			INSERT INTO point_wallets (user_id, balance, lifetime_earned, lifetime_spent, updated_at)
			SELECT ?, balance, lifetime_earned, lifetime_spent, NOW()
			FROM point_wallets WHERE user_id = ?
			ON CONFLICT (user_id) DO UPDATE SET
				balance         = point_wallets.balance + EXCLUDED.balance,
				lifetime_earned = point_wallets.lifetime_earned + EXCLUDED.lifetime_earned,
				lifetime_spent  = point_wallets.lifetime_spent + EXCLUDED.lifetime_spent,
				updated_at      = NOW()`, dstID, srcID).Error; err != nil {
			return fmt.Errorf("merge point_wallets: %w", err)
		}
		if err := tx.Exec("DELETE FROM point_wallets WHERE user_id = ?", srcID).Error; err != nil {
			return fmt.Errorf("cleanup point_wallets: %w", err)
		}

		if err := tx.Exec(`
			INSERT INTO user_streaks (user_id, current_streak, longest_streak, last_visit_date, total_visits, created_at, updated_at)
			SELECT ?, current_streak, longest_streak, last_visit_date, total_visits, NOW(), NOW()
			FROM user_streaks WHERE user_id = ?
			ON CONFLICT (user_id) DO UPDATE SET
				current_streak = GREATEST(user_streaks.current_streak, EXCLUDED.current_streak),
				longest_streak = GREATEST(user_streaks.longest_streak, EXCLUDED.longest_streak),
				total_visits   = user_streaks.total_visits + EXCLUDED.total_visits,
				updated_at     = NOW()`, dstID, srcID).Error; err != nil {
			return fmt.Errorf("merge user_streaks: %w", err)
		}
		if err := tx.Exec("DELETE FROM user_streaks WHERE user_id = ?", srcID).Error; err != nil {
			return fmt.Errorf("cleanup user_streaks: %w", err)
		}

		return nil
	})
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
