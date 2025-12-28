// 견적 생성 플로우에서 사용하는 타입/상수 정의

import { PhoneOptionResponse } from "./PhoneModelTypes";

export type Carrier = "SKT" | "KT" | "LGU" | "SKT_ALD" | "KT_ALD" | "LGU_ALD" | "ANY";
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
  storage: string | null; // nullable: 상관없음 선택 시
  color: string | null; // nullable: 상관없음 선택 시
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
  lowestPrice: number | null;
}

export interface QuoteListItem {
  id: string;
  phoneModel: {
    id: string;
    brand: string;
    model: string;
  };
  storage: PhoneOptionResponse | null;
  color: PhoneOptionResponse | null;
  carrier: Carrier;
  status: "OPEN" | "CLOSED" | "CONTRACTED";
  expiredAt: string;
  purchaseMethod: PurchaseMethod;
  currentCarrier?: Carrier;
  activationMethod: ActivationMethod;
  createdAt: string;
  bidCount: number | null;
  lowestPrice: number | null;
}

export interface BidListItem {
  id: string;
  sellerId: string;
  sellerStoreName: string;
  sellerRating: number | null;
  installmentPrincipal: number;
  totalMaintenanceCost: number;
  pricePlanName: string | null;
  pricePlanPrice: number | null;
  status: "ACTIVE" | "SELECTED" | "REJECTED" | "WITHDRAWN";
  createdAt: string;
}

export const DEFAULT_EXPIRED_HOURS = 24;
