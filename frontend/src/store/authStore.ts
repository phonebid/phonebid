import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { AuthState, User } from "types/UserTypes";
import { apiClient } from "services/apiClient";
import { toast } from "react-toastify";

interface AuthStore extends AuthState {
  // Loading states
  isLoading: boolean;

  // Actions
  login: (user: User, accessToken: string) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
  setAccessToken: (token: string) => void;

  // OAuth Actions
  handleOAuthCallback: (
    provider: string,
    code: string,
    state: string
  ) => Promise<void>;

  // Utility Actions
  setLoading: (loading: boolean) => void;
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
        isLoading: false,

        // Basic Actions
        login: (user: User, accessToken: string) => {
          // 사용자 데이터를 로컬 스토리지에도 저장
          localStorage.setItem("userData", JSON.stringify(user));
          localStorage.setItem("accessToken", accessToken);

          set(
            {
              isAuthenticated: true,
              user,
              accessToken,
              isLoading: false,
            },
            false,
            "auth/login"
          );
        },

        logout: () => {
          // 로컬 스토리지 정리
          localStorage.removeItem("accessToken");
          localStorage.removeItem("userData");

          set(
            {
              isAuthenticated: false,
              user: null,
              accessToken: null,
              isLoading: false,
            },
            false,
            "auth/logout"
          );
        },

        updateUser: (userData: Partial<User>) => {
          const currentUser = get().user;
          if (currentUser) {
            const updatedUser = { ...currentUser, ...userData };
            localStorage.setItem("userData", JSON.stringify(updatedUser));

            set(
              {
                user: updatedUser,
              },
              false,
              "auth/updateUser"
            );
          }
        },

        setAccessToken: (token: string) => {
          localStorage.setItem("accessToken", token);
          set(
            {
              accessToken: token,
            },
            false,
            "auth/setAccessToken"
          );
        },

        // OAuth Actions
        handleOAuthCallback: async (
          provider: string,
          code: string,
          state: string
        ) => {
          try {
            set({ isLoading: true });

            // OAuth 토큰 교환 및 사용자 정보 조회
            const response = await apiClient.post(
              "/api/v1/auth/oauth/callback",
              {
                provider,
                code,
                state,
              }
            );

            const responseData = response as {
              data: { accessToken: string; user: User };
            };
            const { accessToken, user } = responseData.data;

            // 로그인 상태 업데이트
            get().login(user, accessToken);

            toast.success(`${provider} 로그인이 완료되었습니다.`);
          } catch (error: any) {
            console.error("OAuth 콜백 처리 실패:", error);
            const errorMessage =
              error.response?.data?.message || "OAuth 로그인에 실패했습니다.";
            toast.error(errorMessage);
            throw error;
          } finally {
            set({ isLoading: false });
          }
        },

        // Utility Actions
        setLoading: (loading: boolean) => {
          set({ isLoading: loading }, false, "auth/setLoading");
        },

        initializeAuth: () => {
          // 페이지 새로고침 시 로컬 스토리지에서 인증 상태 복원
          const accessToken = localStorage.getItem("accessToken");
          const userData = localStorage.getItem("userData");

          if (accessToken && userData) {
            try {
              const user = JSON.parse(userData);
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
              // 잘못된 데이터가 있으면 정리
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
