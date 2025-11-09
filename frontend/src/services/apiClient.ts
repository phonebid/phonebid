import axios, { AxiosInstance, AxiosResponse } from "axios";
import { toast } from "react-toastify";
import type { ApiResponse } from "types/ApiTypes";
import { ApiErrorClass } from "types/ApiTypes";
import { getApiBaseUrl, getApiTimeout, API_CONSTANTS } from "utils/apiUtils";
import { useAuthStore } from "store/authStore";

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    const baseURL = getApiBaseUrl();

    this.client = axios.create({
      baseURL: `${baseURL}${API_CONSTANTS.ENDPOINTS.API_V1}`,
      timeout: getApiTimeout(),
      headers: API_CONSTANTS.DEFAULT_HEADERS,
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor for auth token
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem("accessToken");
        if (token) {
          config.headers.Authorization = "Bearer " + token;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response: AxiosResponse<ApiResponse<unknown>>) => response,
      (error) => {
        const errorMessage =
          error.response?.data?.message || "알 수 없는 오류가 발생했습니다.";
        const errorCode = error.response?.status || 500;

        console.error("API Error:", error);

        // 토큰 관련 에러 처리 (401, 403)
        if (errorCode === 401 || errorCode === 403) {
          const { forceLogout } = useAuthStore.getState();
          toast.error("세션이 만료되었습니다. 다시 로그인해주세요.");
          forceLogout();
          return Promise.reject(
            new ApiErrorClass(errorCode, "인증이 필요합니다.")
          );
        }

        toast.error(errorMessage);
        return Promise.reject(new ApiErrorClass(errorCode, errorMessage));
      }
    );
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

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<ApiResponse<T>>(url);
    return response.data.data;
  }
}

export const apiClient = new ApiClient();
