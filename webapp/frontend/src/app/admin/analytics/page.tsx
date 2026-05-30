"use client";

import { useUserAnalytics } from "@/hooks/useAnalytics";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  Users,
  Smartphone,
  Award,
  BookOpen,
  TrendingUp,
  Activity,
  Chrome,
  AlertTriangle
} from "lucide-react";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell
} from "recharts";

const COLORS = ["#3b82f6", "#10b981", "#6366f1", "#f59e0b", "#ec4899", "#8b5cf6", "#14b8a6"];

export default function AnalyticsPage() {
  const { data, isLoading, error } = useUserAnalytics();

  if (isLoading) {
    return (
      <div className="flex min-h-[400px] flex-col items-center justify-center gap-3">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
        <p className="text-muted-foreground text-sm">Đang tải dữ liệu phân tích...</p>
      </div>
    );
  }

  const analytics = data?.data;

  if (error || !data || !data.success || !analytics) {
    return (
      <div className="flex min-h-[400px] flex-col items-center justify-center gap-3 text-center">
        <AlertTriangle className="h-12 w-12 text-destructive" />
        <h3 className="text-lg font-bold">Không thể tải dữ liệu phân tích</h3>
        <p className="text-muted-foreground text-sm max-w-md">
          Có lỗi xảy ra khi kết nối tới máy chủ. Vui lòng tải lại trang hoặc kiểm tra quyền truy cập.
        </p>
      </div>
    );
  }

  // Formatting active users data for Recharts
  const activeData = analytics.active_users_30d.map((d) => ({
    date: d.date,
    "Đã đăng nhập": d.users,
    "Khách (Guest)": d.guests,
    "Tổng hoạt động": d.users + d.guests
  }));

  // Formatting user growth data
  const growthData = analytics.growth_30d.map((d) => ({
    date: d.date,
    "Lượt đăng ký mới": d.count
  }));

  // Formatting providers for Pie chart
  const providerData = analytics.providers.map((p) => ({
    name: p.provider === "google" ? "Google Account" : "Email & Mật khẩu",
    value: p.count
  }));

  // Formatting platforms for Pie chart
  const platformData = analytics.platforms.map((p) => ({
    name: p.platform,
    value: p.count
  }));

  // Formatting streaks data
  const streakData = [
    { name: "0 ngày", "Số user": analytics.streaks.range_0 },
    { name: "1-3 ngày", "Số user": analytics.streaks.range_1_3 },
    { name: "4-7 ngày", "Số user": analytics.streaks.range_4_7 },
    { name: "8-14 ngày", "Số user": analytics.streaks.range_8_14 },
    { name: "15+ ngày", "Số user": analytics.streaks.range_15_plus }
  ];

  // App version distribution data
  const versionData = analytics.app_versions.map((v) => ({
    version: v.app_version,
    "Lượt cài": v.count
  }));

  // Get initials for Avatar
  const getInitials = (firstName: string, lastName: string) => {
    const f = firstName ? firstName.charAt(0) : "";
    const l = lastName ? lastName.charAt(0) : "";
    return (f + l).toUpperCase() || "U";
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Thống kê & Phân tích người dùng</h1>
        <p className="text-muted-foreground">
          Báo cáo chi tiết về đăng ký mới, mức độ hoạt động và phân phối thiết bị.
        </p>
      </div>

      {/* Summary Row */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Total Users */}
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Tổng ví điểm</CardTitle>
            <Users className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{analytics.points.total_wallets}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Người dùng đã tạo ví trên hệ thống
            </p>
          </CardContent>
        </Card>

        {/* Total Points */}
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Tổng điểm tích lũy</CardTitle>
            <Award className="h-4 w-4 text-emerald-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {analytics.points.total_points.toLocaleString()} pts
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Trung bình: {Math.round(analytics.points.average_points)} pts/ví
            </p>
          </CardContent>
        </Card>

        {/* Active Devices */}
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Thiết bị hoạt động</CardTitle>
            <Smartphone className="h-4 w-4 text-indigo-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {analytics.platforms.reduce((acc, curr) => acc + curr.count, 0)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Thiết bị Android & iOS đã liên kết
            </p>
          </CardContent>
        </Card>

        {/* User Engagement */}
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Lượt tương tác</CardTitle>
            <BookOpen className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {analytics.engagement.total_notes +
                analytics.engagement.total_bookmarks +
                analytics.engagement.total_reminders}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {analytics.engagement.total_bookmarks} Bookmarks • {analytics.engagement.total_notes} Ghi chú
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Main Charts Row */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* User Growth */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-primary" />
              Lượt đăng ký mới (30 ngày gần đây)
            </CardTitle>
            <CardDescription>Biểu đồ thể hiện sự phát triển số lượng thành viên mới</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={growthData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="growthGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(128,128,128,0.1)" />
                  <XAxis dataKey="date" stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                  <YAxis stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "rgba(0, 0, 0, 0.8)",
                      border: "none",
                      borderRadius: "6px",
                      color: "#fff"
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="Lượt đăng ký mới"
                    stroke="#3b82f6"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#growthGrad)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Daily Active Users breakdown */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5 text-indigo-500" />
              Hoạt động hàng ngày (DAU Breakdown)
            </CardTitle>
            <CardDescription>Sự phân biệt giữa thành viên đăng nhập và khách truy cập</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={activeData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(128,128,128,0.1)" />
                  <XAxis dataKey="date" stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                  <YAxis stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "rgba(0, 0, 0, 0.8)",
                      border: "none",
                      borderRadius: "6px",
                      color: "#fff"
                    }}
                  />
                  <Legend iconType="circle" wrapperStyle={{ fontSize: 12 }} />
                  <Bar dataKey="Đã đăng nhập" stackId="a" fill="#10b981" />
                  <Bar dataKey="Khách (Guest)" stackId="a" fill="#6366f1" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Share / Donut Row */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {/* Auth Provider Share */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Phương thức đăng nhập</CardTitle>
            <CardDescription>Tỉ lệ phương thức xác thực tài khoản</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center pb-6">
            {providerData.length === 0 ? (
              <div className="py-12 text-sm text-muted-foreground">Không có dữ liệu</div>
            ) : (
              <>
                <div className="h-[200px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={providerData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        {providerData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="flex flex-wrap justify-center gap-x-4 gap-y-2 mt-4">
                  {providerData.map((d, i) => (
                    <div key={d.name} className="flex items-center gap-2 text-xs">
                      <div className="h-3 w-3 rounded-full" style={{ backgroundColor: COLORS[i % COLORS.length] }} />
                      <span className="text-muted-foreground">
                        {d.name}: <strong className="text-foreground">{d.value}</strong>
                      </span>
                    </div>
                  ))}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Platforms Donut */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Nền tảng thiết bị</CardTitle>
            <CardDescription>Tỉ lệ thiết bị cài đặt ứng dụng</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center pb-6">
            {platformData.length === 0 ? (
              <div className="py-12 text-sm text-muted-foreground">Chưa có thiết bị đăng ký</div>
            ) : (
              <>
                <div className="h-[200px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={platformData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        {platformData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[(index + 2) % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="flex flex-wrap justify-center gap-x-4 gap-y-2 mt-4">
                  {platformData.map((d, i) => (
                    <div key={d.name} className="flex items-center gap-2 text-xs">
                      <div
                        className="h-3 w-3 rounded-full"
                        style={{ backgroundColor: COLORS[(i + 2) % COLORS.length] }}
                      />
                      <span className="text-muted-foreground">
                        {d.name}: <strong className="text-foreground">{d.value}</strong>
                      </span>
                    </div>
                  ))}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Streaks Ranges Bar */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Chuỗi truy cập liên tục</CardTitle>
            <CardDescription>Phân bố thành viên theo ngày điểm danh (Streaks)</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[220px] w-full mt-2">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={streakData} layout="vertical" margin={{ top: 0, right: 10, left: -10, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="rgba(128,128,128,0.1)" />
                  <XAxis type="number" stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                  <YAxis dataKey="name" type="category" stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                  <Tooltip />
                  <Bar dataKey="Số user" fill="#ec4899" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Engagement Dist and App Versions */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* App Version distribution */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Phiên bản ứng dụng</CardTitle>
            <CardDescription>Thống kê lượt cài đặt theo từng phiên bản app di động</CardDescription>
          </CardHeader>
          <CardContent>
            {versionData.length === 0 ? (
              <div className="py-24 text-center text-sm text-muted-foreground">Chưa có thiết bị đăng ký</div>
            ) : (
              <div className="h-[250px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={versionData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(128,128,128,0.1)" />
                    <XAxis dataKey="version" stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                    <YAxis stroke="#888888" fontSize={11} tickLine={false} axisLine={false} />
                    <Tooltip />
                    <Bar dataKey="Lượt cài" fill="#14b8a6" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Top 10 device names */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Thiết bị phổ biến</CardTitle>
            <CardDescription>Các dòng máy người dùng sử dụng để truy cập app</CardDescription>
          </CardHeader>
          <CardContent className="h-[250px] overflow-auto">
            {analytics.top_devices.length === 0 ? (
              <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
                Chưa có dữ liệu thiết bị
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tên thiết bị</TableHead>
                    <TableHead className="text-right">Số lượt truy cập</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {analytics.top_devices.map((device, i) => (
                    <TableRow key={device.device_name + i}>
                      <TableCell className="font-medium">{device.device_name}</TableCell>
                      <TableCell className="text-right">
                        <Badge variant="secondary">{device.count}</Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Leaderboard Table (Top Point Earners) */}
      <Card className="glass-card">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Award className="h-5 w-5 text-emerald-500" />
            Top người dùng tích cực (Điểm số cao nhất)
          </CardTitle>
          <CardDescription>Danh sách top 10 thành viên sở hữu số điểm ví cao nhất</CardDescription>
        </CardHeader>
        <CardContent>
          {analytics.points.top_earners.length === 0 ? (
            <div className="py-12 text-center text-sm text-muted-foreground">
              Chưa có người dùng tích lũy điểm
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[80px]">Thứ hạng</TableHead>
                  <TableHead>Thành viên</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead className="text-right">Điểm ví</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {analytics.points.top_earners.map((earner, index) => (
                  <TableRow key={earner.user_id}>
                    <TableCell className="font-bold">
                      {index + 1 === 1 && <span className="text-amber-500">🥇 1</span>}
                      {index + 1 === 2 && <span className="text-slate-400">🥈 2</span>}
                      {index + 1 === 3 && <span className="text-amber-700">🥉 3</span>}
                      {index + 1 > 3 && index + 1}
                    </TableCell>
                    <TableCell className="flex items-center gap-3">
                      <Avatar className="h-8 w-8">
                        <AvatarFallback>
                          {getInitials(earner.first_name, earner.last_name || "")}
                        </AvatarFallback>
                      </Avatar>
                      <span className="font-medium">
                        {[earner.first_name, earner.last_name].filter(Boolean).join(" ") || "Người dùng"}
                      </span>
                    </TableCell>
                    <TableCell className="text-muted-foreground text-sm">{earner.email}</TableCell>
                    <TableCell className="text-right font-bold text-emerald-500">
                      {earner.points.toLocaleString()} pts
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
