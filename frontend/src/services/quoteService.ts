import { apiClient } from "services/apiClient";
import type {
  QuoteCreateRequestDto,
  QuoteDetail,
  QuoteListItem,
  BidListItem,
  BidDetail,
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
  pricePlanId: string | null;
  pricePlanName: string | null;
  pricePlanPrice: number | null;
  pricePlanCategory: "FIVE_G" | "LTE" | null;
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
    lowestPrice: response.lowestPrice ?? null,
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
    pricePlanId: item.pricePlanId,
    pricePlanName: item.pricePlanName,
    pricePlanPrice: item.pricePlanPrice,
    pricePlanCategory: item.pricePlanCategory,
    status: item.status,
    createdAt: item.createdAt,
  }));
};

export const closeQuote = async (quoteId: string): Promise<void> => {
  await apiClient.put(`${BASE_URL}/${quoteId}/close`);
};

interface BidResponseDto {
  id: string;
  quoteId: string;
  sellerId: string;
  sellerStoreName: string;
  sellerRating: number | null;
  price: number;
  installmentPrincipal: number;
  additionalSubsidy: number | null;
  totalMaintenanceCost: number;
  pricePlanId: string | null;
  pricePlanName: string | null;
  pricePlanPrice: number | null;
  pricePlanCategory: "FIVE_G" | "LTE" | null;
  pricePlanDataAllowance: string | null;
  pricePlanThrottleSpeed: string | null;
  pricePlanVoiceSms: string | null;
  additionalServices: Array<{
    id: string;
    serviceName: string;
    servicePrice: number;
    description: string | null;
    mandatory: boolean;
    cancellableAfterMonths: number | null;
    cancellableDescription: string | null;
  }>;
  additionalServicesCount: number;
  additionalServicesTotalPrice: number;
  purchaseMethod: QuoteDetail["purchaseMethod"];
  carrier: QuoteDetail["carrier"];
  currentCarrier: QuoteDetail["carrier"] | null;
  activationMethod: QuoteDetail["activationMethod"];
  contractMonths: number | null;
  deliveryDays: number;
  status: "ACTIVE" | "SELECTED" | "REJECTED" | "WITHDRAWN";
  createdAt: string;
}

export const getBidDetail = async (bidId: string): Promise<BidDetail> => {
  const response = await apiClient.get<BidResponseDto>(`/auction/bids/${bidId}`);
  
  return {
    id: response.id,
    quoteId: response.quoteId,
    sellerId: response.sellerId,
    sellerStoreName: response.sellerStoreName,
    sellerRating: response.sellerRating,
    price: response.price,
    installmentPrincipal: response.installmentPrincipal,
    additionalSubsidy: response.additionalSubsidy,
    totalMaintenanceCost: response.totalMaintenanceCost,
    pricePlanId: response.pricePlanId,
    pricePlanName: response.pricePlanName,
    pricePlanPrice: response.pricePlanPrice,
    pricePlanCategory: response.pricePlanCategory,
    pricePlanDataAllowance: response.pricePlanDataAllowance,
    pricePlanThrottleSpeed: response.pricePlanThrottleSpeed,
    pricePlanVoiceSms: response.pricePlanVoiceSms,
    additionalServices: response.additionalServices.map((service) => ({
      id: service.id,
      serviceName: service.serviceName,
      servicePrice: service.servicePrice,
      description: service.description,
      mandatory: service.mandatory,
      cancellableAfterMonths: service.cancellableAfterMonths,
      cancellableDescription: service.cancellableDescription,
    })),
    additionalServicesCount: response.additionalServicesCount,
    additionalServicesTotalPrice: response.additionalServicesTotalPrice,
    purchaseMethod: response.purchaseMethod,
    carrier: response.carrier,
    currentCarrier: response.currentCarrier,
    activationMethod: response.activationMethod,
    contractMonths: response.contractMonths,
    deliveryDays: response.deliveryDays,
    status: response.status,
    createdAt: response.createdAt,
  };
};