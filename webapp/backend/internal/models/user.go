package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Auth provider constants
const (
	ProviderLocal  = "local"
	ProviderGoogle = "google"
)

// User represents a user in the system
type User struct {
	ID         uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Email      string         `gorm:"type:varchar(255);uniqueIndex;not null" json:"email"`
	Password   string         `gorm:"type:varchar(255)" json:"-"`
	FirstName  string         `gorm:"type:varchar(100);not null;default:''" json:"first_name"`
	LastName   string         `gorm:"type:varchar(100);not null;default:''" json:"last_name"`
	Avatar     string         `gorm:"type:varchar(500);default:''" json:"avatar"`
	Phone      string         `gorm:"type:varchar(20);default:''" json:"phone"`
	Provider   string         `gorm:"type:varchar(20);not null;default:'local'" json:"provider"`
	ProviderID string         `gorm:"type:varchar(255);default:''" json:"provider_id"`
	IsActive   bool           `gorm:"not null;default:true" json:"is_active"`
	LastLogin  *time.Time     `gorm:"type:timestamptz" json:"last_login"`
	CreatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt  time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt  gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`

	// Associations
	Roles []Role `gorm:"many2many:user_roles;joinForeignKey:user_id;joinReferences:role_id" json:"roles,omitempty"`
}

// TableName overrides the default table name
func (User) TableName() string {
	return "users"
}

// FullName returns the user's full name
func (u *User) FullName() string {
	name := u.FirstName
	if u.LastName != "" {
		if name != "" {
			name += " "
		}
		name += u.LastName
	}
	return name
}

// HasRole checks if the user has a specific role
func (u *User) HasRole(roleName string) bool {
	for _, role := range u.Roles {
		if role.Name == roleName {
			return true
		}
	}
	return false
}

// IsSuperAdmin checks if the user has super_admin role
func (u *User) IsSuperAdmin() bool {
	return u.HasRole("super_admin")
}

// IsAdmin checks if the user has admin or super_admin role
func (u *User) IsAdmin() bool {
	return u.HasRole("admin") || u.HasRole("super_admin")
}

// UserResponse represents the user data returned in API responses
type UserResponse struct {
	ID        uuid.UUID   `json:"id"`
	Email     string      `json:"email"`
	FirstName string      `json:"first_name"`
	LastName  string      `json:"last_name"`
	FullName  string      `json:"full_name"`
	Avatar    string      `json:"avatar"`
	Phone     string      `json:"phone"`
	Provider  string      `json:"provider"`
	IsActive  bool        `json:"is_active"`
	LastLogin *time.Time  `json:"last_login"`
	CreatedAt time.Time   `json:"created_at"`
	UpdatedAt time.Time   `json:"updated_at"`
	Roles     []RoleBrief `json:"roles,omitempty"`
}

// UserAdminListItem is the enriched user row returned in the admin user list.
type UserAdminListItem struct {
	UserResponse
	DeviceCount   int    `json:"device_count"`
	Platforms     string `json:"platforms"`      // e.g. "android,ios"
	LatestVersion string `json:"latest_version"` // latest app version across devices
}

// DeviceTokenBrief is a compact representation of a device token for admin views.
type DeviceTokenBrief struct {
	Platform   string    `json:"platform"`
	AppVersion string    `json:"app_version"`
	DeviceID   string    `json:"device_id"`
	LastSeen   time.Time `json:"last_seen"`
	CreatedAt  time.Time `json:"created_at"`
}

// UserStats holds aggregated activity statistics for a user.
type UserStats struct {
	BookmarkCount int `json:"bookmark_count"`
	NoteCount     int `json:"note_count"`
	DeviceCount   int `json:"device_count"`
	StreakDays    int `json:"streak_days"`
	Points        int `json:"points"`
}

// UserAdminDetail is the full enriched user returned by the admin detail endpoint.
type UserAdminDetail struct {
	UserResponse
	ProviderID string             `json:"provider_id"`
	Devices    []DeviceTokenBrief `json:"devices"`
	Stats      UserStats          `json:"stats"`
}

// RoleBrief is a minimal role representation for nested responses
type RoleBrief struct {
	ID          uuid.UUID `json:"id"`
	Name        string    `json:"name"`
	DisplayName string    `json:"display_name"`
}

// ToResponse converts User model to UserResponse
func (u *User) ToResponse() UserResponse {
	resp := UserResponse{
		ID:        u.ID,
		Email:     u.Email,
		FirstName: u.FirstName,
		LastName:  u.LastName,
		FullName:  u.FullName(),
		Avatar:    u.Avatar,
		Phone:     u.Phone,
		Provider:  u.Provider,
		IsActive:  u.IsActive,
		LastLogin: u.LastLogin,
		CreatedAt: u.CreatedAt,
		UpdatedAt: u.UpdatedAt,
	}

	for _, role := range u.Roles {
		resp.Roles = append(resp.Roles, RoleBrief{
			ID:          role.ID,
			Name:        role.Name,
			DisplayName: role.DisplayName,
		})
	}

	return resp
}
