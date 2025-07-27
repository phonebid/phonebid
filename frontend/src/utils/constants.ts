// API 관련 상수
export const API_BASE_URL = "/api";
export const API_TIMEOUT = 10000;

// 경매 관련 상수
export const AUCTION_DURATION_HOURS = 24;
export const AUCTION_STATUS = {
  ACTIVE: "ACTIVE",
  COMPLETED: "COMPLETED",
  CANCELLED: "CANCELLED",
} as const;

// 사용자 타입
export const USER_TYPES = {
  CONSUMER: "CONSUMER",
  SELLER: "SELLER",
  ADMIN: "ADMIN",
} as const;

// OAuth 제공자
export const OAUTH_PROVIDERS = {
  KAKAO: "KAKAO",
  NAVER: "NAVER",
} as const;

// OAuth 설정
export const OAUTH_CONFIG = {
  KAKAO_CLIENT_ID: import.meta.env.VITE_KAKAO_CLIENT_ID,
  NAVER_CLIENT_ID: import.meta.env.VITE_NAVER_CLIENT_ID,
  REDIRECT_URI: import.meta.env.VITE_REDIRECT_URI,
} as const;

// OAuth URL 설정
export const OAUTH_URLS = {
  KAKAO_AUTH: "https://kauth.kakao.com/oauth/authorize",
  NAVER_AUTH: "https://nid.naver.com/oauth2.0/authorize",
} as const;

// 휴대폰 브랜드
export const PHONE_BRANDS = {
  APPLE: "Apple",
  SAMSUNG: "Samsung",
  LG: "LG",
} as const;

// 통신사
export const CARRIERS = {
  SKT: "SKT",
  KT: "KT",
  LGU: "LG U+",
} as const;

// 페이지네이션
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 100,
} as const;

// 로컬 스토리지 키
export const STORAGE_KEYS = {
  ACCESS_TOKEN: "accessToken",
  REFRESH_TOKEN: "refreshToken",
  USER_DATA: "userData",
} as const;
