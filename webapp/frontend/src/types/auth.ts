// ============================================
// Auth Types
// ============================================

/**
 * User roles
 */
export type UserRole = "super_admin" | "admin" | "editor" | "viewer";

/**
 * Auth user data returned from login/me endpoints
 */
export interface AuthUser {
  id: string;
  email: string;
  first_name: string;
  last_name: string;
  full_name: string;
  avatar: string;
  roles: UserRole[];
  permissions: string[];
}

/**
 * Login request payload
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Register request payload
 */
export interface RegisterRequest {
  email: string;
  password: string;
  first_name: string;
  last_name: string;
}

/**
 * Login response data
 */
export interface LoginResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
  user: AuthUser;
}

/**
 * Refresh token request payload
 */
export interface RefreshTokenRequest {
  refresh_token: string;
}

/**
 * Refresh token response data
 */
export interface RefreshTokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
}

/**
 * Forgot password request payload
 */
export interface ForgotPasswordRequest {
  email: string;
}

/**
 * Reset password request payload
 */
export interface ResetPasswordRequest {
  token: string;
  password: string;
}

/**
 * Change password request payload
 */
export interface ChangePasswordRequest {
  current_password: string;
  new_password: string;
}

/**
 * Google login request payload
 */
export interface GoogleLoginRequest {
  id_token: string;
}

/**
 * Get me response data (same structure as LoginResponse)
 */
export interface GetMeResponse {
  user: AuthUser;
}
