import { apiClient } from "services/apiClient";
import type {
  QuoteCreateRequestDto,
  QuoteDetail,
  QuoteListItem,
  BidListItem,
} from "types/QuoteTypes";
import type { PhoneModelResponse, PhoneOptionResponse } from "types/PhoneModelTypes";
import type { Page } from "types/MyPageTypes";

const BASE_URL = "/auction/quotes";

interface QuoteResponseDto {
  id: string;
  phoneModel: PhoneModelResponse;
  storage: PhoneOptionResponse | null;
  color: PhoneOptionResponse | null;
  carrier: QuoteDetail["carrier"];
  status: "OPEN" | "CLOSED" | "CONTRACTED";
  expiredAt: string;
  createdAt: string;
  purchaseMethod: QuoteDetail["purchaseMethod"];
  activationMethod: QuoteDetail["activationMethod"];
  currentCarrier?: QuoteDetail["currentCarrier"];
  bidCount?: number | null;
  lowestPrice?: number | null;
}

interface BidListResponseDto {
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

export const createQuote = async (payload: QuoteCreateRequestDto) => {
  return await apiClient.post(BASE_URL, payload);
};

export const getQuoteDetail = async (quoteId: string): Promise<QuoteDetail> => {
  const response = await apiClient.get<QuoteResponseDto>(`${BASE_URL}/${quoteId}`);
  
  // 백엔드 응답을 프론트엔드 QuoteDetail 타입으로 변환
  return {
    id: response.id,
    model: response.phoneModel.model,
    storage: response.storage?.optionValue ?? null,
    color: response.color?.optionValue ?? null,
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

export const getMyOpenQuotes = async (
  page: number = 0,
  size: number = 10
): Promise<Page<QuoteListItem>> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });
  const response = await apiClient.get<Page<QuoteResponseDto>>(
    `${BASE_URL}/my?${params.toString()}`
  );
  
  return {
    ...response,
    content: response.content.map((item) => ({
      id: item.id,
      phoneModel: {
        id: item.phoneModel.id,
        brand: item.phoneModel.brand,
        model: item.phoneModel.model,
      },
      storage: item.storage,
      color: item.color,
      carrier: item.carrier,
      status: item.status,
      expiredAt: item.expiredAt,
      purchaseMethod: item.purchaseMethod,
      currentCarrier: item.currentCarrier,
      activationMethod: item.activationMethod,
      createdAt: item.createdAt,
      bidCount: item.bidCount ?? null,
      lowestPrice: item.lowestPrice ?? null,
    })),
  };
};

export const getMyCompletedQuotes = async (
  page: number = 0,
  size: number = 10
): Promise<Page<QuoteListItem>> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });
  const response = await apiClient.get<Page<QuoteResponseDto>>(
    `${BASE_URL}/my/completed?${params.toString()}`
  );
  
  return {
    ...response,
    content: response.content.map((item) => ({
      id: item.id,
      phoneModel: {
        id: item.phoneModel.id,
        brand: item.phoneModel.brand,
        model: item.phoneModel.model,
      },
      storage: item.storage,
      color: item.color,
      carrier: item.carrier,
      status: item.status,
      expiredAt: item.expiredAt,
      purchaseMethod: item.purchaseMethod,
      currentCarrier: item.currentCarrier,
      activationMethod: item.activationMethod,
      createdAt: item.createdAt,
      bidCount: item.bidCount ?? null,
      lowestPrice: item.lowestPrice ?? null,
    })),
  };
};

export const getBidsByQuoteId = async (
  quoteId: string
): Promise<BidListItem[]> => {
  const response = await apiClient.get<BidListResponseDto[]>(
    `${BASE_URL}/${quoteId}/bids`
  );
  
  return response.map((item) => ({
    id: item.id,
    sellerId: item.sellerId,
    sellerStoreName: item.sellerStoreName,
    sellerRating: item.sellerRating,
    installmentPrincipal: item.installmentPrincipal,
    totalMaintenanceCost: item.totalMaintenanceCost,
    pricePlanName: item.pricePlanName,
    pricePlanPrice: item.pricePlanPrice,
    status: item.status,
    createdAt: item.createdAt,
  }));
};
