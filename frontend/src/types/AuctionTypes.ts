import { PhoneModelResponse, PhoneOptionResponse } from "./PhoneModelTypes";

export interface Auction {
  id: number;
  product: string;
  model: string;
  capacity: string;
  color: string;
  carrier: string;
  condition: string;
  hopePrice: number;
  endTime: string;
  status: "ACTIVE" | "COMPLETED" | "CANCELLED";
  createdAt: string;
  updatedAt: string;
}

export interface CreateAuctionRequest {
  product: string;
  model: string;
  capacity: string;
  color: string;
  carrier: string;
  condition: string;
  hopePrice: number;
}

export interface Bid {
  id: number;
  auctionId: number;
  sellerId: number;
  sellerName: string;
  price: number;
  message: string;
  estimatedDelivery: string;
  createdAt: string;
}

export interface PlaceBidRequest {
  price: number;
  message: string;
  estimatedDelivery: string;
}

export interface Quote {
  id: string;
  phoneModel: PhoneModelResponse;
  storage: PhoneOptionResponse | null; // nullable: 상관없음 선택 시
  carrier: string;
  color: PhoneOptionResponse | null; // nullable: 상관없음 선택 시
  status: "OPEN" | "CLOSED" | "CONTRACTED";
  expiredAt: string;
  purchaseMethod?: string;
  currentCarrier?: string;
  activationMethod?: string;
  createdAt: string;
}

export interface QuoteListResponse {
  quotes: Quote[];
  totalCount: number;
  page: number;
  size: number;
}
