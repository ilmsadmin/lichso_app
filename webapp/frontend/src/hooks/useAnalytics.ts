import { useQuery } from "@tanstack/react-query";
import * as analyticsService from "@/services/analyticsService";

const ANALYTICS_KEY = "user-analytics";

export function useUserAnalytics() {
  return useQuery({
    queryKey: [ANALYTICS_KEY],
    queryFn: () => analyticsService.getUserAnalytics(),
    staleTime: 60_000, // 1 minute stale time
    refetchOnWindowFocus: false,
  });
}
