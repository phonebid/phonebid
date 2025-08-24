import { SWRConfiguration } from "swr";
import { apiClient } from "./apiClient";

/**
 * 기본 SWR 설정 - 모든 useCustomSWR 훅에서 기본으로 사용
 * 보수적인 캐싱 전략으로 안정성 우선
 */
export const defaultSWRConfig: SWRConfiguration = {
  // HTTP 요청을 처리하는 함수 - 기존 apiClient 재사용
  fetcher: (url: string) => apiClient.get(url),

  // === 재검증(Revalidation) 설정 ===
  revalidateOnFocus: false, // 브라우저 탭 포커스 시 데이터 갱신 안 함 (성능 고려)
  revalidateOnReconnect: true, // 네트워크 재연결 시 데이터 갱신 (필수)
  revalidateOnMount: true, // 컴포넌트 마운트 시 데이터 갱신 (필수)

  // === 에러 처리 설정 ===
  shouldRetryOnError: true, // API 에러 시 자동 재시도 활성화
  errorRetryCount: 3, // 최대 3회까지 재시도

  // === 글로벌 에러 핸들러 ===
  onError: (error, key) => {
    console.error(`SWR Error for key "${key}":`, error);
    // TODO: 필요시 toast 알림이나 에러 로깅 서비스 연동
  },

  // === 기본값으로 사용되는 SWR 내장 설정들 ===
  // dedupingInterval: 2000,       // 2초간 동일 요청 중복 방지 (SWR 기본값)
  // refreshInterval: undefined,   // 자동 갱신 없음 (필요시 컴포넌트에서 설정)
  // revalidateIfStale: true,      // stale 상태 데이터 자동 재검증 (SWR 기본값)
};

/**
 * 데이터 특성별 SWR 설정 프리셋
 *
 * 사용법:
 * const { data } = useCustomSWR('/api/endpoint', staticDataConfig);
 */

/**
 * 정적/마스터 데이터용 설정
 *
 * 사용 대상: 휴대폰 모델 목록, 통신사 목록, 색상 옵션 등
 * 특징: 거의 변하지 않는 데이터, 장기간 캐싱 가능
 * 갱신 주기: 1시간마다 또는 수동 갱신
 */
export const staticDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: false, // 포커스 시 갱신 안 함 (불필요)
  revalidateOnReconnect: false, // 재연결 시에도 갱신 안 함 (데이터 변화 없음)
  revalidateIfStale: false, // stale 상태여도 갱신 안 함 (장기 캐싱)
  dedupingInterval: 60 * 60 * 1000, // 1시간간 동일 요청 차단 (메모리 효율)
};

/**
 * 사용자 데이터용 설정
 *
 * 사용 대상: 사용자 프로필, 설정 정보, 개인 정보 등
 * 특징: 가끔 변하는 데이터, 중간 수준의 캐싱
 * 갱신 주기: 10분마다 또는 특정 액션 후 수동 갱신
 */
export const userDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: false, // 포커스 시 갱신 안 함 (개인정보 특성상 빈번한 변경 없음)
  dedupingInterval: 10 * 60 * 1000, // 10분간 동일 요청 차단 (적당한 캐싱)
};

// 실시간 데이터용 (자주 변함)
export const realtimeDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: true,
  revalidateOnReconnect: true,
  refreshInterval: 30 * 1000, // 30초
  dedupingInterval: 5 * 1000, // 5초
};

// 민감한 데이터용 (항상 최신 필요)
export const sensitiveDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: true,
  revalidateOnMount: true,
  revalidateOnReconnect: true,
  dedupingInterval: 1000, // 1초
  errorRetryCount: 1,
};
