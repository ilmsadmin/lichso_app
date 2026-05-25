"use client";

import { AIChatMessageResponse } from "@/types/ai";
import { AIStreamingText } from "./AIStreamingText";
import { cn } from "@/lib/utils";
import { Bot, User } from "lucide-react";

interface AIChatMessageProps {
  message: AIChatMessageResponse;
  isStreaming?: boolean;
  streamingContent?: string;
}

export function AIChatMessage({ message, isStreaming, streamingContent }: AIChatMessageProps) {
  const isAssistant = message.role === "assistant";
  const content = isStreaming && streamingContent ? streamingContent : message.content;

  return (
    <div
      className={cn(
        "flex gap-3",
        isAssistant ? "justify-start" : "justify-end flex-row-reverse"
      )}
    >
      {/* Avatar */}
      <div
        className={cn(
          "flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-white",
          isAssistant
            ? "bg-gradient-to-br from-purple-500 to-indigo-500"
            : "bg-gradient-to-br from-amber-500 to-orange-400"
        )}
      >
        {isAssistant ? (
          <Bot className="h-3.5 w-3.5" />
        ) : (
          <User className="h-3.5 w-3.5" />
        )}
      </div>

      {/* Bubble */}
      <div
        className={cn(
          "max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed",
          isAssistant
            ? "rounded-tl-sm bg-white shadow-sm border border-gray-100"
            : "rounded-tr-sm text-white"
        )}
        style={
          !isAssistant
            ? { background: "linear-gradient(135deg, #6366f1, #a855f7)" }
            : undefined
        }
      >
        {isAssistant ? (
          <AIStreamingText
            text={content}
            isStreaming={isStreaming}
            className="text-gray-700"
          />
        ) : (
          <p className="whitespace-pre-wrap">{content}</p>
        )}

        {/* Timestamp + tokens */}
        <div
          className={cn(
            "mt-1.5 text-[10px]",
            isAssistant ? "text-gray-400" : "text-white/60"
          )}
        >
          {new Date(message.created_at).toLocaleTimeString("vi-VN", {
            hour: "2-digit",
            minute: "2-digit",
          })}
          {message.tokens_used ? ` · ${message.tokens_used} tokens` : ""}
        </div>
      </div>
    </div>
  );
}
