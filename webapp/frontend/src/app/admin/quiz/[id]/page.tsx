"use client";

import { useParams } from "next/navigation";
import { QuestionForm } from "../QuestionForm";
import { useQuizQuestion } from "@/hooks/useQuiz";

export default function EditQuizQuestionPage() {
  const { id } = useParams<{ id: string }>();
  const questionId = parseInt(id, 10);
  const { data, isLoading } = useQuizQuestion(questionId);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!data?.data) {
    return (
      <div className="flex items-center justify-center py-24">
        <p className="text-muted-foreground">Không tìm thấy câu hỏi</p>
      </div>
    );
  }

  return <QuestionForm defaultValues={data.data} />;
}
