"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import { PromptForm } from "@/components/ai/PromptForm";

export default function EditPromptPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const promptId = parseInt(id);

  return (
    <PromptForm
      mode="edit"
      promptId={promptId}
      onSuccess={() => router.push("/admin/ai-dashboard")}
      onCancel={() => router.push("/admin/ai-dashboard")}
    />
  );
}
