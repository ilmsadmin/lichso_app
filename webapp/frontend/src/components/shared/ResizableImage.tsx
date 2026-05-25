"use client";

import Image from "@tiptap/extension-image";
import { ReactNodeViewRenderer, NodeViewWrapper } from "@tiptap/react";
import type { NodeViewProps } from "@tiptap/react";
import { useCallback, useRef, useState } from "react";
import { Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";

function ResizableImageView({
  node,
  updateAttributes,
  selected,
  deleteNode,
}: NodeViewProps) {
  const src = node.attrs.src as string;
  const alt = (node.attrs.alt as string) ?? "";
  const title = (node.attrs.title as string) ?? undefined;
  const width = (node.attrs.width as string) ?? "100%";

  const innerRef = useRef<HTMLDivElement>(null);
  const startX = useRef(0);
  const startW = useRef(0);
  const [resizing, setResizing] = useState(false);

  const onPointerDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      e.stopPropagation();
      (e.currentTarget as HTMLElement).setPointerCapture(e.pointerId);
      const el = innerRef.current;
      if (!el) return;
      startX.current = e.clientX;
      startW.current = el.offsetWidth;
      setResizing(true);

      const onMove = (ev: PointerEvent) => {
        const newW = Math.max(60, startW.current + ev.clientX - startX.current);
        el.style.width = newW + "px";
      };
      const onUp = (ev: PointerEvent) => {
        const newW = Math.max(60, startW.current + ev.clientX - startX.current);
        el.style.width = newW + "px";
        updateAttributes({ width: newW + "px" });
        setResizing(false);
        window.removeEventListener("pointermove", onMove);
        window.removeEventListener("pointerup", onUp);
      };
      window.addEventListener("pointermove", onMove);
      window.addEventListener("pointerup", onUp);
    },
    [updateAttributes]
  );

  const applyPreset = useCallback(
    (w: string) => {
      if (innerRef.current) innerRef.current.style.width = w;
      updateAttributes({ width: w });
    },
    [updateAttributes]
  );

  return (
    <NodeViewWrapper as="div" className="relative my-4" contentEditable={false}>
      <div
        ref={innerRef}
        className={resizing ? "relative inline-block select-none" : "relative inline-block"}
        style={{ width }}
      >
        <img
          src={src}
          alt={alt}
          title={title}
          draggable={false}
          className={selected ? "block h-auto w-full rounded-lg ring-2 ring-blue-500 ring-offset-2" : "block h-auto w-full rounded-lg"}
        />

        {selected && (
          <div
            className="absolute -top-9 left-0 z-[100] flex items-center gap-1 rounded-lg border border-border bg-background px-2 py-1 shadow-lg"
            style={{ pointerEvents: "all" }}
          >
            <span className="text-[11px] text-muted-foreground">Rong:</span>
            {["25%", "50%", "75%", "100%"].map((v) => (
              <button
                key={v}
                type="button"
                onMouseDown={(e) => {
                  e.preventDefault();
                  applyPreset(v);
                }}
                className="rounded px-1.5 py-0.5 text-[11px] font-medium hover:bg-accent"
              >
                {v}
              </button>
            ))}
            <div className="mx-1 h-3.5 w-px bg-border" />
            <button
              type="button"
              onMouseDown={(e) => {
                e.preventDefault();
                deleteNode();
              }}
              className="flex h-5 w-5 items-center justify-center rounded hover:bg-destructive/10 hover:text-destructive"
            >
              <Trash2 className="h-3 w-3" />
            </button>
          </div>
        )}

        {selected && (
          <div
            onPointerDown={onPointerDown}
            className="absolute bottom-0 right-0 z-[100] h-5 w-5 cursor-se-resize rounded-tl-md bg-blue-500 shadow"
            style={{ touchAction: "none" }}
          />
        )}
      </div>
    </NodeViewWrapper>
  );
}

export const ResizableImage = Image.extend({
  inline() {
    return false;
  },
  group() {
    return "block";
  },
  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: "100%",
        parseHTML: (el) =>
          el.getAttribute("width") || el.style.width || "100%",
        renderHTML: (attrs) => ({
          width: attrs.width,
          style: "width:" + attrs.width + ";height:auto",
        }),
      },
    };
  },
  addNodeView() {
    return ReactNodeViewRenderer(ResizableImageView);
  },
});
