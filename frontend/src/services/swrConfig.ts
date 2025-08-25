import { SWRConfiguration } from "swr";
import { apiClient } from "services/apiClient";

export const defaultSWRConfig: SWRConfiguration = {
  fetcher: (url: string) => apiClient.get(url),
  revalidateOnFocus: false,
  revalidateOnReconnect: true,
  revalidateOnMount: true,
  shouldRetryOnError: true,
  errorRetryCount: 3,
  onError: (error, key) => {
    console.error(`SWR Error for key "${key}":`, error);
  },
};

export const staticDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: false,
  revalidateOnReconnect: false,
  revalidateIfStale: false,
  dedupingInterval: 60 * 60 * 1000,
};

export const userDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: false,
  dedupingInterval: 10 * 60 * 1000,
};

export const realtimeDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: true,
  revalidateOnReconnect: true,
  refreshInterval: 30 * 1000,
  dedupingInterval: 5 * 1000,
};

export const sensitiveDataConfig: SWRConfiguration = {
  ...defaultSWRConfig,
  revalidateOnFocus: true,
  revalidateOnMount: true,
  revalidateOnReconnect: true,
  dedupingInterval: 1000,
  errorRetryCount: 1,
};
