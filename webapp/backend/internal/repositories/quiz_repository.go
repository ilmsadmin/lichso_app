package repositories

import (
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

// QuizRepository handles all quiz-related database operations.
type QuizRepository struct {
	db *gorm.DB
}

// QuizUserPublicInfo is the minimal user profile needed by leaderboard rows.
type QuizUserPublicInfo struct {
	DisplayName string
	AvatarURL   string
}

// NewQuizRepository creates a new QuizRepository.
func NewQuizRepository(db *gorm.DB) *QuizRepository {
	return &QuizRepository{db: db}
}

// ============================================
// Questions
// ============================================

// GetQuestions returns active questions filtered by category and difficulty.
func (r *QuizRepository) GetQuestions(category, difficulty string, limit int) ([]models.QuizQuestion, error) {
	var questions []models.QuizQuestion
	query := r.db.Where("is_active = ?", true)
	if category != "" {
		query = query.Where("category = ?", category)
	}
	if difficulty != "" {
		query = query.Where("difficulty = ?", difficulty)
	}
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Order("RANDOM()").Find(&questions).Error
	return questions, err
}

// GetDailyQuestions returns the 15 questions for a given date.
// If no daily set exists for that date, it falls back to 5 easy, 7 medium, and 3 hard questions.
func (r *QuizRepository) GetDailyQuestions(date time.Time) ([]models.QuizQuestion, error) {
	// Try to load the daily set first.
	set, err := r.GetDailySet(date)
	if err == nil && len(set.QuestionIDs) > 0 {
		return r.GetQuestionsByIDs([]int64(set.QuestionIDs))
	}

	// Fallback: 5 easy, 7 medium, 3 hard questions.
	var easy, medium, hard []models.QuizQuestion

	if err := r.db.Where("is_active = ? AND difficulty = ?", true, "easy").Order("RANDOM()").Limit(5).Find(&easy).Error; err != nil {
		return nil, err
	}
	if err := r.db.Where("is_active = ? AND difficulty = ?", true, "medium").Order("RANDOM()").Limit(7).Find(&medium).Error; err != nil {
		return nil, err
	}
	if err := r.db.Where("is_active = ? AND difficulty = ?", true, "hard").Order("RANDOM()").Limit(3).Find(&hard).Error; err != nil {
		return nil, err
	}

	questions := make([]models.QuizQuestion, 0, 15)
	questions = append(questions, easy...)
	questions = append(questions, medium...)
	questions = append(questions, hard...)

	// If we couldn't get enough categorized questions, fall back to random active questions
	if len(questions) < 15 {
		var fallback []models.QuizQuestion
		if err := r.db.Where("is_active = ?", true).Order("RANDOM()").Limit(15).Find(&fallback).Error; err != nil {
			return nil, err
		}
		return fallback, nil
	}

	return questions, nil
}

// GetQuestionByID returns a single question by its ID.
func (r *QuizRepository) GetQuestionByID(id int64) (*models.QuizQuestion, error) {
	var q models.QuizQuestion
	err := r.db.Where("id = ?", id).First(&q).Error
	return &q, err
}

// GetQuestionsByIDs returns questions in the order of the supplied IDs.
func (r *QuizRepository) GetQuestionsByIDs(ids []int64) ([]models.QuizQuestion, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	var questions []models.QuizQuestion
	err := r.db.Where("id = ANY(?)", pq.Array(ids)).Find(&questions).Error
	if err != nil {
		return nil, err
	}

	// Preserve order from the ids slice.
	idx := make(map[int64]models.QuizQuestion, len(questions))
	for _, q := range questions {
		idx[q.ID] = q
	}
	ordered := make([]models.QuizQuestion, 0, len(ids))
	for _, id := range ids {
		if q, ok := idx[id]; ok {
			ordered = append(ordered, q)
		}
	}
	return ordered, nil
}

// CreateQuestion inserts a new question.
func (r *QuizRepository) CreateQuestion(q *models.QuizQuestion) error {
	return r.db.Create(q).Error
}

// UpdateQuestion persists changes to an existing question.
func (r *QuizRepository) UpdateQuestion(q *models.QuizQuestion) error {
	return r.db.Save(q).Error
}

// DeleteQuestion hard-deletes a question by ID.
func (r *QuizRepository) DeleteQuestion(id int64) error {
	return r.db.Where("id = ?", id).Delete(&models.QuizQuestion{}).Error
}

// CountQuestions returns the total number of active questions matching the filters.
func (r *QuizRepository) CountQuestions(category, difficulty string) (int64, error) {
	var count int64
	query := r.db.Model(&models.QuizQuestion{}).Where("is_active = ?", true)
	if category != "" {
		query = query.Where("category = ?", category)
	}
	if difficulty != "" {
		query = query.Where("difficulty = ?", difficulty)
	}
	err := query.Count(&count).Error
	return count, err
}

// ListQuestionsAdmin returns paginated questions for admin (all, including inactive).
func (r *QuizRepository) ListQuestionsAdmin(page, limit int, category, difficulty, search string) ([]models.QuizQuestion, int64, error) {
	var questions []models.QuizQuestion
	var total int64

	query := r.db.Model(&models.QuizQuestion{})
	if category != "" {
		query = query.Where("category = ?", category)
	}
	if difficulty != "" {
		query = query.Where("difficulty = ?", difficulty)
	}
	if search != "" {
		like := "%" + strings.ToLower(search) + "%"
		query = query.Where("LOWER(content) LIKE ?", like)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * limit
	err := query.Order("created_at DESC").Offset(offset).Limit(limit).Find(&questions).Error
	return questions, total, err
}

// ============================================
// Daily Sets
// ============================================

// GetDailySet returns the daily question set for the given date (date portion only).
func (r *QuizRepository) GetDailySet(date time.Time) (*models.QuizDailySet, error) {
	var set models.QuizDailySet
	// Truncate to date only for comparison.
	dateOnly := date.Format("2006-01-02")
	err := r.db.Where("date::date = ?", dateOnly).First(&set).Error
	return &set, err
}

// ListDailySets returns all daily sets ordered by date descending.
func (r *QuizRepository) ListDailySets() ([]models.QuizDailySet, error) {
	var sets []models.QuizDailySet
	err := r.db.Order("date DESC").Find(&sets).Error
	return sets, err
}

// GetRandomActiveQuestionIDs returns n random IDs from active (and optionally published) questions.
func (r *QuizRepository) GetRandomActiveQuestionIDs(n int, category, difficulty string) ([]int64, error) {
	query := r.db.Model(&models.QuizQuestion{}).Where("is_active = ?", true)
	if category != "" {
		query = query.Where("category = ?", category)
	}
	if difficulty != "" {
		query = query.Where("difficulty = ?", difficulty)
	}
	var ids []int64
	err := query.Order("RANDOM()").Limit(n).Pluck("id", &ids).Error
	return ids, err
}

// UpsertDailySet creates or replaces the daily set for the given date.
func (r *QuizRepository) UpsertDailySet(set *models.QuizDailySet) error {
	return r.db.Clauses(clause.OnConflict{
		Columns:   []clause.Column{{Name: "date"}},
		DoUpdates: clause.AssignmentColumns([]string{"question_ids", "created_by"}),
	}).Create(set).Error
}

// ============================================
// Sessions
// ============================================

// CreateSession inserts a new quiz session.
func (r *QuizRepository) CreateSession(s *models.QuizSession) error {
	return r.db.Create(s).Error
}

// GetSession returns a session by its UUID.
func (r *QuizRepository) GetSession(id uuid.UUID) (*models.QuizSession, error) {
	var s models.QuizSession
	err := r.db.Where("id = ?", id).First(&s).Error
	return &s, err
}

// GetSessionByClientID returns a synced guest session by its client-generated ID.
func (r *QuizRepository) GetSessionByClientID(clientSessionID string) (*models.QuizSession, error) {
	var s models.QuizSession
	err := r.db.Where("client_session_id = ?", clientSessionID).First(&s).Error
	return &s, err
}

// UpdateSession persists changes to an existing session.
func (r *QuizRepository) UpdateSession(s *models.QuizSession) error {
	return r.db.Save(s).Error
}

// GetUserSessions returns recent sessions for a user.
func (r *QuizRepository) GetUserSessions(userID uuid.UUID, limit int) ([]models.QuizSession, error) {
	var sessions []models.QuizSession
	query := r.db.Where("user_id = ?", userID).Order("started_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&sessions).Error
	return sessions, err
}

// ============================================
// Scores / Leaderboard
// ============================================

// UpsertScore creates or updates a user's aggregated quiz score row.
func (r *QuizRepository) UpsertScore(score *models.QuizScore) error {
	return r.db.Clauses(clause.OnConflict{
		Columns: []clause.Column{{Name: "user_id"}},
		DoUpdates: clause.AssignmentColumns([]string{
			"display_name", "avatar_url",
			"total_score", "week_score", "month_score",
			"best_streak", "cur_streak", "last_quiz", "updated_at",
		}),
	}).Create(score).Error
}

// GetScore returns a user's quiz score record.
func (r *QuizRepository) GetScore(userID uuid.UUID) (*models.QuizScore, error) {
	var score models.QuizScore
	err := r.db.Where("user_id = ?", userID).First(&score).Error
	return &score, err
}

// GetLeaderboard returns the top scores for the requested period.
// period: "weekly" | "monthly" | "alltime"
func (r *QuizRepository) GetLeaderboard(period string, limit int) ([]models.QuizScore, error) {
	var scores []models.QuizScore
	query := r.db.Table("quiz_scores AS qs").
		Select(`
			qs.user_id,
			COALESCE(
				NULLIF(REGEXP_REPLACE(TRIM(COALESCE(qs.display_name, '')), '\s+', ' ', 'g'), ''),
				NULLIF(REGEXP_REPLACE(TRIM(COALESCE(u.first_name, '') || ' ' || COALESCE(u.last_name, '')), '\s+', ' ', 'g'), ''),
				u.email,
				'Người dùng'
			) AS display_name,
			COALESCE(NULLIF(qs.avatar_url, ''), u.avatar, '') AS avatar_url,
			qs.total_score,
			qs.week_score,
			qs.month_score,
			qs.best_streak,
			qs.cur_streak,
			qs.last_quiz,
			qs.updated_at
		`).
		Joins("LEFT JOIN users u ON u.id = qs.user_id")

	switch period {
	case "monthly":
		query = query.Order("month_score DESC, total_score DESC")
	case "alltime":
		query = query.Order("total_score DESC, best_streak DESC")
	default: // weekly
		query = query.Order("week_score DESC, total_score DESC")
	}

	if limit > 0 {
		query = query.Limit(limit)
	}

	err := query.Find(&scores).Error
	return scores, err
}

// GetUserRank returns the 1-based rank of a user for the given period.
func (r *QuizRepository) GetUserRank(userID uuid.UUID, period string) (int, error) {
	var rank int64
	var col string
	switch period {
	case "monthly":
		col = "month_score"
	case "alltime":
		col = "total_score"
	default:
		col = "week_score"
	}

	// Count how many users have a higher score than this user.
	subQuery := r.db.Model(&models.QuizScore{}).Select(col).Where("user_id = ?", userID)
	err := r.db.Model(&models.QuizScore{}).
		Where(col+" > (?)", subQuery).
		Count(&rank).Error
	if err != nil {
		return 0, err
	}
	return int(rank) + 1, nil
}

// GetUserPublicInfo returns the best available display name + avatar for leaderboard usage.
func (r *QuizRepository) GetUserPublicInfo(userID uuid.UUID) (*QuizUserPublicInfo, error) {
	var row struct {
		FirstName string `gorm:"column:first_name"`
		LastName  string `gorm:"column:last_name"`
		Email     string `gorm:"column:email"`
		Avatar    string `gorm:"column:avatar"`
	}

	if err := r.db.Table("users").
		Select("first_name, last_name, email, avatar").
		Where("id = ?", userID).
		Take(&row).Error; err != nil {
		return nil, err
	}

	displayName := strings.TrimSpace(strings.TrimSpace(row.FirstName) + " " + strings.TrimSpace(row.LastName))
	if displayName == "" {
		displayName = strings.TrimSpace(row.Email)
	}
	if displayName == "" {
		displayName = fmt.Sprintf("User %s", userID.String()[:8])
	}

	return &QuizUserPublicInfo{
		DisplayName: displayName,
		AvatarURL:   strings.TrimSpace(row.Avatar),
	}, nil
}

// ResetWeeklyScores zeroes out week_score for all users.
func (r *QuizRepository) ResetWeeklyScores() error {
	return r.db.Model(&models.QuizScore{}).Where("1 = 1").Update("week_score", 0).Error
}

// ResetMonthlyScores zeroes out month_score for all users.
func (r *QuizRepository) ResetMonthlyScores() error {
	return r.db.Model(&models.QuizScore{}).Where("1 = 1").Update("month_score", 0).Error
}

// GetAssistUsages returns all assists used for a specific question in a session.
func (r *QuizRepository) GetAssistUsages(sessionID uuid.UUID, questionID int64) ([]models.QuizAssistUsage, error) {
	var usages []models.QuizAssistUsage
	err := r.db.Where("session_id = ? AND question_id = ?", sessionID, questionID).Find(&usages).Error
	return usages, err
}

// DB returns the underlying GORM database instance.
func (r *QuizRepository) DB() *gorm.DB {
	return r.db
}
