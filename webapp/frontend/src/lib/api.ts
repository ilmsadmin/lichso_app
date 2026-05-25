import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import Cookies from "js-cookie";

// ============================================
// Axios Instance
// ============================================
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api",
  timeout: Number(process.env.NEXT_PUBLIC_API_TIMEOUT) || 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// ============================================
// Request Interceptor - Attach Access Token
// ============================================
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const tokenKey = process.env.NEXT_PUBLIC_ACCESS_TOKEN_KEY || "zplus_access_token";
    const token = Cookies.get(tokenKey);

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// ============================================
// Response Interceptor - Handle Token Refresh
// ============================================
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: AxiosError | null, token: string | null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // If 401 and not already retrying
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Queue the request while refreshing
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshTokenKey = process.env.NEXT_PUBLIC_REFRESH_TOKEN_KEY || "zplus_refresh_token";
        const refreshToken = Cookies.get(refreshTokenKey);

        if (!refreshToken) {
          throw new Error("No refresh token available");
        }

        const { data } = await axios.post(
          `${process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api"}/auth/refresh`,
          { refresh_token: refreshToken }
        );

        const accessTokenKey = process.env.NEXT_PUBLIC_ACCESS_TOKEN_KEY || "zplus_access_token";

        // Store new tokens (secure flags in production)
        const cookieOpts = {
          secure: process.env.NODE_ENV === "production",
          sameSite: "lax" as const,
        };
        Cookies.set(accessTokenKey, data.data.access_token, cookieOpts);
        Cookies.set(refreshTokenKey, data.data.refresh_token, cookieOpts);

        // Update the authorization header
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${data.data.access_token}`;
        }

        processQueue(null, data.data.access_token);

        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as AxiosError, null);

        // Clear tokens and redirect to login
        const accessTokenKey = process.env.NEXT_PUBLIC_ACCESS_TOKEN_KEY || "zplus_access_token";
        const refreshTokenKey = process.env.NEXT_PUBLIC_REFRESH_TOKEN_KEY || "zplus_refresh_token";

        Cookies.remove(accessTokenKey);
        Cookies.remove(refreshTokenKey);

        if (typeof window !== "undefined") {
          window.location.href = "/login";
        }

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
