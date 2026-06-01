"use client";

import { useParams } from "next/navigation";
import { useSurvey } from "@/hooks/useSurveys";
import SurveyForm from "../SurveyForm";

export default function EditSurveyPage() {
  const { id } = useParams<{ id: string }>();
  const { data, isLoading } = useSurvey(id);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="flex flex-col items-center gap-3">
          <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
          <p className="text-muted-foreground text-sm">Đang tải thông tin khảo sát...</p>
        </div>
      </div>
    );
  }

  const survey = data?.data;

  if (!survey) {
    return (
      <div className="py-12 text-center">
        <p className="text-muted-foreground">Không tìm thấy thông tin khảo sát.</p>
      </div>
    );
  }

  return <SurveyForm survey={survey} isEdit={true} />;
}
