import api from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RefreshTokenRequest,
  RefreshTokenResponse,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  ChangePasswordRequest,
  GoogleLoginRequest,
  GetMeResponse,
  AuthUser,
} from "@/types/auth";

// ============================================
// Auth API Service
// ============================================

/**
 * Login with email and password
 */
export async function login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  const response = await api.post<ApiResponse<LoginResponse>>("/auth/login", data);
  return response.data;
}

/**
 * Register a new account
 */
export async function register(data: RegisterRequest): Promise<ApiResponse<{ user: AuthUser }>> {
  const response = await api.post<ApiResponse<{ user: AuthUser }>>("/auth/register", data);
  return response.data;
}

/**
 * Login with Google ID token
 */
export async function googleLogin(data: GoogleLoginRequest): Promise<ApiResponse<LoginResponse>> {
  const response = await api.post<ApiResponse<LoginResponse>>("/auth/google", data);
  return response.data;
}

/**
 * Refresh access token
 */
export async function refreshToken(
  data: RefreshTokenRequest
): Promise<ApiResponse<RefreshTokenResponse>> {
  const response = await api.post<ApiResponse<RefreshTokenResponse>>("/auth/refresh", data);
  return response.data;
}

/**
 * Logout current session
 */
export async function logout(): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>("/auth/logout");
  return response.data;
}

/**
 * Request password reset email
 */
export async function forgotPassword(data: ForgotPasswordRequest): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>("/auth/forgot-password", data);
  return response.data;
}

/**
 * Reset password with token
 */
export async function resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>("/auth/reset-password", data);
  return response.data;
}

/**
 * Change password (authenticated)
 */
export async function changePassword(data: ChangePasswordRequest): Promise<ApiResponse<null>> {
  const response = await api.put<ApiResponse<null>>("/auth/change-password", data);
  return response.data;
}

/**
 * Get current user profile
 */
export async function getMe(): Promise<ApiResponse<GetMeResponse>> {
  const response = await api.get<ApiResponse<GetMeResponse>>("/auth/me");
  return response.data;
}

/**
 * Update current user profile (first/last name)
 */
export async function updateProfile(data: {
  first_name: string;
  last_name: string;
}): Promise<ApiResponse<GetMeResponse>> {
  const response = await api.put<ApiResponse<GetMeResponse>>("/auth/me", data);
  return response.data;
}

export async function deleteAccount(): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>("/auth/me");
  return response.data;
}
