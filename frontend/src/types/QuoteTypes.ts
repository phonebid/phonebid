// 견적 생성 플로우에서 사용하는 타입/상수 정의

import { PhoneOptionResponse } from "./PhoneModelTypes";

export type Carrier = "SKT" | "KT" | "LGU" | "SKT_ALD" | "KT_ALD" | "LGU_ALD";
export type PurchaseMethod =
  | "NUMBER_TRANSFER"
  | "DEVICE_CHANGE"
  | "NEW_SUBSCRIPTION"
  | "LOWEST_PRICE"
  | "ANY";
export type ActivationMethod = "COMMON_SUBSIDY" | "SELECTIVE_SUBSIDY" | "ANY";
export interface QuoteDraft {
  // Step 1: 기기 선택
  model?: string; // PhoneModelResponse.id
  storage?: PhoneOptionResponse; // PhoneOptionResponse.optionValue
  color?: PhoneOptionResponse; // PhoneOptionResponse.optionValue

  // Step 2: 통신/구매 옵션
  carrier?: Carrier;
  currentCarrier?: Carrier; // 번호이동 시 필요
  purchaseMethod?: PurchaseMethod;
  activationMethod?: ActivationMethod;
}

export interface QuoteCreateRequestDto {
  phoneModelId: string;
  storageOptionId?: string;
  colorOptionId?: string;
  carrier: Carrier;
  purchaseMethod: PurchaseMethod;
  activationMethod: ActivationMethod;
  currentCarrier?: Carrier;
}

export interface QuoteSummary {
  id: string;
  model: string;
  storage: string;
  color: string;
  carrier: Carrier;
  expiredAt: string;
  createdAt: string;
}

export interface QuoteDetail extends QuoteSummary {
  status: "OPEN" | "CLOSED" | "CONTRACTED";
  purchaseMethod: PurchaseMethod;
  activationMethod: ActivationMethod;
  currentCarrier?: Carrier;
  bidCount: number;
}

export const DEFAULT_EXPIRED_HOURS = 24;
