import axios, { AxiosInstance, AxiosResponse } from "axios";
import { toast } from "react-toastify";
import type { ApiResponse } from "types/ApiTypes";
import { ApiErrorClass } from "types/ApiTypes";

// 환경별 API URL 설정
const getApiBaseUrl = (): string => {
  // 환경변수가 있으면 사용, 없으면 환경에 따라 기본값 사용
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL;

  if (envBaseUrl) {
    return envBaseUrl;
  }

  // 개발 환경: localhost:8080
  if (import.meta.env.DEV) {
    return "http://localhost:8080";
  }

  // 프로덕션 환경: 실제 도메인 (배포 시 수정 필요)
  return "https://api.phonebid.com";
};

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    const baseURL = getApiBaseUrl();

    this.client = axios.create({
      baseURL: `${baseURL}/api/v1`,
      timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || "10000"),
      headers: {
        "Content-Type": "application/json",
      },
    });

    console.log("API Client initialized with baseURL:", `${baseURL}/api/v1`);
    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor for auth token
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem("accessToken");
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
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
