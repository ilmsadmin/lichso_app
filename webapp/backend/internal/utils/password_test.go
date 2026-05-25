package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestHashPassword(t *testing.T) {
	password := "SecureP@ssw0rd!"

	hash, err := HashPassword(password)

	require.NoError(t, err)
	assert.NotEmpty(t, hash)
	assert.NotEqual(t, password, hash) // Hash should differ from plaintext
}

func TestHashPassword_DifferentPasswords(t *testing.T) {
	hash1, err := HashPassword("password1")
	require.NoError(t, err)

	hash2, err := HashPassword("password2")
	require.NoError(t, err)

	assert.NotEqual(t, hash1, hash2)
}

func TestHashPassword_SamePasswordDifferentHashes(t *testing.T) {
	// Bcrypt generates different hashes for the same password (due to random salt)
	hash1, err := HashPassword("samepassword")
	require.NoError(t, err)

	hash2, err := HashPassword("samepassword")
	require.NoError(t, err)

	assert.NotEqual(t, hash1, hash2)
}

func TestVerifyPassword_Correct(t *testing.T) {
	password := "SecureP@ssw0rd!"
	hash, err := HashPassword(password)
	require.NoError(t, err)

	assert.True(t, VerifyPassword(hash, password))
}

func TestVerifyPassword_WrongPassword(t *testing.T) {
	hash, err := HashPassword("correctpassword")
	require.NoError(t, err)

	assert.False(t, VerifyPassword(hash, "wrongpassword"))
}

func TestVerifyPassword_EmptyPassword(t *testing.T) {
	hash, err := HashPassword("somepassword")
	require.NoError(t, err)

	assert.False(t, VerifyPassword(hash, ""))
}

func TestVerifyPassword_InvalidHash(t *testing.T) {
	assert.False(t, VerifyPassword("not-a-valid-bcrypt-hash", "password"))
}

func TestVerifyPassword_EmptyHash(t *testing.T) {
	assert.False(t, VerifyPassword("", "password"))
}

func TestHashPassword_EmptyPassword(t *testing.T) {
	// bcrypt can hash empty strings
	hash, err := HashPassword("")
	require.NoError(t, err)
	assert.NotEmpty(t, hash)
	assert.True(t, VerifyPassword(hash, ""))
}

func TestHashPassword_LongPassword(t *testing.T) {
	// bcrypt has a 72-byte limit, passwords longer than 72 bytes cause an error
	longPassword := string(make([]byte, 100))
	_, err := HashPassword(longPassword)
	// bcrypt returns error for passwords > 72 bytes
	assert.Error(t, err)
}

func TestHashPassword_MaxLengthPassword(t *testing.T) {
	// 72-byte password should work fine with bcrypt
	maxPassword := "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffff1234567890ab"
	assert.Len(t, maxPassword, 72)
	hash, err := HashPassword(maxPassword)
	require.NoError(t, err)
	assert.NotEmpty(t, hash)
	assert.True(t, VerifyPassword(hash, maxPassword))
}

func TestVerifyPassword_VariousPasswords(t *testing.T) {
	passwords := []string{
		"simple",
		"With Numbers 123",
		"Special!@#$%^&*()",
		"Üñíçödé",
		"  spaces  ",
		"P@$$w0rd!2026",
	}

	for _, pw := range passwords {
		t.Run(pw, func(t *testing.T) {
			hash, err := HashPassword(pw)
			require.NoError(t, err)
			assert.True(t, VerifyPassword(hash, pw), "Password '%s' should verify", pw)
			assert.False(t, VerifyPassword(hash, pw+"x"), "Modified password should not verify")
		})
	}
}
