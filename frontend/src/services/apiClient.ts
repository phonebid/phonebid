import axios, { AxiosInstance, AxiosResponse } from "axios";
import { toast } from "react-toastify";
import type { ApiResponse } from "types/ApiTypes";
import { ApiErrorClass } from "types/ApiTypes";

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: "/api",
      timeout: 10000,
      headers: {
        "Content-Type": "application/json",
      },
    });

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
      (response: AxiosResponse<ApiResponse<any>>) => response,
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

  async post<T>(url: string, data?: any): Promise<T> {
    const response = await this.client.post<ApiResponse<T>>(url, data);
    return response.data.data;
  }

  async put<T>(url: string, data?: any): Promise<T> {
    const response = await this.client.put<ApiResponse<T>>(url, data);
    return response.data.data;
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<ApiResponse<T>>(url);
    return response.data.data;
  }
}

export const apiClient = new ApiClient();
