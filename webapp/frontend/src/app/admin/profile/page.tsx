"use client";

import { useState } from "react";
import { User, Lock, Camera, Save, Eye, EyeOff } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Separator } from "@/components/ui/separator";
import { useAuth } from "@/hooks/useAuth";
import { getInitials } from "@/lib/utils";
import { toast } from "sonner";

export default function ProfilePage() {
  const { user, changePassword, isChangingPassword } = useAuth();
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

  const handleSaveProfile = () => {
    toast.success("Profile updated successfully");
  };

  const handleChangePassword = async () => {
    if (passwordForm.new_password !== passwordForm.confirm_password) {
      toast.error("New passwords do not match");
      return;
    }
    if (passwordForm.new_password.length < 8) {
      toast.error("Password must be at least 8 characters");
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
      toast.success("Password changed successfully");
    } catch {
      toast.error("Failed to change password. Check your current password.");
    }
  };

  return (
    <div className="max-w-3xl space-y-6">
      {/* Header */}
      <div>
        <h1 className="flex items-center gap-2 text-3xl font-bold tracking-tight">
          <User className="h-8 w-8" />
          Profile
        </h1>
        <p className="text-muted-foreground mt-1">Manage your account settings and preferences</p>
      </div>

      {/* Avatar & Basic Info */}
      <Card>
        <CardHeader>
          <CardTitle>Profile Picture</CardTitle>
          <CardDescription>Your avatar is shown across the platform</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-6">
            <div className="relative">
              <Avatar size="lg" className="h-20 w-20 text-lg">
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
                  <Badge key={role} variant="secondary">
                    {role}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Edit Profile */}
      <Card>
        <CardHeader>
          <CardTitle>Personal Information</CardTitle>
          <CardDescription>Update your personal details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="firstName">First Name</Label>
              <Input
                id="firstName"
                value={profileForm.first_name}
                onChange={(e) => setProfileForm((f) => ({ ...f, first_name: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="lastName">Last Name</Label>
              <Input
                id="lastName"
                value={profileForm.last_name}
                onChange={(e) => setProfileForm((f) => ({ ...f, last_name: e.target.value }))}
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              value={profileForm.email}
              onChange={(e) => setProfileForm((f) => ({ ...f, email: e.target.value }))}
              disabled
            />
            <p className="text-muted-foreground text-xs">
              Contact an administrator to change your email
            </p>
          </div>
          <div className="flex justify-end">
            <Button onClick={handleSaveProfile}>
              <Save className="mr-2 h-4 w-4" />
              Save Changes
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Change Password */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Lock className="h-5 w-5" />
            Change Password
          </CardTitle>
          <CardDescription>Update your password to keep your account secure</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="currentPassword">Current Password</Label>
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
              <Label htmlFor="newPassword">New Password</Label>
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
              <Label htmlFor="confirmPassword">Confirm New Password</Label>
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
          <p className="text-muted-foreground text-xs">
            Password must be at least 8 characters long
          </p>
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
              {isChangingPassword ? "Changing..." : "Change Password"}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Account Info */}
      <Card>
        <CardHeader>
          <CardTitle>Account Information</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3 text-sm">
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">User ID</span>
              <span className="font-mono text-xs">{user?.id}</span>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Roles</span>
              <div className="flex gap-1.5">
                {user?.roles?.map((role) => (
                  <Badge key={role} variant="outline">
                    {role}
                  </Badge>
                ))}
              </div>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Permissions</span>
              <span className="text-xs">{user?.permissions?.length ?? 0} permissions</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
