package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

// ActivityLog represents an activity log entry stored in MongoDB
type ActivityLog struct {
	ID          primitive.ObjectID     `bson:"_id,omitempty" json:"id"`
	UserID      string                 `bson:"user_id" json:"user_id"`
	UserEmail   string                 `bson:"user_email" json:"user_email"`
	Action      string                 `bson:"action" json:"action"`
	Module      string                 `bson:"module" json:"module"`
	Description string                 `bson:"description" json:"description"`
	Metadata    map[string]interface{} `bson:"metadata,omitempty" json:"metadata,omitempty"`
	Status      string                 `bson:"status" json:"status"`
	IPAddress   string                 `bson:"ip_address" json:"ip_address"`
	UserAgent   string                 `bson:"user_agent" json:"user_agent"`
	CreatedAt   time.Time              `bson:"created_at" json:"created_at"`
}

// CollectionName returns the MongoDB collection name
func (ActivityLog) CollectionName() string {
	return "activity_logs"
}

// Activity log actions
const (
	ActionUserLogin          = "user.login"
	ActionUserLogout         = "user.logout"
	ActionUserCreate         = "user.create"
	ActionUserUpdate         = "user.update"
	ActionUserDelete         = "user.delete"
	ActionUserPasswordChange = "user.password_change"
	ActionUserPasswordReset  = "user.password_reset"
	ActionUserActivate       = "user.activate"
	ActionUserDeactivate     = "user.deactivate"
	ActionRoleCreate         = "role.create"
	ActionRoleUpdate         = "role.update"
	ActionRoleDelete         = "role.delete"
	ActionRoleAssign         = "role.assign"
	ActionRoleUnassign       = "role.unassign"
	ActionPermissionAssign   = "permission.assign"
	ActionPermissionUnassign = "permission.unassign"
	ActionSettingsUpdate     = "settings.update"
	ActionLoginFailed        = "user.login_failed"
)

// Activity log statuses
const (
	StatusSuccess = "success"
	StatusFailure = "failure"
)

// Activity log modules
const (
	ModuleAuth    = "auth"
	ModuleUser    = "users"
	ModuleRole    = "roles"
	ModulePerm    = "permissions"
	ModuleSetting = "settings"
	ModuleSystem  = "system"
)

// NewActivityLog creates a new activity log entry
func NewActivityLog(userID, userEmail, action, module, description string) *ActivityLog {
	return &ActivityLog{
		UserID:      userID,
		UserEmail:   userEmail,
		Action:      action,
		Module:      module,
		Description: description,
		Status:      StatusSuccess,
		CreatedAt:   time.Now(),
	}
}

// WithMetadata adds metadata to the activity log
func (a *ActivityLog) WithMetadata(metadata map[string]interface{}) *ActivityLog {
	a.Metadata = metadata
	return a
}

// WithIPAndAgent adds IP address and user agent to the activity log
func (a *ActivityLog) WithIPAndAgent(ip, userAgent string) *ActivityLog {
	a.IPAddress = ip
	a.UserAgent = userAgent
	return a
}

// WithStatus sets the status of the activity log
func (a *ActivityLog) WithStatus(status string) *ActivityLog {
	a.Status = status
	return a
}

// ActivityLogResponse represents the activity log data returned in API responses
type ActivityLogResponse struct {
	ID          string                 `json:"id"`
	UserID      string                 `json:"user_id"`
	UserEmail   string                 `json:"user_email"`
	Action      string                 `json:"action"`
	Module      string                 `json:"module"`
	Description string                 `json:"description"`
	Metadata    map[string]interface{} `json:"metadata,omitempty"`
	Status      string                 `json:"status"`
	IPAddress   string                 `json:"ip_address"`
	UserAgent   string                 `json:"user_agent"`
	CreatedAt   time.Time              `json:"created_at"`
}

// ToResponse converts ActivityLog to ActivityLogResponse
func (a *ActivityLog) ToResponse() ActivityLogResponse {
	return ActivityLogResponse{
		ID:          a.ID.Hex(),
		UserID:      a.UserID,
		UserEmail:   a.UserEmail,
		Action:      a.Action,
		Module:      a.Module,
		Description: a.Description,
		Metadata:    a.Metadata,
		Status:      a.Status,
		IPAddress:   a.IPAddress,
		UserAgent:   a.UserAgent,
		CreatedAt:   a.CreatedAt,
	}
}
