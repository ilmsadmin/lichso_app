package dto

// CalendarDateRequest represents a date query parameter.
type CalendarDateRequest struct {
	Date string `query:"date" validate:"omitempty"` // YYYY-MM-DD format
}

// CalendarMonthRequest represents a month query.
type CalendarMonthRequest struct {
	Year  int `params:"year" validate:"required,min=1900,max=2100"`
	Month int `params:"month" validate:"required,min=1,max=12"`
}

// CalendarConvertRequest represents a conversion request.
type CalendarConvertRequest struct {
	Day       int  `query:"day" validate:"required,min=1,max=31"`
	Month     int  `query:"month" validate:"required,min=1,max=12"`
	Year      int  `query:"year" validate:"required,min=1900,max=2100"`
	ToLunar   bool `query:"to_lunar"`   // If true: solar→lunar; if false: lunar→solar
	LeapMonth bool `query:"leap_month"` // For lunar→solar conversion
}

// CalendarGoodDaysRequest represents a good days query.
type CalendarGoodDaysRequest struct {
	Year  int `query:"year" validate:"required,min=1900,max=2100"`
	Month int `query:"month" validate:"required,min=1,max=12"`
}

// CalendarSolarTermsRequest represents a solar terms query.
type CalendarSolarTermsRequest struct {
	Year int `params:"year" validate:"required,min=1900,max=2100"`
}
