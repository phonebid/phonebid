import useSWR, { SWRConfiguration, SWRResponse } from "swr";
import { apiClient } from "services/apiClient";
import { defaultSWRConfig } from "services/swrConfig";

// 기본 SWR 훅
export function useCustomSWR<T>(
  key: string | null,
  config?: SWRConfiguration
): SWRResponse<T, Error> {
  return useSWR<T, Error>(
    key,
    key ? (url: string) => apiClient.get<T>(url) : null,
    { ...defaultSWRConfig, ...config }
  );
}

// 캐시 무효화 함수들
export { mutate } from "swr";
export const invalidatePattern = async (pattern: string): Promise<void> => {
  const { mutate } = await import("swr");
  await mutate(
    (key) => typeof key === "string" && key.includes(pattern),
    undefined,
    { revalidate: true }
  );
};
