"use client";

import { useEffect, useRef } from "react";
import { cn } from "@/lib/utils";

interface AIStreamingTextProps {
  text: string;
  isStreaming?: boolean;
  className?: string;
  /** Render as markdown-like (replace newlines with <br>) */
  asMarkdown?: boolean;
}

/**
 * AIStreamingText — displays streaming AI text with a blinking cursor
 * while streaming is active.
 */
export function AIStreamingText({
  text,
  isStreaming = false,
  className,
  asMarkdown = true,
}: AIStreamingTextProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom while streaming
  useEffect(() => {
    if (isStreaming && bottomRef.current) {
      bottomRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [text, isStreaming]);

  if (!text && !isStreaming) return null;

  if (asMarkdown) {
    const html = text
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      // Bold
      .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
      // Headers
      .replace(/^### (.*$)/gm, '<h3 class="text-base font-semibold mt-3 mb-1">$1</h3>')
      .replace(/^## (.*$)/gm, '<h2 class="text-lg font-bold mt-4 mb-1">$1</h2>')
      .replace(/^# (.*$)/gm, '<h1 class="text-xl font-bold mt-4 mb-2">$1</h1>')
      // Bullet lists
      .replace(/^[-•] (.*$)/gm, '<li class="ml-4 list-disc">$1</li>')
      // Newlines
      .replace(/\n/g, "<br/>");

    return (
      <div className={cn("text-sm leading-relaxed", className)}>
        <div dangerouslySetInnerHTML={{ __html: html }} />
        {isStreaming && (
          <span className="inline-block w-0.5 h-4 bg-amber-500 animate-pulse ml-0.5 align-middle" />
        )}
        <div ref={bottomRef} />
      </div>
    );
  }

  return (
    <p className={cn("text-sm leading-relaxed whitespace-pre-wrap", className)}>
      {text}
      {isStreaming && (
        <span className="inline-block w-0.5 h-4 bg-amber-500 animate-pulse ml-0.5 align-middle" />
      )}
      <span ref={bottomRef} />
    </p>
  );
}
