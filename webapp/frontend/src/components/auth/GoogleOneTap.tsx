"use client";

import { useEffect, useRef } from "react";
import { useAuthStore } from "@/stores/authStore";
import { useAuth } from "@/hooks/useAuth";

const SESSION_KEY = "ls_onetap_dismissed";

export function GoogleOneTap() {
  const { isAuthenticated, isLoading } = useAuthStore();
  const { googleLogin } = useAuth();
  const initialized = useRef(false);

  const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;

  useEffect(() => {
    // Don't show if already authenticated, still loading, no client id, or dismissed this session
    if (isLoading || isAuthenticated || !googleClientId) return;
    if (sessionStorage.getItem(SESSION_KEY)) return;
    if (initialized.current) return;

    const initOneTap = () => {
      if (!window.google?.accounts) return;
      initialized.current = true;

      window.google.accounts.id.initialize({
        client_id: googleClientId,
        callback: (response: { credential: string }) => {
          googleLogin({ id_token: response.credential });
        },
        auto_select: false,
        cancel_on_tap_outside: true,
      });

      window.google.accounts.id.prompt((notification: {
        isNotDisplayed: () => boolean;
        isSkippedMoment: () => boolean;
        isDismissedMoment: () => boolean;
        getMomentType: () => string;
      }) => {
        if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
          // Prompt couldn't show or was skipped — mark dismissed so we don't retry
          sessionStorage.setItem(SESSION_KEY, "1");
        }
        if (notification.isDismissedMoment()) {
          sessionStorage.setItem(SESSION_KEY, "1");
        }
      });
    };

    // If script already loaded
    if (window.google?.accounts) {
      initOneTap();
      return;
    }

    // Load script if not present
    const existingScript = document.getElementById("google-identity-services");
    if (existingScript) {
      existingScript.addEventListener("load", initOneTap);
      return () => existingScript.removeEventListener("load", initOneTap);
    }

    const script = document.createElement("script");
    script.id = "google-identity-services";
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = initOneTap;
    document.head.appendChild(script);
  }, [isLoading, isAuthenticated, googleClientId, googleLogin]);

  // Cancel One Tap when user becomes authenticated
  useEffect(() => {
    if (isAuthenticated && window.google?.accounts) {
      window.google.accounts.id.cancel();
    }
  }, [isAuthenticated]);

  return null; // renders nothing — purely behavioral
}
