import { toast } from "react-toastify";
import { createKakaoAuthURL, createNaverAuthURL } from "utils/constants";
import { apiClient } from "services/apiClient";
import type { User } from "types/UserTypes";
import type { ApiResponse } from "types/ApiTypes";

/**
 * 카카오 로그인 - URL 리다이렉트 방식
 */
export const loginWithKakao = (): void => {
  try {
    const authURL = createKakaoAuthURL();
    window.location.href = authURL;
  } catch (error) {
    console.error("카카오 로그인 URL 생성 실패:", error);
    toast.error("카카오 로그인 URL 생성에 실패했습니다.");
    throw error;
  }
};

/**
 * 네이버 로그인 - URL 리다이렉트 방식
 */
export const loginWithNaver = (): void => {
  try {
    const authURL = createNaverAuthURL();
    window.location.href = authURL;
  } catch (error) {
    console.error("네이버 로그인 URL 생성 실패:", error);
    toast.error("네이버 로그인 URL 생성에 실패했습니다.");
    throw error;
  }
};

/**
 * 로그아웃
 */
export const logout = async (): Promise<void> => {
  try {
    // 백엔드 로그아웃 API 호출 (DB에서 RefreshToken 삭제 및 쿠키 삭제)
    try {
      await apiClient.post<ApiResponse<void>>("/users/logout");
    } catch (apiError) {
      // API 호출 실패해도 로컬 스토리지는 정리
      console.error("로그아웃 API 호출 실패:", apiError);
    }

    // 로컬 스토리지 정리
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userData");

    toast.success("로그아웃되었습니다.");
  } catch (error) {
    console.error("로그아웃 중 오류:", error);
    // 에러가 발생해도 로컬 스토리지는 정리
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userData");
    toast.error("로그아웃 중 오류가 발생했습니다.");
  }
};

/**
 * 현재 로그인 상태 확인
 */
export const isLoggedIn = (): boolean => {
  const accessToken = localStorage.getItem("accessToken");
  return !!accessToken;
};

/**
 * 저장된 사용자 정보 조회
 */
export const getCurrentUser = (): User | null => {
  const userData = localStorage.getItem("userData");
  return userData ? JSON.parse(userData) : null;
};

/**
 * Refresh Token으로 Access Token 갱신
 * @returns 새로운 Access Token (Bearer 접두사 포함)
 */
export const refreshAccessToken = async (): Promise<string> => {
  try {
    // apiClient를 직접 사용하지 않고 axios를 직접 사용하여 인터셉터 제외
    const axios = (await import("axios")).default;
    const { getApiBaseUrl, API_CONSTANTS } = await import("utils/apiUtils");
    
    const baseURL = getApiBaseUrl();
    const response = await axios.post<ApiResponse<string>>(
      `${baseURL}${API_CONSTANTS.ENDPOINTS.API_V1}/auth/refresh`,
      {},
      {
        withCredentials: true, // 쿠키 자동 전송
      }
    );
    
    // 응답에서 토큰 추출
    return response.data.data || "";
  } catch (error) {
    console.error("토큰 갱신 실패:", error);
    throw error;
  }
};
