import { toast } from "react-toastify";
import { createKakaoAuthURL, createNaverAuthURL } from "utils/constants";
import type { User } from "types/UserTypes";

/**
 * 카카오 로그인 - URL 리다이렉트 방식
 */
export const loginWithKakao = (): void => {
  console.log("카카오 로그인 리다이렉트 시작");
  try {
    const authURL = createKakaoAuthURL();
    console.log("카카오 OAuth URL:", authURL);
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
  console.log("네이버 로그인 리다이렉트 시작");
  try {
    const authURL = createNaverAuthURL();
    console.log("네이버 OAuth URL:", authURL);
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
    // 로컬 스토리지 정리
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userData");

    toast.success("로그아웃되었습니다.");
  } catch (error) {
    console.error("로그아웃 중 오류:", error);
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
