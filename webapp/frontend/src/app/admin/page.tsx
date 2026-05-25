"use client";

import {
  Users,
  UserCheck,
  UserPlus,
  Shield,
  Activity,
  TrendingUp,
  Clock,
  Newspaper,
  MessageSquareQuote,
  Landmark,
  Crown,
  Layers3,
  Hash,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useDashboardStats } from "@/hooks/useDashboard";
import { useContentStats } from "@/hooks/useContentStats";
import { useAuth } from "@/hooks/useAuth";
import { formatDate } from "@/lib/utils";

function StatCard({
  title,
  value,
  icon: Icon,
  description,
  trend,
}: {
  title: string;
  value: number | string;
  icon: typeof Users;
  description?: string;
  trend?: string;
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-muted-foreground text-sm font-medium">{title}</CardTitle>
        <Icon className="text-muted-foreground h-4 w-4" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {description && <p className="text-muted-foreground mt-1 text-xs">{description}</p>}
        {trend && (
          <div className="mt-1 flex items-center gap-1">
            <TrendingUp className="h-3 w-3 text-green-500" />
            <span className="text-xs text-green-500">{trend}</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function StatsCardSkeleton() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-4 w-4" />
      </CardHeader>
      <CardContent>
        <Skeleton className="mb-1 h-8 w-16" />
        <Skeleton className="h-3 w-32" />
      </CardContent>
    </Card>
  );
}

function getActionBadgeVariant(
  action: string
): "default" | "secondary" | "destructive" | "outline" {
  if (action.includes("delete") || action.includes("failed")) return "destructive";
  if (action.includes("create") || action.includes("login")) return "default";
  if (action.includes("update") || action.includes("assign")) return "secondary";
  return "outline";
}

function getStatusColor(status: string) {
  switch (status) {
    case "success":
      return "text-green-500";
    case "failure":
      return "text-red-500";
    default:
      return "text-muted-foreground";
  }
}

export default function AdminDashboard() {
  const { user } = useAuth();
  const { data: stats, isLoading, error } = useDashboardStats();
  const { data: contentStats, isLoading: isContentLoading } = useContentStats();

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground mt-1">
          Welcome back, {user?.full_name || user?.first_name || "User"}!
        </p>
      </div>

      {/* Stats Cards */}
      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <StatsCardSkeleton key={i} />
          ))}
        </div>
      ) : error ? (
        <Card>
          <CardContent className="text-muted-foreground py-8 text-center">
            Failed to load dashboard statistics. Please try again later.
          </CardContent>
        </Card>
      ) : stats ? (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Total Users"
              value={stats.total_users}
              icon={Users}
              description={`${stats.active_users} active, ${stats.inactive_users} inactive`}
            />
            <StatCard
              title="Active Users"
              value={stats.active_users}
              icon={UserCheck}
              description={`${Math.round((stats.active_users / Math.max(stats.total_users, 1)) * 100)}% of total`}
            />
            <StatCard
              title="New Users Today"
              value={stats.new_users_today}
              icon={UserPlus}
              description={`${stats.new_users_week} this week, ${stats.new_users_month} this month`}
              trend={stats.new_users_today > 0 ? `+${stats.new_users_today} today` : undefined}
            />
            <StatCard
              title="Total Roles"
              value={stats.total_roles}
              icon={Shield}
              description="System roles configured"
            />
          </div>

          {/* Content Stats */}
          <div>
            <h2 className="mb-3 text-lg font-semibold">Quản lý nội dung</h2>
            {isContentLoading ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                {Array.from({ length: 4 }).map((_, i) => (
                  <StatsCardSkeleton key={`content-${i}`} />
                ))}
              </div>
            ) : contentStats ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <StatCard
                  title="Bài viết"
                  value={contentStats.totalArticles}
                  icon={Newspaper}
                  description={`${contentStats.totalCategories} danh mục, ${contentStats.totalTags} tags`}
                />
                <StatCard
                  title="Danh ngôn"
                  value={contentStats.totalQuotes}
                  icon={MessageSquareQuote}
                  description="Câu nói nổi tiếng"
                />
                <StatCard
                  title="Danh nhân"
                  value={contentStats.totalFamousPeople}
                  icon={Crown}
                  description="Nhân vật lịch sử"
                />
                <StatCard
                  title="Sự kiện & Lễ hội"
                  value={contentStats.totalEvents + contentStats.totalFestivals}
                  icon={Landmark}
                  description={`${contentStats.totalEvents} sự kiện, ${contentStats.totalFestivals} lễ hội`}
                />
              </div>
            ) : null}
          </div>

          {/* Recent Activity & Quick Info */}
          <div className="grid gap-4 lg:grid-cols-7">
            {/* Recent Activity */}
            <Card className="lg:col-span-4">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Activity className="h-5 w-5" />
                  Recent Activity
                </CardTitle>
              </CardHeader>
              <CardContent>
                {stats.recent_activity.length === 0 ? (
                  <div className="text-muted-foreground py-8 text-center">No recent activity</div>
                ) : (
                  <div className="space-y-4">
                    {stats.recent_activity.map((entry) => (
                      <div
                        key={entry.id}
                        className="flex items-start gap-3 border-b pb-3 last:border-0 last:pb-0"
                      >
                        <div className="mt-0.5">
                          <div
                            className={`h-2 w-2 rounded-full ${entry.status === "success" ? "bg-green-500" : "bg-red-500"}`}
                          />
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <Badge
                              variant={getActionBadgeVariant(entry.action)}
                              className="text-xs"
                            >
                              {entry.action}
                            </Badge>
                            <span className="text-muted-foreground text-xs">{entry.module}</span>
                          </div>
                          <p className="mt-1 truncate text-sm">{entry.description}</p>
                          <div className="mt-1 flex items-center gap-2">
                            <span className="text-muted-foreground text-xs">
                              {entry.user_email}
                            </span>
                            <span className="text-muted-foreground flex items-center gap-1 text-xs">
                              <Clock className="h-3 w-3" />
                              {formatDate(entry.created_at)}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Module Activity Breakdown */}
            <Card className="lg:col-span-3">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5" />
                  Activity by Module
                </CardTitle>
              </CardHeader>
              <CardContent>
                {Object.keys(stats.module_counts).length === 0 ? (
                  <div className="text-muted-foreground py-8 text-center">No activity data</div>
                ) : (
                  <div className="space-y-3">
                    {Object.entries(stats.module_counts)
                      .sort(([, a], [, b]) => b - a)
                      .map(([module, count]) => {
                        const total = Object.values(stats.module_counts).reduce(
                          (sum, c) => sum + c,
                          0
                        );
                        const percentage = Math.round((count / total) * 100);
                        return (
                          <div key={module} className="space-y-1">
                            <div className="flex items-center justify-between text-sm">
                              <span className="font-medium capitalize">{module}</span>
                              <span className="text-muted-foreground">
                                {count} ({percentage}%)
                              </span>
                            </div>
                            <div className="bg-muted h-2 w-full rounded-full">
                              <div
                                className="bg-primary h-full rounded-full transition-all"
                                style={{ width: `${percentage}%` }}
                              />
                            </div>
                          </div>
                        );
                      })}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* User Info Card */}
          <Card>
            <CardHeader>
              <CardTitle>Your Profile</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 text-sm">
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground w-24 font-medium">Email:</span>
                  <span>{user?.email}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground w-24 font-medium">Name:</span>
                  <span>{user?.full_name}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground w-24 font-medium">Roles:</span>
                  <div className="flex flex-wrap gap-1.5">
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
        </>
      ) : null}
    </div>
  );
}
