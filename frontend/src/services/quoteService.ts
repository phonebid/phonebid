import { apiClient } from "services/apiClient";
import type { QuoteCreateRequestDto, QuoteDetail } from "types/QuoteTypes";
import type { PhoneModelResponse, PhoneOptionResponse } from "types/PhoneModelTypes";

const BASE_URL = "/auction/quotes";

interface QuoteResponseDto {
  id: string;
  phoneModel: PhoneModelResponse;
  storage: PhoneOptionResponse;
  color: PhoneOptionResponse;
  carrier: QuoteDetail["carrier"];
  status: "OPEN" | "CLOSED" | "CONTRACTED";
  expiredAt: string;
  createdAt: string;
  purchaseMethod: QuoteDetail["purchaseMethod"];
  activationMethod: QuoteDetail["activationMethod"];
  currentCarrier?: QuoteDetail["currentCarrier"];
  bidCount?: number;
}

export const createQuote = async (payload: QuoteCreateRequestDto) => {
  return await apiClient.post(BASE_URL, payload);
};

export const getQuoteDetail = async (quoteId: string): Promise<QuoteDetail> => {
  const response = await apiClient.get<QuoteResponseDto>(`${BASE_URL}/${quoteId}`);
  
  // 백엔드 응답을 프론트엔드 QuoteDetail 타입으로 변환
  return {
    id: response.id,
    model: response.phoneModel.model,
    storage: response.storage.optionValue,
    color: response.color.optionValue,
    carrier: response.carrier,
    status: response.status,
    expiredAt: response.expiredAt,
    createdAt: response.createdAt,
    purchaseMethod: response.purchaseMethod,
    activationMethod: response.activationMethod,
    currentCarrier: response.currentCarrier,
    bidCount: response.bidCount ?? 0,
  };
};
