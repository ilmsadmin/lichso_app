"use client";

import { useState } from "react";
import {
  User,
  Lock,
  Camera,
  Save,
  Eye,
  EyeOff,
  Shield,
  Calendar,
  Mail,
  BookmarkIcon,
  StickyNote,
  Timer,
  Bell,
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Separator } from "@/components/ui/separator";
import { useAuth } from "@/hooks/useAuth";
import { getInitials } from "@/lib/utils";
import { toast } from "sonner";
import Link from "next/link";
import { StreakWidget } from "@/components/lichso/StreakWidget";

const ROLE_LABELS: Record<string, string> = {
  super_admin: "Super Admin",
  admin: "Quản trị viên",
  editor: "Biên tập viên",
  viewer: "Người xem",
};

const ROLE_COLORS: Record<string, "default" | "secondary" | "destructive" | "outline"> = {
  super_admin: "destructive",
  admin: "default",
  editor: "secondary",
  viewer: "outline",
};

export default function ProfilePage() {
  const { user, changePassword, isChangingPassword, hasAdminAccess, updateProfile, isUpdatingProfile } = useAuth();
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [profileForm, setProfileForm] = useState({
    first_name: user?.first_name ?? "",
    last_name: user?.last_name ?? "",
    email: user?.email ?? "",
  });

  const [passwordForm, setPasswordForm] = useState({
    current_password: "",
    new_password: "",
    confirm_password: "",
  });

  const handleSaveProfile = async () => {
    try {
      await updateProfile({
        first_name: profileForm.first_name,
        last_name: profileForm.last_name,
      });
      toast.success("Cập nhật hồ sơ thành công");
    } catch {
      toast.error("Cập nhật thất bại. Vui lòng thử lại.");
    }
  };

  const handleChangePassword = async () => {
    if (passwordForm.new_password !== passwordForm.confirm_password) {
      toast.error("Mật khẩu mới không khớp");
      return;
    }
    if (passwordForm.new_password.length < 8) {
      toast.error("Mật khẩu phải có ít nhất 8 ký tự");
      return;
    }

    try {
      await changePassword({
        current_password: passwordForm.current_password,
        new_password: passwordForm.new_password,
      });
      setPasswordForm({
        current_password: "",
        new_password: "",
        confirm_password: "",
      });
      toast.success("Đổi mật khẩu thành công");
    } catch {
      toast.error("Đổi mật khẩu thất bại. Vui lòng kiểm tra mật khẩu hiện tại.");
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="flex items-center gap-2 text-3xl font-bold tracking-tight">
          <User className="h-8 w-8" />
          Hồ sơ cá nhân
        </h1>
        <p className="text-muted-foreground mt-1">Quản lý tài khoản và thông tin cá nhân của bạn</p>
      </div>

      {/* Avatar & Basic Info */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center gap-6">
            <div className="relative">
              <Avatar className="h-20 w-20 text-lg">
                <AvatarImage src={user?.avatar} alt={user?.full_name ?? "Avatar"} />
                <AvatarFallback className="text-xl">
                  {user?.full_name ? getInitials(user.full_name) : "U"}
                </AvatarFallback>
              </Avatar>
              <Button
                variant="outline"
                size="icon"
                className="absolute -right-1 -bottom-1 h-8 w-8 rounded-full"
              >
                <Camera className="h-4 w-4" />
              </Button>
            </div>
            <div>
              <p className="text-lg font-semibold">{user?.full_name}</p>
              <p className="text-muted-foreground text-sm">{user?.email}</p>
              <div className="mt-2 flex gap-1.5">
                {user?.roles?.map((role) => (
                  <Badge key={role} variant={ROLE_COLORS[role] || "outline"}>
                    {ROLE_LABELS[role] || role}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Quick Links */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Card className="hover:border-primary/50 transition-colors">
          <Link href="/profile/bookmarks" className="block">
            <CardContent className="flex items-center gap-4 pt-6">
              <div className="bg-primary/10 flex h-12 w-12 items-center justify-center rounded-lg">
                <BookmarkIcon className="text-primary h-6 w-6" />
              </div>
              <div>
                <p className="font-medium">Bookmarks</p>
                <p className="text-muted-foreground text-sm">Các ngày đã lưu</p>
              </div>
            </CardContent>
          </Link>
        </Card>

        <Card className="hover:border-primary/50 transition-colors">
          <Link href="/profile/notes" className="block">
            <CardContent className="flex items-center gap-4 pt-6">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-yellow-500/10">
                <StickyNote className="h-6 w-6 text-yellow-500" />
              </div>
              <div>
                <p className="font-medium">Ghi chú</p>
                <p className="text-muted-foreground text-sm">Ghi chú cá nhân</p>
              </div>
            </CardContent>
          </Link>
        </Card>

        <Card className="hover:border-primary/50 transition-colors">
          <Link href="/profile/countdowns" className="block">
            <CardContent className="flex items-center gap-4 pt-6">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-500/10">
                <Timer className="h-6 w-6 text-blue-500" />
              </div>
              <div>
                <p className="font-medium">Đếm ngược</p>
                <p className="text-muted-foreground text-sm">Sự kiện sắp tới</p>
              </div>
            </CardContent>
          </Link>
        </Card>

        <Card className="hover:border-primary/50 transition-colors">
          <Link href="/profile/reminders" className="block">
            <CardContent className="flex items-center gap-4 pt-6">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-orange-500/10">
                <Bell className="h-6 w-6 text-orange-500" />
              </div>
              <div>
                <p className="font-medium">Nhắc nhở</p>
                <p className="text-muted-foreground text-sm">Ngày kỷ niệm & sự kiện</p>
              </div>
            </CardContent>
          </Link>
        </Card>

        <Card className="hover:border-primary/50 transition-colors">
          <Link href="/" className="block">
            <CardContent className="flex items-center gap-4 pt-6">
              <div className="bg-primary/10 flex h-12 w-12 items-center justify-center rounded-lg">
                <Calendar className="text-primary h-6 w-6" />
              </div>
              <div>
                <p className="font-medium">Lịch vạn niên</p>
                <p className="text-muted-foreground text-sm">Xem lịch hôm nay</p>
              </div>
            </CardContent>
          </Link>
        </Card>

        {hasAdminAccess() && (
          <Card className="hover:border-primary/50 transition-colors">
            <Link href="/admin" className="block">
              <CardContent className="flex items-center gap-4 pt-6">
                <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-orange-500/10">
                  <Shield className="h-6 w-6 text-orange-500" />
                </div>
                <div>
                  <p className="font-medium">Quản trị</p>
                  <p className="text-muted-foreground text-sm">Vào trang admin</p>
                </div>
              </CardContent>
            </Link>
          </Card>
        )}
      </div>

      {/* Streak & Achievements */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">🔥 Chuỗi Hoạt Động</CardTitle>
          <CardDescription>Theo dõi chuỗi truy cập và thành tựu của bạn</CardDescription>
        </CardHeader>
        <CardContent>
          <StreakWidget />
        </CardContent>
      </Card>

      {/* Edit Profile */}
      <Card>
        <CardHeader>
          <CardTitle>Thông tin cá nhân</CardTitle>
          <CardDescription>Cập nhật thông tin cá nhân của bạn</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="firstName">Họ</Label>
              <Input
                id="firstName"
                value={profileForm.first_name}
                onChange={(e) => setProfileForm((f) => ({ ...f, first_name: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="lastName">Tên</Label>
              <Input
                id="lastName"
                value={profileForm.last_name}
                onChange={(e) => setProfileForm((f) => ({ ...f, last_name: e.target.value }))}
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" value={profileForm.email} disabled />
            <p className="text-muted-foreground text-xs">Liên hệ quản trị viên để thay đổi email</p>
          </div>
          <div className="flex justify-end">
            <Button onClick={handleSaveProfile} disabled={isUpdatingProfile}>
              <Save className="mr-2 h-4 w-4" />
              {isUpdatingProfile ? "Đang lưu..." : "Lưu thay đổi"}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Change Password */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Lock className="h-5 w-5" />
            Đổi mật khẩu
          </CardTitle>
          <CardDescription>Cập nhật mật khẩu để bảo mật tài khoản</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="currentPassword">Mật khẩu hiện tại</Label>
            <div className="relative">
              <Input
                id="currentPassword"
                type={showCurrentPassword ? "text" : "password"}
                value={passwordForm.current_password}
                onChange={(e) =>
                  setPasswordForm((f) => ({
                    ...f,
                    current_password: e.target.value,
                  }))
                }
              />
              <Button
                variant="ghost"
                size="icon"
                type="button"
                className="absolute top-0 right-0 h-full px-3"
                onClick={() => setShowCurrentPassword(!showCurrentPassword)}
              >
                {showCurrentPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </Button>
            </div>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="newPassword">Mật khẩu mới</Label>
              <div className="relative">
                <Input
                  id="newPassword"
                  type={showNewPassword ? "text" : "password"}
                  value={passwordForm.new_password}
                  onChange={(e) =>
                    setPasswordForm((f) => ({
                      ...f,
                      new_password: e.target.value,
                    }))
                  }
                />
                <Button
                  variant="ghost"
                  size="icon"
                  type="button"
                  className="absolute top-0 right-0 h-full px-3"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                >
                  {showNewPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Xác nhận mật khẩu mới</Label>
              <div className="relative">
                <Input
                  id="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  value={passwordForm.confirm_password}
                  onChange={(e) =>
                    setPasswordForm((f) => ({
                      ...f,
                      confirm_password: e.target.value,
                    }))
                  }
                />
                <Button
                  variant="ghost"
                  size="icon"
                  type="button"
                  className="absolute top-0 right-0 h-full px-3"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                >
                  {showConfirmPassword ? (
                    <EyeOff className="h-4 w-4" />
                  ) : (
                    <Eye className="h-4 w-4" />
                  )}
                </Button>
              </div>
            </div>
          </div>
          <p className="text-muted-foreground text-xs">Mật khẩu phải có ít nhất 8 ký tự</p>
          <div className="flex justify-end">
            <Button
              onClick={handleChangePassword}
              disabled={
                isChangingPassword ||
                !passwordForm.current_password ||
                !passwordForm.new_password ||
                !passwordForm.confirm_password
              }
            >
              <Lock className="mr-2 h-4 w-4" />
              {isChangingPassword ? "Đang đổi..." : "Đổi mật khẩu"}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Account Info */}
      <Card>
        <CardHeader>
          <CardTitle>Thông tin tài khoản</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3 text-sm">
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground flex items-center gap-2">
                <Mail className="h-4 w-4" />
                Email
              </span>
              <span>{user?.email}</span>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground flex items-center gap-2">
                <Shield className="h-4 w-4" />
                Vai trò
              </span>
              <div className="flex gap-1.5">
                {user?.roles?.map((role) => (
                  <Badge key={role} variant={ROLE_COLORS[role] || "outline"}>
                    {ROLE_LABELS[role] || role}
                  </Badge>
                ))}
              </div>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">ID tài khoản</span>
              <span className="font-mono text-xs">{user?.id}</span>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Quyền hạn</span>
              <span className="text-xs">{user?.permissions?.length ?? 0} quyền</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
