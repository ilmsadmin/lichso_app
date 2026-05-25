package dto

// ============================================
// User Note DTOs
// ============================================

// CreateUserNoteRequest represents a request to create a user note.
type CreateUserNoteRequest struct {
	NoteDate string `json:"note_date" validate:"required"` // YYYY-MM-DD
	Title    string `json:"title" validate:"required,max=255"`
	Content  string `json:"content"`
	Color    string `json:"color" validate:"omitempty,max=20"`
	IsPinned bool   `json:"is_pinned"`
}

// UpdateUserNoteRequest represents a request to update a user note.
type UpdateUserNoteRequest struct {
	NoteDate *string `json:"note_date"`
	Title    *string `json:"title" validate:"omitempty,max=255"`
	Content  *string `json:"content"`
	Color    *string `json:"color" validate:"omitempty,max=20"`
	IsPinned *bool   `json:"is_pinned"`
}

// UserNoteResponse represents a user note in API responses.
type UserNoteResponse struct {
	ID        string `json:"id"`
	NoteDate  string `json:"note_date"`
	Title     string `json:"title"`
	Content   string `json:"content,omitempty"`
	Color     string `json:"color"`
	IsPinned  bool   `json:"is_pinned"`
	CreatedAt string `json:"created_at"`
	UpdatedAt string `json:"updated_at"`
}

// ============================================
// User Countdown DTOs
// ============================================

// CreateUserCountdownRequest represents a request to create a countdown.
type CreateUserCountdownRequest struct {
	Title            string `json:"title" validate:"required,max=255"`
	Description      string `json:"description"`
	TargetDate       string `json:"target_date" validate:"required"` // YYYY-MM-DD
	TargetTime       string `json:"target_time"`                     // HH:MM
	Color            string `json:"color" validate:"omitempty,max=20"`
	Icon             string `json:"icon" validate:"omitempty,max=50"`
	IsRecurring      bool   `json:"is_recurring"`
	RecurringType    string `json:"recurring_type" validate:"omitempty,oneof=yearly monthly"`
	NotifyBeforeDays int    `json:"notify_before_days" validate:"min=0,max=365"`
}

// UpdateUserCountdownRequest represents a request to update a countdown.
type UpdateUserCountdownRequest struct {
	Title            *string `json:"title" validate:"omitempty,max=255"`
	Description      *string `json:"description"`
	TargetDate       *string `json:"target_date"`
	TargetTime       *string `json:"target_time"`
	Color            *string `json:"color" validate:"omitempty,max=20"`
	Icon             *string `json:"icon" validate:"omitempty,max=50"`
	IsRecurring      *bool   `json:"is_recurring"`
	RecurringType    *string `json:"recurring_type" validate:"omitempty,oneof=yearly monthly"`
	NotifyBeforeDays *int    `json:"notify_before_days" validate:"omitempty,min=0,max=365"`
	IsActive         *bool   `json:"is_active"`
}

// UserCountdownResponse represents a countdown in API responses.
type UserCountdownResponse struct {
	ID               string `json:"id"`
	Title            string `json:"title"`
	Description      string `json:"description,omitempty"`
	TargetDate       string `json:"target_date"`
	TargetTime       string `json:"target_time,omitempty"`
	Color            string `json:"color"`
	Icon             string `json:"icon"`
	IsRecurring      bool   `json:"is_recurring"`
	RecurringType    string `json:"recurring_type,omitempty"`
	NotifyBeforeDays int    `json:"notify_before_days"`
	DaysRemaining    int    `json:"days_remaining"`
	IsActive         bool   `json:"is_active"`
	CreatedAt        string `json:"created_at"`
	UpdatedAt        string `json:"updated_at"`
}
