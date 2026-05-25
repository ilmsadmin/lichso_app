"use client";

import { useCallback } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/authStore";
import * as authService from "@/services/authService";
import { ROUTES } from "@/lib/constants";
import type {
  LoginRequest,
  RegisterRequest,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  GoogleLoginRequest,
} from "@/types/auth";

// ============================================
// useAuth Hook
// ============================================

export function useAuth() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const {
    user,
    isAuthenticated,
    isLoading,
    setUser,
    setTokens,
    clearAuth,
    setLoading,
    hasRole,
    hasPermission,
    hasAnyRole,
    isSuperAdmin,
    isAdmin,
    hasAdminAccess,
    getHomeRoute,
  } = useAuthStore();

  // ============================================
  // Get current user (on mount)
  // ============================================
  const { refetch: refetchMe } = useQuery({
    queryKey: ["auth", "me"],
    queryFn: async () => {
      try {
        const response = await authService.getMe();
        if (response.success && response.data?.user) {
          setUser(response.data.user);
          return response.data.user;
        }
        clearAuth();
        return null;
      } catch {
        clearAuth();
        return null;
      }
    },
    enabled: false, // Manual trigger
    retry: false,
  });

  // ============================================
  // Login Mutation
  // ============================================
  const loginMutation = useMutation({
    mutationFn: (data: LoginRequest) => authService.login(data),
    onSuccess: (response) => {
      if (response.success && response.data) {
        setTokens(response.data.access_token, response.data.refresh_token);
        setUser(response.data.user);
        queryClient.invalidateQueries({ queryKey: ["auth"] });
        // Redirect based on user role
        const userRoles = response.data.user.roles || [];
        const adminRoles = ["super_admin", "admin", "editor"];
        const canAccessAdmin = userRoles.some((r) => adminRoles.includes(r));
        router.push(canAccessAdmin ? ROUTES.ADMIN : ROUTES.PROFILE);
      }
    },
  });

  // ============================================
  // Register Mutation
  // ============================================
  const registerMutation = useMutation({
    mutationFn: (data: RegisterRequest) => authService.register(data),
    onSuccess: (response) => {
      if (response.success) {
        router.push(ROUTES.LOGIN + "?registered=true");
      }
    },
  });

  // ============================================
  // Google Login Mutation
  // ============================================
  const googleLoginMutation = useMutation({
    mutationFn: (data: GoogleLoginRequest) => authService.googleLogin(data),
    onSuccess: (response) => {
      if (response.success && response.data) {
        setTokens(response.data.access_token, response.data.refresh_token);
        setUser(response.data.user);
        queryClient.invalidateQueries({ queryKey: ["auth"] });
        // Redirect based on user role
        const userRoles = response.data.user.roles || [];
        const adminRoles = ["super_admin", "admin", "editor"];
        const canAccessAdmin = userRoles.some((r) => adminRoles.includes(r));
        router.push(canAccessAdmin ? ROUTES.ADMIN : ROUTES.PROFILE);
      }
    },
  });

  // ============================================
  // Logout Mutation
  // ============================================
  const logoutMutation = useMutation({
    mutationFn: () => authService.logout(),
    onSettled: () => {
      clearAuth();
      queryClient.clear();
      router.push(ROUTES.LOGIN);
    },
  });

  // ============================================
  // Change Password Mutation
  // ============================================
  const changePasswordMutation = useMutation({
    mutationFn: (data: ChangePasswordRequest) => authService.changePassword(data),
  });

  // ============================================
  // Forgot Password Mutation
  // ============================================
  const forgotPasswordMutation = useMutation({
    mutationFn: (data: ForgotPasswordRequest) => authService.forgotPassword(data),
  });

  // ============================================
  // Reset Password Mutation
  // ============================================
  const resetPasswordMutation = useMutation({
    mutationFn: (data: ResetPasswordRequest) => authService.resetPassword(data),
    onSuccess: (response) => {
      if (response.success) {
        router.push(ROUTES.LOGIN + "?reset=true");
      }
    },
  });

  // ============================================
  // Update Profile Mutation
  // ============================================
  const updateProfileMutation = useMutation({
    mutationFn: (data: { first_name: string; last_name: string }) =>
      authService.updateProfile(data),
    onSuccess: (response) => {
      if (response.success && response.data?.user) {
        setUser(response.data.user);
        queryClient.invalidateQueries({ queryKey: ["auth"] });
      }
    },
  });

  // ============================================
  // Initialize Auth (check existing session)
  // ============================================
  const initAuth = useCallback(async () => {
    setLoading(true);
    try {
      await refetchMe();
    } catch {
      clearAuth();
    }
  }, [refetchMe, setLoading, clearAuth]);

  return {
    // State
    user,
    isAuthenticated,
    isLoading,

    // Auth actions
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error,

    register: registerMutation.mutate,
    registerAsync: registerMutation.mutateAsync,
    isRegistering: registerMutation.isPending,
    registerError: registerMutation.error,

    googleLogin: googleLoginMutation.mutate,
    googleLoginAsync: googleLoginMutation.mutateAsync,
    isGoogleLoggingIn: googleLoginMutation.isPending,
    googleLoginError: googleLoginMutation.error,

    logout: logoutMutation.mutate,
    isLoggingOut: logoutMutation.isPending,

    changePassword: changePasswordMutation.mutate,
    changePasswordAsync: changePasswordMutation.mutateAsync,
    isChangingPassword: changePasswordMutation.isPending,
    changePasswordError: changePasswordMutation.error,

    updateProfile: updateProfileMutation.mutate,
    updateProfileAsync: updateProfileMutation.mutateAsync,
    isUpdatingProfile: updateProfileMutation.isPending,
    updateProfileError: updateProfileMutation.error,

    forgotPassword: forgotPasswordMutation.mutate,
    forgotPasswordAsync: forgotPasswordMutation.mutateAsync,
    isSendingReset: forgotPasswordMutation.isPending,
    forgotPasswordError: forgotPasswordMutation.error,

    resetPassword: resetPasswordMutation.mutate,
    resetPasswordAsync: resetPasswordMutation.mutateAsync,
    isResettingPassword: resetPasswordMutation.isPending,
    resetPasswordError: resetPasswordMutation.error,

    // Permission helpers
    hasRole,
    hasPermission,
    hasAnyRole,
    isSuperAdmin,
    isAdmin,
    hasAdminAccess,
    getHomeRoute,

    // Utils
    initAuth,
    refetchMe,
  };
}
