import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { AuthState, User, ProfileResponseDto } from "types/UserTypes";
import type { ApiResponse } from "types/ApiTypes";
import { apiClient } from "services/apiClient";

interface AuthStore extends AuthState {
  // Actions
  login: (user: User) => void;
  logout: () => Promise<void>;
  forceLogout: () => void;
  initializeAuth: () => Promise<void>;
  checkAuth: () => Promise<void>;
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
        login: (user: User) => {
          // 쿠키 기반 인증이므로 사용자 정보만 저장
          localStorage.setItem("userData", JSON.stringify(user));

          set(
            {
              isAuthenticated: true,
              user,
              accessToken: null, // 쿠키에 저장되므로 null로 설정
            },
            false,
            "auth/login"
          );
        },

        logout: async () => {
          try {
            // 백엔드 로그아웃 API 호출 (DB에서 RefreshToken 삭제 및 쿠키 삭제)
            await apiClient.post<ApiResponse<void>>("/users/logout");
          } catch (error) {
            // API 호출 실패해도 로컬 스토리지는 정리
            console.error("로그아웃 API 호출 실패:", error);
          }

          localStorage.removeItem("userData");
          // persist 스토리지도 정리
          localStorage.removeItem("auth-storage");
          // 쿠키는 백엔드에서 삭제됨 (HttpOnly 쿠키는 프론트엔드에서 접근 불가)

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

        forceLogout: () => {
          localStorage.removeItem("userData");

          set(
            {
              isAuthenticated: false,
              user: null,
              accessToken: null,
            },
            false,
            "auth/forceLogout"
          );

          // 이미 로그인 페이지에 있으면 리다이렉트하지 않음 (무한 루프 방지)
          if (window.location.pathname !== "/login") {
            window.location.href = "/login";
          }
        },

        // Utility Actions
        initializeAuth: async () => {
          // 쿠키 기반 인증이므로 항상 API 호출로 인증 상태 확인
          // localStorage에 userData가 없어도 쿠키에 토큰이 있을 수 있음 (예: 카카오 로그인 직후)
          try {
            // 쿠키에 토큰이 있는지 확인하기 위해 프로필 API 호출
            await get().checkAuth();
          } catch (error) {
            // 인증 실패 시 로컬 스토리지 정리
            console.error("인증 상태 확인 실패:", error);
            localStorage.removeItem("userData");
            set(
              {
                isAuthenticated: false,
                user: null,
                accessToken: null,
              },
              false,
              "auth/initialize"
            );
          }
        },

        checkAuth: async () => {
          try {
            // 쿠키에 토큰이 있는지 확인하기 위해 프로필 API 호출
            const profile = await apiClient.get<ProfileResponseDto>("/mypage/profile");
            
            const user: User = {
              username: profile.username,
              nickname: profile.nickname,
              role: profile.role,
            };

            // 사용자 정보 업데이트
            localStorage.setItem("userData", JSON.stringify(user));

            set(
              {
                isAuthenticated: true,
                user,
                accessToken: null, // 쿠키에 저장되므로 null
              },
              false,
              "auth/checkAuth"
            );
          } catch (error) {
            // 인증 실패
            localStorage.removeItem("userData");
            set(
              {
                isAuthenticated: false,
                user: null,
                accessToken: null,
              },
              false,
              "auth/checkAuth"
            );
            throw error;
          }
        },
      }),
      {
        name: "auth-storage",
        partialize: (state) => {
          // 로그아웃 상태일 때는 저장하지 않음
          if (!state.isAuthenticated) {
            return {};
          }
          return {
            isAuthenticated: state.isAuthenticated,
            user: state.user,
            accessToken: state.accessToken,
          };
        },
      }
    ),
    {
      name: "auth-store",
    }
  )
);
