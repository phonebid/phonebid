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
  KAKAO_REDIRECT_URI: import.meta.env.VITE_KAKAO_REDIRECT_URI,
  NAVER_REDIRECT_URI: import.meta.env.VITE_NAVER_REDIRECT_URI,
} as const;

// OAuth URL 설정
export const OAUTH_URLS = {
  KAKAO_AUTH: "https://kauth.kakao.com/oauth/authorize",
  NAVER_AUTH: "https://nid.naver.com/oauth2.0/authorize",
} as const;

// OAuth URL 생성 함수들
export const createKakaoAuthURL = (): string => {
  const state = generateRandomState("KAKAO");
  const params = new URLSearchParams({
    client_id: OAUTH_CONFIG.KAKAO_CLIENT_ID || "",
    redirect_uri: OAUTH_CONFIG.KAKAO_REDIRECT_URI || "",
    response_type: "code",
    state: state,
  });

  return `${OAUTH_URLS.KAKAO_AUTH}?${params.toString()}`;
};

export const createNaverAuthURL = (): string => {
  const state = generateRandomState("NAVER");
  const params = new URLSearchParams({
    client_id: OAUTH_CONFIG.NAVER_CLIENT_ID || "",
    redirect_uri: OAUTH_CONFIG.NAVER_REDIRECT_URI || "",
    response_type: "code",
    state: state,
  });

  return `${OAUTH_URLS.NAVER_AUTH}?${params.toString()}`;
};

// CSRF 방지를 위한 랜덤 state 생성 (provider 정보 포함)
export const generateRandomState = (provider: string): string => {
  const randomStr =
    Math.random().toString(36).substring(2, 15) +
    Math.random().toString(36).substring(2, 15);
  const state = `${provider}_${randomStr}`;

  // 세션 스토리지에 저장하여 콜백에서 검증
  sessionStorage.setItem("oauth_state", state);
  sessionStorage.setItem("oauth_provider", provider);

  return state;
};

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
  USER_DATA: "userData",
} as const;

// 입찰 폼 기본값
export const BID_FORM_DEFAULTS = {
  DEVICE_PRICE: 1231230,
  PUBLIC_SUBSIDY: 301000,
  ADDITIONAL_SUBSIDY: 301000,
  DELIVERY_DAYS: 3,
  PRICE_PLAN_MAINTENANCE_MONTHS: 24,
  LINE_MAINTENANCE_MONTHS: 24,
} as const;