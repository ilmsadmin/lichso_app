package services

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/zplus/lichso/internal/config"
	"go.uber.org/zap"
	"golang.org/x/oauth2/google"
)

const (
	fcmScope   = "https://www.googleapis.com/auth/firebase.messaging"
	fcmSendURL = "https://fcm.googleapis.com/v1/projects/%s/messages:send"
)

// FCMNotification is the notification payload.
type FCMNotification struct {
	Title    string `json:"title"`
	Body     string `json:"body"`
	ImageURL string `json:"image,omitempty"`
}

// FCMSendResult holds per-token send outcome.
type FCMSendResult struct {
	Token   string
	Success bool
	Error   string
}

type fcmMessageBody struct {
	Token        string            `json:"token"`
	Notification *FCMNotification  `json:"notification,omitempty"`
	Android      *fcmAndroid       `json:"android,omitempty"`
	Data         map[string]string `json:"data,omitempty"`
}

type fcmAndroid struct {
	Notification *fcmAndroidNotif `json:"notification,omitempty"`
}

type fcmAndroidNotif struct {
	ClickAction string `json:"click_action,omitempty"`
	ChannelID   string `json:"channel_id,omitempty"`
}

type fcmSendRequest struct {
	Message fcmMessageBody `json:"message"`
}

// FCMService sends push notifications via Firebase Cloud Messaging HTTP v1 API.
// Uses golang.org/x/oauth2/google for reliable service-account authentication.
type FCMService struct {
	projectID   string
	credentials []byte // raw service-account JSON
	httpClient  *http.Client
	logger      *zap.Logger
}

// NewFCMService creates and initialises a new FCMService.
// Returns (nil, nil) when FCM is disabled — callers must nil-check before use.
func NewFCMService(cfg *config.FCMConfig, logger *zap.Logger) (*FCMService, error) {
	if !cfg.Enabled {
		logger.Info("FCM disabled — push notifications will not be sent")
		return nil, nil
	}

	raw, err := loadCredentials(cfg)
	if err != nil {
		return nil, fmt.Errorf("FCM credentials: %w", err)
	}

	// Validate that the JSON parses as a service account credential.
	creds, err := google.CredentialsFromJSON(context.Background(), raw, fcmScope)
	if err != nil {
		return nil, fmt.Errorf("FCM: invalid service account credentials: %w", err)
	}

	projectID := cfg.ProjectID
	if projectID == "" {
		// Extract project_id from the JSON as fallback.
		var sa struct {
			ProjectID string `json:"project_id"`
		}
		_ = json.Unmarshal(raw, &sa)
		projectID = sa.ProjectID
	}

	// Build an HTTP client that automatically attaches OAuth2 Bearer tokens.
	httpClient := &http.Client{
		Transport: &oauth2Transport{
			creds:      creds,
			underlying: &http.Transport{},
		},
		Timeout: 15 * time.Second,
	}

	logger.Info("FCM service initialised", zap.String("project_id", projectID))
	return &FCMService{
		projectID:   projectID,
		credentials: raw,
		httpClient:  httpClient,
		logger:      logger,
	}, nil
}

// SendToToken sends a single notification to one FCM token.
func (s *FCMService) SendToToken(token, title, body, imageURL, clickAction string, data map[string]string) error {
	results, err := s.SendToTokens([]string{token}, title, body, imageURL, clickAction, data)
	if err != nil {
		return err
	}
	if len(results) > 0 && !results[0].Success {
		return fmt.Errorf("FCM send failed: %s", results[0].Error)
	}
	return nil
}

// SendToTokens sends to multiple tokens, returning per-token results.
func (s *FCMService) SendToTokens(tokens []string, title, body, imageURL, clickAction string, data map[string]string) ([]FCMSendResult, error) {
	sendURL := fmt.Sprintf(fcmSendURL, s.projectID)
	results := make([]FCMSendResult, 0, len(tokens))

	for _, token := range tokens {
		req := fcmSendRequest{
			Message: fcmMessageBody{
				Token: token,
				Notification: &FCMNotification{
					Title:    title,
					Body:     body,
					ImageURL: imageURL,
				},
				Android: &fcmAndroid{
					Notification: &fcmAndroidNotif{
						ClickAction: clickAction,
						ChannelID:   "default",
					},
				},
				Data: data,
			},
		}

		result := FCMSendResult{Token: token}
		if err := s.doSend(sendURL, req); err != nil {
			result.Success = false
			result.Error = err.Error()
			s.logger.Warn("FCM: failed to send to token",
				zap.String("token_prefix", safeTokenPrefix(token)),
				zap.Error(err),
			)
		} else {
			result.Success = true
		}
		results = append(results, result)
	}

	return results, nil
}

func (s *FCMService) doSend(sendURL string, payload fcmSendRequest) error {
	bodyBytes, err := json.Marshal(payload)
	if err != nil {
		return err
	}

	// The oauth2Transport automatically adds Authorization: Bearer <token>
	req, err := http.NewRequest(http.MethodPost, sendURL, bytes.NewReader(bodyBytes))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := s.httpClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 200 && resp.StatusCode < 300 {
		return nil
	}

	respBody, _ := io.ReadAll(resp.Body)
	return fmt.Errorf("HTTP %d: %s", resp.StatusCode, string(respBody))
}

// ── helpers ───────────────────────────────────────────────────────────────

func loadCredentials(cfg *config.FCMConfig) ([]byte, error) {
	if cfg.CredentialsJSON != "" {
		return []byte(cfg.CredentialsJSON), nil
	}
	if cfg.CredentialsFile != "" {
		return os.ReadFile(cfg.CredentialsFile)
	}
	return nil, fmt.Errorf("neither FCM_CREDENTIALS_FILE nor FCM_CREDENTIALS_JSON is set")
}

func safeTokenPrefix(token string) string {
	if len(token) <= 10 {
		return token
	}
	return token[:10] + "..."
}

// oauth2Transport wraps an HTTP transport and injects OAuth2 Bearer tokens automatically.
type oauth2Transport struct {
	creds      *google.Credentials
	underlying http.RoundTripper
}

func (t *oauth2Transport) RoundTrip(req *http.Request) (*http.Response, error) {
	token, err := t.creds.TokenSource.Token()
	if err != nil {
		return nil, fmt.Errorf("FCM: get access token: %w", err)
	}
	reqCopy := req.Clone(req.Context())
	reqCopy.Header.Set("Authorization", "Bearer "+token.AccessToken)
	return t.underlying.RoundTrip(reqCopy)
}
