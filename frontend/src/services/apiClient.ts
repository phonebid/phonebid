import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { toast } from "react-toastify";
import type { ApiResponse } from "types/ApiTypes";
import { ApiErrorClass } from "types/ApiTypes";
import { getApiBaseUrl, getApiTimeout, API_CONSTANTS } from "utils/apiUtils";
import { useAuthStore } from "store/authStore";
import { refreshAccessToken } from "services/authService";

class ApiClient {
  private client: AxiosInstance;
  private isRefreshing = false;
  private failedQueue: Array<{
    resolve: (value?: unknown) => void;
    reject: (error?: unknown) => void;
  }> = [];

  constructor() {
    const baseURL = getApiBaseUrl();

    this.client = axios.create({
      baseURL: `${baseURL}${API_CONSTANTS.ENDPOINTS.API_V1}`,
      timeout: getApiTimeout(),
      headers: API_CONSTANTS.DEFAULT_HEADERS,
      withCredentials: true, // 쿠키 자동 전송 활성화
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    // 쿠키 기반 인증 사용: 쿠키가 자동으로 전송되므로 헤더에 명시적으로 설정할 필요 없음
    this.client.interceptors.request.use(
      (config) => {
        // FormData인 경우 Content-Type 헤더를 삭제하여 브라우저가 자동으로 boundary를 포함한 올바른 Content-Type을 설정하도록 함
        if (config.data instanceof FormData) {
          delete config.headers["Content-Type"];
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response: AxiosResponse<ApiResponse<unknown>>) => response,
      async (error) => {
        const errorMessage =
          error.response?.data?.message || "알 수 없는 오류가 발생했습니다.";
        const errorDetails = error.response?.data?.data;
        const errorCode = error.response?.status || 500;
        const requestUrl = error.config?.url || "";
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        console.error("API Error:", error);

        // 401 에러 처리 (토큰 갱신 시도)
        if (errorCode === 401 && originalRequest && !originalRequest._retry) {
          // Refresh Token 갱신 요청은 인터셉터 제외
          if (requestUrl.includes("/auth/refresh")) {
            const { forceLogout } = useAuthStore.getState();
            forceLogout();
            return Promise.reject(new ApiErrorClass(errorCode, "인증이 필요합니다."));
          }

          // 이미 갱신 중이면 대기
          if (this.isRefreshing) {
            return new Promise((resolve, reject) => {
              this.failedQueue.push({ resolve, reject });
            })
              .then(() => {
                return this.client(originalRequest);
              })
              .catch((err) => {
                return Promise.reject(err);
              });
          }

          originalRequest._retry = true;
          this.isRefreshing = true;

          try {
            // Refresh Token으로 Access Token 갱신
            // 쿠키 기반 인증: 백엔드에서 쿠키로 새 토큰을 설정하므로 클라이언트는 별도 처리 불필요
            await refreshAccessToken();

            // 갱신 완료 후 큐 해제
            this.isRefreshing = false;
            this.processQueue(null);

            // 원래 요청 재시도 (쿠키에 새 토큰이 자동으로 포함됨)
            return this.client(originalRequest);
          } catch (refreshError) {
            // 갱신 실패 시 즉시 플래그 해제
            this.isRefreshing = false;
            
            // 갱신 실패 시 대기 중인 요청들 모두 실패 처리
            this.processQueue(refreshError);
            
            // 인증 확인용 요청(/mypage/profile)이거나 이미 로그인 페이지에 있으면 forceLogout 호출 안 함
            const isAuthCheckRequest = requestUrl.includes("/mypage/profile");
            const isOnLoginPage = window.location.pathname === "/login";

            if (!isAuthCheckRequest && !isOnLoginPage) {
              const { forceLogout } = useAuthStore.getState();
              forceLogout();
            }

            return Promise.reject(
              new ApiErrorClass(errorCode, "인증이 필요합니다.")
            );
          }
        }

        // 403 에러 처리
        if (errorCode === 403) {
          const isAuthCheckRequest = requestUrl.includes("/mypage/profile");
          const isOnLoginPage = window.location.pathname === "/login";

          if (!isAuthCheckRequest && !isOnLoginPage) {
            const { forceLogout } = useAuthStore.getState();
            forceLogout();
          }

          return Promise.reject(
            new ApiErrorClass(errorCode, "권한이 없습니다.")
          );
        }

        if (errorCode !== 400 || !errorDetails || typeof errorDetails !== "object") {
          toast.error(errorMessage);
        }
        return Promise.reject(new ApiErrorClass(errorCode, errorMessage, errorDetails));
      }
    );
  }

  private processQueue(error: unknown) {
    this.failedQueue.forEach((prom) => {
      if (error) {
        prom.reject(error);
      } else {
        prom.resolve(undefined);
      }
    });
    this.failedQueue = [];
  }

  async get<T>(url: string): Promise<T> {
    const response = await this.client.get<ApiResponse<T>>(url);
    return response.data.data;
  }

  async post<T>(url: string, data?: unknown): Promise<T> {
    const response = await this.client.post<ApiResponse<T>>(url, data);
    return response.data.data;
  }

  async put<T>(url: string, data?: unknown): Promise<T> {
    const response = await this.client.put<ApiResponse<T>>(url, data);
    return response.data.data;
  }

  async patch<T>(url: string, data?: unknown): Promise<T> {
    const response = await this.client.patch<ApiResponse<T>>(url, data);
    return response.data.data;
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<ApiResponse<T>>(url);
    return response.data.data;
  }

  /**
   * 인증 관련 헤더 및 설정 제거
   * 로그아웃 시 호출하여 토큰 재사용 방지
   * 
   * 주의: 쿠키 기반 인증을 사용하므로 Authorization 헤더를 기본적으로 사용하지 않습니다.
   * 이 메서드는 혹시 모를 헤더 설정을 제거하기 위한 안전장치입니다.
   */
  clearAuth(): void {
    delete this.client.defaults.headers.common.Authorization;
  }
}

export const apiClient = new ApiClient();
