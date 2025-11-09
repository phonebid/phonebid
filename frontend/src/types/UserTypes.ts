export interface User {
  username: string;
  nickname: string;
  role: string;
}

export interface LoginRequest {
  provider: "KAKAO" | "NAVER";
  accessToken?: string;
  authorizationCode?: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  username: string;
  nickname: string;
  role: string;
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
