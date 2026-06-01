"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Users, BarChart3, MessageSquare, Clipboard } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { useSurveyStats } from "@/hooks/useSurveys";
import { ROUTES } from "@/lib/constants";

export default function SurveyResponsesPage() {
  const { id } = useParams<{ id: string }>();
  const { data, isLoading, error } = useSurveyStats(id);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="flex flex-col items-center gap-3">
          <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
          <p className="text-muted-foreground text-sm">Đang tải kết quả khảo sát...</p>
        </div>
      </div>
    );
  }

  const stats = data?.data;

  if (error || !stats) {
    return (
      <div className="py-12 text-center space-y-4">
        <p className="text-muted-foreground">Không tìm thấy thông tin hoặc thống kê khảo sát.</p>
        <Link href={ROUTES.ADMIN_SURVEYS}>
          <Button variant="outline">
            <ArrowLeft className="mr-2 h-4 w-4" /> Quay lại danh sách
          </Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Link href={ROUTES.ADMIN_SURVEYS}>
          <Button variant="ghost" size="sm">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Danh sách khảo sát
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Thống kê & Kết quả</h1>
          <p className="text-muted-foreground">
            Phân tích số liệu và phản hồi chi tiết từ người dùng.
          </p>
        </div>
      </div>

      {/* Survey Title & Total Responses Summary */}
      <div className="grid gap-6 md:grid-cols-3">
        <Card className="md:col-span-2">
          <CardHeader className="pb-3">
            <CardDescription>Cuộc khảo sát</CardDescription>
            <CardTitle className="text-xl md:text-2xl text-primary">{stats.survey_title}</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">
              Khảo sát đang hoạt động thu thập thông tin tự động trên ứng dụng di động Lịch Số. Các số liệu được cập nhật theo thời gian thực khi người dùng gửi biểu mẫu.
            </p>
          </CardContent>
        </Card>

        <Card className="bg-primary/5 border-primary/25">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-1.5">
              <Users className="h-4 w-4 text-primary" />
              Tổng lượt tham gia
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-2">
            <span className="text-4xl font-extrabold tracking-tight text-primary">
              {stats.total_responses}
            </span>
            <p className="text-xs text-muted-foreground mt-1">phản hồi từ người dùng</p>
          </CardContent>
        </Card>
      </div>

      {/* Aggregated Question Stats */}
      <div className="space-y-6">
        <h2 className="text-lg font-semibold tracking-tight flex items-center gap-2">
          <BarChart3 className="h-5 w-5 text-primary" />
          Phân tích chi tiết từng câu hỏi
        </h2>

        {stats.question_stats.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4 text-center">Khảo sát này chưa có câu hỏi nào.</p>
        ) : (
          stats.question_stats.map((q, qIdx) => {
            const isText = q.type === "text";

            return (
              <Card key={q.question_index}>
                <CardHeader className="pb-3 border-b">
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <Badge variant="outline">Câu {qIdx + 1}</Badge>
                        <Badge variant="secondary" className="capitalize text-[10px]">
                          {q.type === "single_choice"
                            ? "Lựa chọn duy nhất"
                            : q.type === "multiple_choice"
                              ? "Lựa chọn nhiều"
                              : "Tự luận văn bản"}
                        </Badge>
                      </div>
                      <CardTitle className="text-lg mt-1.5">{q.question_title}</CardTitle>
                    </div>
                    <div className="text-right shrink-0">
                      <span className="text-sm font-semibold block">{q.total_answers}</span>
                      <span className="text-[10px] text-muted-foreground">lượt trả lời</span>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="pt-4">
                  {isText ? (
                    /* Text answers list */
                    <div className="space-y-2">
                      <div className="flex items-center gap-1.5 text-xs text-muted-foreground font-semibold mb-2">
                        <MessageSquare className="h-3.5 w-3.5" />
                        Ý kiến tự luận từ người dùng ({q.text_answers?.length ?? 0}):
                      </div>
                      {(!q.text_answers || q.text_answers.length === 0) ? (
                        <p className="text-sm text-muted-foreground italic py-2">Chưa có câu phản hồi bằng văn bản.</p>
                      ) : (
                        <div className="max-h-[250px] overflow-y-auto border rounded-lg bg-muted/20 p-2 space-y-2 divide-y divide-border">
                          {q.text_answers.map((txt, tIdx) => (
                            <div key={tIdx} className="text-sm pt-2 first:pt-0 leading-relaxed text-foreground/90">
                              &ldquo;{txt}&rdquo;
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  ) : (
                    /* Choice percentage progress bars */
                    <div className="space-y-4">
                      {(!q.option_counts || q.option_counts.length === 0) ? (
                        <p className="text-sm text-muted-foreground italic">Chưa có dữ liệu lựa chọn.</p>
                      ) : (
                        q.option_counts.map((opt, oIdx) => (
                          <div key={oIdx} className="space-y-1.5">
                            <div className="flex items-center justify-between text-sm">
                              <span className="font-medium text-foreground">{opt.option}</span>
                              <div className="flex items-center gap-2 font-semibold">
                                <span>{opt.count} lượt</span>
                                <Badge variant="outline" className="font-mono text-xs">
                                  {opt.percentage.toFixed(1)}%
                                </Badge>
                              </div>
                            </div>
                            <Progress
                              value={opt.percentage}
                              className="h-2.5"
                              style={{
                                backgroundColor: "hsl(var(--muted))",
                              }}
                            />
                          </div>
                        ))
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })
        )}
      </div>
    </div>
  );
}
