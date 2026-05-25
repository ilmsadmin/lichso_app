"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { AlertTriangle, RefreshCw, Home } from "lucide-react";
import Link from "next/link";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("Application error:", error);
  }, [error]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center px-4">
      <div className="mx-auto max-w-md text-center">
        <div className="mb-8 flex justify-center">
          <div className="bg-destructive/10 flex h-24 w-24 items-center justify-center rounded-full">
            <AlertTriangle className="text-destructive h-12 w-12" />
          </div>
        </div>
        <h1 className="mb-2 text-4xl font-bold tracking-tight">Something Went Wrong</h1>
        <p className="text-muted-foreground mb-8">
          An unexpected error occurred. Our team has been notified and is working on a fix. Please
          try again or return to the homepage.
        </p>
        {error.digest && (
          <p className="bg-muted text-muted-foreground mb-6 rounded-lg px-4 py-2 font-mono text-xs">
            Error ID: {error.digest}
          </p>
        )}
        <div className="flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Button onClick={reset}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Try Again
          </Button>
          <Button variant="outline" asChild>
            <Link href="/">
              <Home className="mr-2 h-4 w-4" />
              Go Home
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
