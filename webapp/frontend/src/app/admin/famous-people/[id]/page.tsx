"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Pencil, Trash2, MapPin, Briefcase, Calendar, Hash, User, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useFamousPerson, useDeleteFamousPerson } from "@/hooks/useFamousPeople";
import { formatDate, getInitials, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { useState } from "react";

export default function FamousPersonDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data, isLoading } = useFamousPerson(id);
  const deleteFamousPerson = useDeleteFamousPerson();
  const [showDelete, setShowDelete] = useState(false);

  const person = data?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!person) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Không tìm thấy nhân vật.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_FAMOUS_PEOPLE)}>
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.push(ROUTES.ADMIN_FAMOUS_PEOPLE)}
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">Chi tiết nhân vật</h1>
            <p className="text-muted-foreground text-sm">Xem và quản lý thông tin nhân vật</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" asChild>
            <a href={`${ROUTES.FAMOUS_PEOPLE}/${person.id}`} target="_blank" rel="noopener noreferrer">
              <ExternalLink className="mr-2 h-4 w-4" />
              Xem trang công khai
            </a>
          </Button>
          <PermissionGate permission="content.update">
            <Button variant="outline" size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_FAMOUS_PEOPLE}/${person.id}/edit`}>
                <Pencil className="mr-2 h-4 w-4" />
                Chỉnh sửa
              </Link>
            </Button>
          </PermissionGate>
          <PermissionGate permission="content.delete">
            <Button
              variant="outline"
              size="sm"
              className="text-destructive hover:text-destructive"
              onClick={() => setShowDelete(true)}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Xóa
            </Button>
          </PermissionGate>
        </div>
      </div>

      {/* Hero Card */}
      <Card className="overflow-hidden">
        <div className="bg-gradient-to-br from-primary/5 via-primary/10 to-primary/5 p-8">
          <div className="flex flex-col items-center text-center sm:flex-row sm:items-start sm:text-left gap-6">
            <Avatar className="h-24 w-24 shrink-0 ring-4 ring-background shadow-lg">
              {person.image_url && <AvatarImage src={getImageUrl(person.image_url)} alt={person.name} />}
              <AvatarFallback className="bg-primary text-primary-foreground text-2xl">
                {getInitials(person.name)}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1 min-w-0">
              <h2 className="text-2xl font-bold">{person.name}</h2>
              {person.original_name && (
                <p className="text-muted-foreground mt-0.5">{person.original_name}</p>
              )}
              {person.short_bio && (
                <p className="text-muted-foreground mt-2 text-sm leading-relaxed">{person.short_bio}</p>
              )}
              <div className="mt-3 flex flex-wrap items-center gap-2">
                {person.is_vietnamese && (
                  <Badge variant="default">🇻🇳 Người Việt Nam</Badge>
                )}
                {person.category && (
                  <Badge variant="outline">{person.category}</Badge>
                )}
                <Badge variant={person.is_active ? "default" : "secondary"}>
                  {person.is_active ? "Hoạt động" : "Ẩn"}
                </Badge>
              </div>
            </div>
          </div>
        </div>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Personal Info Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <User className="h-4 w-4 text-primary" />
              Thông tin cá nhân
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày sinh</p>
                <p className="text-sm mt-1 font-medium">{person.birth_date ? formatDate(person.birth_date) : "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày mất</p>
                <p className="text-sm mt-1">{person.death_date ? formatDate(person.death_date) : "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Quốc tịch</p>
                <p className="text-sm mt-1">{person.nationality || "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Nghề nghiệp</p>
                <p className="text-sm mt-1">{person.occupation || "—"}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Metadata Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Calendar className="h-4 w-4 text-primary" />
              Thông tin hệ thống
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Danh mục</p>
                <Badge variant="outline" className="mt-1">{person.category || "—"}</Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Người Việt Nam</p>
                <Badge variant={person.is_vietnamese ? "default" : "secondary"} className="mt-1">
                  {person.is_vietnamese ? "Có" : "Không"}
                </Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Trạng thái</p>
                <Badge variant={person.is_active ? "default" : "secondary"} className="mt-1">
                  {person.is_active ? "Hoạt động" : "Ẩn"}
                </Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày tạo</p>
                <p className="text-sm mt-1">{formatDate(person.created_at)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tags Card */}
      {person.tags && person.tags.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Hash className="h-4 w-4 text-primary" />
              Tags
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {person.tags.map((tag) => (
                <Badge key={tag} variant="outline" className="px-3 py-1">
                  {tag}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Delete Dialog */}
      <ConfirmDialog
        open={showDelete}
        onOpenChange={setShowDelete}
        title="Xóa nhân vật"
        description={`Bạn có chắc chắn muốn xóa nhân vật "${person.name}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={deleteFamousPerson.isPending}
        onConfirm={() => {
          deleteFamousPerson.mutate(person.id, {
            onSuccess: () => router.push(ROUTES.ADMIN_FAMOUS_PEOPLE),
          });
        }}
      />
    </div>
  );
}
