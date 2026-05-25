package services

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/services/lunar"
	"go.uber.org/zap"
)

// DailyContentService handles daily content schedule business logic.
type DailyContentService struct {
	repo             *repositories.DailyContentRepository
	quoteRepo        *repositories.QuoteRepository
	eventRepo        *repositories.EventRepository
	articleRepo      *repositories.ArticleRepository
	famousPersonRepo *repositories.FamousPersonRepository
	folkFestivalRepo *repositories.FolkFestivalRepository
	logger           *zap.Logger
}

// NewDailyContentService creates a new DailyContentService.
func NewDailyContentService(
	repo *repositories.DailyContentRepository,
	quoteRepo *repositories.QuoteRepository,
	eventRepo *repositories.EventRepository,
	articleRepo *repositories.ArticleRepository,
	famousPersonRepo *repositories.FamousPersonRepository,
	folkFestivalRepo *repositories.FolkFestivalRepository,
	logger *zap.Logger,
) *DailyContentService {
	return &DailyContentService{
		repo:             repo,
		quoteRepo:        quoteRepo,
		eventRepo:        eventRepo,
		articleRepo:      articleRepo,
		famousPersonRepo: famousPersonRepo,
		folkFestivalRepo: folkFestivalRepo,
		logger:           logger,
	}
}

// Create creates a new daily content schedule.
func (s *DailyContentService) Create(createdBy uuid.UUID, req *dto.CreateDailyContentRequest) (*dto.DailyContentResponse, error) {
	schedule := &models.DailyContentSchedule{
		ContentType:     req.ContentType,
		ScheduleMode:    req.ScheduleMode,
		CustomTitle:     req.CustomTitle,
		CustomContent:   req.CustomContent,
		CustomImage:     req.CustomImage,
		DayOfYear:       req.DayOfYear,
		RecurringMonth:  req.RecurringMonth,
		RecurringDay:    req.RecurringDay,
		LunarMonth:      req.LunarMonth,
		LunarDay:        req.LunarDay,
		YearFilter:      req.YearFilter,
		DisplayPriority: req.DisplayPriority,
		CreatedBy:       &createdBy,
		UpdatedBy:       &createdBy,
	}

	// Parse content ID
	if req.ContentID != "" {
		contentID, err := uuid.Parse(req.ContentID)
		if err != nil {
			return nil, fmt.Errorf("invalid content_id: %w", err)
		}
		schedule.ContentID = &contentID
	}

	// Parse display section
	if req.DisplaySection != "" {
		schedule.DisplaySection = req.DisplaySection
	}

	// Parse fixed date
	if req.FixedDate != "" {
		t, err := time.Parse("2006-01-02", req.FixedDate)
		if err != nil {
			return nil, fmt.Errorf("invalid fixed_date format (YYYY-MM-DD): %w", err)
		}
		schedule.FixedDate = &t
	}

	// Parse date range
	if req.StartDate != "" {
		t, err := time.Parse("2006-01-02", req.StartDate)
		if err == nil {
			schedule.StartDate = &t
		}
	}
	if req.EndDate != "" {
		t, err := time.Parse("2006-01-02", req.EndDate)
		if err == nil {
			schedule.EndDate = &t
		}
	}

	if err := s.repo.Create(schedule); err != nil {
		s.logger.Error("Failed to create daily content schedule", zap.Error(err))
		return nil, fmt.Errorf("failed to create schedule: %w", err)
	}

	return s.toResponse(schedule), nil
}

// GetByID returns a schedule by ID with resolved content.
func (s *DailyContentService) GetByID(id uuid.UUID) (*dto.DailyContentResponse, error) {
	schedule, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("schedule not found: %w", err)
	}
	return s.toResponseWithContent(schedule), nil
}

// GetContentForDate returns all scheduled content for a specific date (public API).
func (s *DailyContentService) GetContentForDate(date time.Time) (*dto.DayContentResponse, error) {
	response := &dto.DayContentResponse{
		Date: date.Format("2006-01-02"),
	}

	// Get scheduled content
	schedules, err := s.repo.GetByDate(date)
	if err != nil {
		s.logger.Error("Failed to get schedules for date", zap.Error(err), zap.Time("date", date))
		return response, nil
	}

	// Also get auto-content from existing tables
	month := int(date.Month())
	day := date.Day()

	// Auto: quotes by day_of_year
	dayOfYear := date.YearDay()
	quotes, _ := s.quoteRepo.GetByDayOfYear(dayOfYear)
	for _, q := range quotes {
		response.Quotes = append(response.Quotes, q)
	}

	// Auto: events by date (solar + lunar)
	lunarDate := lunar.SolarToLunar(day, month, date.Year(), 7)
	events, _ := s.eventRepo.GetByDate(month, day, lunarDate.Month, lunarDate.Day)
	for _, e := range events {
		response.Events = append(response.Events, e)
	}

	// Auto: famous people by birthday
	people, _ := s.famousPersonRepo.GetByBirthday(month, day)
	for _, p := range people {
		response.People = append(response.People, p)
	}

	// Auto: folk festivals by solar date
	festivals, _ := s.folkFestivalRepo.GetBySolarDate(month, day)
	for _, f := range festivals {
		response.Festivals = append(response.Festivals, f)
	}

	// Process scheduled content (manual assignments by admin)
	for _, schedule := range schedules {
		switch schedule.ContentType {
		case models.ContentTypeQuote:
			if schedule.ContentID != nil {
				quote, err := s.quoteRepo.GetByID(*schedule.ContentID)
				if err == nil {
					response.Quotes = append(response.Quotes, quote)
				}
			}
		case models.ContentTypeEvent:
			if schedule.ContentID != nil {
				event, err := s.eventRepo.GetByID(*schedule.ContentID)
				if err == nil {
					response.Events = append(response.Events, event)
				}
			}
		case models.ContentTypeArticle:
			if schedule.ContentID != nil {
				article, err := s.articleRepo.GetByID(*schedule.ContentID)
				if err == nil {
					response.Articles = append(response.Articles, *toRelationArticleListResponse(article))
				}
			}
		case models.ContentTypeFamousPerson:
			if schedule.ContentID != nil {
				person, err := s.famousPersonRepo.GetByID(*schedule.ContentID)
				if err == nil {
					response.People = append(response.People, person)
				}
			}
		case models.ContentTypeFolkFestival:
			if schedule.ContentID != nil {
				festival, err := s.folkFestivalRepo.GetByID(*schedule.ContentID)
				if err == nil {
					response.Festivals = append(response.Festivals, festival)
				}
			}
		case models.ContentTypeCustom:
			response.Custom = append(response.Custom, *s.toResponse(&schedule))
		}
	}

	// Add random articles if no articles scheduled
	if len(response.Articles) == 0 {
		randomArticles, err := s.getRandomPublishedArticles(5)
		if err == nil {
			response.Articles = randomArticles
		}
	}

	return response, nil
}

// List returns all schedules with pagination (admin).
func (s *DailyContentService) List(page, limit int) ([]dto.DailyContentResponse, int64, error) {
	schedules, total, err := s.repo.List(page, limit)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch schedules: %w", err)
	}

	results := make([]dto.DailyContentResponse, len(schedules))
	for i, sch := range schedules {
		results[i] = *s.toResponseWithContent(&sch)
	}
	return results, total, nil
}

// ListByType returns schedules filtered by content type (admin).
func (s *DailyContentService) ListByType(contentType string, page, limit int) ([]dto.DailyContentResponse, int64, error) {
	schedules, total, err := s.repo.GetByContentType(contentType, page, limit)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch schedules: %w", err)
	}

	results := make([]dto.DailyContentResponse, len(schedules))
	for i, sch := range schedules {
		results[i] = *s.toResponseWithContent(&sch)
	}
	return results, total, nil
}

// Update updates a daily content schedule.
func (s *DailyContentService) Update(id, updatedBy uuid.UUID, req *dto.UpdateDailyContentRequest) (*dto.DailyContentResponse, error) {
	schedule, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("schedule not found: %w", err)
	}

	schedule.UpdatedBy = &updatedBy

	if req.ContentType != nil {
		schedule.ContentType = *req.ContentType
	}
	if req.ContentID != nil {
		if *req.ContentID == "" {
			schedule.ContentID = nil
		} else {
			contentID, err := uuid.Parse(*req.ContentID)
			if err == nil {
				schedule.ContentID = &contentID
			}
		}
	}
	if req.CustomTitle != nil {
		schedule.CustomTitle = *req.CustomTitle
	}
	if req.CustomContent != nil {
		schedule.CustomContent = *req.CustomContent
	}
	if req.CustomImage != nil {
		schedule.CustomImage = *req.CustomImage
	}
	if req.ScheduleMode != nil {
		newMode := *req.ScheduleMode
		// Clear fields from previous schedule mode when mode changes
		if newMode != schedule.ScheduleMode {
			schedule.FixedDate = nil
			schedule.DayOfYear = nil
			schedule.RecurringMonth = nil
			schedule.RecurringDay = nil
			schedule.LunarMonth = nil
			schedule.LunarDay = nil
		}
		schedule.ScheduleMode = newMode
	}
	if req.FixedDate != nil {
		if *req.FixedDate == "" {
			schedule.FixedDate = nil
		} else {
			t, err := time.Parse("2006-01-02", *req.FixedDate)
			if err == nil {
				schedule.FixedDate = &t
			}
		}
	}
	if req.DayOfYear != nil {
		schedule.DayOfYear = req.DayOfYear
	}
	if req.RecurringMonth != nil {
		schedule.RecurringMonth = req.RecurringMonth
	}
	if req.RecurringDay != nil {
		schedule.RecurringDay = req.RecurringDay
	}
	if req.LunarMonth != nil {
		schedule.LunarMonth = req.LunarMonth
	}
	if req.LunarDay != nil {
		schedule.LunarDay = req.LunarDay
	}
	if req.YearFilter != nil {
		schedule.YearFilter = req.YearFilter
	}
	if req.DisplayPriority != nil {
		schedule.DisplayPriority = *req.DisplayPriority
	}
	if req.DisplaySection != nil {
		schedule.DisplaySection = *req.DisplaySection
	}
	if req.IsActive != nil {
		schedule.IsActive = *req.IsActive
	}
	if req.StartDate != nil {
		if *req.StartDate == "" {
			schedule.StartDate = nil
		} else {
			t, err := time.Parse("2006-01-02", *req.StartDate)
			if err == nil {
				schedule.StartDate = &t
			}
		}
	}
	if req.EndDate != nil {
		if *req.EndDate == "" {
			schedule.EndDate = nil
		} else {
			t, err := time.Parse("2006-01-02", *req.EndDate)
			if err == nil {
				schedule.EndDate = &t
			}
		}
	}

	if err := s.repo.Update(schedule); err != nil {
		s.logger.Error("Failed to update schedule", zap.Error(err))
		return nil, fmt.Errorf("failed to update schedule: %w", err)
	}

	return s.toResponseWithContent(schedule), nil
}

// Delete soft-deletes a daily content schedule.
func (s *DailyContentService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete schedule", zap.Error(err))
		return fmt.Errorf("failed to delete schedule: %w", err)
	}
	return nil
}

// ============================================
// Helper functions
// ============================================

func (s *DailyContentService) getRandomPublishedArticles(limit int) ([]dto.ArticleListResponse, error) {
	articles, err := s.articleRepo.GetRandomPublished(limit)
	if err != nil {
		return nil, err
	}

	results := make([]dto.ArticleListResponse, len(articles))
	for i, a := range articles {
		results[i] = *toRelationArticleListResponse(&a)
	}
	return results, nil
}

func (s *DailyContentService) toResponse(sch *models.DailyContentSchedule) *dto.DailyContentResponse {
	resp := &dto.DailyContentResponse{
		ID:              sch.ID.String(),
		ContentType:     sch.ContentType,
		CustomTitle:     sch.CustomTitle,
		CustomContent:   sch.CustomContent,
		CustomImage:     sch.CustomImage,
		ScheduleMode:    sch.ScheduleMode,
		DayOfYear:       sch.DayOfYear,
		RecurringMonth:  sch.RecurringMonth,
		RecurringDay:    sch.RecurringDay,
		LunarMonth:      sch.LunarMonth,
		LunarDay:        sch.LunarDay,
		YearFilter:      sch.YearFilter,
		DisplayPriority: sch.DisplayPriority,
		DisplaySection:  sch.DisplaySection,
		IsActive:        sch.IsActive,
		CreatedAt:       sch.CreatedAt.Format(time.RFC3339),
		UpdatedAt:       sch.UpdatedAt.Format(time.RFC3339),
	}

	if sch.ContentID != nil {
		resp.ContentID = sch.ContentID.String()
	}
	if sch.FixedDate != nil {
		resp.FixedDate = sch.FixedDate.Format("2006-01-02")
	}
	if sch.StartDate != nil {
		resp.StartDate = sch.StartDate.Format("2006-01-02")
	}
	if sch.EndDate != nil {
		resp.EndDate = sch.EndDate.Format("2006-01-02")
	}

	return resp
}

// toResponseWithContent returns a DailyContentResponse with the resolved content object.
func (s *DailyContentService) toResponseWithContent(sch *models.DailyContentSchedule) *dto.DailyContentResponse {
	resp := s.toResponse(sch)

	// Resolve the associated content object so the admin UI can display its name/title
	if sch.ContentID != nil {
		switch sch.ContentType {
		case models.ContentTypeQuote:
			if q, err := s.quoteRepo.GetByID(*sch.ContentID); err == nil {
				resp.Content = q
			}
		case models.ContentTypeEvent:
			if e, err := s.eventRepo.GetByID(*sch.ContentID); err == nil {
				resp.Content = e
			}
		case models.ContentTypeArticle:
			if a, err := s.articleRepo.GetByID(*sch.ContentID); err == nil {
				resp.Content = a
			}
		case models.ContentTypeFamousPerson:
			if p, err := s.famousPersonRepo.GetByID(*sch.ContentID); err == nil {
				resp.Content = p
			}
		case models.ContentTypeFolkFestival:
			if f, err := s.folkFestivalRepo.GetByID(*sch.ContentID); err == nil {
				resp.Content = f
			}
		}
	}

	return resp
}

// GetMonthContentSummary returns lightweight content counts for every day in a month.
// Uses batch queries (one per content type) instead of per-day queries for efficiency.
func (s *DailyContentService) GetMonthContentSummary(year, month int) (*dto.MonthContentSummaryResponse, error) {
	daysInMonth := time.Date(year, time.Month(month+1), 0, 0, 0, 0, 0, time.UTC).Day()

	// Calculate day-of-year range for this month
	firstDOY := time.Date(year, time.Month(month), 1, 0, 0, 0, 0, time.UTC).YearDay()
	lastDOY := time.Date(year, time.Month(month), daysInMonth, 0, 0, 0, 0, time.UTC).YearDay()

	// Batch queries — one per content type
	eventCounts, err := s.eventRepo.CountByDayInMonth(month)
	if err != nil {
		s.logger.Warn("Failed to count events by month", zap.Error(err))
		eventCounts = make(map[int]int64)
	}

	peopleCounts, err := s.famousPersonRepo.CountByDayInMonth(month)
	if err != nil {
		s.logger.Warn("Failed to count famous people by month", zap.Error(err))
		peopleCounts = make(map[int]int64)
	}

	festivalCounts, err := s.folkFestivalRepo.CountByDayInSolarMonth(month)
	if err != nil {
		s.logger.Warn("Failed to count festivals by month", zap.Error(err))
		festivalCounts = make(map[int]int64)
	}

	quoteCounts, err := s.quoteRepo.CountByDayOfYearRange(firstDOY, lastDOY)
	if err != nil {
		s.logger.Warn("Failed to count quotes by DOY range", zap.Error(err))
		quoteCounts = make(map[int]int64)
	}

	scheduledCounts, err := s.repo.CountByDayInMonth(year, month, firstDOY, lastDOY)
	if err != nil {
		s.logger.Warn("Failed to count scheduled content by month", zap.Error(err))
		scheduledCounts = make(map[int]int64)
	}

	// Build per-day summaries
	days := make([]dto.DayContentSummary, 0, daysInMonth)
	for d := 1; d <= daysInMonth; d++ {
		doy := time.Date(year, time.Month(month), d, 0, 0, 0, 0, time.UTC).YearDay()
		q := int(quoteCounts[doy])
		ev := int(eventCounts[d])
		p := int(peopleCounts[d])
		f := int(festivalCounts[d])
		sc := int(scheduledCounts[d])

		total := q + ev + p + f + sc
		if total == 0 {
			continue // Skip days with no content
		}

		days = append(days, dto.DayContentSummary{
			Day:       d,
			Quotes:    q,
			Events:    ev,
			Articles:  0, // Articles don't have per-day assignment (via schedule only)
			People:    p,
			Festivals: f,
			Custom:    sc,
			Total:     total,
		})
	}

	return &dto.MonthContentSummaryResponse{
		Year:  year,
		Month: month,
		Days:  days,
	}, nil
}

// GetStats returns coverage statistics for daily content.
func (s *DailyContentService) GetStats(year int) (*dto.DailyContentStatsResponse, error) {
	total, active, err := s.repo.CountActive()
	if err != nil {
		return nil, fmt.Errorf("failed to count schedules: %w", err)
	}

	byType, err := s.repo.CountByType()
	if err != nil {
		s.logger.Warn("Failed to count by type", zap.Error(err))
		byType = make(map[string]int64)
	}

	byMode, err := s.repo.CountByMode()
	if err != nil {
		s.logger.Warn("Failed to count by mode", zap.Error(err))
		byMode = make(map[string]int64)
	}

	// Calculate coverage for the year
	isLeap := year%4 == 0 && (year%100 != 0 || year%400 == 0)
	totalDays := 365
	if isLeap {
		totalDays = 366
	}

	coveredDays := 0
	for m := 1; m <= 12; m++ {
		summary, err := s.GetMonthContentSummary(year, m)
		if err != nil {
			continue
		}
		coveredDays += len(summary.Days)
	}

	return &dto.DailyContentStatsResponse{
		TotalSchedules:  total,
		ActiveSchedules: active,
		ByType:          byType,
		ByMode:          byMode,
		CoverageSummary: dto.CoverageSummary{
			Year:         year,
			TotalDays:    totalDays,
			CoveredDays:  coveredDays,
			EmptyDays:    totalDays - coveredDays,
			CoverageRate: float64(coveredDays) / float64(totalDays) * 100,
		},
	}, nil
}

// AutoFill automatically creates content schedules for a date range using recurring_annual mode.
func (s *DailyContentService) AutoFill(createdBy uuid.UUID, req *dto.AutoFillRequest) (*dto.AutoFillResult, error) {
	startDate, err := time.Parse("2006-01-02", req.StartDate)
	if err != nil {
		return nil, fmt.Errorf("invalid start_date: %w", err)
	}
	endDate, err := time.Parse("2006-01-02", req.EndDate)
	if err != nil {
		return nil, fmt.Errorf("invalid end_date: %w", err)
	}
	if endDate.Before(startDate) {
		return nil, fmt.Errorf("end_date must be after start_date")
	}
	// Max 366 days
	if endDate.Sub(startDate).Hours()/24 > 366 {
		return nil, fmt.Errorf("date range must not exceed 366 days")
	}

	result := &dto.AutoFillResult{}
	typeSet := make(map[string]bool)
	for _, ct := range req.ContentTypes {
		typeSet[ct] = true
	}

	for d := startDate; !d.After(endDate); d = d.AddDate(0, 0, 1) {
		result.TotalDays++
		month := int(d.Month())
		day := d.Day()
		created := false

		if req.SkipExisting {
			// Check if this day already has content
			existing, _ := s.repo.HasContentForDays(month, []int{day})
			if existing[day] {
				result.SkippedDays++
				continue
			}
		}

		// Auto-fill quotes by day_of_year
		if typeSet["quote"] {
			doy := d.YearDay()
			quotes, _ := s.quoteRepo.GetByDayOfYear(doy)
			if len(quotes) == 0 {
				// No quote for this DOY — could assign a random one
				// For now, skip
			}
		}

		// Auto-fill events
		if typeSet["event"] {
			ld := lunar.SolarToLunar(day, month, d.Year(), 7)
			events, _ := s.eventRepo.GetByDate(month, day, ld.Month, ld.Day)
			for _, ev := range events {
				schedule := &models.DailyContentSchedule{
					ContentType:    models.ContentTypeEvent,
					ContentID:      &ev.ID,
					ScheduleMode:   "recurring_annual",
					RecurringMonth: &month,
					RecurringDay:   &day,
					DisplaySection: "events",
					IsActive:       true,
					CreatedBy:      &createdBy,
					UpdatedBy:      &createdBy,
				}
				if err := s.repo.Create(schedule); err == nil {
					result.ItemsCreated++
					created = true
				}
			}
		}

		// Auto-fill famous people by birthday
		if typeSet["famous_person"] {
			people, _ := s.famousPersonRepo.GetByBirthday(month, day)
			for _, p := range people {
				schedule := &models.DailyContentSchedule{
					ContentType:    models.ContentTypeFamousPerson,
					ContentID:      &p.ID,
					ScheduleMode:   "recurring_annual",
					RecurringMonth: &month,
					RecurringDay:   &day,
					DisplaySection: "people",
					IsActive:       true,
					CreatedBy:      &createdBy,
					UpdatedBy:      &createdBy,
				}
				if err := s.repo.Create(schedule); err == nil {
					result.ItemsCreated++
					created = true
				}
			}
		}

		// Auto-fill folk festivals by solar date
		if typeSet["folk_festival"] {
			festivals, _ := s.folkFestivalRepo.GetBySolarDate(month, day)
			for _, f := range festivals {
				schedule := &models.DailyContentSchedule{
					ContentType:    models.ContentTypeFolkFestival,
					ContentID:      &f.ID,
					ScheduleMode:   "recurring_annual",
					RecurringMonth: &month,
					RecurringDay:   &day,
					DisplaySection: "festivals",
					IsActive:       true,
					CreatedBy:      &createdBy,
					UpdatedBy:      &createdBy,
				}
				if err := s.repo.Create(schedule); err == nil {
					result.ItemsCreated++
					created = true
				}
			}
		}

		if created {
			result.FilledDays++
		}
	}

	return result, nil
}
