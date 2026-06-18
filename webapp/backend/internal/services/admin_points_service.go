package services

import (
	"encoding/json"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

// ============================================
// Admin points management — view & moderate user points/quiz scores.
// ============================================

// AdminUserPointsRow is one row in the admin points overview list.
type AdminUserPointsRow struct {
	UserID         uuid.UUID `json:"user_id" gorm:"column:user_id"`
	DisplayName    string    `json:"display_name" gorm:"column:display_name"`
	Email          string    `json:"email" gorm:"column:email"`
	Avatar         string    `json:"avatar" gorm:"column:avatar"`
	Balance        int       `json:"balance" gorm:"column:balance"`
	LifetimeEarned int       `json:"lifetime_earned" gorm:"column:lifetime_earned"`
	LifetimeSpent  int       `json:"lifetime_spent" gorm:"column:lifetime_spent"`
	QuizTotalScore int       `json:"quiz_total_score" gorm:"column:quiz_total_score"`
	QuizWeekScore  int       `json:"quiz_week_score" gorm:"column:quiz_week_score"`
	QuizMonthScore int       `json:"quiz_month_score" gorm:"column:quiz_month_score"`
	CurStreak      int       `json:"cur_streak" gorm:"column:cur_streak"`
	EarnedToday    int       `json:"earned_today" gorm:"column:earned_today"`
	SessionsToday  int       `json:"sessions_today" gorm:"column:sessions_today"`
}

// AdminUserPointsDetail is the full per-user points view.
type AdminUserPointsDetail struct {
	UserID       uuid.UUID                 `json:"user_id"`
	DisplayName  string                    `json:"display_name"`
	Email        string                    `json:"email"`
	Avatar       string                    `json:"avatar"`
	Wallet       *models.PointWallet       `json:"wallet"`
	QuizScore    *models.QuizScore         `json:"quiz_score"`
	Transactions []models.PointTransaction `json:"transactions"`
	Sessions     []AdminQuizSessionBrief   `json:"sessions"`
}

// AdminQuizSessionBrief is a compact session row for the detail view.
type AdminQuizSessionBrief struct {
	ID          uuid.UUID  `json:"id" gorm:"column:id"`
	SessionType string     `json:"session_type" gorm:"column:session_type"`
	Score       int        `json:"score" gorm:"column:score"`
	ScoreV2     int        `json:"score_v2" gorm:"column:score_v2"`
	Total       int        `json:"total" gorm:"column:total"`
	Completed   bool       `json:"completed" gorm:"column:completed"`
	FinishedAt  *time.Time `json:"finished_at" gorm:"column:finished_at"`
}

// AdminDailyPointsRow is a single VN-day bucket of point activity.
type AdminDailyPointsRow struct {
	Date            string `json:"date"`
	AppPointsEarned int    `json:"app_points_earned"`
	AppPointsSpent  int    `json:"app_points_spent"`
	QuizScore       int    `json:"quiz_score"`
	QuizSessions    int    `json:"quiz_sessions"`
}

// AdminListUserPoints returns a paginated overview of users that have a wallet or a
// quiz score, with today's earnings/sessions to help spot abnormal farming.
func (s *PointsService) AdminListUserPoints(page, limit int, search, sortBy string) ([]AdminUserPointsRow, int64, error) {
	if page <= 0 {
		page = 1
	}
	if limit <= 0 || limit > 100 {
		limit = 20
	}
	offset := (page - 1) * limit

	dayStart := startOfTodayVNUTC()

	// Order whitelist to avoid SQL injection via sortBy.
	orderBy := "qs.total_score DESC NULLS LAST"
	switch sortBy {
	case "balance":
		orderBy = "w.balance DESC NULLS LAST"
	case "week":
		orderBy = "qs.week_score DESC NULLS LAST"
	case "month":
		orderBy = "qs.month_score DESC NULLS LAST"
	case "earned_today":
		orderBy = "earned_today DESC"
	case "sessions_today":
		orderBy = "sessions_today DESC"
	case "lifetime":
		orderBy = "w.lifetime_earned DESC NULLS LAST"
	}

	searchClause := ""
	args := []interface{}{dayStart, dayStart}
	if strings.TrimSpace(search) != "" {
		searchClause = `AND (LOWER(u.email) LIKE ? OR LOWER(u.first_name || ' ' || u.last_name) LIKE ?)`
		like := "%" + strings.ToLower(strings.TrimSpace(search)) + "%"
		args = append(args, like, like)
	}

	base := fmt.Sprintf(`
		FROM users u
		JOIN (
			SELECT user_id FROM point_wallets
			UNION
			SELECT user_id FROM quiz_scores
		) e ON e.user_id = u.id
		LEFT JOIN point_wallets w ON w.user_id = u.id
		LEFT JOIN quiz_scores qs ON qs.user_id = u.id
		LEFT JOIN (
			SELECT user_id, COALESCE(SUM(amount),0) AS earned_today
			FROM point_transactions
			WHERE direction = 'earn' AND created_at >= ?
			GROUP BY user_id
		) et ON et.user_id = u.id
		LEFT JOIN (
			SELECT user_id, COUNT(*) AS sessions_today
			FROM quiz_sessions
			WHERE completed = TRUE AND finished_at >= ?
			GROUP BY user_id
		) st ON st.user_id = u.id
		WHERE u.deleted_at IS NULL %s
	`, searchClause)

	var total int64
	countSQL := "SELECT COUNT(*) " + base
	if err := s.db.Raw(countSQL, args...).Scan(&total).Error; err != nil {
		return nil, 0, fmt.Errorf("count user points: %w", err)
	}

	selectSQL := `SELECT
			u.id AS user_id,
			TRIM(COALESCE(u.first_name,'') || ' ' || COALESCE(u.last_name,'')) AS display_name,
			u.email AS email,
			COALESCE(u.avatar,'') AS avatar,
			COALESCE(w.balance,0) AS balance,
			COALESCE(w.lifetime_earned,0) AS lifetime_earned,
			COALESCE(w.lifetime_spent,0) AS lifetime_spent,
			COALESCE(qs.total_score,0) AS quiz_total_score,
			COALESCE(qs.week_score,0) AS quiz_week_score,
			COALESCE(qs.month_score,0) AS quiz_month_score,
			COALESCE(qs.cur_streak,0) AS cur_streak,
			COALESCE(et.earned_today,0) AS earned_today,
			COALESCE(st.sessions_today,0) AS sessions_today
		` + base + " ORDER BY " + orderBy + " LIMIT ? OFFSET ?"

	rows := make([]AdminUserPointsRow, 0, limit)
	if err := s.db.Raw(selectSQL, append(args, limit, offset)...).Scan(&rows).Error; err != nil {
		return nil, 0, fmt.Errorf("list user points: %w", err)
	}
	return rows, total, nil
}

// AdminGetUserPoints returns the full points detail for one user.
func (s *PointsService) AdminGetUserPoints(userID uuid.UUID) (*AdminUserPointsDetail, error) {
	var u models.User
	if err := s.db.Where("id = ?", userID).First(&u).Error; err != nil {
		return nil, errors.New("user not found")
	}

	detail := &AdminUserPointsDetail{
		UserID:      userID,
		DisplayName: strings.TrimSpace(u.FirstName + " " + u.LastName),
		Email:       u.Email,
		Avatar:      u.Avatar,
	}

	var wallet models.PointWallet
	if err := s.db.Where("user_id = ?", userID).First(&wallet).Error; err == nil {
		detail.Wallet = &wallet
	}

	var score models.QuizScore
	if err := s.db.Where("user_id = ?", userID).First(&score).Error; err == nil {
		detail.QuizScore = &score
	}

	detail.Transactions = make([]models.PointTransaction, 0, 30)
	_ = s.db.Where("user_id = ?", userID).
		Order("created_at DESC").Limit(30).Find(&detail.Transactions).Error

	detail.Sessions = make([]AdminQuizSessionBrief, 0, 20)
	_ = s.db.Table("quiz_sessions").
		Select("id, session_type, score, score_v2, total, completed, finished_at").
		Where("user_id = ?", userID).
		Order("started_at DESC").Limit(20).Scan(&detail.Sessions).Error

	return detail, nil
}

// AdminGetUserDailyPoints returns per-VN-day point activity for the last `days` days.
func (s *PointsService) AdminGetUserDailyPoints(userID uuid.UUID, days int) ([]AdminDailyPointsRow, error) {
	if days <= 0 || days > 365 {
		days = 30
	}
	loc := vnLocation()
	todayStart := startOfDayInLocation(time.Now().In(loc), loc)
	since := todayStart.AddDate(0, 0, -(days - 1)).UTC()

	// App points per day (earn/spend), grouped by VN day.
	type txAgg struct {
		Day    string `gorm:"column:day"`
		Earned int    `gorm:"column:earned"`
		Spent  int    `gorm:"column:spent"`
	}
	var txRows []txAgg
	if err := s.db.Raw(`
		SELECT to_char((created_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date, 'YYYY-MM-DD') AS day,
			COALESCE(SUM(CASE WHEN direction='earn' THEN amount ELSE 0 END),0) AS earned,
			COALESCE(SUM(CASE WHEN direction='spend' THEN amount ELSE 0 END),0) AS spent
		FROM point_transactions
		WHERE user_id = ? AND created_at >= ?
		GROUP BY day
	`, userID, since).Scan(&txRows).Error; err != nil {
		return nil, fmt.Errorf("daily app points: %w", err)
	}

	// Quiz leaderboard score + session count per day, grouped by VN day.
	type quizAgg struct {
		Day      string `gorm:"column:day"`
		Score    int    `gorm:"column:score"`
		Sessions int    `gorm:"column:sessions"`
	}
	var quizRows []quizAgg
	if err := s.db.Raw(`
		SELECT to_char((finished_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date, 'YYYY-MM-DD') AS day,
			COALESCE(SUM(COALESCE(NULLIF(score_v2,0), score)),0) AS score,
			COUNT(*) AS sessions
		FROM quiz_sessions
		WHERE user_id = ? AND completed = TRUE AND finished_at >= ?
		GROUP BY day
	`, userID, since).Scan(&quizRows).Error; err != nil {
		return nil, fmt.Errorf("daily quiz score: %w", err)
	}

	byDay := map[string]*AdminDailyPointsRow{}
	for _, t := range txRows {
		byDay[t.Day] = &AdminDailyPointsRow{Date: t.Day, AppPointsEarned: t.Earned, AppPointsSpent: t.Spent}
	}
	for _, qr := range quizRows {
		row, ok := byDay[qr.Day]
		if !ok {
			row = &AdminDailyPointsRow{Date: qr.Day}
			byDay[qr.Day] = row
		}
		row.QuizScore = qr.Score
		row.QuizSessions = qr.Sessions
	}

	// Emit one row per day, most recent first, including empty days.
	out := make([]AdminDailyPointsRow, 0, days)
	for i := 0; i < days; i++ {
		d := todayStart.AddDate(0, 0, -i).Format("2006-01-02")
		if row, ok := byDay[d]; ok {
			out = append(out, *row)
		} else {
			out = append(out, AdminDailyPointsRow{Date: d})
		}
	}
	return out, nil
}

// AdminAdjustResult is returned after an admin moderation action.
type AdminAdjustResult struct {
	Wallet    *models.PointWallet `json:"wallet"`
	QuizScore *models.QuizScore   `json:"quiz_score"`
}

// AdminAdjustUserPoints applies a manual wallet delta and/or a quiz-score reset.
// walletDelta may be negative (deduct); balance is floored at 0. When resetQuizScore
// is true, the user's quiz_scores aggregate is zeroed and all their completed sessions
// have score/score_v2 cleared so they no longer contribute to the leaderboard.
func (s *PointsService) AdminAdjustUserPoints(adminID, userID uuid.UUID, walletDelta int, resetQuizScore bool, reason string) (*AdminAdjustResult, error) {
	if walletDelta == 0 && !resetQuizScore {
		return nil, errors.New("no-op: provide wallet_delta or reset_quiz_score")
	}

	err := s.db.Transaction(func(tx *gorm.DB) error {
		if walletDelta != 0 {
			var w models.PointWallet
			err := tx.Where("user_id = ?", userID).First(&w).Error
			if errors.Is(err, gorm.ErrRecordNotFound) {
				w = models.PointWallet{UserID: userID, UpdatedAt: time.Now()}
			} else if err != nil {
				return err
			}

			newBalance := w.Balance + walletDelta
			if newBalance < 0 {
				newBalance = 0
			}
			applied := newBalance - w.Balance // actual signed change after flooring
			w.Balance = newBalance
			if applied > 0 {
				w.LifetimeEarned += applied
			} else {
				w.LifetimeSpent += -applied
			}
			w.UpdatedAt = time.Now()
			if err := tx.Save(&w).Error; err != nil {
				return err
			}

			direction := "earn"
			amount := applied
			if applied < 0 {
				direction = "spend"
				amount = -applied
			}
			if amount != 0 {
				meta, _ := json.Marshal(map[string]interface{}{
					"reason":   reason,
					"admin_id": adminID.String(),
				})
				if err := tx.Create(&models.PointTransaction{
					UserID:    userID,
					Amount:    amount,
					Direction: direction,
					Source:    "admin_adjust",
					Metadata:  meta,
					CreatedAt: time.Now(),
				}).Error; err != nil {
					return err
				}
			}
		}

		if resetQuizScore {
			if err := tx.Table("quiz_scores").Where("user_id = ?", userID).Updates(map[string]any{
				"total_score": 0,
				"week_score":  0,
				"month_score": 0,
				"cur_streak":  0,
				"best_streak": 0,
				"xp":          0,
				"updated_at":  time.Now(),
			}).Error; err != nil {
				return err
			}
			// Zero session scores so the SUM-based leaderboard excludes them.
			if err := tx.Table("quiz_sessions").
				Where("user_id = ? AND completed = ?", userID, true).
				Updates(map[string]any{"score": 0, "score_v2": 0}).Error; err != nil {
				return err
			}
		}
		return nil
	})
	if err != nil {
		return nil, err
	}

	s.logger.Info("Admin adjusted user points",
		zap.String("admin_id", adminID.String()),
		zap.String("user_id", userID.String()),
		zap.Int("wallet_delta", walletDelta),
		zap.Bool("reset_quiz_score", resetQuizScore),
	)

	result := &AdminAdjustResult{}
	var w models.PointWallet
	if err := s.db.Where("user_id = ?", userID).First(&w).Error; err == nil {
		result.Wallet = &w
	}
	var sc models.QuizScore
	if err := s.db.Where("user_id = ?", userID).First(&sc).Error; err == nil {
		result.QuizScore = &sc
	}
	return result, nil
}
