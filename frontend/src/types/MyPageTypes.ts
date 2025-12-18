export interface ProfileResponseDto {
  username: string;
  nickname: string;
  phone: string | null;
  name: string;
}

export interface ProfileUpdateRequestDto {
  name?: string | null;
  nickname?: string | null;
  phone?: string | null;
}

export interface ProductInfoDto {
  brand: string;
  model: string;
  storage: string | null;
  color: string | null;
  carrier: string;
}

export interface PurchaseHistoryResponseDto {
  contractId: string;
  productName: string;
  transactionDate: string;
  productImageUrl: string | null;
  productInfo: ProductInfoDto;
  price: number;
  status: string;
  canReview: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface SellerInfoDto {
  sellerName: string;
  rating: number | null;
}

export interface PaymentInfoDto {
  method: string;
  paidAt: string;
}

export interface DeliveryInfoDto {
  status: string;
  trackingNumber: string | null;
}

export interface PurchaseDetailResponseDto {
  contractId: string;
  productName: string;
  transactionDate: string;
  productImageUrl: string | null;
  productInfo: ProductInfoDto;
  price: number;
  status: string;
  sellerInfo: SellerInfoDto;
  paymentInfo: PaymentInfoDto | null;
  deliveryInfo: DeliveryInfoDto | null;
  canReview: boolean;
}

