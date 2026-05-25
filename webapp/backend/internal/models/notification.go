package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

// Notification types
const (
	NotificationTypeInfo    = "info"
	NotificationTypeSuccess = "success"
	NotificationTypeWarning = "warning"
	NotificationTypeError   = "error"
)

// Notification represents a notification stored in MongoDB
type Notification struct {
	ID        primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	UserID    string             `bson:"user_id" json:"user_id"`
	Title     string             `bson:"title" json:"title"`
	Message   string             `bson:"message" json:"message"`
	Type      string             `bson:"type" json:"type"`
	IsRead    bool               `bson:"is_read" json:"is_read"`
	Link      string             `bson:"link,omitempty" json:"link,omitempty"`
	RefType   string             `bson:"ref_type,omitempty" json:"ref_type,omitempty"` // "reminder" | "bookmark" | ""
	RefID     string             `bson:"ref_id,omitempty" json:"ref_id,omitempty"`
	CreatedAt time.Time          `bson:"created_at" json:"created_at"`
}

// CollectionName returns the MongoDB collection name
func (Notification) CollectionName() string {
	return "notifications"
}

// NotificationResponse represents the notification data returned in API responses
type NotificationResponse struct {
	ID        string    `json:"id"`
	UserID    string    `json:"user_id"`
	Title     string    `json:"title"`
	Message   string    `json:"message"`
	Type      string    `json:"type"`
	IsRead    bool      `json:"is_read"`
	Link      string    `json:"link,omitempty"`
	RefType   string    `json:"ref_type,omitempty"`
	RefID     string    `json:"ref_id,omitempty"`
	CreatedAt time.Time `json:"created_at"`
}

// ToResponse converts Notification to NotificationResponse
func (n *Notification) ToResponse() NotificationResponse {
	return NotificationResponse{
		ID:        n.ID.Hex(),
		UserID:    n.UserID,
		Title:     n.Title,
		Message:   n.Message,
		Type:      n.Type,
		IsRead:    n.IsRead,
		Link:      n.Link,
		RefType:   n.RefType,
		RefID:     n.RefID,
		CreatedAt: n.CreatedAt,
	}
}

// NewNotification creates a new notification
func NewNotification(userID, title, message, notifType string) *Notification {
	return &Notification{
		UserID:    userID,
		Title:     title,
		Message:   message,
		Type:      notifType,
		IsRead:    false,
		CreatedAt: time.Now(),
	}
}

// WithLink adds a link to the notification
func (n *Notification) WithLink(link string) *Notification {
	n.Link = link
	return n
}

// WSMessage represents a WebSocket message sent to clients
type WSMessage struct {
	Type string      `json:"type"` // "notification", "notification_read", "notification_count"
	Data interface{} `json:"data"`
}
