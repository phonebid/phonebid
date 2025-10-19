import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { AuthState, LoginResponse, User } from "types/UserTypes";
import { apiClient } from "services/apiClient";
import { toast } from "react-toastify";

interface AuthStore extends AuthState {
  // Actions
  login: (user: User, accessToken: string) => void;
  logout: () => void;
  handleOAuthCallback: (
    provider: string,
    code: string,
    state: string
  ) => Promise<void>;
  initializeAuth: () => void;
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        isAuthenticated: false,
        user: null,
        accessToken: null,

        // Basic Actions
        login: (user: User, accessToken: string) => {
          localStorage.setItem("userData", JSON.stringify(user));
          // Bearer 접두사 제거
          localStorage.setItem(
            "accessToken",
            accessToken.replace("Bearer ", "")
          );

          set(
            {
              isAuthenticated: true,
              user,
              accessToken,
            },
            false,
            "auth/login"
          );
        },

        logout: () => {
          localStorage.removeItem("accessToken");
          localStorage.removeItem("userData");

          set(
            {
              isAuthenticated: false,
              user: null,
              accessToken: null,
            },
            false,
            "auth/logout"
          );
        },

        // OAuth Actions
        handleOAuthCallback: async (
          provider: string,
          code: string,
          state: string
        ) => {
          try {
            // OAuth 토큰 교환 및 사용자 정보 조회
            const response = await apiClient.post(
              "/api/v1/auth/oauth/callback",
              {
                provider,
                code,
                state,
              }
            );

            const { accessToken, username, nickname, role } =
              response as unknown as LoginResponse;

            get().login({ username, nickname, role }, accessToken);

            toast.success(`${provider} 로그인이 완료되었습니다.`);
          } catch (error: any) {
            console.error("OAuth 콜백 처리 실패:", error);
            const errorMessage =
              error.response?.data?.message || "OAuth 로그인에 실패했습니다.";
            toast.error(errorMessage);
            throw error;
          }
        },

        // Utility Actions
        initializeAuth: () => {
          const accessToken = localStorage.getItem("accessToken");
          const userData = localStorage.getItem("userData");

          if (accessToken && userData) {
            try {
              const user = JSON.parse(userData) as User;
              set(
                {
                  isAuthenticated: true,
                  user,
                  accessToken,
                },
                false,
                "auth/initialize"
              );
            } catch (error) {
              console.error("인증 상태 복원 실패:", error);
              localStorage.removeItem("accessToken");
              localStorage.removeItem("userData");
            }
          }
        },
      }),
      {
        name: "auth-storage",
        partialize: (state) => ({
          isAuthenticated: state.isAuthenticated,
          user: state.user,
          accessToken: state.accessToken,
        }),
      }
    ),
    {
      name: "auth-store",
    }
  )
);
