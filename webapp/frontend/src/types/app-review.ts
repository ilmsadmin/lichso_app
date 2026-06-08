export type AppReviewPlatform = "android" | "ios";
export type AppReviewStatus = "new" | "reviewed" | "resolved";
export type AppReviewFlow = "low_rating_feedback" | "high_rating_prompt";

export interface AppReviewUser {
  id: string;
  email: string;
  full_name: string;
  provider: string;
}

export interface AppReview {
  id: string;
  user_id?: string;
  platform: AppReviewPlatform;
  app_version: string;
  device_id: string;
  device_name: string;
  os_version: string;
  stars: number;
  review_text: string;
  review_flow: AppReviewFlow;
  review_source: string;
  status: AppReviewStatus;
  admin_note: string;
  created_at: string;
  updated_at: string;
  user?: AppReviewUser;
}

export interface AppReviewListParams {
  page?: number;
  limit?: number;
  search?: string;
  status?: AppReviewStatus;
  platform?: AppReviewPlatform;
  stars?: number;
}

export interface UpdateAppReviewRequest {
  status?: AppReviewStatus;
  admin_note?: string;
}
