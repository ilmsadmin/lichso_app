export type CampaignStatus = "draft" | "scheduled" | "sending" | "sent" | "failed";
export type CampaignTarget = "all" | "users";

export interface PushCampaign {
  id: string;
  title: string;
  body: string;
  image_url?: string;
  click_action?: string;
  data_payload?: string; // JSON string
  target_type: CampaignTarget;
  target_users?: string; // comma-separated user IDs
  status: CampaignStatus;
  scheduled_at?: string;
  sent_at?: string;
  sent_count: number;
  fail_count: number;
  created_by?: string;
  created_at: string;
  updated_at: string;
}

export interface CreateCampaignRequest {
  title: string;
  body: string;
  image_url?: string;
  click_action?: string;
  data_payload?: Record<string, string>;
  target_type: CampaignTarget;
  target_users?: string[];
  scheduled_at?: string;
}

export interface UpdateCampaignRequest extends CreateCampaignRequest {}

export interface CampaignListParams {
  page?: number;
  limit?: number;
  status?: CampaignStatus;
}

export interface PushStats {
  active_devices: number;
}

export const CAMPAIGN_STATUS_LABELS: Record<CampaignStatus, string> = {
  draft: "Nháp",
  scheduled: "Đã lên lịch",
  sending: "Đang gửi",
  sent: "Đã gửi",
  failed: "Thất bại",
};

export const CAMPAIGN_STATUS_COLORS: Record<CampaignStatus, string> = {
  draft: "secondary",
  scheduled: "outline",
  sending: "default",
  sent: "default",
  failed: "destructive",
};
