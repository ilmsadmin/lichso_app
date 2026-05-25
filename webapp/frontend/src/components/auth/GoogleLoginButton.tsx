"use client";

import { useEffect, useCallback, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import type { ApiResponse } from "@/types/api";
import { AxiosError } from "axios";

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: { credential: string }) => void;
            auto_select?: boolean;
            cancel_on_tap_outside?: boolean;
            prompt_parent_id?: string;
          }) => void;
          renderButton: (
            element: HTMLElement,
            config: {
              theme?: "outline" | "filled_blue" | "filled_black";
              size?: "large" | "medium" | "small";
              type?: "standard" | "icon";
              shape?: "rectangular" | "pill" | "circle" | "square";
              text?: "signin_with" | "signup_with" | "continue_with" | "signin";
              width?: number;
              logo_alignment?: "left" | "center";
            }
          ) => void;
          prompt: (
            momentListener?: (notification: {
              isNotDisplayed: () => boolean;
              isSkippedMoment: () => boolean;
              isDismissedMoment: () => boolean;
              getMomentType: () => string;
            }) => void
          ) => void;
          cancel: () => void;
        };
      };
    };
  }
}

interface GoogleLoginButtonProps {
  onError?: (error: string) => void;
}

export function GoogleLoginButton({ onError }: GoogleLoginButtonProps) {
  const { googleLogin, isGoogleLoggingIn } = useAuth();
  const [isScriptLoaded, setIsScriptLoaded] = useState(false);

  const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;

  const handleCredentialResponse = useCallback(
    (response: { credential: string }) => {
      googleLogin(
        { id_token: response.credential },
        {
          onError: (err) => {
            if (err instanceof AxiosError) {
              const data = err.response?.data as ApiResponse | undefined;
              onError?.(data?.message || "Google login failed. Please try again.");
            } else {
              onError?.("An unexpected error occurred.");
            }
          },
        }
      );
    },
    [googleLogin, onError]
  );

  useEffect(() => {
    if (!googleClientId) return;

    // Load Google Identity Services script
    const existingScript = document.getElementById("google-identity-services");
    if (existingScript) {
      // Script already loaded
      if (window.google?.accounts) {
        setIsScriptLoaded(true);
      }
      return;
    }

    const script = document.createElement("script");
    script.id = "google-identity-services";
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = () => setIsScriptLoaded(true);
    document.head.appendChild(script);
  }, [googleClientId]);

  useEffect(() => {
    if (!isScriptLoaded || !googleClientId || !window.google?.accounts) return;

    window.google.accounts.id.initialize({
      client_id: googleClientId,
      callback: handleCredentialResponse,
      auto_select: false,
      cancel_on_tap_outside: true,
    });

    const buttonDiv = document.getElementById("google-signin-button");
    if (buttonDiv) {
      window.google.accounts.id.renderButton(buttonDiv, {
        theme: "outline",
        size: "large",
        type: "standard",
        shape: "rectangular",
        text: "signin_with",
        width: 400,
        logo_alignment: "left",
      });
    }
  }, [isScriptLoaded, googleClientId, handleCredentialResponse]);

  if (!googleClientId) {
    return null;
  }

  return (
    <div className="w-full">
      {isGoogleLoggingIn ? (
        <div className="border-input bg-background text-muted-foreground flex w-full items-center justify-center rounded-lg border px-4 py-2.5 text-sm">
          <span className="border-primary mr-2 h-4 w-4 animate-spin rounded-full border-2 border-t-transparent" />
          Signing in with Google...
        </div>
      ) : (
        <div
          id="google-signin-button"
          className="flex w-full items-center justify-center [&>div]:w-full"
        />
      )}
    </div>
  );
}
