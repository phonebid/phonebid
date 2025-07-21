export interface User {
  id: number;
  email: string;
  name: string;
  phone: string;
  userType: "CONSUMER" | "SELLER" | "ADMIN";
  profileImage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  provider: "KAKAO" | "NAVER";
  accessToken: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  accessToken: string | null;
}
