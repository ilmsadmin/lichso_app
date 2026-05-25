import Link from "next/link";
import { Button } from "@/components/ui/button";
import { FileQuestion, Home, ArrowLeft } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center px-4">
      <div className="mx-auto max-w-md text-center">
        <div className="mb-8 flex justify-center">
          <div className="bg-muted flex h-24 w-24 items-center justify-center rounded-full">
            <FileQuestion className="text-muted-foreground h-12 w-12" />
          </div>
        </div>
        <h1 className="mb-2 text-6xl font-bold tracking-tight">404</h1>
        <h2 className="text-muted-foreground mb-4 text-xl font-semibold">Page Not Found</h2>
        <p className="text-muted-foreground mb-8">
          Sorry, the page you&apos;re looking for doesn&apos;t exist or has been moved. Please check
          the URL or navigate back.
        </p>
        <div className="flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Button asChild>
            <Link href="/">
              <Home className="mr-2 h-4 w-4" />
              Go Home
            </Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href="javascript:history.back()">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Go Back
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
