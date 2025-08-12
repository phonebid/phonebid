// 견적 생성 플로우에서 사용하는 타입/상수 정의

export type Carrier = "SKT" | "KT" | "LGU+";
export type PurchaseMethod = "NEW" | "NUMBER_TRANSFER" | "DEVICE_CHANGE";
export type ActivationMethod = "DEVICE_ONLY" | "SELECTIVE_SUBSIDY" | "CONTRACT";

export interface QuoteDraft {
  // Step 1: 기기 선택
  model?: string;
  storage?: string; // e.g., "128GB"
  color?: string;

  // Step 2: 통신/구매 옵션
  carrier?: Carrier;
  currentCarrier?: Carrier; // 번호이동 시 필요
  purchaseMethod?: PurchaseMethod;
  activationMethod?: ActivationMethod;

  // Step 3: 가격/마감
  hopePrice?: number; // 희망가(원)
  expiredHours?: number; // 마감까지 시간(기본 24)
}

export interface CreateQuoteRequest {
  model: string;
  storage: string;
  color: string;
  carrier: Carrier;
  purchaseMethod: PurchaseMethod;
  activationMethod: ActivationMethod;
  currentCarrier?: Carrier;
  hopePrice: number;
  expiredAt: string; // ISO datetime
}

export const DEFAULT_EXPIRED_HOURS = 24;
