"use client";

import { useEffect } from "react";

export function ServiceWorkerRegistrar() {
  useEffect(() => {
    if (
      typeof window !== "undefined" &&
      "serviceWorker" in navigator &&
      process.env.NODE_ENV === "production"
    ) {
      navigator.serviceWorker
        .register("/sw.js")
        .then((registration) => {
          console.log("[SW] Registered:", registration.scope);

          // Check for updates periodically (every 60 minutes)
          setInterval(
            () => {
              registration.update();
            },
            60 * 60 * 1000
          );
        })
        .catch((err) => {
          console.error("[SW] Registration failed:", err);
        });
    }
  }, []);

  return null;
}
