"use client";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useCreateArticle, useAllCategories, useAllTags } from "@/hooks/useArticles";
import { ROUTES } from "@/lib/constants";

const ArticleForm = dynamic(
  () =>
    import("@/components/articles/ArticleForm").then((mod) => ({
      default: mod.ArticleForm,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function CreateArticlePage() {
  const router = useRouter();
  const createArticle = useCreateArticle();
  const { data: categoriesData } = useAllCategories();
  const { data: tagsData } = useAllTags();

  const categories = categoriesData?.data ?? [];
  const tags = tagsData?.data ?? [];

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_ARTICLES)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Tạo bài viết</h1>
          <p className="text-muted-foreground">Thêm bài viết mới vào hệ thống.</p>
        </div>
      </div>

      <ArticleForm
        categories={categories}
        tags={tags}
        isSubmitting={createArticle.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_ARTICLES)}
        onSubmit={(data) => {
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { published_at, ...articleData } = data;
          createArticle.mutate(articleData, {
            onSuccess: (response) => {
              if (response.success) {
                router.push(ROUTES.ADMIN_ARTICLES);
              }
            },
          });
        }}
      />
    </div>
  );
}
