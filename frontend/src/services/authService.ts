import { apiClient } from "./apiClient";
import { toast } from "react-toastify";
import { createKakaoAuthURL, createNaverAuthURL } from "utils/constants";
import type { LoginRequest, LoginResponse, User } from "types/UserTypes";

export class AuthService {
  private static instance: AuthService;

  private constructor() {}

  public static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  /**
   * 카카오 로그인 - URL 리다이렉트 방식
   */
  public loginWithKakao(): void {
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
  }

  /**
   * 네이버 로그인 - URL 리다이렉트 방식
   */
  public loginWithNaver(): void {
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
  }

  /**
   * OAuth 콜백 처리 - authorization code로 토큰 교환
   */
  public async handleOAuthCallback(
    provider: string,
    code: string,
    state: string
  ): Promise<LoginResponse> {
    console.log(`${provider} OAuth 콜백 처리:`, { code, state });

    // State 검증 (CSRF 방지)
    const savedState = sessionStorage.getItem("oauth_state");
    if (savedState !== state) {
      console.error("State 파라미터 불일치:", {
        savedState,
        receivedState: state,
      });
      toast.error("보안 검증에 실패했습니다.");
      throw new Error("Invalid state parameter");
    }

    // 세션에서 state 제거
    sessionStorage.removeItem("oauth_state");

    try {
      // 백엔드 API 호출 - authorization code 전송
      const loginRequest: LoginRequest = {
        provider: provider as any,
        authorizationCode: code, // access_token 대신 authorization_code 사용
      };

      const response = await this.authenticateWithBackend(loginRequest);
      return response;
    } catch (error) {
      console.error(`${provider} 콜백 처리 중 오류:`, error);
      toast.error(`${provider} 로그인 처리 중 오류가 발생했습니다.`);
      throw error;
    }
  }

  /**
   * 백엔드 인증 API 호출
   */
  private async authenticateWithBackend(
    loginRequest: LoginRequest
  ): Promise<LoginResponse> {
    try {
      const response = await apiClient.post<LoginResponse>(
        "/auth/oauth/login",
        loginRequest
      );

      // 토큰을 로컬 스토리지에 저장
      localStorage.setItem("accessToken", response.accessToken);

      toast.success(
        `${
          loginRequest.provider === "KAKAO" ? "카카오" : "네이버"
        } 로그인 성공!`
      );

      return response;
    } catch (error) {
      console.error("백엔드 인증 실패:", error);
      toast.error("로그인 처리 중 오류가 발생했습니다.");
      throw error;
    }
  }

  /**
   * 로그아웃
   */
  public async logout(): Promise<void> {
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
  }

  /**
   * 현재 로그인 상태 확인
   */
  public isLoggedIn(): boolean {
    const accessToken = localStorage.getItem("accessToken");
    return !!accessToken;
  }

  /**
   * 저장된 사용자 정보 조회
   */
  public getCurrentUser(): User | null {
    const userData = localStorage.getItem("userData");
    return userData ? JSON.parse(userData) : null;
  }

}

// 싱글톤 인스턴스 내보내기
export const authService = AuthService.getInstance();
