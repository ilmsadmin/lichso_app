export interface SurveyQuestion {
  title: string;
  type: "single_choice" | "multiple_choice" | "text";
  options?: string[];
  required: boolean;
}

export interface Survey {
  id: string;
  title: string;
  description?: string;
  questions: SurveyQuestion[];
  is_active: boolean;
  created_by?: string;
  created_at: string;
  updated_at: string;
}

export interface CreateSurveyRequest {
  title: string;
  description?: string;
  questions: SurveyQuestion[];
  is_active?: boolean;
}

export interface UpdateSurveyRequest {
  title?: string;
  description?: string;
  questions?: SurveyQuestion[];
  is_active?: boolean;
}

export interface UserAnswer {
  question_index: number;
  selected_options?: string[];
  text_answer?: string;
}

export interface SubmitSurveyResponseRequest {
  answers: UserAnswer[];
  device_id?: string;
}

export interface StatsOptionCount {
  option: string;
  count: number;
  percentage: number;
}

export interface QuestionStats {
  question_index: number;
  question_title: string;
  type: "single_choice" | "multiple_choice" | "text";
  total_answers: number;
  option_counts?: StatsOptionCount[];
  text_answers?: string[];
}

export interface SurveyStats {
  survey_id: string;
  survey_title: string;
  total_responses: number;
  question_stats: QuestionStats[];
}

export interface SurveyListParams {
  page?: number;
  limit?: number;
  search?: string;
}
