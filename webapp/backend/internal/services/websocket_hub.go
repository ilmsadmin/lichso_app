package services

import (
	"encoding/json"
	"sync"

	"github.com/gofiber/contrib/websocket"
	"github.com/zplus/lichso/internal/models"
	"go.uber.org/zap"
)

// WSHub manages WebSocket connections per user
type WSHub struct {
	// connections maps userID -> set of WebSocket connections
	connections map[string]map[*websocket.Conn]bool
	mu          sync.RWMutex
	logger      *zap.Logger
}

// NewWSHub creates a new WebSocket hub
func NewWSHub(logger *zap.Logger) *WSHub {
	return &WSHub{
		connections: make(map[string]map[*websocket.Conn]bool),
		logger:      logger,
	}
}

// Register adds a WebSocket connection for a user
func (h *WSHub) Register(userID string, conn *websocket.Conn) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if h.connections[userID] == nil {
		h.connections[userID] = make(map[*websocket.Conn]bool)
	}
	h.connections[userID][conn] = true

	h.logger.Debug("WebSocket client connected",
		zap.String("user_id", userID),
		zap.Int("total_connections", len(h.connections[userID])),
	)
}

// Unregister removes a WebSocket connection for a user
func (h *WSHub) Unregister(userID string, conn *websocket.Conn) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if conns, ok := h.connections[userID]; ok {
		delete(conns, conn)
		if len(conns) == 0 {
			delete(h.connections, userID)
		}
	}

	h.logger.Debug("WebSocket client disconnected",
		zap.String("user_id", userID),
	)
}

// SendToUser sends a message to all connections of a specific user
func (h *WSHub) SendToUser(userID string, msg models.WSMessage) {
	h.mu.RLock()
	conns, ok := h.connections[userID]
	if !ok {
		h.mu.RUnlock()
		return
	}

	// Copy connections slice to avoid holding lock during writes
	connList := make([]*websocket.Conn, 0, len(conns))
	for conn := range conns {
		connList = append(connList, conn)
	}
	h.mu.RUnlock()

	data, err := json.Marshal(msg)
	if err != nil {
		h.logger.Error("Failed to marshal WebSocket message", zap.Error(err))
		return
	}

	for _, conn := range connList {
		if err := conn.WriteMessage(websocket.TextMessage, data); err != nil {
			h.logger.Debug("Failed to send WebSocket message, removing connection",
				zap.String("user_id", userID),
				zap.Error(err),
			)
			h.Unregister(userID, conn)
		}
	}
}

// Broadcast sends a message to all connected users
func (h *WSHub) Broadcast(msg models.WSMessage) {
	h.mu.RLock()
	userIDs := make([]string, 0, len(h.connections))
	for uid := range h.connections {
		userIDs = append(userIDs, uid)
	}
	h.mu.RUnlock()

	for _, uid := range userIDs {
		h.SendToUser(uid, msg)
	}
}

// GetOnlineUserCount returns the number of connected users
func (h *WSHub) GetOnlineUserCount() int {
	h.mu.RLock()
	defer h.mu.RUnlock()
	return len(h.connections)
}

// IsUserOnline checks if a user has any active WebSocket connections
func (h *WSHub) IsUserOnline(userID string) bool {
	h.mu.RLock()
	defer h.mu.RUnlock()
	conns, ok := h.connections[userID]
	return ok && len(conns) > 0
}
