"use client";

import { useState, useEffect } from "react";
import { Settings, Globe, Shield, Mail, Save, Loader2, Bot, Eye, EyeOff, Wifi, WifiOff, CheckCircle2, XCircle } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { useGroupedSettings, useUpdateSettingsGroup } from "@/hooks/useDashboard";
import { testAIConnection } from "@/services/aiService";
import type { SettingResponse } from "@/types/settings";

// Helper to get a setting value from an array of settings
function getSettingValue<T>(
  settings: SettingResponse[] | undefined,
  key: string,
  defaultValue: T
): T {
  if (!settings) return defaultValue;
  const setting = settings.find((s) => s.key === key);
  if (setting === undefined || setting.value === undefined || setting.value === null)
    return defaultValue;
  return setting.value as T;
}

export default function SettingsPage() {
  const { data: grouped, isLoading } = useGroupedSettings();
  const updateGroup = useUpdateSettingsGroup();

  const [generalSettings, setGeneralSettings] = useState({
    siteName: "Zplus Base",
    siteDescription: "Modern admin platform built with Next.js and Go",
    siteUrl: "http://localhost:3000",
    maintenanceMode: false,
  });

  const [emailSettings, setEmailSettings] = useState({
    smtpHost: "",
    smtpPort: "587",
    smtpUser: "",
    smtpPassword: "",
    fromEmail: "noreply@example.com",
    fromName: "Zplus Base",
  });

  const [securitySettings, setSecuritySettings] = useState({
    enableRegistration: true,
    requireEmailVerification: false,
    maxLoginAttempts: 5,
    lockoutDuration: 15,
    sessionTimeout: 60,
    enableTwoFactor: false,
  });

  const [aiSettings, setAISettings] = useState({
    aiEnabled: true,
    openrouterApiKey: "",
    openrouterBaseUrl: "https://openrouter.ai/api/v1",
    aiArticleModel: "deepseek/deepseek-chat",
    aiHoroscopeModel: "anthropic/claude-sonnet-4",
    aiChatModel: "openai/gpt-4o-mini",
    aiMaxTokensArticle: 4096,
    aiMaxTokensHoroscope: 2048,
    aiMaxTokensChat: 1024,
    aiRateHoroscopeGuest: 2,
    aiRateHoroscopeFree: 5,
    aiRateHoroscopePremium: 30,
    aiMonthlyBudgetCap: 50,
  });

  const [showApiKey, setShowApiKey] = useState(false);
  const [testingConnection, setTestingConnection] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<"idle" | "success" | "error">("idle");
  const [connectionResult, setConnectionResult] = useState<{ latency_ms?: number; model?: string } | null>(null);

  // Populate from backend data
  useEffect(() => {
    if (grouped) {
      const general = grouped.general;
      const email = grouped.email;
      const security = grouped.security;

      if (general) {
        setGeneralSettings({
          siteName: getSettingValue(general, "site_name", "Zplus Base"),
          siteDescription: getSettingValue(general, "site_description", ""),
          siteUrl: getSettingValue(general, "site_url", "http://localhost:3000"),
          maintenanceMode: getSettingValue(general, "maintenance_mode", false),
        });
      }

      if (email) {
        setEmailSettings({
          smtpHost: getSettingValue(email, "smtp_host", ""),
          smtpPort: getSettingValue(email, "smtp_port", "587"),
          smtpUser: getSettingValue(email, "smtp_user", ""),
          smtpPassword: getSettingValue(email, "smtp_password", ""),
          fromEmail: getSettingValue(email, "from_email", "noreply@example.com"),
          fromName: getSettingValue(email, "from_name", "Zplus Base"),
        });
      }

      if (security) {
        setSecuritySettings({
          enableRegistration: getSettingValue(security, "enable_registration", true),
          requireEmailVerification: getSettingValue(security, "require_email_verification", false),
          maxLoginAttempts: getSettingValue(security, "max_login_attempts", 5),
          lockoutDuration: getSettingValue(security, "lockout_duration", 15),
          sessionTimeout: getSettingValue(security, "session_timeout", 60),
          enableTwoFactor: getSettingValue(security, "enable_two_factor", false),
        });
      }

      const ai = grouped.ai;
      if (ai) {
        setAISettings({
          aiEnabled: getSettingValue(ai, "ai_enabled", true),
          openrouterApiKey: getSettingValue(ai, "openrouter_api_key", ""),
          openrouterBaseUrl: getSettingValue(ai, "openrouter_base_url", "https://openrouter.ai/api/v1"),
          aiArticleModel: getSettingValue(ai, "ai_article_model", "deepseek/deepseek-chat"),
          aiHoroscopeModel: getSettingValue(ai, "ai_horoscope_model", "anthropic/claude-sonnet-4"),
          aiChatModel: getSettingValue(ai, "ai_chat_model", "openai/gpt-4o-mini"),
          aiMaxTokensArticle: getSettingValue(ai, "ai_max_tokens_article", 4096),
          aiMaxTokensHoroscope: getSettingValue(ai, "ai_max_tokens_horoscope", 2048),
          aiMaxTokensChat: getSettingValue(ai, "ai_max_tokens_chat", 1024),
          aiRateHoroscopeGuest: getSettingValue(ai, "ai_rate_horoscope_guest", 2),
          aiRateHoroscopeFree: getSettingValue(ai, "ai_rate_horoscope_free", 5),
          aiRateHoroscopePremium: getSettingValue(ai, "ai_rate_horoscope_premium", 30),
          aiMonthlyBudgetCap: getSettingValue(ai, "ai_monthly_budget_cap", 50),
        });
      }
    }
  }, [grouped]);

  const handleSaveGeneral = () => {
    updateGroup.mutate(
      {
        group: "general",
        data: {
          group: "general",
          settings: [
            { key: "site_name", value: generalSettings.siteName },
            { key: "site_description", value: generalSettings.siteDescription },
            { key: "site_url", value: generalSettings.siteUrl },
            { key: "maintenance_mode", value: generalSettings.maintenanceMode },
          ],
        },
      },
      {
        onSuccess: () => toast.success("General settings saved successfully"),
        onError: () => toast.error("Failed to save general settings"),
      }
    );
  };

  const handleSaveEmail = () => {
    updateGroup.mutate(
      {
        group: "email",
        data: {
          group: "email",
          settings: [
            { key: "smtp_host", value: emailSettings.smtpHost },
            { key: "smtp_port", value: emailSettings.smtpPort },
            { key: "smtp_user", value: emailSettings.smtpUser },
            { key: "smtp_password", value: emailSettings.smtpPassword },
            { key: "from_email", value: emailSettings.fromEmail },
            { key: "from_name", value: emailSettings.fromName },
          ],
        },
      },
      {
        onSuccess: () => toast.success("Email settings saved successfully"),
        onError: () => toast.error("Failed to save email settings"),
      }
    );
  };

  const handleSaveSecurity = () => {
    updateGroup.mutate(
      {
        group: "security",
        data: {
          group: "security",
          settings: [
            { key: "enable_registration", value: securitySettings.enableRegistration },
            { key: "require_email_verification", value: securitySettings.requireEmailVerification },
            { key: "max_login_attempts", value: securitySettings.maxLoginAttempts },
            { key: "lockout_duration", value: securitySettings.lockoutDuration },
            { key: "session_timeout", value: securitySettings.sessionTimeout },
            { key: "enable_two_factor", value: securitySettings.enableTwoFactor },
          ],
        },
      },
      {
        onSuccess: () => toast.success("Security settings saved successfully"),
        onError: () => toast.error("Failed to save security settings"),
      }
    );
  };

  const handleSaveAI = () => {
    updateGroup.mutate(
      {
        group: "ai",
        data: {
          group: "ai",
          settings: [
            { key: "ai_enabled", value: aiSettings.aiEnabled },
            { key: "openrouter_api_key", value: aiSettings.openrouterApiKey },
            { key: "openrouter_base_url", value: aiSettings.openrouterBaseUrl },
            { key: "ai_article_model", value: aiSettings.aiArticleModel },
            { key: "ai_horoscope_model", value: aiSettings.aiHoroscopeModel },
            { key: "ai_chat_model", value: aiSettings.aiChatModel },
            { key: "ai_max_tokens_article", value: aiSettings.aiMaxTokensArticle },
            { key: "ai_max_tokens_horoscope", value: aiSettings.aiMaxTokensHoroscope },
            { key: "ai_max_tokens_chat", value: aiSettings.aiMaxTokensChat },
            { key: "ai_rate_horoscope_guest", value: aiSettings.aiRateHoroscopeGuest },
            { key: "ai_rate_horoscope_free", value: aiSettings.aiRateHoroscopeFree },
            { key: "ai_rate_horoscope_premium", value: aiSettings.aiRateHoroscopePremium },
            { key: "ai_monthly_budget_cap", value: aiSettings.aiMonthlyBudgetCap },
          ],
        },
      },
      {
        onSuccess: () => toast.success("AI settings saved successfully"),
        onError: () => toast.error("Failed to save AI settings"),
      }
    );
  };

  const handleTestConnection = async () => {
    if (!aiSettings.openrouterApiKey.trim()) {
      toast.error("Vui lòng nhập API key trước khi test");
      return;
    }
    setTestingConnection(true);
    setConnectionStatus("idle");
    setConnectionResult(null);
    try {
      const res = await testAIConnection({
        api_key: aiSettings.openrouterApiKey,
        base_url: aiSettings.openrouterBaseUrl || undefined,
        model: aiSettings.aiArticleModel || undefined,
      });
      if (res.success) {
        setConnectionStatus("success");
        setConnectionResult(res.data ?? null);
        toast.success(`Kết nối thành công! Latency: ${res.data?.latency_ms}ms`);
      } else {
        setConnectionStatus("error");
        toast.error("Kết nối thất bại: " + res.message);
      }
    } catch (err: unknown) {
      setConnectionStatus("error");
      const msg = err instanceof Error ? err.message : "Kết nối thất bại";
      toast.error(msg);
    } finally {
      setTestingConnection(false);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="flex items-center gap-2 text-3xl font-bold tracking-tight">
            <Settings className="h-8 w-8" />
            Settings
          </h1>
          <p className="text-muted-foreground mt-1">Manage your application settings</p>
        </div>
        {[1, 2, 3].map((i) => (
          <Card key={i}>
            <CardHeader>
              <Skeleton className="h-6 w-48" />
              <Skeleton className="h-4 w-72" />
            </CardHeader>
            <CardContent className="space-y-4">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-1/3" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="flex items-center gap-2 text-3xl font-bold tracking-tight">
          <Settings className="h-8 w-8" />
          Settings
        </h1>
        <p className="text-muted-foreground mt-1">Manage your application settings</p>
      </div>

      {/* General Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Globe className="h-5 w-5" />
            General Settings
          </CardTitle>
          <CardDescription>Basic application settings and configuration</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="siteName">Site Name</Label>
              <Input
                id="siteName"
                value={generalSettings.siteName}
                onChange={(e) => setGeneralSettings((s) => ({ ...s, siteName: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="siteUrl">Site URL</Label>
              <Input
                id="siteUrl"
                value={generalSettings.siteUrl}
                onChange={(e) => setGeneralSettings((s) => ({ ...s, siteUrl: e.target.value }))}
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="siteDescription">Site Description</Label>
            <Textarea
              id="siteDescription"
              value={generalSettings.siteDescription}
              onChange={(e) =>
                setGeneralSettings((s) => ({
                  ...s,
                  siteDescription: e.target.value,
                }))
              }
              rows={3}
            />
          </div>
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div className="space-y-0.5">
              <Label>Maintenance Mode</Label>
              <p className="text-muted-foreground text-sm">
                Enable maintenance mode to prevent public access
              </p>
            </div>
            <Switch
              checked={generalSettings.maintenanceMode}
              onCheckedChange={(checked) =>
                setGeneralSettings((s) => ({ ...s, maintenanceMode: checked }))
              }
            />
          </div>
          <div className="flex justify-end">
            <Button onClick={handleSaveGeneral} disabled={updateGroup.isPending}>
              {updateGroup.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Save className="mr-2 h-4 w-4" />
              )}
              Save Changes
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Email Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Mail className="h-5 w-5" />
            Email Settings
          </CardTitle>
          <CardDescription>Configure SMTP settings for sending emails</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="smtpHost">SMTP Host</Label>
              <Input
                id="smtpHost"
                placeholder="smtp.gmail.com"
                value={emailSettings.smtpHost}
                onChange={(e) => setEmailSettings((s) => ({ ...s, smtpHost: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="smtpPort">SMTP Port</Label>
              <Input
                id="smtpPort"
                placeholder="587"
                value={emailSettings.smtpPort}
                onChange={(e) => setEmailSettings((s) => ({ ...s, smtpPort: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="smtpUser">SMTP Username</Label>
              <Input
                id="smtpUser"
                placeholder="user@example.com"
                value={emailSettings.smtpUser}
                onChange={(e) => setEmailSettings((s) => ({ ...s, smtpUser: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="smtpPassword">SMTP Password</Label>
              <Input
                id="smtpPassword"
                type="password"
                value={emailSettings.smtpPassword}
                onChange={(e) =>
                  setEmailSettings((s) => ({
                    ...s,
                    smtpPassword: e.target.value,
                  }))
                }
              />
            </div>
          </div>
          <Separator />
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="fromEmail">From Email</Label>
              <Input
                id="fromEmail"
                value={emailSettings.fromEmail}
                onChange={(e) => setEmailSettings((s) => ({ ...s, fromEmail: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="fromName">From Name</Label>
              <Input
                id="fromName"
                value={emailSettings.fromName}
                onChange={(e) => setEmailSettings((s) => ({ ...s, fromName: e.target.value }))}
              />
            </div>
          </div>
          <div className="flex justify-end">
            <Button onClick={handleSaveEmail} disabled={updateGroup.isPending}>
              {updateGroup.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Save className="mr-2 h-4 w-4" />
              )}
              Save Changes
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Security Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Shield className="h-5 w-5" />
            Security Settings
          </CardTitle>
          <CardDescription>Configure security and authentication settings</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div className="space-y-0.5">
              <Label>Enable Registration</Label>
              <p className="text-muted-foreground text-sm">Allow new users to register accounts</p>
            </div>
            <Switch
              checked={securitySettings.enableRegistration}
              onCheckedChange={(checked) =>
                setSecuritySettings((s) => ({
                  ...s,
                  enableRegistration: checked,
                }))
              }
            />
          </div>
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div className="space-y-0.5">
              <Label>Email Verification</Label>
              <p className="text-muted-foreground text-sm">
                Require email verification for new accounts
              </p>
            </div>
            <Switch
              checked={securitySettings.requireEmailVerification}
              onCheckedChange={(checked) =>
                setSecuritySettings((s) => ({
                  ...s,
                  requireEmailVerification: checked,
                }))
              }
            />
          </div>
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div className="space-y-0.5">
              <Label>Two-Factor Authentication</Label>
              <p className="text-muted-foreground text-sm">Enable 2FA for user accounts</p>
            </div>
            <Switch
              checked={securitySettings.enableTwoFactor}
              onCheckedChange={(checked) =>
                setSecuritySettings((s) => ({
                  ...s,
                  enableTwoFactor: checked,
                }))
              }
            />
          </div>
          <Separator />
          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="maxLoginAttempts">Max Login Attempts</Label>
              <Input
                id="maxLoginAttempts"
                type="number"
                min="1"
                max="20"
                value={securitySettings.maxLoginAttempts}
                onChange={(e) =>
                  setSecuritySettings((s) => ({
                    ...s,
                    maxLoginAttempts: parseInt(e.target.value) || 5,
                  }))
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="lockoutDuration">Lockout Duration (min)</Label>
              <Input
                id="lockoutDuration"
                type="number"
                min="1"
                max="120"
                value={securitySettings.lockoutDuration}
                onChange={(e) =>
                  setSecuritySettings((s) => ({
                    ...s,
                    lockoutDuration: parseInt(e.target.value) || 15,
                  }))
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="sessionTimeout">Session Timeout (min)</Label>
              <Input
                id="sessionTimeout"
                type="number"
                min="5"
                max="1440"
                value={securitySettings.sessionTimeout}
                onChange={(e) =>
                  setSecuritySettings((s) => ({
                    ...s,
                    sessionTimeout: parseInt(e.target.value) || 60,
                  }))
                }
              />
            </div>
          </div>
          <div className="flex justify-end">
            <Button onClick={handleSaveSecurity} disabled={updateGroup.isPending}>
              {updateGroup.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Save className="mr-2 h-4 w-4" />
              )}
              Save Changes
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* AI Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bot className="h-5 w-5" />
            AI Settings
          </CardTitle>
          <CardDescription>
            Configure OpenRouter AI API, models, rate limits and budget
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Enable/Disable AI */}
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div className="space-y-0.5">
              <Label>Enable AI Features</Label>
              <p className="text-muted-foreground text-sm">
                Enable or disable all AI-powered features (Horoscope, Chat, Article generation)
              </p>
            </div>
            <Switch
              checked={aiSettings.aiEnabled}
              onCheckedChange={(checked) =>
                setAISettings((s) => ({ ...s, aiEnabled: checked }))
              }
            />
          </div>

          <Separator />

          {/* API Configuration */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-gray-700">API Configuration</h3>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="openrouterApiKey">OpenRouter API Key</Label>
                <div className="relative">
                  <Input
                    id="openrouterApiKey"
                    type={showApiKey ? "text" : "password"}
                    placeholder="sk-or-v1-..."
                    value={aiSettings.openrouterApiKey}
                    onChange={(e) => {
                      setAISettings((s) => ({ ...s, openrouterApiKey: e.target.value }));
                      setConnectionStatus("idle");
                      setConnectionResult(null);
                    }}
                    className="pr-10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowApiKey(!showApiKey)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showApiKey ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
                <p className="text-muted-foreground text-xs">
                  Get your API key from{" "}
                  <a
                    href="https://openrouter.ai/keys"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-500 hover:underline"
                  >
                    openrouter.ai/keys
                  </a>
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="openrouterBaseUrl">OpenRouter Base URL</Label>
                <Input
                  id="openrouterBaseUrl"
                  placeholder="https://openrouter.ai/api/v1"
                  value={aiSettings.openrouterBaseUrl}
                  onChange={(e) =>
                    setAISettings((s) => ({ ...s, openrouterBaseUrl: e.target.value }))
                  }
                />
              </div>
            </div>

            {/* Test Connection */}
            <div className="mt-3 flex items-center gap-3">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={handleTestConnection}
                disabled={testingConnection || !aiSettings.openrouterApiKey.trim()}
                className="gap-2"
              >
                {testingConnection ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : connectionStatus === "success" ? (
                  <CheckCircle2 className="h-4 w-4 text-green-500" />
                ) : connectionStatus === "error" ? (
                  <WifiOff className="h-4 w-4 text-red-500" />
                ) : (
                  <Wifi className="h-4 w-4" />
                )}
                {testingConnection ? "Đang kiểm tra..." : "Test Connection"}
              </Button>

              {connectionStatus === "success" && connectionResult && (
                <span className="flex items-center gap-1.5 text-xs text-green-600 font-medium">
                  <CheckCircle2 className="h-3.5 w-3.5" />
                  Kết nối OK · {connectionResult.latency_ms}ms
                  {connectionResult.model && (
                    <span className="ml-1 text-gray-400 font-normal">({connectionResult.model})</span>
                  )}
                </span>
              )}
              {connectionStatus === "error" && (
                <span className="flex items-center gap-1.5 text-xs text-red-600 font-medium">
                  <XCircle className="h-3.5 w-3.5" />
                  Kết nối thất bại — kiểm tra lại API key
                </span>
              )}
            </div>
          </div>

          <Separator />

          {/* Default Models */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-gray-700">Default AI Models</h3>
            <div className="grid gap-4 md:grid-cols-3">
              <div className="space-y-2">
                <Label htmlFor="aiArticleModel">Article Model</Label>
                <Select
                  value={aiSettings.aiArticleModel}
                  onValueChange={(v) => setAISettings((s) => ({ ...s, aiArticleModel: v }))}
                >
                  <SelectTrigger id="aiArticleModel">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="deepseek/deepseek-chat">DeepSeek Chat</SelectItem>
                    <SelectItem value="anthropic/claude-sonnet-4">Claude Sonnet 4</SelectItem>
                    <SelectItem value="openai/gpt-4o">GPT-4o</SelectItem>
                    <SelectItem value="openai/gpt-4o-mini">GPT-4o Mini</SelectItem>
                    <SelectItem value="google/gemini-flash-1.5">Gemini Flash 1.5</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-muted-foreground text-xs">Model for article generation</p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="aiHoroscopeModel">Horoscope Model</Label>
                <Select
                  value={aiSettings.aiHoroscopeModel}
                  onValueChange={(v) => setAISettings((s) => ({ ...s, aiHoroscopeModel: v }))}
                >
                  <SelectTrigger id="aiHoroscopeModel">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="anthropic/claude-sonnet-4">Claude Sonnet 4</SelectItem>
                    <SelectItem value="openai/gpt-4o">GPT-4o</SelectItem>
                    <SelectItem value="openai/gpt-4o-mini">GPT-4o Mini</SelectItem>
                    <SelectItem value="deepseek/deepseek-chat">DeepSeek Chat</SelectItem>
                    <SelectItem value="google/gemini-flash-1.5">Gemini Flash 1.5</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-muted-foreground text-xs">Model for horoscope readings</p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="aiChatModel">Chat Model</Label>
                <Select
                  value={aiSettings.aiChatModel}
                  onValueChange={(v) => setAISettings((s) => ({ ...s, aiChatModel: v }))}
                >
                  <SelectTrigger id="aiChatModel">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="openai/gpt-4o-mini">GPT-4o Mini</SelectItem>
                    <SelectItem value="openai/gpt-4o">GPT-4o</SelectItem>
                    <SelectItem value="anthropic/claude-sonnet-4">Claude Sonnet 4</SelectItem>
                    <SelectItem value="deepseek/deepseek-chat">DeepSeek Chat</SelectItem>
                    <SelectItem value="google/gemini-flash-1.5">Gemini Flash 1.5</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-muted-foreground text-xs">Model for AI chat</p>
              </div>
            </div>
          </div>

          <Separator />

          {/* Token Limits */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-gray-700">Token Limits (per request)</h3>
            <div className="grid gap-4 md:grid-cols-3">
              <div className="space-y-2">
                <Label htmlFor="aiMaxTokensArticle">Article Max Tokens</Label>
                <Input
                  id="aiMaxTokensArticle"
                  type="number"
                  min="512"
                  max="16384"
                  value={aiSettings.aiMaxTokensArticle}
                  onChange={(e) =>
                    setAISettings((s) => ({
                      ...s,
                      aiMaxTokensArticle: parseInt(e.target.value) || 4096,
                    }))
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="aiMaxTokensHoroscope">Horoscope Max Tokens</Label>
                <Input
                  id="aiMaxTokensHoroscope"
                  type="number"
                  min="256"
                  max="8192"
                  value={aiSettings.aiMaxTokensHoroscope}
                  onChange={(e) =>
                    setAISettings((s) => ({
                      ...s,
                      aiMaxTokensHoroscope: parseInt(e.target.value) || 2048,
                    }))
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="aiMaxTokensChat">Chat Max Tokens</Label>
                <Input
                  id="aiMaxTokensChat"
                  type="number"
                  min="256"
                  max="8192"
                  value={aiSettings.aiMaxTokensChat}
                  onChange={(e) =>
                    setAISettings((s) => ({
                      ...s,
                      aiMaxTokensChat: parseInt(e.target.value) || 1024,
                    }))
                  }
                />
              </div>
            </div>
          </div>

          <Separator />

          {/* Rate Limits */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-gray-700">
              Horoscope Daily Rate Limits (per user)
            </h3>
            <div className="grid gap-4 md:grid-cols-3">
              <div className="space-y-2">
                <Label htmlFor="aiRateGuest">Guest</Label>
                <Input
                  id="aiRateGuest"
                  type="number"
                  min="0"
                  max="100"
                  value={aiSettings.aiRateHoroscopeGuest}
                  onChange={(e) =>
                    setAISettings((s) => ({
                      ...s,
                      aiRateHoroscopeGuest: parseInt(e.target.value) || 0,
                    }))
                  }
                />
                <p className="text-muted-foreground text-xs">Reads/day for anonymous visitors</p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="aiRateFree">Free Users</Label>
                <Input
                  id="aiRateFree"
                  type="number"
                  min="0"
                  max="100"
                  value={aiSettings.aiRateHoroscopeFree}
                  onChange={(e) =>
                    setAISettings((s) => ({
                      ...s,
                      aiRateHoroscopeFree: parseInt(e.target.value) || 0,
                    }))
                  }
                />
                <p className="text-muted-foreground text-xs">Reads/day for registered users</p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="aiRatePremium">Premium Users</Label>
                <Input
                  id="aiRatePremium"
                  type="number"
                  min="0"
                  max="1000"
                  value={aiSettings.aiRateHoroscopePremium}
                  onChange={(e) =>
                    setAISettings((s) => ({
                      ...s,
                      aiRateHoroscopePremium: parseInt(e.target.value) || 0,
                    }))
                  }
                />
                <p className="text-muted-foreground text-xs">Reads/day for premium users</p>
              </div>
            </div>
          </div>

          <Separator />

          {/* Budget */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-gray-700">Budget Control</h3>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="aiMonthlyBudgetCap">Monthly Budget Cap (USD)</Label>
                <Input
                  id="aiMonthlyBudgetCap"
                  type="number"
                  min="0"
                  step="1"
                  value={aiSettings.aiMonthlyBudgetCap}
                  onChange={(e) =>
                    setAISettings((s) => ({
                      ...s,
                      aiMonthlyBudgetCap: parseFloat(e.target.value) || 0,
                    }))
                  }
                />
                <p className="text-muted-foreground text-xs">
                  Set to 0 for no cap. AI requests will be blocked when budget is exceeded.
                </p>
              </div>
            </div>
          </div>

          <div className="flex justify-end">
            <Button onClick={handleSaveAI} disabled={updateGroup.isPending}>
              {updateGroup.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Save className="mr-2 h-4 w-4" />
              )}
              Save Changes
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
