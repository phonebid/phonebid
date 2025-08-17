import { SWRConfiguration } from "swr";
import { apiClient } from "./apiClient";

export const defaultSWRConfig: SWRConfiguration = {
  // 기본 fetcher - apiClient 사용
  fetcher: (url: string) => apiClient.get(url),

  // 기본 설정
  revalidateOnFocus: false,
  revalidateOnReconnect: true,
  revalidateOnMount: true,
  shouldRetryOnError: true,
  errorRetryCount: 3,

  // 에러 처리
  onError: (error, key) => {
    console.error(`SWR Error for key "${key}":`, error);
  },
};
