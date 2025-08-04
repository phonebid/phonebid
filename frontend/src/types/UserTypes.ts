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
  accessToken?: string; // 기존 방식 (선택적)
  authorizationCode?: string; // 새로운 방식 (선택적)
}

export interface LoginResponse {
  accessToken: string;
  user: User;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  accessToken: string | null;
}

export interface SignupRequest {
  username: string;
  password: string;
  email: string;
  name: string;
  nickname: string;
}
