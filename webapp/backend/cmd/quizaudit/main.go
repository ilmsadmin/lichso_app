// Command quizaudit detects and (optionally) resets farmed quiz scores.
//
// Background: the quiz leaderboard sums score over every completed quiz_session in
// the period. Before the anti-farming caps were added, a user could complete an
// unbounded number of sessions per day (especially via the offline-sync endpoint),
// inflating their leaderboard score by months' worth in a single day.
//
// This tool re-applies the same daily caps retroactively:
//   - at most `-cap` completed sessions per VN day earn score;
//   - at most `-daily-cap` "daily"-type sessions per VN day earn score.
//
// Sessions beyond the cap (ordered by finished_at, earliest first) have their
// score/score_v2 zeroed so they no longer contribute to the leaderboard SUM. Each
// affected user's quiz_scores aggregate (total/week/month/streaks/xp) is then
// recomputed from the surviving sessions.
//
// Usage:
//
//	go run ./cmd/quizaudit                 # dry-run: report offenders, change nothing
//	go run ./cmd/quizaudit -apply          # apply the reset
//	go run ./cmd/quizaudit -cap 15 -daily-cap 1 -apply
//	go run ./cmd/quizaudit -user <uuid> -apply   # limit to one user
package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"sort"
	"time"

	"github.com/zplus/lichso/internal/config"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func vnLocation() *time.Location {
	loc, err := time.LoadLocation("Asia/Ho_Chi_Minh")
	if err != nil {
		return time.FixedZone("ICT", 7*60*60)
	}
	return loc
}

type sessionRow struct {
	ID          string    `gorm:"column:id"`
	UserID      string    `gorm:"column:user_id"`
	SessionType string    `gorm:"column:session_type"`
	Score       int       `gorm:"column:score"`
	ScoreV2     int       `gorm:"column:score_v2"`
	FinishedAt  time.Time `gorm:"column:finished_at"`
	Answers     []byte    `gorm:"column:answers"`
}

func effectiveScore(s sessionRow) int {
	if s.ScoreV2 != 0 {
		return s.ScoreV2
	}
	return s.Score
}

type quizAnswer struct {
	IsCorrect bool `json:"is_correct"`
}

func correctCount(answers []byte) int {
	var a []quizAnswer
	if json.Unmarshal(answers, &a) != nil {
		return 0
	}
	n := 0
	for _, x := range a {
		if x.IsCorrect {
			n++
		}
	}
	return n
}

func xpForSession(sessionType string, cc int) int {
	if sessionType == "daily" {
		xp := 50 + cc*5
		if cc >= 15 {
			xp += 100
		}
		return xp
	}
	return 30 + cc*3
}

func main() {
	apply := flag.Bool("apply", false, "apply changes (default is dry-run)")
	dayCap := flag.Int("cap", 15, "max scoring sessions per VN day")
	dailyCap := flag.Int("daily-cap", 1, "max scoring 'daily'-type sessions per VN day")
	onlyUser := flag.String("user", "", "limit to a single user UUID (optional)")
	flag.Parse()

	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("load config: %v", err)
	}
	db, err := gorm.Open(postgres.Open(cfg.Database.DSN()), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		log.Fatalf("connect postgres: %v", err)
	}

	loc := vnLocation()
	mode := "DRY-RUN"
	if *apply {
		mode = "APPLY"
	}
	fmt.Printf("🔎 Quiz score audit  [%s]  cap=%d daily-cap=%d\n", mode, *dayCap, *dailyCap)
	fmt.Println("======================================================")

	// Collect distinct users with completed sessions.
	var userIDs []string
	q := db.Table("quiz_sessions").Where("completed = ?", true).Distinct().Pluck("user_id", &userIDs)
	if *onlyUser != "" {
		q = db.Table("quiz_sessions").
			Where("completed = ? AND user_id = ?", true, *onlyUser).
			Distinct().Pluck("user_id", &userIDs)
	}
	if err := q.Error; err != nil {
		log.Fatalf("list users: %v", err)
	}

	totalOffenders := 0
	totalRemoved := 0

	for _, uid := range userIDs {
		var sessions []sessionRow
		if err := db.Table("quiz_sessions").
			Select("id, user_id, session_type, score, score_v2, finished_at, answers").
			Where("user_id = ? AND completed = ? AND finished_at IS NOT NULL", uid, true).
			Order("finished_at ASC").
			Scan(&sessions).Error; err != nil {
			log.Printf("⚠️  load sessions for %s: %v", uid, err)
			continue
		}
		if len(sessions) == 0 {
			continue
		}

		// Group by VN day.
		byDay := map[string][]sessionRow{}
		dayOrder := []string{}
		for _, s := range sessions {
			day := s.FinishedAt.In(loc).Format("2006-01-02")
			if _, ok := byDay[day]; !ok {
				dayOrder = append(dayOrder, day)
			}
			byDay[day] = append(byDay[day], s)
		}

		// Decide which sessions to zero (exceed the per-day caps).
		var idsToZero []string
		keptDays := map[string]bool{}
		newTotal, newXP := 0, 0
		oldTotal := 0
		for _, s := range sessions {
			oldTotal += effectiveScore(s)
		}

		for _, day := range dayOrder {
			scored, dailyScored := 0, 0
			for _, s := range byDay[day] {
				capped := scored >= *dayCap
				if s.SessionType == "daily" && dailyScored >= *dailyCap {
					capped = true
				}
				if capped {
					if effectiveScore(s) != 0 {
						idsToZero = append(idsToZero, s.ID)
					}
					continue
				}
				scored++
				if s.SessionType == "daily" {
					dailyScored++
				}
				keptDays[day] = true
				newTotal += effectiveScore(s)
				newXP += xpForSession(s.SessionType, correctCount(s.Answers))
			}
		}

		removed := oldTotal - newTotal
		if len(idsToZero) == 0 {
			continue // nothing to do for this user
		}
		totalOffenders++
		totalRemoved += removed

		// Recompute streaks from kept active days.
		curStreak, bestStreak := recomputeStreaks(keptDays, loc)
		// Recompute week/month from kept sessions in current VN period.
		newWeek, newMonth := recomputePeriods(sessions, idsToZero, loc)

		fmt.Printf("• user=%s  sessions=%d  zero=%d  score %d → %d (−%d)  streak→cur:%d/best:%d\n",
			uid, len(sessions), len(idsToZero), oldTotal, newTotal, removed, curStreak, bestStreak)

		if !*apply {
			continue
		}

		err := db.Transaction(func(tx *gorm.DB) error {
			// Zero surplus sessions in batches to stay under parameter limits.
			for start := 0; start < len(idsToZero); start += 500 {
				end := start + 500
				if end > len(idsToZero) {
					end = len(idsToZero)
				}
				if err := tx.Table("quiz_sessions").
					Where("id IN ?", idsToZero[start:end]).
					Updates(map[string]any{"score": 0, "score_v2": 0}).Error; err != nil {
					return err
				}
			}
			// Recompute the aggregate row (only if it exists).
			res := tx.Table("quiz_scores").Where("user_id = ?", uid).Updates(map[string]any{
				"total_score": newTotal,
				"week_score":  newWeek,
				"month_score": newMonth,
				"cur_streak":  curStreak,
				"best_streak": bestStreak,
				"xp":          newXP,
				"updated_at":  time.Now(),
			})
			return res.Error
		})
		if err != nil {
			log.Printf("⚠️  apply for %s failed: %v", uid, err)
		}
	}

	fmt.Println("======================================================")
	fmt.Printf("Offenders: %d   Total leaderboard score removed: %d\n", totalOffenders, totalRemoved)
	if !*apply {
		fmt.Println("Dry-run only — re-run with -apply to persist. Remember to clear the")
		fmt.Println("leaderboard cache (quiz:leaderboard:v2:*) afterwards, or wait 15 min TTL.")
	} else {
		fmt.Println("✅ Applied. Clear Redis keys quiz:leaderboard:v2:* to refresh immediately.")
	}
}

// recomputeStreaks returns (cur, best) consecutive-day streaks from the set of
// VN days on which the user has at least one surviving (scoring) session.
func recomputeStreaks(keptDays map[string]bool, loc *time.Location) (int, int) {
	if len(keptDays) == 0 {
		return 0, 0
	}
	days := make([]time.Time, 0, len(keptDays))
	for d := range keptDays {
		t, err := time.ParseInLocation("2006-01-02", d, loc)
		if err == nil {
			days = append(days, t)
		}
	}
	sort.Slice(days, func(i, j int) bool { return days[i].Before(days[j]) })

	best, run := 1, 1
	for i := 1; i < len(days); i++ {
		if days[i].Sub(days[i-1]) == 24*time.Hour {
			run++
		} else {
			run = 1
		}
		if run > best {
			best = run
		}
	}

	// Current streak: count back from the last active day only if it is today or
	// yesterday (VN); otherwise the streak is broken.
	today := time.Date(time.Now().In(loc).Year(), time.Now().In(loc).Month(), time.Now().In(loc).Day(), 0, 0, 0, 0, loc)
	last := days[len(days)-1]
	cur := 0
	if last.Equal(today) || last.Equal(today.AddDate(0, 0, -1)) {
		cur = 1
		for i := len(days) - 1; i > 0; i-- {
			if days[i].Sub(days[i-1]) == 24*time.Hour {
				cur++
			} else {
				break
			}
		}
	}
	return cur, best
}

// recomputePeriods sums surviving session scores within the current VN week/month.
func recomputePeriods(sessions []sessionRow, zeroedIDs []string, loc *time.Location) (int, int) {
	zeroed := map[string]bool{}
	for _, id := range zeroedIDs {
		zeroed[id] = true
	}

	nowVN := time.Now().In(loc)
	weekday := int(nowVN.Weekday())
	if weekday == 0 {
		weekday = 7
	}
	weekStart := time.Date(nowVN.Year(), nowVN.Month(), nowVN.Day(), 0, 0, 0, 0, loc).AddDate(0, 0, -(weekday - 1))
	weekEnd := weekStart.AddDate(0, 0, 7)
	monthStart := time.Date(nowVN.Year(), nowVN.Month(), 1, 0, 0, 0, 0, loc)
	monthEnd := monthStart.AddDate(0, 1, 0)

	week, month := 0, 0
	for _, s := range sessions {
		if zeroed[s.ID] {
			continue
		}
		f := s.FinishedAt.In(loc)
		val := effectiveScore(s)
		if !f.Before(weekStart) && f.Before(weekEnd) {
			week += val
		}
		if !f.Before(monthStart) && f.Before(monthEnd) {
			month += val
		}
	}
	return week, month
}
