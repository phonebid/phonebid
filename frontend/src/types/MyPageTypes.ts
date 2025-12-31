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

export interface AccountCreateRequestDto {
  bankName: string;
  accountNumber: string;
  accountHolderName: string;
}

export interface AccountResponseDto {
  accountId: string;
  bankName: string;
  accountNumber: string;
  accountHolderName: string;
  createdAt: string;
}

export interface DeliveryAddressCreateRequestDto {
  addressName: string;
  recipientName: string;
  phone: string;
  postalCode: string;
  address: string;
  detailAddress?: string;
  saveAsDefault?: boolean;
}

export interface DeliveryAddressResponseDto {
  addressId: string;
  addressName: string;
  recipientName: string;
  phone: string;
  postalCode: string;
  address: string;
  detailAddress: string | null;
  isDefault: boolean;
  createdAt: string;
}

export const BANK_LIST = [
  "KB국민은행",
  "신한은행",
  "하나은행",
  "우리은행",
  "NH농협은행",
  "IBK기업은행",
  "카카오뱅크",
  "토스뱅크",
  "KEB하나은행",
  "SC제일은행",
  "한국씨티은행",
  "KDB산업은행",
  "저축은행",
  "우체국",
  "수협은행",
] as const;

