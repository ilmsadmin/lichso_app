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
	Hint          string `json:"hint,omitempty"`
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

// OfflineQuizAnswer is used when syncing a guest session to the backend.
type OfflineQuizAnswer struct {
	QuestionID int64  `json:"question_id"`
	Chosen     string `json:"chosen"`
	TimeMs     int    `json:"time_ms"`
}

// OfflineQuizSession is one locally completed guest session waiting to be synced.
type OfflineQuizSession struct {
	ClientSessionID string              `json:"client_session_id"`
	SessionType     string              `json:"session_type"`
	Category        string              `json:"category,omitempty"`
	QuestionIDs     []int64             `json:"question_ids"`
	Answers         []OfflineQuizAnswer `json:"answers"`
	StartedAtMs     int64               `json:"started_at_ms"`
	FinishedAtMs    int64               `json:"finished_at_ms"`
}

// SyncOfflineQuizRequest wraps pending guest sessions.
type SyncOfflineQuizRequest struct {
	Sessions []OfflineQuizSession `json:"sessions"`
}

// SyncOfflineQuizResponse returns sync results for the client queue.
type SyncOfflineQuizResponse struct {
	SyncedSessionIDs  []string `json:"synced_session_ids"`
	SkippedSessionIDs []string `json:"skipped_session_ids"`
}

// SessionResult is returned after finishing a session.
type SessionResult struct {
	SessionID             uuid.UUID           `json:"session_id"`
	Score                 int                 `json:"score"`
	ScoreV2               int                 `json:"score_v2"`
	Total                 int                 `json:"total"`
	Answers               []models.QuizAnswer `json:"answers"`
	PointsEarned          int                 `json:"points_earned"`
	BonusPoints           int                 `json:"bonus_points"` // +5 if perfect score
	NewWeekScore          int                 `json:"new_week_score"`
	Rank                  int                 `json:"rank"`
	AppPointsEarned       int                 `json:"app_points_earned"`
	XPEarned              int                 `json:"xp_earned"`
	SessionTitle          string              `json:"session_title"`
	UnlockedMasteryTitles []string            `json:"unlocked_mastery_titles,omitempty"`
	UnlockedBadges        []string            `json:"unlocked_badges,omitempty"`
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

func vnLocation() *time.Location {
	loc, err := time.LoadLocation("Asia/Ho_Chi_Minh")
	if err != nil {
		return time.FixedZone("ICT", 7*60*60)
	}
	return loc
}

func startOfDayInLocation(t time.Time, loc *time.Location) time.Time {
	localTime := t.In(loc)
	return time.Date(localTime.Year(), localTime.Month(), localTime.Day(), 0, 0, 0, 0, loc)
}

// QuizService handles all quiz business logic.
type QuizService struct {
	repo       *repositories.QuizRepository
	cache      *CacheService
	logger     *zap.Logger
	openRouter *OpenRouterService
	points     *PointsService
}

// NewQuizService creates a new QuizService.
func NewQuizService(repo *repositories.QuizRepository, cache *CacheService, logger *zap.Logger) *QuizService {
	return &QuizService{repo: repo, cache: cache, logger: logger}
}

// SetPointsService injects the PointsService.
func (s *QuizService) SetPointsService(ps *PointsService) {
	s.points = ps
}

// SetOpenRouterService injects the AI service for quiz generation.
func (s *QuizService) SetOpenRouterService(or *OpenRouterService) {
	s.openRouter = or
}

// ============================================
// AI-assisted question generation
// ============================================

// GenerateQuizQuestionsRequest is the input for AI generation.
type GenerateQuizQuestionsRequest struct {
	Topic      string `json:"topic"`      // free-form subject
	Text       string `json:"text"`       // paste raw text as source
	Count      int    `json:"count"`      // 1-10
	Category   string `json:"category"`   // e.g. history_vn
	Difficulty string `json:"difficulty"` // easy | medium | hard
}

// GenerateQuizTopicsRequest is the input for AI topic suggestions by category.
type GenerateQuizTopicsRequest struct {
	Category       string   `json:"category"`        // e.g. history_vn
	Count          int      `json:"count"`           // 5-10
	ExistingTopics []string `json:"existing_topics"` // topics admin already has in the dialog
}

// GeneratedQuizTopic is a draft topic returned by the AI.
type GeneratedQuizTopic struct {
	Title       string `json:"title"`
	Description string `json:"description,omitempty"`
}

// GeneratedQuizQuestion is a draft question returned by the AI (not yet saved).
type GeneratedQuizQuestion struct {
	Content     string `json:"content"`
	OptionA     string `json:"option_a"`
	OptionB     string `json:"option_b"`
	OptionC     string `json:"option_c"`
	OptionD     string `json:"option_d"`
	Correct     string `json:"correct"` // a | b | c | d
	Hint        string `json:"hint"`
	Explanation string `json:"explanation"`
	Category    string `json:"category"`
	Difficulty  string `json:"difficulty"`
}

var categoryLabels = map[string]string{
	"history_vn":    "Lịch sử Việt Nam",
	"history_world": "Lịch sử Thế giới",
	"culture":       "Văn hóa",
	"geography":     "Địa lý",
	"general":       "Kiến thức chung",
}

var difficultyLabels = map[string]string{
	"easy":   "Dễ",
	"medium": "Trung bình",
	"hard":   "Khó",
}

// GenerateQuizQuestions calls the AI to generate draft quiz questions.
func (s *QuizService) GenerateQuizQuestions(req GenerateQuizQuestionsRequest) ([]GeneratedQuizQuestion, error) {
	if s.openRouter == nil || !s.openRouter.IsConfigured() {
		return nil, fmt.Errorf("dịch vụ AI chưa được cấu hình (thiếu OPENROUTER_API_KEY)")
	}

	if req.Count <= 0 || req.Count > 10 {
		req.Count = 5
	}
	if req.Category == "" {
		req.Category = "history_vn"
	}
	if req.Difficulty == "" {
		req.Difficulty = "medium"
	}

	catLabel := categoryLabels[req.Category]
	if catLabel == "" {
		catLabel = req.Category
	}
	diffLabel := difficultyLabels[req.Difficulty]
	if diffLabel == "" {
		diffLabel = req.Difficulty
	}

	var sourceSection string
	if strings.TrimSpace(req.Text) != "" {
		sourceSection = fmt.Sprintf("Đoạn văn bản nguồn:\n---\n%s\n---", strings.TrimSpace(req.Text))
	} else {
		sourceSection = fmt.Sprintf("Chủ đề: %s", strings.TrimSpace(req.Topic))
	}

	systemPrompt := `Bạn là chuyên gia soạn câu hỏi trắc nghiệm về lịch sử, văn hóa, địa lý và kiến thức chung.
Nhiệm vụ: sinh câu hỏi trắc nghiệm 4 lựa chọn (A, B, C, D) chính xác, hấp dẫn.

Quy tắc bắt buộc:
- Câu hỏi và 4 lựa chọn phải bằng tiếng Việt, chính xác về kiến thức
- Chỉ đúng 1 đáp án; 3 đáp án sai phải hợp lý, có tính đánh lừa nhưng không gây nhầm lẫn không công bằng
- hint: gợi ý nhỏ giúp người chơi, KHÔNG tiết lộ trực tiếp đáp án (1-2 câu)
- explanation: giải thích tại sao đáp án đúng là đúng, thêm kiến thức thú vị (2-4 câu)
- Trả về JSON array thuần túy, KHÔNG có markdown code block, KHÔNG có text ngoài JSON`

	userPrompt := fmt.Sprintf(`Sinh %d câu hỏi trắc nghiệm với:
- Danh mục: %s
- Độ khó: %s
- %s

Trả về JSON array (chỉ JSON, không có gì khác):
[
  {
    "content": "Nội dung câu hỏi?",
    "option_a": "Lựa chọn A",
    "option_b": "Lựa chọn B",
    "option_c": "Lựa chọn C",
    "option_d": "Lựa chọn D",
    "correct": "b",
    "hint": "Gợi ý ngắn không lộ đáp án",
    "explanation": "Giải thích tại sao đáp án đúng và kiến thức bổ sung"
  }
]`, req.Count, catLabel, diffLabel, sourceSection)

	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}

	ctx, cancel := context.WithTimeout(context.Background(), 90*time.Second)
	defer cancel()

	resp, err := s.openRouter.Complete(ctx, s.openRouter.cfg.DefaultChatModel, messages, 4096, 0.7)
	if err != nil {
		return nil, fmt.Errorf("AI generation failed: %w", err)
	}

	raw := strings.TrimSpace(resp.Content())
	// Strip markdown code fences if model wrapped the output
	raw = strings.TrimPrefix(raw, "```json")
	raw = strings.TrimPrefix(raw, "```")
	raw = strings.TrimSuffix(raw, "```")
	raw = strings.TrimSpace(raw)

	var generated []GeneratedQuizQuestion
	if err := json.Unmarshal([]byte(raw), &generated); err != nil {
		s.logger.Error("Failed to parse AI quiz response", zap.String("raw", raw), zap.Error(err))
		return nil, fmt.Errorf("không thể phân tích kết quả AI: %w", err)
	}

	for i := range generated {
		generated[i].Category = req.Category
		generated[i].Difficulty = req.Difficulty
		generated[i].Correct = normalizeChoice(generated[i].Correct)
	}

	return generated, nil
}

// GenerateQuizTopics calls the AI to suggest quiz topics for a selected category.
func (s *QuizService) GenerateQuizTopics(req GenerateQuizTopicsRequest) ([]GeneratedQuizTopic, error) {
	if s.openRouter == nil || !s.openRouter.IsConfigured() {
		return nil, fmt.Errorf("dịch vụ AI chưa được cấu hình (thiếu OPENROUTER_API_KEY)")
	}

	if req.Count < 5 || req.Count > 10 {
		req.Count = 8
	}
	if req.Category == "" {
		req.Category = "history_vn"
	}

	catLabel := categoryLabels[req.Category]
	if catLabel == "" {
		catLabel = req.Category
	}

	existingTopics := make([]string, 0, len(req.ExistingTopics))
	for _, topic := range req.ExistingTopics {
		topic = strings.TrimSpace(topic)
		if topic != "" {
			existingTopics = append(existingTopics, topic)
		}
	}

	systemPrompt := `Bạn là biên tập viên nội dung quiz cho app Lịch Số.
Nhiệm vụ: gợi ý các chủ đề nhỏ, cụ thể, hấp dẫn để admin dùng làm đầu vào sinh câu hỏi trắc nghiệm.

Quy tắc bắt buộc:
- Chủ đề phải bằng tiếng Việt, ngắn gọn, rõ phạm vi, dễ dùng để sinh 5-10 câu hỏi
- Ưu tiên chủ đề cụ thể hơn là quá rộng
- Không trùng hoặc quá gần với danh sách chủ đề đã có
- Trả về JSON array thuần túy, KHÔNG có markdown code block, KHÔNG có text ngoài JSON`

	userPrompt := fmt.Sprintf(`Gợi ý %d chủ đề quiz cho danh mục: %s.

Các chủ đề admin đã có, cần tránh trùng:
%s

Trả về JSON array (chỉ JSON, không có gì khác):
[
  {
    "title": "Tên chủ đề ngắn gọn",
    "description": "Mô tả 1 câu về phạm vi nội dung"
  }
]`, req.Count, catLabel, strings.Join(existingTopics, "\n- "))

	messages := []OpenRouterMessage{
		{Role: "system", Content: systemPrompt},
		{Role: "user", Content: userPrompt},
	}

	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()

	resp, err := s.openRouter.Complete(ctx, s.openRouter.cfg.DefaultChatModel, messages, 2048, 0.75)
	if err != nil {
		return nil, fmt.Errorf("AI topic generation failed: %w", err)
	}

	raw := strings.TrimSpace(resp.Content())
	raw = strings.TrimPrefix(raw, "```json")
	raw = strings.TrimPrefix(raw, "```")
	raw = strings.TrimSuffix(raw, "```")
	raw = strings.TrimSpace(raw)

	var topics []GeneratedQuizTopic
	if err := json.Unmarshal([]byte(raw), &topics); err != nil {
		s.logger.Error("Failed to parse AI quiz topic response", zap.String("raw", raw), zap.Error(err))
		return nil, fmt.Errorf("không thể phân tích danh sách chủ đề AI: %w", err)
	}

	out := make([]GeneratedQuizTopic, 0, len(topics))
	seen := map[string]bool{}
	for _, topic := range topics {
		topic.Title = strings.TrimSpace(topic.Title)
		topic.Description = strings.TrimSpace(topic.Description)
		if topic.Title == "" {
			continue
		}
		key := strings.ToLower(topic.Title)
		if seen[key] {
			continue
		}
		seen[key] = true
		out = append(out, topic)
		if len(out) >= req.Count {
			break
		}
	}
	if len(out) == 0 {
		return nil, fmt.Errorf("AI không trả về chủ đề hợp lệ")
	}

	return out, nil
}

// toPublic converts a model question to the normalized DTO consumed by mobile.
func toPublic(q models.QuizQuestion, hideAnswers bool) QuizQuestionPublic {
	correct := ""
	correctAnswer := ""
	explanation := q.Explanation
	if !hideAnswers {
		correct = normalizeChoice(q.Correct)
		correctAnswer = answerTextForChoice(q, correct)
	} else {
		explanation = ""
	}
	return QuizQuestionPublic{
		ID:            q.ID,
		Content:       q.Content,
		OptionA:       q.OptionA,
		OptionB:       q.OptionB,
		OptionC:       q.OptionC,
		OptionD:       q.OptionD,
		Correct:       correct,
		CorrectAnswer: correctAnswer,
		Hint:          strings.TrimSpace(q.Hint),
		Explanation:   explanation,
		Category:      q.Category,
		Difficulty:    q.Difficulty,
		ArticleID:     q.ArticleID,
	}
}

func toPublicList(qs []models.QuizQuestion, hideAnswers bool) []QuizQuestionPublic {
	out := make([]QuizQuestionPublic, len(qs))
	for i, q := range qs {
		out[i] = toPublic(q, hideAnswers)
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

func timeFromMillis(ms int64) time.Time {
	if ms <= 0 {
		return time.Now()
	}
	return time.UnixMilli(ms)
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
	return toPublicList(qs, false), nil
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
	return toPublicList(qs, false), nil
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

	return session, toPublicList(qs, true), nil
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
	if chosen != "" && chosen != "a" && chosen != "b" && chosen != "c" && chosen != "d" {
		return nil, errors.New("chosen must be one of: a, b, c, d")
	}

	// Load question.
	q, err := s.repo.GetQuestionByID(questionID)
	if err != nil {
		return nil, errors.New("question not found")
	}

	correct := normalizeChoice(q.Correct)
	isCorrect := chosen != "" && correct == chosen
	points := 0
	if isCorrect {
		points = pointsPerCorrectAnswer
	}

	scoreV2 := 0
	if isCorrect {
		baseCorrect := 100
		speedBonus := 0
		if timeMs >= 0 && timeMs < 30000 {
			speedBonus = int(float64(30000-timeMs) / 30000.0 * 50.0)
		}
		if speedBonus < 0 {
			speedBonus = 0
		} else if speedBonus > 50 {
			speedBonus = 50
		}

		noAssistBonus := 20
		assistPenalty := 0

		// Query assist usages from DB
		usages, _ := s.repo.GetAssistUsages(sessionID, questionID)
		if len(usages) > 0 {
			noAssistBonus = 0
			for _, u := range usages {
				switch u.AssistType {
				case "hint":
					assistPenalty += 20
				case "fifty_fifty":
					assistPenalty += 30
				case "extra_time":
					assistPenalty += 15
				}
			}
		}

		diffMult := 1.0
		switch strings.ToLower(q.Difficulty) {
		case "medium":
			diffMult = 1.25
		case "hard":
			diffMult = 1.5
		}

		scoreV2 = int(float64(baseCorrect+speedBonus+noAssistBonus-assistPenalty) * diffMult)
		if scoreV2 < 0 {
			scoreV2 = 0
		}
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
		session.ScoreV2 += scoreV2
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
		PointsEarned: scoreV2,
	}, nil
}

func (s *QuizService) applyScoreForUser(userID uuid.UUID, totalPoints int, xp int, now time.Time) (*models.QuizScore, int, error) {
	loc := vnLocation()
	nowVN := now.In(loc)
	today := startOfDayInLocation(nowVN, loc)
	existing, err := s.repo.GetScore(userID)
	publicInfo, publicInfoErr := s.repo.GetUserPublicInfo(userID)
	if publicInfoErr != nil {
		s.logger.Warn("Failed to resolve user public info for quiz score",
			zap.String("user_id", userID.String()),
			zap.Error(publicInfoErr),
		)
	}

	var scoreRow models.QuizScore
	switch {
	case err != nil && errors.Is(err, gorm.ErrRecordNotFound):
		scoreRow = models.QuizScore{
			UserID:     userID,
			TotalScore: totalPoints,
			WeekScore:  totalPoints,
			MonthScore: totalPoints,
			XP:         xp,
			CurStreak:  1,
			BestStreak: 1,
			LastQuiz:   &today,
			UpdatedAt:  now,
		}
		if publicInfo != nil {
			scoreRow.DisplayName = publicInfo.DisplayName
			scoreRow.AvatarURL = publicInfo.AvatarURL
		}
	case err == nil:
		scoreRow = *existing
		if publicInfo != nil {
			scoreRow.DisplayName = publicInfo.DisplayName
			scoreRow.AvatarURL = publicInfo.AvatarURL
		}
		scoreRow.TotalScore += totalPoints
		scoreRow.XP += xp
		scoreRow.UpdatedAt = now

		lastQuizDay := time.Time{}
		hasLastQuiz := existing.LastQuiz != nil
		if hasLastQuiz {
			lastQuizDay = startOfDayInLocation(*existing.LastQuiz, loc)
		}

		// Reset week_score at the start of a new ISO week in VN time.
		if existing.LastQuiz != nil {
			nowYear, nowWeek := today.ISOWeek()
			lastYear, lastWeek := lastQuizDay.ISOWeek()
			if nowYear != lastYear || nowWeek != lastWeek {
				scoreRow.WeekScore = 0
			}
		}
		scoreRow.WeekScore += totalPoints

		// Reset month_score at the start of a new calendar month in VN time.
		if existing.LastQuiz != nil {
			if today.Year() != lastQuizDay.Year() || today.Month() != lastQuizDay.Month() {
				scoreRow.MonthScore = 0
			}
		}
		scoreRow.MonthScore += totalPoints

		if hasLastQuiz {
			diff := today.Sub(lastQuizDay)
			switch {
			case diff == 24*time.Hour:
				scoreRow.CurStreak++
			case diff == 0:
			default:
				scoreRow.CurStreak = 1
			}
		} else {
			scoreRow.CurStreak = 1
		}

		if scoreRow.CurStreak > scoreRow.BestStreak {
			scoreRow.BestStreak = scoreRow.CurStreak
		}
		scoreRow.LastQuiz = &today
	default:
		return nil, 0, fmt.Errorf("failed to fetch user score: %w", err)
	}

	if err := s.repo.UpsertScore(&scoreRow); err != nil {
		s.logger.Error("Failed to upsert quiz score", zap.Error(err))
	}

	rank, _ := s.repo.GetUserRank(userID, "weekly")
	return &scoreRow, rank, nil
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

	// 1. Calculate App Points & XP Earned
	appPointsEarned := 0
	xpEarned := 0
	sessionTitle := ""

	correctCount := 0
	for _, ans := range answers {
		if ans.IsCorrect {
			correctCount++
		}
	}

	if session.SessionType == "daily" {
		// Daily Quiz rewards
		appPointsEarned += 5  // Entering play
		appPointsEarned += 20 // Completion
		appPointsEarned += correctCount * 2

		if correctCount >= 10 {
			appPointsEarned += 10
		}
		if correctCount >= 15 {
			appPointsEarned += 30
		}

		// Daily Quiz XP
		xpEarned += 50
		xpEarned += correctCount * 5
		if correctCount >= 15 {
			xpEarned += 100
		}

		// Session Title based on correct count
		if correctCount == 15 {
			sessionTitle = "Trạng Nguyên Lịch Số"
		} else if correctCount == 14 {
			sessionTitle = "Bảng Nhãn Tri Thức"
		} else if correctCount == 13 {
			sessionTitle = "Thám Hoa Sử Việt"
		} else if correctCount >= 11 {
			sessionTitle = "Sĩ Tử Uyên Bác"
		} else if correctCount >= 9 {
			sessionTitle = "Người Giữ Mạch Sử"
		} else if correctCount >= 6 {
			sessionTitle = "Học Giả Tập Sự"
		} else {
			sessionTitle = "Tân Binh Lịch Số"
		}
	} else {
		// Topic Quiz rewards
		appPointsEarned += 5 + correctCount*1
		xpEarned += 30 + correctCount*3
		sessionTitle = "Người Ôn Tập Chủ Đề"
	}

	// Credit App Points if wallet service is available
	if s.points != nil {
		idempKey := fmt.Sprintf("quiz_session_finish_%s", session.ID.String())
		_, err = s.points.CreditPoints(userID, appPointsEarned, "quiz_"+session.SessionType+"_complete", nil, &idempKey, nil)
		if err != nil {
			s.logger.Error("Failed to credit App Points after quiz finish", zap.Error(err))
		}
	}

	// 2. Update Category Mastery for each answer
	var unlockedMasteryTitles []string
	for _, ans := range answers {
		q, err := s.repo.GetQuestionByID(ans.QuestionID)
		if err == nil {
			m, mErr := s.updateCategoryMastery(userID, q.Category, ans.IsCorrect)
			if mErr == nil && m != nil && m.Title != "" {
				if m.LastUnlockedAt != nil && m.LastUnlockedAt.After(now.Add(-5*time.Second)) {
					found := false
					for _, t := range unlockedMasteryTitles {
						if t == m.Title {
							found = true
							break
						}
					}
					if !found {
						unlockedMasteryTitles = append(unlockedMasteryTitles, m.Title)
					}
				}
			}
		}
	}

	// Apply Score for User (using ScoreV2 for leaderboard)
	totalPointsForLeaderboard := session.ScoreV2
	if totalPointsForLeaderboard == 0 {
		totalPointsForLeaderboard = session.Score
	}

	scoreRow, rank, err := s.applyScoreForUser(userID, totalPointsForLeaderboard, xpEarned, now)
	if err != nil {
		return nil, err
	}

	// 3. Check and unlock badges
	elapsedSeconds := 0
	if session.FinishedAt != nil {
		elapsedSeconds = int(session.FinishedAt.Sub(session.StartedAt).Seconds())
	}
	unlockedBadges := s.checkAndUnlockBadges(userID, session, answers, scoreRow, elapsedSeconds)

	// Invalidate leaderboard cache.
	ctx := context.Background()
	for _, period := range []string{"weekly", "monthly", "alltime"} {
		_ = s.cache.DeleteKey(ctx, leaderboardCacheKey(period))
	}

	return &SessionResult{
		SessionID:             session.ID,
		Score:                 session.Score,
		ScoreV2:               session.ScoreV2,
		Total:                 session.Total,
		Answers:               answers,
		PointsEarned:          totalPointsForLeaderboard,
		BonusPoints:           0,
		NewWeekScore:          scoreRow.WeekScore,
		Rank:                  rank,
		AppPointsEarned:       appPointsEarned,
		XPEarned:              xpEarned,
		SessionTitle:          sessionTitle,
		UnlockedMasteryTitles: unlockedMasteryTitles,
		UnlockedBadges:        unlockedBadges,
	}, nil
}

// SyncOfflineSessions imports locally completed guest sessions into the user's account.
func (s *QuizService) SyncOfflineSessions(userID uuid.UUID, sessions []OfflineQuizSession) (*SyncOfflineQuizResponse, error) {
	syncedIDs := make([]string, 0, len(sessions))
	skippedIDs := make([]string, 0)

	for _, session := range sessions {
		clientID := strings.TrimSpace(session.ClientSessionID)
		if clientID == "" {
			continue
		}

		if _, err := s.repo.GetSessionByClientID(clientID); err == nil {
			skippedIDs = append(skippedIDs, clientID)
			continue
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("failed to check existing offline session: %w", err)
		}

		if len(session.QuestionIDs) == 0 {
			skippedIDs = append(skippedIDs, clientID)
			continue
		}

		answerByQuestion := make(map[int64]OfflineQuizAnswer, len(session.Answers))
		for _, ans := range session.Answers {
			answerByQuestion[ans.QuestionID] = ans
		}

		answers := make([]models.QuizAnswer, 0, len(session.Answers))
		score := 0
		for _, questionID := range session.QuestionIDs {
			q, err := s.repo.GetQuestionByID(questionID)
			if err != nil {
				return nil, fmt.Errorf("question not found during sync: %d", questionID)
			}
			ans, ok := answerByQuestion[questionID]
			if !ok {
				continue
			}
			chosen := normalizeChoice(ans.Chosen)
			correct := normalizeChoice(q.Correct)
			isCorrect := chosen != "" && chosen == correct
			if isCorrect {
				score += pointsPerCorrectAnswer
			}
			answers = append(answers, models.QuizAnswer{
				QuestionID: questionID,
				Chosen:     chosen,
				IsCorrect:  isCorrect,
				TimeMs:     ans.TimeMs,
			})
		}

		startedAt := timeFromMillis(session.StartedAtMs)
		finishedAt := timeFromMillis(session.FinishedAtMs)
		clientIDCopy := clientID
		sessionRow := &models.QuizSession{
			UserID:          userID,
			ClientSessionID: &clientIDCopy,
			SessionType:     session.SessionType,
			Category:        session.Category,
			QuestionIDs:     session.QuestionIDs,
			Score:           score,
			Total:           len(session.QuestionIDs),
			Completed:       true,
			StartedAt:       startedAt,
			FinishedAt:      &finishedAt,
		}
		answersJSON, _ := json.Marshal(answers)
		sessionRow.Answers = answersJSON

		if err := s.repo.CreateSession(sessionRow); err != nil {
			return nil, fmt.Errorf("failed to import offline session: %w", err)
		}

		totalPoints := score
		if sessionRow.Total > 0 && score/pointsPerCorrectAnswer == sessionRow.Total {
			totalPoints += bonusPerfectScore
		}

		// Calculate XP for offline session
		xpEarned := 0
		correctCount := score / pointsPerCorrectAnswer
		if session.SessionType == "daily" {
			xpEarned += 50 + correctCount*5
			if correctCount >= 15 {
				xpEarned += 100
			}
		} else {
			xpEarned += 30 + correctCount*3
		}

		if _, _, err := s.applyScoreForUser(userID, totalPoints, xpEarned, finishedAt); err != nil {
			return nil, err
		}

		syncedIDs = append(syncedIDs, clientID)
	}

	return &SyncOfflineQuizResponse{
		SyncedSessionIDs:  syncedIDs,
		SkippedSessionIDs: skippedIDs,
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
	return fmt.Sprintf("quiz:leaderboard:v2:%s", period)
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

	weekScore, monthScore, err := s.repo.GetUserPeriodScores(userID)
	if err != nil {
		return nil, fmt.Errorf("failed to get user period scores: %w", err)
	}

	return &MyRankResponse{
		Rank:       rank,
		WeekScore:  weekScore,
		MonthScore: monthScore,
		TotalScore: scoreRow.TotalScore,
		CurStreak:  scoreRow.CurStreak,
	}, nil
}

func startOfWeekMonday(t time.Time) time.Time {
	weekday := int(t.Weekday())
	if weekday == 0 {
		weekday = 7
	}
	return time.Date(t.Year(), t.Month(), t.Day(), 0, 0, 0, 0, t.Location()).
		AddDate(0, 0, -(weekday - 1))
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

// ListQuestions returns paginated questions for admin, with optional full-text search.
func (s *QuizService) ListQuestions(page, limit int, category, difficulty, search string) ([]models.QuizQuestion, int64, error) {
	if page <= 0 {
		page = 1
	}
	if limit <= 0 || limit > 100 {
		limit = 20
	}
	return s.repo.ListQuestionsAdmin(page, limit, category, difficulty, search)
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

// RandomizeDailySet picks `count` random active questions and saves them as the daily set for `date`.
func (s *QuizService) RandomizeDailySet(adminID uuid.UUID, date time.Time, count int, category, difficulty string, fromID, toID int64) ([]int64, error) {
	if count <= 0 {
		count = 20
	}
	ids, err := s.repo.GetRandomActiveQuestionIDs(count, category, difficulty, fromID, toID)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch random questions: %w", err)
	}
	if len(ids) == 0 {
		return nil, errors.New("không có câu hỏi nào phù hợp để chọn ngẫu nhiên")
	}
	if err := s.SetDailySet(adminID, date, ids); err != nil {
		return nil, err
	}
	return ids, nil
}

// updateCategoryMastery updates category answered/correct count, rate, streak, level, and unlocks titles.
func (s *QuizService) updateCategoryMastery(userID uuid.UUID, category string, isCorrect bool) (*models.QuizCategoryMastery, error) {
	if category == "" {
		category = "history_vn"
	}

	db := s.repo.DB()
	var mastery models.QuizCategoryMastery
	err := db.Where("user_id = ? AND category = ?", userID, category).First(&mastery).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		mastery = models.QuizCategoryMastery{
			UserID:   userID,
			Category: category,
		}
		if err := db.Create(&mastery).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	}

	mastery.AnsweredCount++
	if isCorrect {
		mastery.CorrectCount++
		mastery.CurrentStreak++
		if mastery.CurrentStreak > mastery.BestStreak {
			mastery.BestStreak = mastery.CurrentStreak
		}
	} else {
		mastery.CurrentStreak = 0
	}

	mastery.CorrectRate = float64(mastery.CorrectCount) / float64(mastery.AnsweredCount) * 100.0
	mastery.UpdatedAt = time.Now()

	// Title Mapping based on strategy docs
	titles := map[string][]string{
		"history":   {"Sử Sinh", "Sử Gia", "Quốc Sử Quán", "Bậc Thầy Sử Việt", "Đại Sử Gia"},
		"culture":   {"Người Am Tường Phong Tục", "Nhà Văn Hoá", "Người Giữ Nếp Xưa", "Bậc Thầy Văn Hoá", "Quốc Hồn Quốc Tuý"},
		"festival":  {"Người Rành Lễ Tết", "Sứ Giả Mùa Lễ", "Chuyên Gia Lễ Hội", "Bậc Thầy Phong Tục", "Lễ Quan Lịch Số"},
		"dynasty":   {"Sĩ Tử Triều Đại", "Người Thuộc Long Mạch", "Chuyên Gia Vương Triều", "Bậc Thầy Triều Đại", "Quốc Triều Thông Giám"},
		"figure":    {"Người Kể Chuyện Danh Nhân", "Tri Kỷ Anh Hùng", "Chuyên Gia Nhân Vật", "Bậc Thầy Danh Nhân", "Danh Nhân Ký Sự"},
		"geography": {"Người Theo Dấu Địa Danh", "Lữ Khách Sử Việt", "Chuyên Gia Địa Danh", "Bậc Thầy Non Sông", "Sơn Hà Ký"},
	}

	// Resolve title category key (defaults to history if not matched)
	titleKey := "history"
	for k := range titles {
		if strings.Contains(strings.ToLower(category), k) {
			titleKey = k
			break
		}
	}

	oldLevel := mastery.MasteryLevel
	newLevel := 0

	cc := mastery.CorrectCount
	cr := mastery.CorrectRate

	if cc >= 200 && cr >= 90 {
		newLevel = 5
	} else if cc >= 100 && cr >= 85 {
		newLevel = 4
	} else if cc >= 60 && cr >= 80 {
		newLevel = 3
	} else if cc >= 30 && cr >= 70 {
		newLevel = 2
	} else if cc >= 10 && cr >= 60 {
		newLevel = 1
	}

	if newLevel > oldLevel {
		mastery.MasteryLevel = newLevel
		tList := titles[titleKey]
		if newLevel-1 < len(tList) {
			mastery.Title = tList[newLevel-1]
		}
		now := time.Now()
		mastery.LastUnlockedAt = &now
	}

	if err := db.Save(&mastery).Error; err != nil {
		return nil, err
	}

	return &mastery, nil
}

// checkAndUnlockBadges checks user quiz behavior and awards badges.
func (s *QuizService) checkAndUnlockBadges(userID uuid.UUID, session *models.QuizSession, answers []models.QuizAnswer, scoreRow *models.QuizScore, elapsedSeconds int) []string {
	var unlocked []string
	db := s.repo.DB()

	// 1. Check streak 7 days
	if scoreRow.CurStreak >= 7 {
		s.grantBadge(userID, "streak_7", "daily_streak", &unlocked)
	}

	correctCount := 0
	for _, ans := range answers {
		if ans.IsCorrect {
			correctCount++
		}
	}

	// 2. Check perfect 15
	if session.SessionType == "daily" && session.Total == 15 && correctCount == 15 {
		s.grantBadge(userID, "perfect_15", "perfect_session", &unlocked)
	}

	// 3. Check no assists (>= 12 correct without using hints/50-50/extra time)
	if session.SessionType == "daily" && correctCount >= 12 {
		var assistCount int64
		db.Model(&models.QuizAssistUsage{}).Where("session_id = ?", session.ID).Count(&assistCount)
		if assistCount == 0 {
			s.grantBadge(userID, "no_assists", "no_assists_session", &unlocked)
		}
	}

	// 4. Check speed run (under 5 mins, correct >= 12)
	if session.SessionType == "daily" && correctCount >= 12 && elapsedSeconds > 0 && elapsedSeconds < 300 {
		s.grantBadge(userID, "speed_run", "speed_run_session", &unlocked)
	}

	return unlocked
}

// grantBadge inserts a user badge record if it doesn't already exist.
func (s *QuizService) grantBadge(userID uuid.UUID, badgeKey string, source string, unlockedList *[]string) {
	db := s.repo.DB()
	var badge models.UserBadge
	err := db.Where("user_id = ? AND badge_key = ?", userID, badgeKey).First(&badge).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		badge = models.UserBadge{
			UserID:     userID,
			BadgeKey:   badgeKey,
			Source:     source,
			UnlockedAt: time.Now(),
		}
		if err := db.Create(&badge).Error; err == nil {
			*unlockedList = append(*unlockedList, badgeKey)
		}
	}
}
