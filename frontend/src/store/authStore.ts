import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { AuthState, User } from "types/UserTypes";

interface AuthStore extends AuthState {
  // Actions
  login: (user: User, accessToken: string) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
  setAccessToken: (token: string) => void;
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        isAuthenticated: false,
        user: null,
        accessToken: null,

        // Actions
        login: (user: User, accessToken: string) => {
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

        updateUser: (userData: Partial<User>) => {
          const currentUser = get().user;
          if (currentUser) {
            set(
              {
                user: { ...currentUser, ...userData },
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
