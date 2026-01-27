import { toast } from "react-toastify";
import { createKakaoAuthURL, createNaverAuthURL } from "utils/constants";
import { apiClient } from "services/apiClient";
import { useAuthStore } from "store/authStore";
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
 * authStore.logout에서 호출되는 중앙화된 로그아웃 로직
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

    // 로컬 스토리지 정리 (토큰은 쿠키에만 저장되므로 제거 불필요)
    localStorage.removeItem("userData");
    
    // apiClient의 Authorization 헤더 제거 (혹시 모를 헤더 설정 제거)
    apiClient.clearAuth();
    // axios 기본 Authorization 헤더도 제거 (다른 axios 인스턴스에서 사용할 수 있음)
    const axios = (await import("axios")).default;
    delete axios.defaults.headers.common.Authorization;

    toast.success("로그아웃되었습니다.");
  } catch (error) {
    console.error("로그아웃 중 오류:", error);
    // 에러가 발생해도 로컬 스토리지는 정리
    localStorage.removeItem("userData");
    toast.error("로그아웃 중 오류가 발생했습니다.");
  }
};

/**
 * 현재 로그인 상태 확인
 * 쿠키 기반 인증: localStorage 대신 authStore의 인증 상태 확인
 */
export const isLoggedIn = (): boolean => {
  // authStore를 사용하여 인증 상태 확인
  // 주의: 이 함수는 동기적으로 동작하므로, 실제 인증 상태는 authStore.checkAuth()로 확인해야 함
  return useAuthStore.getState().isAuthenticated;
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
 * RTR(Refresh Token Rotation) 방식: 백엔드에서 쿠키로 새 토큰을 설정하므로 반환값 불필요
 */
export const refreshAccessToken = async (): Promise<void> => {
  try {
    // apiClient를 직접 사용하지 않고 axios를 직접 사용하여 인터셉터 제외
    const axios = (await import("axios")).default;
    const { getApiBaseUrl, API_CONSTANTS } = await import("utils/apiUtils");
    
    const baseURL = getApiBaseUrl();
    await axios.post<ApiResponse<void>>(
      `${baseURL}${API_CONSTANTS.ENDPOINTS.API_V1}/auth/refresh`,
      {},
      {
        withCredentials: true, // 쿠키 자동 전송
      }
    );
    
    // 백엔드에서 쿠키로 새 토큰을 설정하므로 별도 처리 불필요
  } catch (error) {
    console.error("토큰 갱신 실패:", error);
    throw error;
  }
};
