import { apiClient } from "./apiClient";
import { toast } from "react-toastify";
import { OAUTH_CONFIG, OAUTH_PROVIDERS } from "utils/constants";
import type { LoginRequest, LoginResponse, User } from "types/UserTypes";

// 전역 타입 선언 (window 객체 확장)
declare global {
  interface Window {
    Kakao: any;
    naver_id_login: any;
    naverLogin: any;
  }
}

export class AuthService {
  private static instance: AuthService;
  private isKakaoInitialized = false;
  private isNaverInitialized = false;

  private constructor() {
    this.initializeSDKs();
  }

  public static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  /**
   * OAuth SDK 초기화
   */
  private async initializeSDKs(): Promise<void> {
    await this.initializeKakaoSDK();
    await this.initializeNaverSDK();
  }

  /**
   * 카카오 SDK 초기화
   */
  private async initializeKakaoSDK(): Promise<void> {
    try {
      if (typeof window === "undefined" || !window.Kakao) {
        console.warn("Kakao SDK가 로드되지 않았습니다.");
        return;
      }

      if (!OAUTH_CONFIG.KAKAO_CLIENT_ID) {
        console.warn("KAKAO_CLIENT_ID가 설정되지 않았습니다.");
        return;
      }

      if (!window.Kakao.isInitialized()) {
        window.Kakao.init(OAUTH_CONFIG.KAKAO_CLIENT_ID);
        console.log("Kakao SDK 초기화 완료");
      }

      this.isKakaoInitialized = true;
    } catch (error) {
      console.error("Kakao SDK 초기화 실패:", error);
      toast.error("카카오 로그인 초기화에 실패했습니다.");
    }
  }

  /**
   * 네이버 SDK 초기화
   */
  private async initializeNaverSDK(): Promise<void> {
    try {
      if (typeof window === "undefined" || !window.naver_id_login) {
        console.warn("Naver SDK가 로드되지 않았습니다.");
        return;
      }

      if (!OAUTH_CONFIG.NAVER_CLIENT_ID) {
        console.warn("NAVER_CLIENT_ID가 설정되지 않았습니다.");
        return;
      }

      const naverLogin = new window.naver_id_login(
        OAUTH_CONFIG.NAVER_CLIENT_ID,
        OAUTH_CONFIG.REDIRECT_URI
      );

      // 네이버 로그인 객체를 전역에 저장
      (window as any).naverLogin = naverLogin;
      this.isNaverInitialized = true;
      console.log("Naver SDK 초기화 완료");
    } catch (error) {
      console.error("Naver SDK 초기화 실패:", error);
      toast.error("네이버 로그인 초기화에 실패했습니다.");
    }
  }

  /**
   * 카카오 로그인
   */
  public async loginWithKakao(): Promise<LoginResponse> {
    if (!this.isKakaoInitialized) {
      throw new Error("카카오 SDK가 초기화되지 않았습니다.");
    }

    return new Promise((resolve, reject) => {
      window.Kakao.Auth.login({
        success: async (authObj: any) => {
          try {
            console.log("카카오 로그인 성공:", authObj);

            // 백엔드 API 호출
            const loginRequest: LoginRequest = {
              provider: OAUTH_PROVIDERS.KAKAO,
              accessToken: authObj.access_token,
            };

            const response = await this.authenticateWithBackend(loginRequest);
            resolve(response);
          } catch (error) {
            console.error("카카오 로그인 처리 중 오류:", error);
            reject(error);
          }
        },
        fail: (error: any) => {
          console.error("카카오 로그인 실패:", error);
          reject(new Error("카카오 로그인에 실패했습니다."));
        },
      });
    });
  }

  /**
   * 네이버 로그인
   */
  public async loginWithNaver(): Promise<LoginResponse> {
    if (!this.isNaverInitialized) {
      throw new Error("네이버 SDK가 초기화되지 않았습니다.");
    }

    return new Promise((resolve, reject) => {
      const naverLogin = (window as any).naverLogin;

      naverLogin.getLoginStatus(async (status: boolean) => {
        if (status) {
          try {
            const accessToken = naverLogin.getAccessToken();
            console.log("네이버 로그인 성공, 토큰:", accessToken);

            // 백엔드 API 호출
            const loginRequest: LoginRequest = {
              provider: OAUTH_PROVIDERS.NAVER,
              accessToken: accessToken,
            };

            const response = await this.authenticateWithBackend(loginRequest);
            resolve(response);
          } catch (error) {
            console.error("네이버 로그인 처리 중 오류:", error);
            reject(error);
          }
        } else {
          // 로그인 창 열기
          naverLogin.init();
          naverLogin.authorize();
          reject(new Error("네이버 로그인이 취소되었습니다."));
        }
      });
    });
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
      localStorage.setItem("refreshToken", response.refreshToken);

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
      // 카카오 로그아웃
      if (this.isKakaoInitialized && window.Kakao.Auth.getAccessToken()) {
        await new Promise<void>((resolve) => {
          window.Kakao.Auth.logout(() => {
            console.log("카카오 로그아웃 완료");
            resolve();
          });
        });
      }

      // 네이버 로그아웃
      if (this.isNaverInitialized && (window as any).naverLogin) {
        (window as any).naverLogin.logout();
        console.log("네이버 로그아웃 완료");
      }

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

  /**
   * 토큰 갱신
   */
  public async refreshToken(): Promise<string | null> {
    try {
      const refreshToken = localStorage.getItem("refreshToken");
      if (!refreshToken) {
        throw new Error("리프레시 토큰이 없습니다.");
      }

      const response = await apiClient.post<{ accessToken: string }>(
        "/auth/refresh",
        {
          refreshToken,
        }
      );

      localStorage.setItem("accessToken", response.accessToken);
      return response.accessToken;
    } catch (error) {
      console.error("토큰 갱신 실패:", error);
      // 토큰 갱신 실패 시 로그아웃 처리
      await this.logout();
      return null;
    }
  }
}

// 싱글톤 인스턴스 내보내기
export const authService = AuthService.getInstance();
