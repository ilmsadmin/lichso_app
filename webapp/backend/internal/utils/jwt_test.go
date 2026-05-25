package utils

import (
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func testJWTConfig() *JWTConfig {
	return &JWTConfig{
		Secret:             "test-secret-key-minimum-32-chars!!",
		AccessTokenExpiry:  15 * time.Minute,
		RefreshTokenExpiry: 7 * 24 * time.Hour,
		Issuer:             "zplus-test",
	}
}

func TestGenerateAccessToken(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()
	email := "test@example.com"
	roles := []string{"admin", "editor"}

	token, jti, err := GenerateAccessToken(cfg, userID, email, roles)

	require.NoError(t, err)
	assert.NotEmpty(t, token)
	assert.NotEmpty(t, jti)
}

func TestGenerateRefreshToken(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()

	token, jti, err := GenerateRefreshToken(cfg, userID)

	require.NoError(t, err)
	assert.NotEmpty(t, token)
	assert.NotEmpty(t, jti)
}

func TestGenerateTokenPair(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()
	email := "test@example.com"
	roles := []string{"admin"}

	pair, refreshJTI, err := GenerateTokenPair(cfg, userID, email, roles)

	require.NoError(t, err)
	assert.NotEmpty(t, pair.AccessToken)
	assert.NotEmpty(t, pair.RefreshToken)
	assert.NotEmpty(t, refreshJTI)
	assert.Equal(t, "Bearer", pair.TokenType)
	assert.Equal(t, int64(cfg.AccessTokenExpiry.Seconds()), pair.ExpiresIn)
}

func TestValidateToken_ValidAccessToken(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()
	email := "test@example.com"
	roles := []string{"admin", "editor"}

	token, _, err := GenerateAccessToken(cfg, userID, email, roles)
	require.NoError(t, err)

	claims, err := ValidateToken(token, cfg.Secret)

	require.NoError(t, err)
	assert.Equal(t, userID, claims.UserID)
	assert.Equal(t, email, claims.Email)
	assert.Equal(t, roles, claims.Roles)
	assert.Equal(t, AccessToken, claims.Type)
	assert.Equal(t, cfg.Issuer, claims.Issuer)
}

func TestValidateToken_ValidRefreshToken(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()

	token, _, err := GenerateRefreshToken(cfg, userID)
	require.NoError(t, err)

	claims, err := ValidateToken(token, cfg.Secret)

	require.NoError(t, err)
	assert.Equal(t, userID, claims.UserID)
	assert.Equal(t, RefreshToken, claims.Type)
}

func TestValidateToken_InvalidSecret(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()

	token, _, err := GenerateAccessToken(cfg, userID, "test@example.com", []string{"admin"})
	require.NoError(t, err)

	_, err = ValidateToken(token, "wrong-secret-key-that-is-different")

	assert.Error(t, err)
	assert.Contains(t, err.Error(), "invalid token")
}

func TestValidateToken_ExpiredToken(t *testing.T) {
	cfg := &JWTConfig{
		Secret:            "test-secret-key-minimum-32-chars!!",
		AccessTokenExpiry: -1 * time.Hour, // Already expired
		Issuer:            "zplus-test",
	}
	userID := uuid.New()

	token, _, err := GenerateAccessToken(cfg, userID, "test@example.com", []string{})
	require.NoError(t, err)

	_, err = ValidateToken(token, cfg.Secret)

	assert.Error(t, err)
	assert.Contains(t, err.Error(), "expired")
}

func TestValidateToken_MalformedToken(t *testing.T) {
	_, err := ValidateToken("not.a.valid.jwt.token", "any-secret")
	assert.Error(t, err)
}

func TestValidateToken_EmptyToken(t *testing.T) {
	_, err := ValidateToken("", "any-secret")
	assert.Error(t, err)
}

func TestGetTokenRemainingTime(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()

	token, _, err := GenerateAccessToken(cfg, userID, "test@example.com", []string{})
	require.NoError(t, err)

	remaining, err := GetTokenRemainingTime(token, cfg.Secret)

	require.NoError(t, err)
	assert.True(t, remaining > 0)
	assert.True(t, remaining <= cfg.AccessTokenExpiry)
}

func TestGetTokenRemainingTime_ExpiredToken(t *testing.T) {
	cfg := &JWTConfig{
		Secret:            "test-secret-key-minimum-32-chars!!",
		AccessTokenExpiry: -1 * time.Hour,
		Issuer:            "zplus-test",
	}
	userID := uuid.New()

	token, _, err := GenerateAccessToken(cfg, userID, "test@example.com", []string{})
	require.NoError(t, err)

	_, err = GetTokenRemainingTime(token, cfg.Secret)
	assert.Error(t, err)
}

func TestGenerateRandomToken(t *testing.T) {
	token1, err := GenerateRandomToken(32)
	require.NoError(t, err)
	assert.Len(t, token1, 64) // hex encoding doubles the length

	token2, err := GenerateRandomToken(32)
	require.NoError(t, err)

	// Two generated tokens should be different
	assert.NotEqual(t, token1, token2)
}

func TestGenerateRandomToken_DifferentLengths(t *testing.T) {
	tests := []struct {
		length      int
		expectedHex int
	}{
		{16, 32},
		{32, 64},
		{64, 128},
	}

	for _, tt := range tests {
		token, err := GenerateRandomToken(tt.length)
		require.NoError(t, err)
		assert.Len(t, token, tt.expectedHex)
	}
}

func TestTokenPairContainsDifferentTokens(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()

	pair, _, err := GenerateTokenPair(cfg, userID, "test@example.com", []string{"admin"})
	require.NoError(t, err)

	// Access and refresh tokens should be different
	assert.NotEqual(t, pair.AccessToken, pair.RefreshToken)

	// Both should be valid
	accessClaims, err := ValidateToken(pair.AccessToken, cfg.Secret)
	require.NoError(t, err)
	assert.Equal(t, AccessToken, accessClaims.Type)

	refreshClaims, err := ValidateToken(pair.RefreshToken, cfg.Secret)
	require.NoError(t, err)
	assert.Equal(t, RefreshToken, refreshClaims.Type)
}

func TestTokenClaimsPreserveUserData(t *testing.T) {
	cfg := testJWTConfig()
	userID := uuid.New()
	email := "admin@zplus.vn"
	roles := []string{"super_admin", "admin", "editor"}

	token, _, err := GenerateAccessToken(cfg, userID, email, roles)
	require.NoError(t, err)

	claims, err := ValidateToken(token, cfg.Secret)
	require.NoError(t, err)

	assert.Equal(t, userID, claims.UserID)
	// Note: claims.Subject from RegisteredClaims is overridden by UserID json:"sub"
	// so we verify UserID instead
	assert.Equal(t, email, claims.Email)
	assert.Equal(t, roles, claims.Roles)
	assert.Equal(t, "zplus-test", claims.Issuer)
}
