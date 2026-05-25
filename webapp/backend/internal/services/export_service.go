package services

import (
	"fmt"
	"strings"
	"time"

	"github.com/zplus/lichso/internal/services/calendar"
	"go.uber.org/zap"
)

// ExportService handles calendar export to iCal and PDF.
type ExportService struct {
	calService *calendar.Service
	logger     *zap.Logger
}

// NewExportService creates a new ExportService.
func NewExportService(calService *calendar.Service, logger *zap.Logger) *ExportService {
	return &ExportService{calService: calService, logger: logger}
}

// GenerateICal generates iCal format for a month's calendar data.
func (s *ExportService) GenerateICal(year, month int) ([]byte, error) {
	monthData := s.calService.GetMonth(year, month)

	var buf strings.Builder
	buf.WriteString("BEGIN:VCALENDAR\r\n")
	buf.WriteString("VERSION:2.0\r\n")
	buf.WriteString("PRODID:-//Lich So//Lich Van Nien Viet Nam//VI\r\n")
	buf.WriteString("CALSCALE:GREGORIAN\r\n")
	buf.WriteString("METHOD:PUBLISH\r\n")
	buf.WriteString(fmt.Sprintf("X-WR-CALNAME:Lịch Số — Tháng %d/%d\r\n", month, year))
	buf.WriteString("X-WR-TIMEZONE:Asia/Ho_Chi_Minh\r\n")
	buf.WriteString("\r\n")

	// Add timezone definition
	buf.WriteString("BEGIN:VTIMEZONE\r\n")
	buf.WriteString("TZID:Asia/Ho_Chi_Minh\r\n")
	buf.WriteString("BEGIN:STANDARD\r\n")
	buf.WriteString("DTSTART:19700101T000000\r\n")
	buf.WriteString("TZOFFSETFROM:+0700\r\n")
	buf.WriteString("TZOFFSETTO:+0700\r\n")
	buf.WriteString("END:STANDARD\r\n")
	buf.WriteString("END:VTIMEZONE\r\n")

	now := time.Now().UTC().Format("20060102T150405Z")

	for _, day := range monthData.Days {
		// Get full day info for events
		dayData := s.calService.GetDate(day.SolarDay, month, year, 12)

		// Create event for each day with lunar info
		dateStr := fmt.Sprintf("%d%02d%02d", year, month, day.SolarDay)
		uid := fmt.Sprintf("lichso-%s@lichso.vn", dateStr)

		// Summary includes lunar date
		summary := fmt.Sprintf("Âm: %s", day.LunarDayName)
		if dayData.PhongThuy.ChiSoNgay >= 70 {
			summary += " ✦"
		}

		// Description with feng shui info
		var desc strings.Builder
		desc.WriteString(fmt.Sprintf("📅 %s — %s\\n", dayData.DayOfWeek, dayData.TuTru.Ngay.CanChi))
		desc.WriteString(fmt.Sprintf("🌙 %s %s\\n", dayData.LunarDayName, dayData.LunarMonthName))
		desc.WriteString(fmt.Sprintf("⭐ %s — %s (%d%%)\\n",
			dayData.PhongThuy.TrucNgay.Name,
			dayData.PhongThuy.DanhGia,
			dayData.PhongThuy.ChiSoNgay))

		// Add events / holidays
		if len(dayData.Events) > 0 {
			desc.WriteString("\\n🎊 Sự kiện:\\n")
			for _, ev := range dayData.Events {
				desc.WriteString(fmt.Sprintf("  %s %s\\n", ev.Emoji, ev.Name))
			}
		}

		buf.WriteString("BEGIN:VEVENT\r\n")
		buf.WriteString(fmt.Sprintf("UID:%s\r\n", uid))
		buf.WriteString(fmt.Sprintf("DTSTAMP:%s\r\n", now))
		buf.WriteString(fmt.Sprintf("DTSTART;VALUE=DATE:%s\r\n", dateStr))
		buf.WriteString(fmt.Sprintf("DTEND;VALUE=DATE:%s\r\n", dateStr))
		buf.WriteString(fmt.Sprintf("SUMMARY:%s\r\n", escapeICalText(summary)))
		buf.WriteString(fmt.Sprintf("DESCRIPTION:%s\r\n", escapeICalText(desc.String())))

		// Color coding for good/bad days
		if dayData.PhongThuy.ChiSoNgay >= 70 {
			buf.WriteString("CATEGORIES:Ngày Tốt\r\n")
		} else if dayData.PhongThuy.ChiSoNgay < 40 {
			buf.WriteString("CATEGORIES:Ngày Xấu\r\n")
		}

		// Add holidays as separate all-day events
		for _, ev := range dayData.Events {
			buf.WriteString("END:VEVENT\r\n")
			buf.WriteString("BEGIN:VEVENT\r\n")
			evUID := fmt.Sprintf("lichso-ev-%s-%d@lichso.vn", dateStr, ev.Day)
			buf.WriteString(fmt.Sprintf("UID:%s\r\n", evUID))
			buf.WriteString(fmt.Sprintf("DTSTAMP:%s\r\n", now))
			buf.WriteString(fmt.Sprintf("DTSTART;VALUE=DATE:%s\r\n", dateStr))
			buf.WriteString(fmt.Sprintf("DTEND;VALUE=DATE:%s\r\n", dateStr))
			buf.WriteString(fmt.Sprintf("SUMMARY:%s %s\r\n", ev.Emoji, escapeICalText(ev.Name)))
			if ev.IsOff {
				buf.WriteString("CATEGORIES:Nghỉ Lễ\r\n")
			}
		}

		buf.WriteString("END:VEVENT\r\n")
	}

	buf.WriteString("END:VCALENDAR\r\n")

	return []byte(buf.String()), nil
}

// GenerateMonthText generates a plain text calendar for a month (for PDF).
func (s *ExportService) GenerateMonthText(year, month int) string {
	monthData := s.calService.GetMonth(year, month)

	var buf strings.Builder
	buf.WriteString(fmt.Sprintf("LỊCH SỐ — THÁNG %d NĂM %d\n", month, year))
	buf.WriteString(fmt.Sprintf("Thông tin âm lịch: %s\n", monthData.LunarInfo))
	buf.WriteString(strings.Repeat("═", 60) + "\n\n")

	weekdays := []string{"CN", "T2", "T3", "T4", "T5", "T6", "T7"}
	for _, wd := range weekdays {
		buf.WriteString(fmt.Sprintf("%-8s", wd))
	}
	buf.WriteString("\n" + strings.Repeat("─", 56) + "\n")

	// Calculate offset for first day
	firstDate := time.Date(year, time.Month(month), 1, 0, 0, 0, 0, time.UTC)
	offset := int(firstDate.Weekday())

	// Write empty cells for offset
	for i := 0; i < offset; i++ {
		buf.WriteString("        ")
	}

	for _, day := range monthData.Days {
		dayStr := fmt.Sprintf("%2d/%-4s", day.SolarDay, day.LunarDayName)
		if day.IsGoodDay {
			dayStr += "✦"
		} else {
			dayStr += " "
		}
		buf.WriteString(fmt.Sprintf("%-8s", dayStr))

		if (day.SolarDay+offset)%7 == 0 {
			buf.WriteString("\n")
		}
	}

	return buf.String()
}

// escapeICalText escapes text for iCal format.
func escapeICalText(s string) string {
	s = strings.ReplaceAll(s, "\\", "\\\\")
	s = strings.ReplaceAll(s, ";", "\\;")
	s = strings.ReplaceAll(s, ",", "\\,")
	s = strings.ReplaceAll(s, "\n", "\\n")
	return s
}
