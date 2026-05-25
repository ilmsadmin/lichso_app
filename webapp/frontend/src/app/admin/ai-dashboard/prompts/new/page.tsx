"use client";

import { useRouter } from "next/navigation";
import { PromptForm } from "@/components/ai/PromptForm";

export default function NewPromptPage() {
  const router = useRouter();
  return (
    <PromptForm
      mode="create"
      onSuccess={() => router.push("/admin/ai-dashboard")}
      onCancel={() => router.push("/admin/ai-dashboard")}
    />
  );
}
