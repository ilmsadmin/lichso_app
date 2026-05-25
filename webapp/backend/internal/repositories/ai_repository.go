package repositories

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// ============================================
// AI Log Repository
// ============================================

// AILogRepository handles persistence for AIGenerationLog
type AILogRepository struct {
	db *gorm.DB
}

// NewAILogRepository creates a new AILogRepository
func NewAILogRepository(db *gorm.DB) *AILogRepository {
	return &AILogRepository{db: db}
}

// Create persists a new generation log entry
func (r *AILogRepository) Create(log *models.AIGenerationLog) error {
	return r.db.Create(log).Error
}

// ListWithFilter returns paginated log entries filtered by type / model / date range
func (r *AILogRepository) ListWithFilter(genType, model string, from, to time.Time, page, limit int) ([]models.AIGenerationLog, int64, error) {
	q := r.db.Model(&models.AIGenerationLog{})

	if genType != "" {
		q = q.Where("generation_type = ?", genType)
	}
	if model != "" {
		q = q.Where("model_used = ?", model)
	}
	if !from.IsZero() {
		q = q.Where("created_at >= ?", from)
	}
	if !to.IsZero() {
		q = q.Where("created_at <= ?", to)
	}

	var total int64
	if err := q.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	var logs []models.AIGenerationLog
	offset := (page - 1) * limit
	if err := q.Order("created_at DESC").Offset(offset).Limit(limit).Find(&logs).Error; err != nil {
		return nil, 0, err
	}

	return logs, total, nil
}

// Stats aggregates token / cost totals grouped by generation_type and model
func (r *AILogRepository) Stats(from, to time.Time) (map[string]interface{}, error) {
	type Row struct {
		GenerationType string
		ModelUsed      string
		Requests       int64
		Tokens         int64
		CostUSD        float64
	}

	var rows []Row
	err := r.db.Raw(`
		SELECT generation_type, model_used,
		       COUNT(*)            AS requests,
		       SUM(total_tokens)   AS tokens,
		       SUM(cost_usd)       AS cost_usd
		FROM ai_generation_logs
		WHERE created_at BETWEEN ? AND ?
		GROUP BY generation_type, model_used
	`, from, to).Scan(&rows).Error

	// Compute summary totals
	var totalRequests, totalTokens int64
	var totalCostUSD float64
	var articlesGenerated, horoscopesRead, chatMessages int64

	// Group by model for requests_by_model
	type modelSummary struct {
		model    string
		requests int64
		costUSD  float64
	}
	modelMap := map[string]*modelSummary{}

	for _, row := range rows {
		totalRequests += row.Requests
		totalTokens += row.Tokens
		totalCostUSD += row.CostUSD

		switch row.GenerationType {
		case "article":
			articlesGenerated += row.Requests
		case "horoscope":
			horoscopesRead += row.Requests
		case "chat":
			chatMessages += row.Requests
		}

		if ms, ok := modelMap[row.ModelUsed]; ok {
			ms.requests += row.Requests
			ms.costUSD += row.CostUSD
		} else {
			modelMap[row.ModelUsed] = &modelSummary{model: row.ModelUsed, requests: row.Requests, costUSD: row.CostUSD}
		}
	}

	requestsByModel := make([]map[string]interface{}, 0, len(modelMap))
	for _, ms := range modelMap {
		requestsByModel = append(requestsByModel, map[string]interface{}{
			"model":    ms.model,
			"requests": ms.requests,
			"cost_usd": ms.costUSD,
		})
	}

	result := map[string]interface{}{
		"total_requests":     totalRequests,
		"total_tokens":       totalTokens,
		"total_cost_usd":     totalCostUSD,
		"articles_generated": articlesGenerated,
		"horoscopes_read":    horoscopesRead,
		"chat_messages":      chatMessages,
		"requests_by_model":  requestsByModel,
		"breakdown":          rows,
	}
	return result, err
}

// CostByDay returns daily cost aggregation
func (r *AILogRepository) CostByDay(from, to time.Time) ([]map[string]interface{}, error) {
	type Row struct {
		Date     string
		Requests int64
		Tokens   int64
		CostUSD  float64
	}
	var rows []Row
	err := r.db.Raw(`
		SELECT to_char(created_at AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYYY-MM-DD') AS date,
		       COUNT(*)           AS requests,
		       SUM(total_tokens)  AS tokens,
		       SUM(cost_usd)      AS cost_usd
		FROM ai_generation_logs
		WHERE created_at BETWEEN ? AND ?
		GROUP BY 1
		ORDER BY 1
	`, from, to).Scan(&rows).Error

	result := make([]map[string]interface{}, len(rows))
	for i, r := range rows {
		result[i] = map[string]interface{}{"date": r.Date, "requests": r.Requests, "tokens": r.Tokens, "cost_usd": r.CostUSD}
	}
	return result, err
}

// ============================================
// AI Horoscope Repository
// ============================================

// AIHoroscopeRepository handles persistence for AIHoroscopeSession
type AIHoroscopeRepository struct {
	db *gorm.DB
}

// NewAIHoroscopeRepository creates a new AIHoroscopeRepository
func NewAIHoroscopeRepository(db *gorm.DB) *AIHoroscopeRepository {
	return &AIHoroscopeRepository{db: db}
}

// Create persists a new horoscope session
func (r *AIHoroscopeRepository) Create(s *models.AIHoroscopeSession) error {
	return r.db.Create(s).Error
}

// GetBySessionKey finds an existing session by its cache key
func (r *AIHoroscopeRepository) GetBySessionKey(key string) (*models.AIHoroscopeSession, error) {
	var s models.AIHoroscopeSession
	if err := r.db.Where("session_key = ?", key).First(&s).Error; err != nil {
		return nil, err
	}
	return &s, nil
}

// ListByUser returns paginated sessions for a user
func (r *AIHoroscopeRepository) ListByUser(userID *uuid.UUID, page, limit int) ([]models.AIHoroscopeSession, int64, error) {
	var sessions []models.AIHoroscopeSession
	var total int64

	q := r.db.Model(&models.AIHoroscopeSession{}).Where("user_id = ?", userID)
	if err := q.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * limit
	if err := q.Order("created_at DESC").Offset(offset).Limit(limit).Find(&sessions).Error; err != nil {
		return nil, 0, err
	}

	return sessions, total, nil
}

// GetByIDAndUser returns a session only when it belongs to the given user
func (r *AIHoroscopeRepository) GetByIDAndUser(id uint64, userID *uuid.UUID) (*models.AIHoroscopeSession, error) {
	var s models.AIHoroscopeSession
	if err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&s).Error; err != nil {
		return nil, err
	}
	return &s, nil
}

// DeleteByIDAndUser removes a session that belongs to the given user
func (r *AIHoroscopeRepository) DeleteByIDAndUser(id uint64, userID *uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.AIHoroscopeSession{}).Error
}

// ============================================
// AI Quota Repository
// ============================================

// AIQuotaRepository handles AI usage quota tracking
type AIQuotaRepository struct {
	db *gorm.DB
}

// NewAIQuotaRepository creates a new AIQuotaRepository
func NewAIQuotaRepository(db *gorm.DB) *AIQuotaRepository {
	return &AIQuotaRepository{db: db}
}

// GetOrCreate returns the quota record for today, creating it if it does not
// exist yet.
//
// Design notes:
//   - Logged-in users  → user_id = <uuid>, ip_address = NULL  (unique on user_id+type+period)
//   - Guests           → user_id = NULL,   ip_address = <ip>  (unique on ip+type+period)
//
// Keeping ip_address NULL for authenticated users prevents a cross-constraint
// conflict when the same IP was previously used as a guest.
func (r *AIQuotaRepository) GetOrCreate(userID *uuid.UUID, ipAddress, quotaType string, limitCount int) (*models.AIUsageQuota, error) {
	periodKey := time.Now().Format("2006-01-02")
	resetAt := time.Now().Truncate(24 * time.Hour).Add(24 * time.Hour)

	var quota models.AIUsageQuota

	if userID != nil {
		// Authenticated user: store user_id only, ip_address stays NULL to
		// avoid conflicting with any guest row that shares the same IP.
		err := r.db.Exec(`
			INSERT INTO ai_usage_quotas
				(user_id, ip_address, quota_type, period_key, used_count, limit_count, reset_at, created_at, updated_at)
			VALUES (?, NULL, ?, ?, 0, ?, ?, NOW(), NOW())
			ON CONFLICT ON CONSTRAINT ai_usage_quotas_user_id_quota_type_period_key_key
			DO NOTHING
		`, *userID, quotaType, periodKey, limitCount, resetAt).Error
		if err != nil {
			return nil, fmt.Errorf("quota GetOrCreate insert (user): %w", err)
		}
		if err := r.db.
			Where("user_id = ? AND quota_type = ? AND period_key = ?", *userID, quotaType, periodKey).
			First(&quota).Error; err != nil {
			return nil, fmt.Errorf("quota GetOrCreate select (user): %w", err)
		}
	} else {
		// Guest: store ip_address only, user_id stays NULL.
		err := r.db.Exec(`
			INSERT INTO ai_usage_quotas
				(user_id, ip_address, quota_type, period_key, used_count, limit_count, reset_at, created_at, updated_at)
			VALUES (NULL, ?, ?, ?, 0, ?, ?, NOW(), NOW())
			ON CONFLICT ON CONSTRAINT ai_usage_quotas_ip_address_quota_type_period_key_key
			DO NOTHING
		`, ipAddress, quotaType, periodKey, limitCount, resetAt).Error
		if err != nil {
			return nil, fmt.Errorf("quota GetOrCreate insert (ip): %w", err)
		}
		if err := r.db.
			Where("ip_address = ? AND user_id IS NULL AND quota_type = ? AND period_key = ?", ipAddress, quotaType, periodKey).
			First(&quota).Error; err != nil {
			return nil, fmt.Errorf("quota GetOrCreate select (ip): %w", err)
		}
	}

	return &quota, nil
}

// Increment adds 1 to the used count for a quota record
func (r *AIQuotaRepository) Increment(id uint64) error {
	return r.db.Model(&models.AIUsageQuota{}).
		Where("id = ?", id).
		UpdateColumn("used_count", gorm.Expr("used_count + 1")).Error
}

// ============================================
// AI Prompt Template Repository
// ============================================

// AIPromptTemplateRepository handles CRUD for AIPromptTemplate
type AIPromptTemplateRepository struct {
	db *gorm.DB
}

// NewAIPromptTemplateRepository creates a new AIPromptTemplateRepository
func NewAIPromptTemplateRepository(db *gorm.DB) *AIPromptTemplateRepository {
	return &AIPromptTemplateRepository{db: db}
}

// List returns all prompt templates (optionally filtered by type)
func (r *AIPromptTemplateRepository) List(tplType string) ([]models.AIPromptTemplate, error) {
	var tpls []models.AIPromptTemplate
	q := r.db.Model(&models.AIPromptTemplate{})
	if tplType != "" {
		q = q.Where("type = ?", tplType)
	}
	return tpls, q.Order("name ASC").Find(&tpls).Error
}

// GetByID returns a template by ID
func (r *AIPromptTemplateRepository) GetByID(id uint64) (*models.AIPromptTemplate, error) {
	var tpl models.AIPromptTemplate
	return &tpl, r.db.First(&tpl, id).Error
}

// GetByName returns a template by name (exact match)
func (r *AIPromptTemplateRepository) GetByName(name string) (*models.AIPromptTemplate, error) {
	var tpl models.AIPromptTemplate
	err := r.db.Where("name = ?", name).First(&tpl).Error
	if err != nil {
		return nil, err
	}
	return &tpl, nil
}

// GetActiveByType returns the first active template for a given type
func (r *AIPromptTemplateRepository) GetActiveByType(tplType string) (*models.AIPromptTemplate, error) {
	var tpl models.AIPromptTemplate
	return &tpl, r.db.Where("type = ? AND is_active = TRUE", tplType).First(&tpl).Error
}

// Create persists a new template
func (r *AIPromptTemplateRepository) Create(tpl *models.AIPromptTemplate) error {
	return r.db.Create(tpl).Error
}

// Update saves changes to an existing template
func (r *AIPromptTemplateRepository) Update(tpl *models.AIPromptTemplate) error {
	return r.db.Save(tpl).Error
}

// Delete removes a template by ID
func (r *AIPromptTemplateRepository) Delete(id uint64) error {
	return r.db.Delete(&models.AIPromptTemplate{}, id).Error
}

// ============================================
// AI Chat Session Repository
// ============================================

// AIChatRepository handles persistence for AIChatSession
type AIChatRepository struct {
	db *gorm.DB
}

// NewAIChatRepository creates a new AIChatRepository
func NewAIChatRepository(db *gorm.DB) *AIChatRepository {
	return &AIChatRepository{db: db}
}

// Create persists a new chat session
func (r *AIChatRepository) Create(s *models.AIChatSession) error {
	return r.db.Create(s).Error
}

// GetByUUID returns a chat session by its UUID
func (r *AIChatRepository) GetByUUID(uuid string) (*models.AIChatSession, error) {
	var s models.AIChatSession
	return &s, r.db.Where("session_uuid = ?", uuid).First(&s).Error
}

// ListByUser returns active chat sessions for a user
func (r *AIChatRepository) ListByUser(userID uuid.UUID, page, limit int) ([]models.AIChatSession, int64, error) {
	var sessions []models.AIChatSession
	var total int64

	q := r.db.Model(&models.AIChatSession{}).Where("user_id = ? AND is_active = TRUE", userID)
	if err := q.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * limit
	err := q.Select("id, user_id, session_uuid, title, total_tokens, total_cost, last_message_at, created_at").
		Order("last_message_at DESC NULLS LAST").
		Offset(offset).Limit(limit).Find(&sessions).Error

	return sessions, total, err
}

// Save persists updates to an existing session
func (r *AIChatRepository) Save(s *models.AIChatSession) error {
	return r.db.Save(s).Error
}

// SoftDelete marks a session as inactive
func (r *AIChatRepository) SoftDelete(uuid string, userID uuid.UUID) error {
	return r.db.Model(&models.AIChatSession{}).
		Where("session_uuid = ? AND user_id = ?", uuid, userID).
		UpdateColumn("is_active", false).Error
}

// ============================================
// Helper: AI total cost this month
// ============================================

// TotalCostThisMonth returns the total USD spent on AI this calendar month
func AITotalCostThisMonth(db *gorm.DB) (float64, error) {
	var total float64
	now := time.Now()
	from := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, now.Location())
	to := from.AddDate(0, 1, 0)

	err := db.Model(&models.AIGenerationLog{}).
		Where("created_at >= ? AND created_at < ? AND status = 'success'", from, to).
		Select(fmt.Sprintf("COALESCE(SUM(cost_usd), 0)")).
		Scan(&total).Error

	return total, err
}
