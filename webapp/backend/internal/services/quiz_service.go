package services

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

// ============================================
// Public response types
// ============================================

// QuizQuestionPublic is the mobile-facing question DTO.
type QuizQuestionPublic struct {
	ID            int64  `json:"id"`
	Content       string `json:"content"`
	OptionA       string `json:"option_a"`
	OptionB       string `json:"option_b"`
	OptionC       string `json:"option_c"`
	OptionD       string `json:"option_d"`
	Correct       string `json:"correct"`
	CorrectAnswer string `json:"correct_answer"`
	Explanation   string `json:"explanation,omitempty"`
	Category      string `json:"category"`
	Difficulty    string `json:"difficulty"`
	ArticleID     *int64 `json:"article_id,omitempty"`
}

// SubmitAnswerResult is returned after a user submits an answer.
type SubmitAnswerResult struct {
	QuestionID   int64  `json:"question_id"`
	Chosen       string `json:"chosen"`
	Correct      string `json:"correct"`
	IsCorrect    bool   `json:"is_correct"`
	Explanation  string `json:"explanation,omitempty"`
	ArticleID    *int64 `json:"article_id,omitempty"`
	PointsEarned int    `json:"points_earned"` // 3 if correct
}

// SessionResult is returned after finishing a session.
type SessionResult struct {
	SessionID    uuid.UUID           `json:"session_id"`
	Score        int                 `json:"score"`
	Total        int                 `json:"total"`
	Answers      []models.QuizAnswer `json:"answers"`
	PointsEarned int                 `json:"points_earned"`
	BonusPoints  int                 `json:"bonus_points"` // +5 if perfect score
	NewWeekScore int                 `json:"new_week_score"`
	Rank         int                 `json:"rank"`
}

// MyRankResponse is the personalised leaderboard info for the current user.
type MyRankResponse struct {
	Rank       int `json:"rank"`
	WeekScore  int `json:"week_score"`
	MonthScore int `json:"month_score"`
	TotalScore int `json:"total_score"`
	CurStreak  int `json:"cur_streak"`
}

// ============================================
// Service
// ============================================

const (
	pointsPerCorrectAnswer = 3
	bonusPerfectScore      = 5
	leaderboardCacheTTL    = 15 * time.Minute
)

// QuizService handles all quiz business logic.
type QuizService struct {
	repo   *repositories.QuizRepository
	cache  *CacheService
	logger *zap.Logger
}

// NewQuizService creates a new QuizService.
func NewQuizService(repo *repositories.QuizRepository, cache *CacheService, logger *zap.Logger) *QuizService {
	return &QuizService{repo: repo, cache: cache, logger: logger}
}

// toPublic converts a model question to the normalized DTO consumed by mobile.
func toPublic(q models.QuizQuestion) QuizQuestionPublic {
	correct := normalizeChoice(q.Correct)
	return QuizQuestionPublic{
		ID:            q.ID,
		Content:       q.Content,
		OptionA:       q.OptionA,
		OptionB:       q.OptionB,
		OptionC:       q.OptionC,
		OptionD:       q.OptionD,
		Correct:       correct,
		CorrectAnswer: answerTextForChoice(q, correct),
		Explanation:   q.Explanation,
		Category:      q.Category,
		Difficulty:    q.Difficulty,
		ArticleID:     q.ArticleID,
	}
}

func toPublicList(qs []models.QuizQuestion) []QuizQuestionPublic {
	out := make([]QuizQuestionPublic, len(qs))
	for i, q := range qs {
		out[i] = toPublic(q)
	}
	return out
}

func normalizeChoice(choice string) string {
	return strings.ToLower(strings.TrimSpace(choice))
}

func answerTextForChoice(q models.QuizQuestion, choice string) string {
	switch normalizeChoice(choice) {
	case "a":
		return q.OptionA
	case "b":
		return q.OptionB
	case "c":
		return q.OptionC
	case "d":
		return q.OptionD
	default:
		return ""
	}
}

// ============================================
// Public question queries
// ============================================

// GetDailyQuestions returns the daily question set including the correct answer.
func (s *QuizService) GetDailyQuestions(date time.Time) ([]QuizQuestionPublic, error) {
	qs, err := s.repo.GetDailyQuestions(date)
	if err != nil {
		s.logger.Error("GetDailyQuestions failed", zap.Error(err))
		return nil, fmt.Errorf("failed to get daily questions: %w", err)
	}
	return toPublicList(qs), nil
}

// GetQuestions returns questions filtered by category/difficulty including the correct answer.
func (s *QuizService) GetQuestions(category, difficulty string, limit int) ([]QuizQuestionPublic, error) {
	if limit <= 0 || limit > 50 {
		limit = 10
	}
	qs, err := s.repo.GetQuestions(category, difficulty, limit)
	if err != nil {
		return nil, fmt.Errorf("failed to get questions: %w", err)
	}
	return toPublicList(qs), nil
}

// ============================================
// Sessions
// ============================================

// StartSession creates a new quiz session for the user.
func (s *QuizService) StartSession(userID uuid.UUID, sessionType, category string) (*models.QuizSession, []QuizQuestionPublic, error) {
	if sessionType == "" {
		sessionType = "daily"
	}

	var qs []models.QuizQuestion
	var err error

	switch sessionType {
	case "daily":
		qs, err = s.repo.GetDailyQuestions(time.Now())
	case "topic":
		qs, err = s.repo.GetQuestions(category, "", 10)
	default:
		return nil, nil, errors.New("invalid session_type: must be 'daily' or 'topic'")
	}
	if err != nil {
		return nil, nil, fmt.Errorf("failed to load questions: %w", err)
	}
	if len(qs) == 0 {
		return nil, nil, errors.New("no questions available for this session")
	}

	ids := make([]int64, len(qs))
	for i, q := range qs {
		ids[i] = q.ID
	}

	session := &models.QuizSession{
		UserID:      userID,
		SessionType: sessionType,
		Category:    category,
		QuestionIDs: ids,
		Answers:     []byte("[]"),
		Total:       len(ids),
	}

	if err := s.repo.CreateSession(session); err != nil {
		return nil, nil, fmt.Errorf("failed to create session: %w", err)
	}

	return session, toPublicList(qs), nil
}

// SubmitAnswer records a user's answer for a single question within a session.
func (s *QuizService) SubmitAnswer(userID, sessionID uuid.UUID, questionID int64, chosen string, timeMs int) (*SubmitAnswerResult, error) {
	session, err := s.repo.GetSession(sessionID)
	if err != nil {
		return nil, errors.New("session not found")
	}
	if session.UserID != userID {
		return nil, errors.New("forbidden: session does not belong to this user")
	}
	if session.Completed {
		return nil, errors.New("session is already completed")
	}

	// Validate that questionID is in the session.
	found := false
	for _, id := range session.QuestionIDs {
		if id == questionID {
			found = true
			break
		}
	}
	if !found {
		return nil, errors.New("question is not part of this session")
	}

	// Validate chosen option.
	chosen = normalizeChoice(chosen)
	if chosen != "a" && chosen != "b" && chosen != "c" && chosen != "d" {
		return nil, errors.New("chosen must be one of: a, b, c, d")
	}

	// Load question.
	q, err := s.repo.GetQuestionByID(questionID)
	if err != nil {
		return nil, errors.New("question not found")
	}

	correct := normalizeChoice(q.Correct)
	isCorrect := correct == chosen
	points := 0
	if isCorrect {
		points = pointsPerCorrectAnswer
	}

	// Decode existing answers.
	var answers []models.QuizAnswer
	if err := json.Unmarshal(session.Answers, &answers); err != nil {
		answers = []models.QuizAnswer{}
	}

	// Check if already answered.
	for _, a := range answers {
		if a.QuestionID == questionID {
			return nil, errors.New("question already answered in this session")
		}
	}

	answers = append(answers, models.QuizAnswer{
		QuestionID: questionID,
		Chosen:     chosen,
		IsCorrect:  isCorrect,
		TimeMs:     timeMs,
	})

	newAnswers, _ := json.Marshal(answers)
	session.Answers = newAnswers
	if isCorrect {
		session.Score += points
	}

	if err := s.repo.UpdateSession(session); err != nil {
		return nil, fmt.Errorf("failed to update session: %w", err)
	}

	return &SubmitAnswerResult{
		QuestionID:   questionID,
		Chosen:       chosen,
		Correct:      correct,
		IsCorrect:    isCorrect,
		Explanation:  q.Explanation,
		ArticleID:    q.ArticleID,
		PointsEarned: points,
	}, nil
}

// FinishSession marks a session as completed and updates the user's aggregated scores.
func (s *QuizService) FinishSession(userID, sessionID uuid.UUID) (*SessionResult, error) {
	session, err := s.repo.GetSession(sessionID)
	if err != nil {
		return nil, errors.New("session not found")
	}
	if session.UserID != userID {
		return nil, errors.New("forbidden: session does not belong to this user")
	}
	if session.Completed {
		return nil, errors.New("session is already completed")
	}

	now := time.Now()
	session.Completed = true
	session.FinishedAt = &now

	if err := s.repo.UpdateSession(session); err != nil {
		return nil, fmt.Errorf("failed to finish session: %w", err)
	}

	// Decode answers.
	var answers []models.QuizAnswer
	_ = json.Unmarshal(session.Answers, &answers)

	// Calculate points including bonus.
	pointsEarned := session.Score
	bonusPoints := 0
	if session.Total > 0 && session.Score/pointsPerCorrectAnswer == session.Total {
		bonusPoints = bonusPerfectScore
	}
	totalPoints := pointsEarned + bonusPoints

	// Upsert quiz_scores.
	today := now.Truncate(24 * time.Hour)
	existing, err := s.repo.GetScore(userID)

	var scoreRow models.QuizScore
	if err != nil && errors.Is(err, gorm.ErrRecordNotFound) {
		scoreRow = models.QuizScore{
			UserID:     userID,
			TotalScore: totalPoints,
			WeekScore:  totalPoints,
			MonthScore: totalPoints,
			CurStreak:  1,
			BestStreak: 1,
			LastQuiz:   &today,
			UpdatedAt:  now,
		}
	} else if err == nil {
		scoreRow = *existing
		scoreRow.TotalScore += totalPoints
		scoreRow.WeekScore += totalPoints
		scoreRow.MonthScore += totalPoints
		scoreRow.UpdatedAt = now

		// Streak calculation.
		if existing.LastQuiz != nil {
			diff := today.Sub(existing.LastQuiz.Truncate(24 * time.Hour))
			switch {
			case diff == 24*time.Hour:
				// Consecutive day.
				scoreRow.CurStreak++
			case diff == 0:
				// Same day — no streak change.
			default:
				// Gap — reset streak.
				scoreRow.CurStreak = 1
			}
		} else {
			scoreRow.CurStreak = 1
		}

		if scoreRow.CurStreak > scoreRow.BestStreak {
			scoreRow.BestStreak = scoreRow.CurStreak
		}
		scoreRow.LastQuiz = &today
	} else {
		return nil, fmt.Errorf("failed to fetch user score: %w", err)
	}

	if err := s.repo.UpsertScore(&scoreRow); err != nil {
		s.logger.Error("Failed to upsert quiz score", zap.Error(err))
	}

	// Get updated rank.
	rank, _ := s.repo.GetUserRank(userID, "weekly")

	// Invalidate leaderboard cache.
	ctx := context.Background()
	for _, period := range []string{"weekly", "monthly", "alltime"} {
		_ = s.cache.DeleteKey(ctx, leaderboardCacheKey(period))
	}

	return &SessionResult{
		SessionID:    session.ID,
		Score:        session.Score,
		Total:        session.Total,
		Answers:      answers,
		PointsEarned: pointsEarned,
		BonusPoints:  bonusPoints,
		NewWeekScore: scoreRow.WeekScore,
		Rank:         rank,
	}, nil
}

// GetSession returns a session, enforcing ownership.
func (s *QuizService) GetSession(userID, sessionID uuid.UUID) (*models.QuizSession, error) {
	session, err := s.repo.GetSession(sessionID)
	if err != nil {
		return nil, errors.New("session not found")
	}
	if session.UserID != userID {
		return nil, errors.New("forbidden")
	}
	return session, nil
}

// GetUserHistory returns recent sessions for a user.
func (s *QuizService) GetUserHistory(userID uuid.UUID) ([]models.QuizSession, error) {
	return s.repo.GetUserSessions(userID, 20)
}

// ============================================
// Leaderboard
// ============================================

func leaderboardCacheKey(period string) string {
	return fmt.Sprintf("quiz:leaderboard:%s", period)
}

// GetLeaderboard returns the top scores, with Redis caching.
func (s *QuizService) GetLeaderboard(period string, limit int) ([]models.QuizScore, error) {
	if limit <= 0 || limit > 100 {
		limit = 50
	}
	key := leaderboardCacheKey(period)
	ctx := context.Background()

	// Try cache first.
	if cached, err := s.cache.GetString(ctx, key); err == nil && cached != "" {
		var scores []models.QuizScore
		if json.Unmarshal([]byte(cached), &scores) == nil {
			return scores, nil
		}
	}

	scores, err := s.repo.GetLeaderboard(period, limit)
	if err != nil {
		return nil, fmt.Errorf("failed to get leaderboard: %w", err)
	}

	// Cache the result.
	if data, err := json.Marshal(scores); err == nil {
		_ = s.cache.SetString(ctx, key, string(data), leaderboardCacheTTL)
	}

	return scores, nil
}

// GetMyRank returns the personalised rank info for a user.
func (s *QuizService) GetMyRank(userID uuid.UUID, period string) (*MyRankResponse, error) {
	scoreRow, err := s.repo.GetScore(userID)
	if err != nil {
		// No score yet — user hasn't played.
		return &MyRankResponse{}, nil
	}

	rank, err := s.repo.GetUserRank(userID, period)
	if err != nil {
		return nil, fmt.Errorf("failed to get rank: %w", err)
	}

	return &MyRankResponse{
		Rank:       rank,
		WeekScore:  scoreRow.WeekScore,
		MonthScore: scoreRow.MonthScore,
		TotalScore: scoreRow.TotalScore,
		CurStreak:  scoreRow.CurStreak,
	}, nil
}

// ============================================
// Admin operations
// ============================================

// AdminGetQuestion returns a single question by ID (admin, includes Correct field).
func (s *QuizService) AdminGetQuestion(id int64) (*models.QuizQuestion, error) {
	q, err := s.repo.GetQuestionByID(id)
	if err != nil {
		return nil, fmt.Errorf("question not found: %w", err)
	}
	return q, nil
}

// CreateQuestion creates a new quiz question.
func (s *QuizService) CreateQuestion(q *models.QuizQuestion) error {
	q.Correct = normalizeChoice(q.Correct)
	if err := s.repo.CreateQuestion(q); err != nil {
		return fmt.Errorf("failed to create question: %w", err)
	}
	return nil
}

// UpdateQuestion updates an existing question.
func (s *QuizService) UpdateQuestion(q *models.QuizQuestion) error {
	q.Correct = normalizeChoice(q.Correct)
	if err := s.repo.UpdateQuestion(q); err != nil {
		return fmt.Errorf("failed to update question: %w", err)
	}
	return nil
}

// DeleteQuestion deletes a question by ID.
func (s *QuizService) DeleteQuestion(id int64) error {
	if err := s.repo.DeleteQuestion(id); err != nil {
		return fmt.Errorf("failed to delete question: %w", err)
	}
	return nil
}

// ListQuestions returns paginated questions for admin.
func (s *QuizService) ListQuestions(page, limit int, category, difficulty string) ([]models.QuizQuestion, int64, error) {
	if page <= 0 {
		page = 1
	}
	if limit <= 0 || limit > 100 {
		limit = 20
	}
	return s.repo.ListQuestionsAdmin(page, limit, category, difficulty)
}

// BulkImport inserts multiple questions in a single call.
func (s *QuizService) BulkImport(questions []models.QuizQuestion) error {
	for i := range questions {
		questions[i].Correct = normalizeChoice(questions[i].Correct)
		if err := s.repo.CreateQuestion(&questions[i]); err != nil {
			return fmt.Errorf("failed to import question %d: %w", i+1, err)
		}
	}
	return nil
}

// SetDailySet creates or replaces the daily set for the given date.
func (s *QuizService) SetDailySet(adminID uuid.UUID, date time.Time, questionIDs []int64) error {
	if len(questionIDs) == 0 {
		return errors.New("question_ids must not be empty")
	}
	set := &models.QuizDailySet{
		Date:        date,
		QuestionIDs: questionIDs,
		CreatedBy:   &adminID,
	}
	if err := s.repo.UpsertDailySet(set); err != nil {
		return fmt.Errorf("failed to set daily set: %w", err)
	}
	return nil
}

// GetDailySets returns all daily sets ordered by date desc.
func (s *QuizService) GetDailySets() ([]models.QuizDailySet, error) {
	return s.repo.ListDailySets()
}
