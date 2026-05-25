"use client";

import { useEffect, useRef, useState } from "react";
import { useChatSession, useStreamChat, useCreateChatSession, useDeleteChatSession, useChatSessions } from "@/hooks/useAI";
import { AIChatMessage } from "./AIChatMessage";
import { AIChatMessageResponse } from "@/types/ai";
import { Send, Plus, Trash2, Loader2, Bot, MessageSquare } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";

export function AIChatWindow() {
  const [activeUUID, setActiveUUID] = useState<string | null>(null);
  const [inputText, setInputText] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const { data: sessionsResp, isLoading: sessionsLoading } = useChatSessions();
  const sessions = sessionsResp?.data ?? [];

  const { data: sessionResp, isLoading: sessionLoading } = useChatSession(activeUUID ?? "");
  const session = sessionResp?.data;

  const createSession = useCreateChatSession();
  const deleteSession = useDeleteChatSession();
  const { isStreaming, streamText, error, sendMessage } = useStreamChat(activeUUID ?? "");

  // Auto-select first session
  useEffect(() => {
    if (!activeUUID && (sessions as AIChatMessageResponse[]).length === 0) return;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const first = (sessions as any[])[0];
    if (!activeUUID && first) {
      setActiveUUID(first.session_uuid);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessions]);

  // Scroll to bottom when messages change
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [session?.messages, streamText]);

  const handleNewSession = async () => {
    const res = await createSession.mutateAsync({ title: "Phiên tư vấn phong thủy mới" });
    const newUUID = res?.data?.session_uuid;
    if (newUUID) setActiveUUID(newUUID);
  };

  const handleSend = () => {
    const text = inputText.trim();
    if (!text || isStreaming || !activeUUID) return;
    setInputText("");
    sendMessage({ content: text, stream: true });
  };

  const messages: AIChatMessageResponse[] = session?.messages ?? [];

  return (
    <div className="flex h-[calc(100vh-12rem)] overflow-hidden rounded-2xl shadow-sm"
      style={{ border: "1px solid var(--ls-border-warm)" }}
    >
      {/* Sessions sidebar */}
      <div
        className="hidden w-56 shrink-0 flex-col border-r sm:flex"
        style={{
          background: "var(--ls-card-bg-strong)",
          borderColor: "var(--ls-border-soft)",
        }}
      >
        <div className="flex items-center justify-between px-4 py-3 border-b"
          style={{ borderColor: "var(--ls-border-soft)" }}
        >
          <span className="text-text-soft text-xs font-medium uppercase tracking-wider">
            Phiên chat
          </span>
          <button
            onClick={handleNewSession}
            disabled={createSession.isPending}
            className="rounded-lg p-1 text-indigo-500 hover:bg-indigo-50 transition-colors"
          >
            <Plus className="h-4 w-4" />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto p-2 space-y-1">
          {sessionsLoading ? (
            [...Array(3)].map((_, i) => <Skeleton key={i} className="h-10 w-full rounded-lg" />)
          ) : (sessions as any[]).length === 0 ? (
            <p className="px-3 py-4 text-center text-xs text-text-soft">
              Chưa có phiên chat nào
            </p>
          ) : (
            (sessions as any[]).map((s) => (
              <button
                key={s.session_uuid}
                onClick={() => setActiveUUID(s.session_uuid)}
                className={`group flex w-full items-center justify-between rounded-xl px-3 py-2.5 text-left text-xs transition-all ${
                  activeUUID === s.session_uuid
                    ? "text-indigo-700 font-medium"
                    : "text-text-mid hover:bg-gray-50"
                }`}
                style={
                  activeUUID === s.session_uuid
                    ? { background: "rgba(99,102,241,0.08)" }
                    : undefined
                }
              >
                <span className="flex-1 truncate">{s.title}</span>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteSession.mutate(s.session_uuid, {
                      onSuccess: () => {
                        if (activeUUID === s.session_uuid) setActiveUUID(null);
                      },
                    });
                  }}
                  className="ml-1 shrink-0 rounded p-0.5 text-gray-300 opacity-0 hover:text-red-400 group-hover:opacity-100 transition-opacity"
                >
                  <Trash2 className="h-3 w-3" />
                </button>
              </button>
            ))
          )}
        </div>
      </div>

      {/* Chat area */}
      <div className="flex flex-1 flex-col" style={{ background: "var(--ls-card-bg)" }}>
        {activeUUID ? (
          <>
            {/* Messages */}
            <div className="flex-1 overflow-y-auto px-4 py-5 space-y-4">
              {sessionLoading ? (
                [...Array(3)].map((_, i) => (
                  <div key={i} className={`flex gap-3 ${i % 2 ? "justify-end flex-row-reverse" : ""}`}>
                    <Skeleton className="h-8 w-8 rounded-full" />
                    <Skeleton className="h-16 w-60 rounded-2xl" />
                  </div>
                ))
              ) : messages.length === 0 ? (
                <div className="flex h-full flex-col items-center justify-center gap-3 text-center">
                  <div className="flex h-14 w-14 items-center justify-center rounded-2xl"
                    style={{ background: "linear-gradient(135deg, #a855f7, #6366f1)" }}
                  >
                    <Bot className="h-7 w-7 text-white" />
                  </div>
                  <p className="text-text-dark font-medium">Xin chào! Tôi là trợ lý phong thủy AI.</p>
                  <p className="text-text-soft text-sm max-w-xs">
                    Hãy hỏi tôi về phong thủy, xem ngày tốt, bát tự, hay bất kỳ điều gì về lịch sử văn hóa Việt Nam.
                  </p>
                </div>
              ) : (
                messages.map((msg, i) => (
                  <AIChatMessage
                    key={i}
                    message={msg}
                    isStreaming={isStreaming && i === messages.length - 1 && msg.role === "assistant"}
                    streamingContent={
                      isStreaming && i === messages.length - 1 && msg.role === "assistant"
                        ? streamText
                        : undefined
                    }
                  />
                ))
              )}

              {/* Streaming placeholder */}
              {isStreaming && streamText && (
                <AIChatMessage
                  message={{
                    role: "assistant",
                    content: streamText,
                    created_at: new Date().toISOString(),
                  }}
                  isStreaming={true}
                  streamingContent={streamText}
                />
              )}

              {error && (
                <p className="text-center text-xs text-red-500">{error}</p>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input */}
            <div
              className="border-t px-4 py-3"
              style={{ borderColor: "var(--ls-border-soft)" }}
            >
              <div className="flex items-end gap-2">
                <textarea
                  value={inputText}
                  onChange={(e) => setInputText(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && !e.shiftKey) {
                      e.preventDefault();
                      handleSend();
                    }
                  }}
                  placeholder="Hỏi về phong thủy, ngày tốt, bát tự… (Enter để gửi)"
                  rows={2}
                  className="text-text-dark flex-1 resize-none rounded-xl px-3 py-2.5 text-sm outline-none"
                  style={{
                    background: "rgba(255,252,248,0.5)",
                    border: "1px solid var(--ls-border-warm)",
                  }}
                />
                <button
                  onClick={handleSend}
                  disabled={!inputText.trim() || isStreaming}
                  className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-white transition-all hover:opacity-90 disabled:opacity-40"
                  style={{ background: "linear-gradient(135deg, #a855f7, #6366f1)" }}
                >
                  {isStreaming ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Send className="h-4 w-4" />
                  )}
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="flex h-full flex-col items-center justify-center gap-4 text-center px-6">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl"
              style={{ background: "linear-gradient(135deg, #a855f7, #6366f1)" }}
            >
              <MessageSquare className="h-8 w-8 text-white" />
            </div>
            <div>
              <p className="text-text-dark font-semibold text-lg">Chat Phong Thủy AI</p>
              <p className="text-text-soft text-sm mt-1">Bắt đầu một phiên tư vấn mới</p>
            </div>
            <button
              onClick={handleNewSession}
              disabled={createSession.isPending}
              className="flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-medium text-white"
              style={{ background: "linear-gradient(135deg, #a855f7, #6366f1)" }}
            >
              {createSession.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Plus className="h-4 w-4" />
              )}
              Bắt đầu chat
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
