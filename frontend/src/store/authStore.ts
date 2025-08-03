import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { AuthState, User } from "types/UserTypes";
import { authService } from "services/authService";
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
  loginWithKakao: () => void;
  loginWithNaver: () => void;

  performLogout: () => Promise<void>;

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
          set(
            {
              accessToken: token,
            },
            false,
            "auth/setAccessToken"
          );
        },

        // OAuth Actions - URL 리다이렉트 방식
        loginWithKakao: () => {
          try {
            authService.loginWithKakao();
          } catch (error) {
            console.error("카카오 로그인 실패:", error);
            toast.error("카카오 로그인에 실패했습니다.");
          }
        },

        loginWithNaver: () => {
          try {
            authService.loginWithNaver();
          } catch (error) {
            console.error("네이버 로그인 실패:", error);
            toast.error("네이버 로그인에 실패했습니다.");
          }
        },

        performLogout: async () => {
          const { logout, setLoading } = get();

          try {
            setLoading(true);
            await authService.logout();
            logout();
          } catch (error) {
            console.error("로그아웃 실패:", error);
            // 로그아웃은 실패해도 상태는 초기화
            logout();
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
