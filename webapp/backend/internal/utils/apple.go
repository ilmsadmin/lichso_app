package utils

import (
	"crypto/rsa"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"math/big"
	"net/http"
	"sync"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

// AppleClaims represents the custom claims from Apple identityToken
type AppleClaims struct {
	Email          string      `json:"email"`
	EmailVerified  interface{} `json:"email_verified"`  // can be string or bool
	IsPrivateEmail interface{} `json:"is_private_email"` // can be string or bool
	jwt.RegisteredClaims
}

type AppleJWK struct {
	Kty string `json:"kty"`
	Kid string `json:"kid"`
	Use string `json:"use"`
	Alg string `json:"alg"`
	N   string `json:"n"`
	E   string `json:"e"`
}

type AppleJWKS struct {
	Keys []AppleJWK `json:"keys"`
}

var (
	applePublicKeys map[string]*rsa.PublicKey
	appleKeysExpiry time.Time
	appleKeysMu     sync.RWMutex
)

func fetchAppleKeys() (map[string]*rsa.PublicKey, error) {
	appleKeysMu.RLock()
	if applePublicKeys != nil && time.Now().Before(appleKeysExpiry) {
		keys := applePublicKeys
		appleKeysMu.RUnlock()
		return keys, nil
	}
	appleKeysMu.RUnlock()

	appleKeysMu.Lock()
	defer appleKeysMu.Unlock()

	// Double check inside lock
	if applePublicKeys != nil && time.Now().Before(appleKeysExpiry) {
		return applePublicKeys, nil
	}

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Get("https://appleid.apple.com/auth/keys")
	if err != nil {
		return nil, fmt.Errorf("failed to fetch apple keys: %w", err)
	}
	defer resp.Body.Close()

	var jwks AppleJWKS
	if err := json.NewDecoder(resp.Body).Decode(&jwks); err != nil {
		return nil, fmt.Errorf("failed to decode apple jwks: %w", err)
	}

	keys := make(map[string]*rsa.PublicKey)
	for _, key := range jwks.Keys {
		if key.Kty != "RSA" {
			continue
		}

		nBytes, err := base64.RawURLEncoding.DecodeString(key.N)
		if err != nil {
			continue
		}
		eBytes, err := base64.RawURLEncoding.DecodeString(key.E)
		if err != nil {
			continue
		}

		// Convert exponent bytes to int
		var eVal int
		for _, b := range eBytes {
			eVal = (eVal << 8) | int(b)
		}

		nVal := new(big.Int).SetBytes(nBytes)
		pubKey := &rsa.PublicKey{
			N: nVal,
			E: eVal,
		}
		keys[key.Kid] = pubKey
	}

	applePublicKeys = keys
	appleKeysExpiry = time.Now().Add(24 * time.Hour) // Cache for 24 hours
	return keys, nil
}

// VerifyAppleIDToken verifies the Apple identityToken (JWT) signature and claims
func VerifyAppleIDToken(idToken string, allowedAudience string) (*AppleClaims, error) {
	token, err := jwt.ParseWithClaims(idToken, &AppleClaims{}, func(token *jwt.Token) (interface{}, error) {
		// Verify signature algorithm
		if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}

		kid, ok := token.Header["kid"].(string)
		if !ok {
			return nil, errors.New("missing kid in token header")
		}

		keys, err := fetchAppleKeys()
		if err != nil {
			return nil, err
		}

		pubKey, ok := keys[kid]
		if !ok {
			return nil, fmt.Errorf("apple public key not found for kid: %s", kid)
		}

		return pubKey, nil
	})

	if err != nil {
		return nil, fmt.Errorf("invalid apple token signature: %w", err)
	}

	claims, ok := token.Claims.(*AppleClaims)
	if !ok || !token.Valid {
		return nil, errors.New("invalid apple token claims")
	}

	// Verify Issuer
	if claims.Issuer != "https://appleid.apple.com" && claims.Issuer != "appleid.apple.com" {
		return nil, fmt.Errorf("invalid apple token issuer: %s", claims.Issuer)
	}

	// Verify Audience
	aud, err := claims.GetAudience()
	if err != nil || len(aud) == 0 {
		return nil, errors.New("invalid or empty apple token audience")
	}

	validAud := false
	for _, a := range aud {
		if a == allowedAudience {
			validAud = true
			break
		}
	}
	if !validAud {
		return nil, fmt.Errorf("apple token audience mismatch: got %v, expected %s", aud, allowedAudience)
	}

	return claims, nil
}
