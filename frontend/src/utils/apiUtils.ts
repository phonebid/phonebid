/**
 * API 관련 공통 유틸리티 함수들
 */

// API 관련 공통 상수
export const API_CONSTANTS = {
  DEFAULT_TIMEOUT: 10000,
  DEFAULT_HEADERS: {
    "Content-Type": "application/json",
  },
  ENDPOINTS: {
    API_V1: "/api/v1",
  },
} as const;

/**
 * 환경별 API Base URL을 반환하는 함수
 * 환경변수 > 개발환경 > 프로덕션 순으로 우선순위 적용
 */
export const getApiBaseUrl = (): string => {
  // 환경변수가 있으면 사용, 없으면 환경에 따라 기본값 사용
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL;

  if (envBaseUrl) {
    return envBaseUrl;
  }

  // 개발 환경: localhost:8080
  if (import.meta.env.DEV) {
    return "http://localhost:8080";
  }

  // 프로덕션 환경: 실제 도메인 (배포 시 수정 필요)
  return "https://api.phonebid.com";
};

/**
 * API 타임아웃 값을 반환하는 함수
 */
export const getApiTimeout = (): number => {
  return parseInt(
    import.meta.env.VITE_API_TIMEOUT || String(API_CONSTANTS.DEFAULT_TIMEOUT)
  );
};

/**
 * 전체 API URL을 생성하는 함수
 */
export const createApiUrl = (endpoint: string): string => {
  const baseUrl = getApiBaseUrl();
  const apiPath = API_CONSTANTS.ENDPOINTS.API_V1;

  // endpoint가 이미 http로 시작하면 그대로 반환
  if (endpoint.startsWith("http")) {
    return endpoint;
  }

  // endpoint가 /로 시작하지 않으면 추가
  const normalizedEndpoint = endpoint.startsWith("/")
    ? endpoint
    : `/${endpoint}`;

  return `${baseUrl}${apiPath}${normalizedEndpoint}`;
};
