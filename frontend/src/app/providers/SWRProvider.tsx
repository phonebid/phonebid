import React from "react";
import { SWRConfig } from "swr";
import { defaultSWRConfig } from "lib/swrConfig";

interface SWRProviderProps {
  children: React.ReactNode;
}

/**
 * SWR Provider 컴포넌트
 *
 * 애플리케이션 전체에 SWR 설정을 제공합니다.
 * App.tsx에서 최상위 레벨에 적용해야 합니다.
 */
export const SWRProvider: React.FC<SWRProviderProps> = ({ children }) => {
  return <SWRConfig value={defaultSWRConfig}>{children}</SWRConfig>;
};

export default SWRProvider;
