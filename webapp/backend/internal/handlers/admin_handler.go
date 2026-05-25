package handlers

import (
	"bytes"
	"context"
	"encoding/csv"
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/redis/go-redis/v9"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// AdminHandler handles admin dashboard HTTP requests
type AdminHandler struct {
	userRepo     *repositories.UserRepository
	roleRepo     *repositories.RoleRepository
	activityRepo *repositories.ActivityLogRepository
	redisClient  *redis.Client
	logger       *zap.Logger
}

// NewAdminHandler creates a new AdminHandler
func NewAdminHandler(
	userRepo *repositories.UserRepository,
	roleRepo *repositories.RoleRepository,
	activityRepo *repositories.ActivityLogRepository,
	redisClient *redis.Client,
	logger *zap.Logger,
) *AdminHandler {
	return &AdminHandler{
		userRepo:     userRepo,
		roleRepo:     roleRepo,
		activityRepo: activityRepo,
		redisClient:  redisClient,
		logger:       logger,
	}
}

// DashboardStatsResponse represents the dashboard statistics
type DashboardStatsResponse struct {
	TotalUsers     int64            `json:"total_users"`
	ActiveUsers    int64            `json:"active_users"`
	InactiveUsers  int64            `json:"inactive_users"`
	TotalRoles     int64            `json:"total_roles"`
	NewUsersToday  int64            `json:"new_users_today"`
	NewUsersWeek   int64            `json:"new_users_week"`
	NewUsersMonth  int64            `json:"new_users_month"`
	RecentActivity []ActivityEntry  `json:"recent_activity"`
	ActionCounts   map[string]int64 `json:"action_counts"`
	ModuleCounts   map[string]int64 `json:"module_counts"`
}

// ActivityEntry represents a single activity log entry in the dashboard
type ActivityEntry struct {
	ID          string                 `json:"id"`
	UserEmail   string                 `json:"user_email"`
	Action      string                 `json:"action"`
	Module      string                 `json:"module"`
	Description string                 `json:"description"`
	Status      string                 `json:"status"`
	CreatedAt   time.Time              `json:"created_at"`
	Metadata    map[string]interface{} `json:"metadata,omitempty"`
}

// GetDashboardStats handles GET /api/admin/dashboard/stats
func (h *AdminHandler) GetDashboardStats(c *fiber.Ctx) error {
	ctx := context.Background()
	now := time.Now()

	// Get user counts
	totalUsers, err := h.userRepo.CountAll()
	if err != nil {
		h.logger.Error("Failed to count all users", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	activeUsers, err := h.userRepo.CountActive()
	if err != nil {
		h.logger.Error("Failed to count active users", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	// Get new users counts
	todayStart := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, now.Location())
	weekStart := todayStart.AddDate(0, 0, -7)
	monthStart := todayStart.AddDate(0, -1, 0)

	newUsersToday, err := h.userRepo.CountCreatedSince(todayStart)
	if err != nil {
		h.logger.Error("Failed to count new users today", zap.Error(err))
		newUsersToday = 0
	}

	newUsersWeek, err := h.userRepo.CountCreatedSince(weekStart)
	if err != nil {
		h.logger.Error("Failed to count new users this week", zap.Error(err))
		newUsersWeek = 0
	}

	newUsersMonth, err := h.userRepo.CountCreatedSince(monthStart)
	if err != nil {
		h.logger.Error("Failed to count new users this month", zap.Error(err))
		newUsersMonth = 0
	}

	// Get role count
	totalRoles, err := h.roleRepo.CountAll()
	if err != nil {
		h.logger.Error("Failed to count roles", zap.Error(err))
		totalRoles = 0
	}

	// Get recent activity logs
	recentLogs, err := h.activityRepo.GetRecentLogs(ctx, 10)
	if err != nil {
		h.logger.Error("Failed to get recent activity logs", zap.Error(err))
		recentLogs = nil
	}

	recentActivity := make([]ActivityEntry, len(recentLogs))
	for i, log := range recentLogs {
		recentActivity[i] = ActivityEntry{
			ID:          log.ID.Hex(),
			UserEmail:   log.UserEmail,
			Action:      log.Action,
			Module:      log.Module,
			Description: log.Description,
			Status:      log.Status,
			CreatedAt:   log.CreatedAt,
			Metadata:    log.Metadata,
		}
	}

	// Get action counts for last 30 days
	thirtyDaysAgo := now.AddDate(0, 0, -30)
	actionCounts, err := h.activityRepo.GetActionCounts(ctx, thirtyDaysAgo, now)
	if err != nil {
		h.logger.Error("Failed to get action counts", zap.Error(err))
		actionCounts = make(map[string]int64)
	}

	// Get module counts for last 30 days
	moduleCounts, err := h.activityRepo.GetModuleCounts(ctx, thirtyDaysAgo, now)
	if err != nil {
		h.logger.Error("Failed to get module counts", zap.Error(err))
		moduleCounts = make(map[string]int64)
	}

	stats := DashboardStatsResponse{
		TotalUsers:     totalUsers,
		ActiveUsers:    activeUsers,
		InactiveUsers:  totalUsers - activeUsers,
		TotalRoles:     totalRoles,
		NewUsersToday:  newUsersToday,
		NewUsersWeek:   newUsersWeek,
		NewUsersMonth:  newUsersMonth,
		RecentActivity: recentActivity,
		ActionCounts:   actionCounts,
		ModuleCounts:   moduleCounts,
	}

	return utils.SuccessResponse(c, "Dashboard stats retrieved successfully", stats)
}

// GetActivityLogs handles GET /api/admin/logs
func (h *AdminHandler) GetActivityLogs(c *fiber.Ctx) error {
	ctx := context.Background()

	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))

	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 20
	}

	query := repositories.ActivityLogQuery{
		Page:      page,
		Limit:     limit,
		UserID:    c.Query("user_id"),
		Action:    c.Query("action"),
		Module:    c.Query("module"),
		Status:    c.Query("status"),
		Search:    c.Query("search"),
		SortBy:    c.Query("sort_by", "created_at"),
		SortOrder: c.Query("sort_order", "desc"),
	}

	// Parse date filters
	if startDate := c.Query("start_date"); startDate != "" {
		if t, err := time.Parse("2006-01-02", startDate); err == nil {
			query.StartDate = &t
		}
	}
	if endDate := c.Query("end_date"); endDate != "" {
		if t, err := time.Parse("2006-01-02", endDate); err == nil {
			end := t.Add(24*time.Hour - time.Second)
			query.EndDate = &end
		}
	}

	logs, total, err := h.activityRepo.FindAllPaginated(ctx, query)
	if err != nil {
		h.logger.Error("Failed to list activity logs", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	// Convert to response
	responses := make([]ActivityEntry, len(logs))
	for i, log := range logs {
		responses[i] = ActivityEntry{
			ID:          log.ID.Hex(),
			UserEmail:   log.UserEmail,
			Action:      log.Action,
			Module:      log.Module,
			Description: log.Description,
			Status:      log.Status,
			CreatedAt:   log.CreatedAt,
			Metadata:    log.Metadata,
		}
	}

	return utils.PaginatedResponse(c, "Activity logs retrieved successfully", responses, page, limit, total)
}

// GetActivityLog handles GET /api/admin/logs/:id
func (h *AdminHandler) GetActivityLog(c *fiber.Ctx) error {
	ctx := context.Background()
	id := c.Params("id")

	log, err := h.activityRepo.FindByID(ctx, id)
	if err != nil {
		h.logger.Error("Failed to get activity log", zap.Error(err), zap.String("id", id))
		return utils.ErrorResponse(c, fiber.StatusNotFound, "Activity log not found")
	}

	resp := log.ToResponse()
	return utils.SuccessResponse(c, "Activity log retrieved successfully", resp)
}

// ExportActivityLogs handles GET /api/admin/logs/export
func (h *AdminHandler) ExportActivityLogs(c *fiber.Ctx) error {
	ctx := context.Background()

	// Parse query params (same as GetActivityLogs but with higher limit)
	query := repositories.ActivityLogQuery{
		Page:      1,
		Limit:     10000, // Export up to 10,000 logs
		UserID:    c.Query("user_id"),
		Action:    c.Query("action"),
		Module:    c.Query("module"),
		Status:    c.Query("status"),
		Search:    c.Query("search"),
		SortBy:    c.Query("sort_by", "created_at"),
		SortOrder: c.Query("sort_order", "desc"),
	}

	// Parse date filters
	if startDate := c.Query("start_date"); startDate != "" {
		if t, err := time.Parse("2006-01-02", startDate); err == nil {
			query.StartDate = &t
		}
	}
	if endDate := c.Query("end_date"); endDate != "" {
		if t, err := time.Parse("2006-01-02", endDate); err == nil {
			end := t.Add(24*time.Hour - time.Second)
			query.EndDate = &end
		}
	}

	logs, _, err := h.activityRepo.FindAllPaginated(ctx, query)
	if err != nil {
		h.logger.Error("Failed to export activity logs", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	// Generate CSV
	var buf bytes.Buffer
	writer := csv.NewWriter(&buf)

	// Write CSV header
	header := []string{"ID", "User Email", "Action", "Module", "Description", "Status", "IP Address", "User Agent", "Created At"}
	if err := writer.Write(header); err != nil {
		h.logger.Error("Failed to write CSV header", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	// Write CSV rows
	for _, log := range logs {
		row := []string{
			log.ID.Hex(),
			log.UserEmail,
			log.Action,
			log.Module,
			log.Description,
			log.Status,
			log.IPAddress,
			log.UserAgent,
			log.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		if err := writer.Write(row); err != nil {
			h.logger.Error("Failed to write CSV row", zap.Error(err))
			return utils.InternalErrorResponse(c)
		}
	}

	writer.Flush()
	if err := writer.Error(); err != nil {
		h.logger.Error("Failed to flush CSV writer", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	// Send CSV response
	filename := fmt.Sprintf("activity_logs_%s.csv", time.Now().Format("20060102_150405"))
	c.Set("Content-Type", "text/csv")
	c.Set("Content-Disposition", "attachment; filename="+filename)
	return c.Send(buf.Bytes())
}

// GetCacheStats handles GET /api/admin/cache/stats
func (h *AdminHandler) GetCacheStats(c *fiber.Ctx) error {
	ctx := context.Background()

	info, err := h.redisClient.Info(ctx, "stats", "memory", "keyspace").Result()
	if err != nil {
		h.logger.Error("Failed to get Redis info", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	// Parse Redis INFO output
	stats := parseRedisInfo(info)

	// Get DB size
	dbSize, err := h.redisClient.DBSize(ctx).Result()
	if err != nil {
		h.logger.Error("Failed to get Redis DB size", zap.Error(err))
		dbSize = 0
	}

	return utils.SuccessResponse(c, "Cache stats retrieved successfully", fiber.Map{
		"total_keys":        dbSize,
		"hits":              stats["keyspace_hits"],
		"misses":            stats["keyspace_misses"],
		"hit_ratio":         stats["hit_ratio"],
		"used_memory_human": stats["used_memory_human"],
		"used_memory_peak":  stats["used_memory_peak_human"],
		"connected_clients": stats["connected_clients"],
		"evicted_keys":      stats["evicted_keys"],
		"expired_keys":      stats["expired_keys"],
		"total_commands":    stats["total_commands_processed"],
	})
}

// parseRedisInfo extracts key-value pairs from Redis INFO command output
func parseRedisInfo(info string) map[string]string {
	result := make(map[string]string)
	lines := strings.Split(info, "\r\n")
	for _, line := range lines {
		if strings.HasPrefix(line, "#") || line == "" {
			continue
		}
		parts := strings.SplitN(line, ":", 2)
		if len(parts) == 2 {
			result[parts[0]] = parts[1]
		}
	}

	// Calculate hit ratio
	hits := result["keyspace_hits"]
	misses := result["keyspace_misses"]
	if hits != "" && misses != "" {
		h, _ := strconv.ParseFloat(hits, 64)
		m, _ := strconv.ParseFloat(misses, 64)
		total := h + m
		if total > 0 {
			ratio := fmt.Sprintf("%.2f%%", (h/total)*100)
			result["hit_ratio"] = ratio
		} else {
			result["hit_ratio"] = "N/A"
		}
	}

	return result
}
