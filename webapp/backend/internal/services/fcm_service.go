package services

import (
	"bytes"
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"strings"
	"sync"
	"time"

	"github.com/zplus/lichso/internal/config"
	"go.uber.org/zap"
)

const (
	fcmScope   = "https://www.googleapis.com/auth/firebase.messaging"
	fcmSendURL = "https://fcm.googleapis.com/v1/projects/%s/messages:send"
	tokenURL   = "https://oauth2.googleapis.com/token"
	grantType  = "urn:ietf:params:oauth2:grant-type:jwt-bearer"
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

// serviceAccount mirrors the fields we need from the JSON credentials file.
type serviceAccount struct {
	ProjectID   string `json:"project_id"`
	ClientEmail string `json:"client_email"`
	PrivateKey  string `json:"private_key"`
	TokenURI    string `json:"token_uri"`
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
// Uses service-account JWT auth implemented entirely from stdlib — no extra deps.
type FCMService struct {
	projectID  string
	sa         *serviceAccount
	privateKey *rsa.PrivateKey
	httpClient *http.Client

	mu          sync.Mutex
	cachedToken string
	tokenExpiry time.Time

	logger *zap.Logger
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

	var sa serviceAccount
	if err := json.Unmarshal(raw, &sa); err != nil {
		return nil, fmt.Errorf("FCM: parse service account JSON: %w", err)
	}

	key, err := parseRSAPrivateKey(sa.PrivateKey)
	if err != nil {
		return nil, fmt.Errorf("FCM: parse private key: %w", err)
	}

	projectID := cfg.ProjectID
	if projectID == "" {
		projectID = sa.ProjectID
	}

	return &FCMService{
		projectID:  projectID,
		sa:         &sa,
		privateKey: key,
		httpClient: &http.Client{Timeout: 15 * time.Second},
		logger:     logger,
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
// Individual token failures are captured in results, not returned as an error.
func (s *FCMService) SendToTokens(tokens []string, title, body, imageURL, clickAction string, data map[string]string) ([]FCMSendResult, error) {
	accessToken, err := s.getAccessToken()
	if err != nil {
		return nil, fmt.Errorf("FCM: get access token: %w", err)
	}

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
		if err := s.doSend(sendURL, accessToken, req); err != nil {
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

func (s *FCMService) doSend(sendURL, accessToken string, payload fcmSendRequest) error {
	bodyBytes, err := json.Marshal(payload)
	if err != nil {
		return err
	}

	req, err := http.NewRequest(http.MethodPost, sendURL, bytes.NewReader(bodyBytes))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+accessToken)

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

// ── OAuth2 service-account JWT (stdlib only) ─────────────────────────────

func (s *FCMService) getAccessToken() (string, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	// Return cached token when it has ≥60 seconds remaining
	if s.cachedToken != "" && time.Now().Add(60*time.Second).Before(s.tokenExpiry) {
		return s.cachedToken, nil
	}

	token, expiry, err := s.fetchAccessToken()
	if err != nil {
		return "", err
	}
	s.cachedToken = token
	s.tokenExpiry = expiry
	return token, nil
}

func (s *FCMService) fetchAccessToken() (string, time.Time, error) {
	now := time.Now()

	headerJSON, _ := json.Marshal(map[string]string{"alg": "RS256", "typ": "JWT"})
	claimJSON, _ := json.Marshal(map[string]interface{}{
		"iss":   s.sa.ClientEmail,
		"scope": fcmScope,
		"aud":   tokenURL,
		"iat":   now.Unix(),
		"exp":   now.Add(time.Hour).Unix(),
	})

	sigInput := base64.RawURLEncoding.EncodeToString(headerJSON) + "." +
		base64.RawURLEncoding.EncodeToString(claimJSON)

	sig, err := rsaSign(s.privateKey, []byte(sigInput))
	if err != nil {
		return "", time.Time{}, fmt.Errorf("sign JWT: %w", err)
	}
	assertion := sigInput + "." + base64.RawURLEncoding.EncodeToString(sig)

	resp, err := s.httpClient.PostForm(tokenURL, url.Values{
		"grant_type": {grantType},
		"assertion":  {assertion},
	})
	if err != nil {
		return "", time.Time{}, err
	}
	defer resp.Body.Close()

	var result struct {
		AccessToken string `json:"access_token"`
		ExpiresIn   int    `json:"expires_in"`
		Error       string `json:"error"`
		Description string `json:"error_description"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return "", time.Time{}, err
	}
	if result.Error != "" {
		return "", time.Time{}, fmt.Errorf("token exchange: %s — %s", result.Error, result.Description)
	}

	expiry := now.Add(time.Duration(result.ExpiresIn) * time.Second)
	return result.AccessToken, expiry, nil
}

// ── helpers ───────────────────────────────────────────────────────────────

func rsaSign(key *rsa.PrivateKey, data []byte) ([]byte, error) {
	digest := sha256.Sum256(data)
	return rsa.SignPKCS1v15(rand.Reader, key, crypto.SHA256, digest[:])
}

func loadCredentials(cfg *config.FCMConfig) ([]byte, error) {
	if cfg.CredentialsJSON != "" {
		return []byte(cfg.CredentialsJSON), nil
	}
	if cfg.CredentialsFile != "" {
		return os.ReadFile(cfg.CredentialsFile)
	}
	return nil, fmt.Errorf("neither FCM_CREDENTIALS_FILE nor FCM_CREDENTIALS_JSON is set")
}

func parseRSAPrivateKey(pemStr string) (*rsa.PrivateKey, error) {
	pemStr = strings.ReplaceAll(pemStr, `\n`, "\n")
	block, _ := pem.Decode([]byte(pemStr))
	if block == nil {
		return nil, fmt.Errorf("failed to decode PEM block from private key")
	}
	key, err := x509.ParsePKCS8PrivateKey(block.Bytes)
	if err != nil {
		return nil, err
	}
	rsaKey, ok := key.(*rsa.PrivateKey)
	if !ok {
		return nil, fmt.Errorf("private key is not RSA")
	}
	return rsaKey, nil
}

func safeTokenPrefix(token string) string {
	if len(token) <= 10 {
		return token
	}
	return token[:10] + "..."
}
