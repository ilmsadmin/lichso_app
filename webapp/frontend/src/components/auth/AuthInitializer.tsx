"use client";

import { useEffect, useRef } from "react";
import { useAuthStore } from "@/stores/authStore";
import * as authService from "@/services/authService";
import Cookies from "js-cookie";
import { ACCESS_TOKEN_KEY } from "@/lib/constants";

/**
 * AuthInitializer restores the auth session on app load.
 * It checks for an existing access token cookie and fetches the current user.
 * Place this component once in the root Providers or layout.
 */
export function AuthInitializer() {
  const { setUser, clearAuth, setLoading } = useAuthStore();
  const initialized = useRef(false);

  useEffect(() => {
    if (initialized.current) return;
    initialized.current = true;

    const token = Cookies.get(ACCESS_TOKEN_KEY);

    if (!token) {
      // No token → not authenticated
      setLoading(false);
      return;
    }

    // Token exists → fetch user info
    setLoading(true);
    authService
      .getMe()
      .then((response) => {
        if (response.success && response.data?.user) {
          setUser(response.data.user);
        } else {
          clearAuth();
        }
      })
      .catch(() => {
        clearAuth();
      });
  }, [setUser, clearAuth, setLoading]);

  return null;
}
