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
// and returns the user information if valid
func VerifyGoogleIDToken(idToken, expectedClientID string) (*GoogleUserInfo, error) {
	// Use Google's tokeninfo endpoint to verify the token
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

	// Parse the response
	var tokenInfo struct {
		GoogleUserInfo
		Aud string `json:"aud"` // Client ID
		Iss string `json:"iss"` // Issuer
		Exp string `json:"exp"` // Expiry
	}

	if err := json.Unmarshal(body, &tokenInfo); err != nil {
		return nil, fmt.Errorf("failed to parse Google token info: %w", err)
	}

	// Verify the audience (client ID) matches
	if tokenInfo.Aud != expectedClientID {
		return nil, fmt.Errorf("Google token audience mismatch: expected %s, got %s", expectedClientID, tokenInfo.Aud)
	}

	// Verify the issuer
	if tokenInfo.Iss != "accounts.google.com" && tokenInfo.Iss != "https://accounts.google.com" {
		return nil, fmt.Errorf("Google token issuer mismatch: %s", tokenInfo.Iss)
	}

	return &tokenInfo.GoogleUserInfo, nil
}
