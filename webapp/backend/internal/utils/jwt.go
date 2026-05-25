package utils

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

// TokenType represents the type of JWT token
type TokenType string

const (
	AccessToken  TokenType = "access"
	RefreshToken TokenType = "refresh"
)

// JWTClaims represents the custom JWT claims
type JWTClaims struct {
	UserID uuid.UUID `json:"sub"`
	Email  string    `json:"email,omitempty"`
	Roles  []string  `json:"roles,omitempty"`
	Type   TokenType `json:"type"`
	jwt.RegisteredClaims
}

// JWTConfig holds JWT configuration for token generation
type JWTConfig struct {
	Secret             string
	AccessTokenExpiry  time.Duration
	RefreshTokenExpiry time.Duration
	Issuer             string
}

// TokenPair holds access and refresh tokens
type TokenPair struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
	ExpiresIn    int64  `json:"expires_in"`
	TokenType    string `json:"token_type"`
}

// GenerateAccessToken creates a new JWT access token
func GenerateAccessToken(cfg *JWTConfig, userID uuid.UUID, email string, roles []string) (string, string, error) {
	jti := uuid.New().String()

	claims := JWTClaims{
		UserID: userID,
		Email:  email,
		Roles:  roles,
		Type:   AccessToken,
		RegisteredClaims: jwt.RegisteredClaims{
			ID:        jti,
			Subject:   userID.String(),
			Issuer:    cfg.Issuer,
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(cfg.AccessTokenExpiry)),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	signedToken, err := token.SignedString([]byte(cfg.Secret))
	if err != nil {
		return "", "", err
	}

	return signedToken, jti, nil
}

// GenerateRefreshToken creates a new JWT refresh token
func GenerateRefreshToken(cfg *JWTConfig, userID uuid.UUID) (string, string, error) {
	jti := uuid.New().String()

	claims := JWTClaims{
		UserID: userID,
		Type:   RefreshToken,
		RegisteredClaims: jwt.RegisteredClaims{
			ID:        jti,
			Subject:   userID.String(),
			Issuer:    cfg.Issuer,
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(cfg.RefreshTokenExpiry)),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	signedToken, err := token.SignedString([]byte(cfg.Secret))
	if err != nil {
		return "", "", err
	}

	return signedToken, jti, nil
}

// GenerateTokenPair creates both access and refresh tokens
func GenerateTokenPair(cfg *JWTConfig, userID uuid.UUID, email string, roles []string) (*TokenPair, string, error) {
	accessToken, _, err := GenerateAccessToken(cfg, userID, email, roles)
	if err != nil {
		return nil, "", err
	}

	refreshToken, refreshJTI, err := GenerateRefreshToken(cfg, userID)
	if err != nil {
		return nil, "", err
	}

	return &TokenPair{
		AccessToken:  accessToken,
		RefreshToken: refreshToken,
		ExpiresIn:    int64(cfg.AccessTokenExpiry.Seconds()),
		TokenType:    "Bearer",
	}, refreshJTI, nil
}

// ValidateToken parses and validates a JWT token
func ValidateToken(tokenString string, secret string) (*JWTClaims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &JWTClaims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return []byte(secret), nil
	})

	if err != nil {
		if errors.Is(err, jwt.ErrTokenExpired) {
			return nil, errors.New("token has expired")
		}
		return nil, errors.New("invalid token")
	}

	claims, ok := token.Claims.(*JWTClaims)
	if !ok || !token.Valid {
		return nil, errors.New("invalid token claims")
	}

	return claims, nil
}

// GetTokenRemainingTime returns the remaining time until token expiration
func GetTokenRemainingTime(tokenString string, secret string) (time.Duration, error) {
	claims, err := ValidateToken(tokenString, secret)
	if err != nil {
		return 0, err
	}

	remaining := time.Until(claims.ExpiresAt.Time)
	if remaining < 0 {
		return 0, errors.New("token has expired")
	}

	return remaining, nil
}

// GenerateRandomToken generates a secure random token string
func GenerateRandomToken(length int) (string, error) {
	bytes := make([]byte, length)
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}
	return hex.EncodeToString(bytes), nil
}
