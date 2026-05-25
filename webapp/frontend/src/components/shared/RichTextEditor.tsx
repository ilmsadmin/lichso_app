"use client";

import { useEditor, EditorContent } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import Link from "@tiptap/extension-link";
import { ResizableImage } from "@/components/shared/ResizableImage";
import Placeholder from "@tiptap/extension-placeholder";
import TextAlign from "@tiptap/extension-text-align";
import Underline from "@tiptap/extension-underline";
import Highlight from "@tiptap/extension-highlight";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  Bold,
  Italic,
  Underline as UnderlineIcon,
  Strikethrough,
  Code,
  List,
  ListOrdered,
  AlignLeft,
  AlignCenter,
  AlignRight,
  Heading1,
  Heading2,
  Heading3,
  Quote,
  Undo,
  Redo,
  LinkIcon,
  ImageIcon,
  Highlighter,
  Minus,
  Pilcrow,
  RemoveFormatting,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { MediaPickerV3 } from "@/components/shared/MediaPickerV3";
import { cn, getImageUrl, fixContentImageUrls, stripContentImageUrls } from "@/lib/utils";

// ============================================
// Toggle component (if not available from ui)
// ============================================

interface ToolbarButtonProps {
  onClick: () => void;
  isActive?: boolean;
  disabled?: boolean;
  title?: string;
  children: React.ReactNode;
}

function ToolbarButton({ onClick, isActive, disabled, title, children }: ToolbarButtonProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      title={title}
      className={cn(
        "hover:bg-accent hover:text-accent-foreground inline-flex h-8 w-8 items-center justify-center rounded-md text-sm transition-colors disabled:pointer-events-none disabled:opacity-50",
        isActive && "bg-accent text-accent-foreground"
      )}
    >
      {children}
    </button>
  );
}

// ============================================
// RichTextEditor Props
// ============================================

interface RichTextEditorProps {
  content: string;
  onChange: (html: string) => void;
  placeholder?: string;
  className?: string;
  editable?: boolean;
}

// ============================================
// RichTextEditor Component
// ============================================

export function RichTextEditor({
  content,
  onChange,
  placeholder = "Bắt đầu viết nội dung...",
  className,
  editable = true,
}: RichTextEditorProps) {
  const [linkDialogOpen, setLinkDialogOpen] = useState(false);
  const [linkUrl, setLinkUrl] = useState("");
  const [imageDialogOpen, setImageDialogOpen] = useState(false);
  const [imageUrl, setImageUrl] = useState("");
  const [imageAlt, setImageAlt] = useState("");
  const [mediaPickerOpen, setMediaPickerOpen] = useState(false);
  const isInternalUpdate = useRef(false);

  const editor = useEditor({
    immediatelyRender: false,
    extensions: [
      StarterKit.configure({
        heading: { levels: [1, 2, 3] },
        codeBlock: false,
      }),
      Link.configure({
        openOnClick: false,
        HTMLAttributes: {
          class: "text-primary underline cursor-pointer",
        },
      }),
      ResizableImage,
      Placeholder.configure({ placeholder }),
      TextAlign.configure({ types: ["heading", "paragraph"] }),
      Underline,
      Highlight.configure({ multicolor: false }),
    ],
    content: fixContentImageUrls(content),
    editable,
    onUpdate: ({ editor }) => {
      isInternalUpdate.current = true;
      // Strip hosts so the parent always receives clean relative paths
      onChange(stripContentImageUrls(editor.getHTML()));
    },
    editorProps: {
      attributes: {
        class:
          "article-content min-h-[200px] p-4 focus:outline-none",
      },
    },
  });

  // Sync content from outside (skip if the change originated from the editor itself)
  useEffect(() => {
    if (isInternalUpdate.current) {
      isInternalUpdate.current = false;
      return;
    }
    if (editor && content !== stripContentImageUrls(editor.getHTML())) {
      editor.commands.setContent(fixContentImageUrls(content));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [content]);

  // ============================================
  // Link Dialog
  // ============================================

  const openLinkDialog = useCallback(() => {
    const previousUrl = editor?.getAttributes("link").href ?? "";
    setLinkUrl(previousUrl);
    setLinkDialogOpen(true);
  }, [editor]);

  const setLink = useCallback(() => {
    if (!editor) return;
    if (linkUrl === "") {
      editor.chain().focus().extendMarkRange("link").unsetLink().run();
    } else {
      editor.chain().focus().extendMarkRange("link").setLink({ href: linkUrl }).run();
    }
    setLinkDialogOpen(false);
    setLinkUrl("");
  }, [editor, linkUrl]);

  // ============================================
  // Image Dialog
  // ============================================

  const openImageDialog = useCallback(() => {
    setImageUrl("");
    setImageAlt("");
    setImageDialogOpen(true);
  }, []);

  const insertImage = useCallback(() => {
    if (!editor || !imageUrl) return;
    editor.chain().focus().setImage({ src: imageUrl, alt: imageAlt }).run();
    setImageDialogOpen(false);
    setImageUrl("");
    setImageAlt("");
  }, [editor, imageUrl, imageAlt]);

  if (!editor) return null;

  return (
    <div className={cn("bg-background rounded-lg border", className)}>
      {/* Toolbar */}
      {editable && (
        <div className="flex flex-wrap items-center gap-0.5 border-b p-1">
          {/* Undo / Redo */}
          <ToolbarButton
            onClick={() => editor.chain().focus().undo().run()}
            disabled={!editor.can().undo()}
            title="Hoàn tác"
          >
            <Undo className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().redo().run()}
            disabled={!editor.can().redo()}
            title="Làm lại"
          >
            <Redo className="h-4 w-4" />
          </ToolbarButton>

          <Separator orientation="vertical" className="mx-1 h-6" />

          {/* Paragraph */}
          <ToolbarButton
            onClick={() => editor.chain().focus().setParagraph().run()}
            isActive={editor.isActive("paragraph")}
            title="Đoạn văn"
          >
            <Pilcrow className="h-4 w-4" />
          </ToolbarButton>

          {/* Headings */}
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
            isActive={editor.isActive("heading", { level: 1 })}
            title="Heading 1"
          >
            <Heading1 className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
            isActive={editor.isActive("heading", { level: 2 })}
            title="Heading 2"
          >
            <Heading2 className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
            isActive={editor.isActive("heading", { level: 3 })}
            title="Heading 3"
          >
            <Heading3 className="h-4 w-4" />
          </ToolbarButton>

          <Separator orientation="vertical" className="mx-1 h-6" />

          {/* Inline formatting */}
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleBold().run()}
            isActive={editor.isActive("bold")}
            title="Đậm"
          >
            <Bold className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleItalic().run()}
            isActive={editor.isActive("italic")}
            title="Nghiêng"
          >
            <Italic className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleUnderline().run()}
            isActive={editor.isActive("underline")}
            title="Gạch chân"
          >
            <UnderlineIcon className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleStrike().run()}
            isActive={editor.isActive("strike")}
            title="Gạch ngang"
          >
            <Strikethrough className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleCode().run()}
            isActive={editor.isActive("code")}
            title="Code"
          >
            <Code className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleHighlight().run()}
            isActive={editor.isActive("highlight")}
            title="Highlight"
          >
            <Highlighter className="h-4 w-4" />
          </ToolbarButton>

          <Separator orientation="vertical" className="mx-1 h-6" />

          {/* Lists */}
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleBulletList().run()}
            isActive={editor.isActive("bulletList")}
            title="Danh sách"
          >
            <List className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleOrderedList().run()}
            isActive={editor.isActive("orderedList")}
            title="Danh sách số"
          >
            <ListOrdered className="h-4 w-4" />
          </ToolbarButton>

          {/* Blockquote */}
          <ToolbarButton
            onClick={() => editor.chain().focus().toggleBlockquote().run()}
            isActive={editor.isActive("blockquote")}
            title="Trích dẫn"
          >
            <Quote className="h-4 w-4" />
          </ToolbarButton>

          {/* Horizontal rule */}
          <ToolbarButton
            onClick={() => editor.chain().focus().setHorizontalRule().run()}
            title="Đường kẻ ngang"
          >
            <Minus className="h-4 w-4" />
          </ToolbarButton>

          <Separator orientation="vertical" className="mx-1 h-6" />

          {/* Alignment */}
          <ToolbarButton
            onClick={() => editor.chain().focus().setTextAlign("left").run()}
            isActive={editor.isActive({ textAlign: "left" })}
            title="Căn trái"
          >
            <AlignLeft className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().setTextAlign("center").run()}
            isActive={editor.isActive({ textAlign: "center" })}
            title="Căn giữa"
          >
            <AlignCenter className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor.chain().focus().setTextAlign("right").run()}
            isActive={editor.isActive({ textAlign: "right" })}
            title="Căn phải"
          >
            <AlignRight className="h-4 w-4" />
          </ToolbarButton>

          <Separator orientation="vertical" className="mx-1 h-6" />

          {/* Link & Image */}
          <ToolbarButton
            onClick={openLinkDialog}
            isActive={editor.isActive("link")}
            title="Thêm liên kết"
          >
            <LinkIcon className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton onClick={openImageDialog} title="Thêm hình ảnh">
            <ImageIcon className="h-4 w-4" />
          </ToolbarButton>

          <Separator orientation="vertical" className="mx-1 h-6" />

          {/* Clear formatting */}
          <ToolbarButton
            onClick={() => editor.chain().focus().clearNodes().unsetAllMarks().run()}
            title="Xóa định dạng"
          >
            <RemoveFormatting className="h-4 w-4" />
          </ToolbarButton>
        </div>
      )}

      {/* Editor Content */}
      <EditorContent editor={editor} />

      {/* Link Dialog */}
      <Dialog open={linkDialogOpen} onOpenChange={setLinkDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Thêm liên kết</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-2">
              <Label htmlFor="link-url">URL</Label>
              <Input
                id="link-url"
                value={linkUrl}
                onChange={(e) => setLinkUrl(e.target.value)}
                placeholder="https://example.com"
                onKeyDown={(e) => e.key === "Enter" && setLink()}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setLinkDialogOpen(false)}>
              Hủy
            </Button>
            <Button onClick={setLink}>{linkUrl ? "Áp dụng" : "Xóa liên kết"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Image Dialog — URL input or open media library */}
      <Dialog open={imageDialogOpen} onOpenChange={setImageDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Thêm hình ảnh</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            {/* Choose from library */}
            <Button
              type="button"
              variant="outline"
              className="h-auto w-full justify-start py-3"
              onClick={() => {
                setImageDialogOpen(false);
                setTimeout(() => setMediaPickerOpen(true), 150);
              }}
            >
              <ImageIcon className="text-primary mr-3 h-5 w-5" />
              <div className="text-left">
                <p className="font-medium">Chọn từ thư viện</p>
                <p className="text-muted-foreground text-xs">Chọn ảnh đã upload hoặc upload mới</p>
              </div>
            </Button>

            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-background text-muted-foreground px-2">hoặc nhập URL</span>
              </div>
            </div>

            {/* URL input */}
            <div className="space-y-3">
              <div className="space-y-2">
                <Label htmlFor="image-url">URL hình ảnh</Label>
                <Input
                  id="image-url"
                  value={imageUrl}
                  onChange={(e) => setImageUrl(e.target.value)}
                  placeholder="https://example.com/image.jpg"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="image-alt">Mô tả (alt text)</Label>
                <Input
                  id="image-alt"
                  value={imageAlt}
                  onChange={(e) => setImageAlt(e.target.value)}
                  placeholder="Mô tả hình ảnh"
                />
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setImageDialogOpen(false)}>
              Hủy
            </Button>
            <Button onClick={insertImage} disabled={!imageUrl}>
              Chèn hình ảnh
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Media Library Picker (V3) */}
      <MediaPickerV3
        open={mediaPickerOpen}
        onOpenChange={setMediaPickerOpen}
        accept={["image"]}
        title="Chọn hình ảnh cho bài viết"
        onSelect={(file) => {
          if (editor) {
            editor
              .chain()
              .focus()
              .setImage({
                src: getImageUrl(file.url),
                alt: file.alt || file.original_name,
              })
              .run();
          }
          setMediaPickerOpen(false);
        }}
      />
    </div>
  );
}
