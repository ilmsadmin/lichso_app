package utils

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

// GoogleUserInfo represents the user information from a verified Google ID token
type GoogleUserInfo struct {
	Sub           string `json:"sub"` // Google user ID
	Email         string `json:"email"`
	EmailVerified string `json:"email_verified"`
	Name          string `json:"name"`
	GivenName     string `json:"given_name"`
	FamilyName    string `json:"family_name"`
	Picture       string `json:"picture"`
}

// VerifyGoogleIDToken verifies a Google ID token using Google's tokeninfo endpoint
// and returns the user information if valid.
// allowedClientIDs accepts tokens from multiple OAuth clients (web + Android Firebase).
func VerifyGoogleIDToken(idToken string, allowedClientIDs ...string) (*GoogleUserInfo, error) {
	url := fmt.Sprintf("https://oauth2.googleapis.com/tokeninfo?id_token=%s", idToken)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Get(url)
	if err != nil {
		return nil, fmt.Errorf("failed to verify Google token: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read Google response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("invalid Google ID token: %s", string(body))
	}

	var tokenInfo struct {
		GoogleUserInfo
		Aud string `json:"aud"`
		Iss string `json:"iss"`
		Exp string `json:"exp"`
	}

	if err := json.Unmarshal(body, &tokenInfo); err != nil {
		return nil, fmt.Errorf("failed to parse Google token info: %w", err)
	}

	// Accept token if aud matches any of the allowed client IDs
	validAud := false
	for _, id := range allowedClientIDs {
		if tokenInfo.Aud == id {
			validAud = true
			break
		}
	}
	if !validAud {
		return nil, fmt.Errorf("Google token audience mismatch: got %s", tokenInfo.Aud)
	}

	if tokenInfo.Iss != "accounts.google.com" && tokenInfo.Iss != "https://accounts.google.com" {
		return nil, fmt.Errorf("Google token issuer mismatch: %s", tokenInfo.Iss)
	}

	return &tokenInfo.GoogleUserInfo, nil
}
