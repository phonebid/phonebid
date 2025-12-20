export enum InquiryCategory {
  PAYMENT = "PAYMENT",
  DELIVERY = "DELIVERY",
  ACCOUNT = "ACCOUNT",
  PRODUCT = "PRODUCT",
  ETC = "ETC",
}

export const INQUIRY_CATEGORY_LABELS: Record<InquiryCategory, string> = {
  [InquiryCategory.PAYMENT]: "결제",
  [InquiryCategory.DELIVERY]: "배송",
  [InquiryCategory.ACCOUNT]: "계정",
  [InquiryCategory.PRODUCT]: "상품",
  [InquiryCategory.ETC]: "기타",
};

export enum InquiryStatus {
  PENDING = "PENDING",
  ANSWERED = "ANSWERED",
  CLOSED = "CLOSED",
}

export const INQUIRY_STATUS_LABELS: Record<InquiryStatus, string> = {
  [InquiryStatus.PENDING]: "대기중",
  [InquiryStatus.ANSWERED]: "답변완료",
  [InquiryStatus.CLOSED]: "종료",
};

export enum FaqCategory {
  SERVICE = "SERVICE",
  PAYMENT = "PAYMENT",
  DELIVERY = "DELIVERY",
  ACCOUNT = "ACCOUNT",
  PRODUCT = "PRODUCT",
  ETC = "ETC",
}

export const FAQ_CATEGORY_LABELS: Record<FaqCategory, string> = {
  [FaqCategory.SERVICE]: "서비스 이용",
  [FaqCategory.PAYMENT]: "결제/환불",
  [FaqCategory.DELIVERY]: "배송",
  [FaqCategory.ACCOUNT]: "계정",
  [FaqCategory.PRODUCT]: "상품",
  [FaqCategory.ETC]: "기타",
};

export interface InquiryCreateRequestDto {
  category: InquiryCategory;
  title: string;
  content: string;
}

export interface InquiryResponseDto {
  id: string;
  category: InquiryCategory;
  title: string;
  status: InquiryStatus;
  createdAt: string;
  updatedAt: string;
}

export interface InquiryReplyDto {
  id: string;
  content: string;
  adminNickname: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface InquiryDetailResponseDto {
  id: string;
  category: InquiryCategory;
  title: string;
  content: string;
  status: InquiryStatus;
  createdAt: string;
  updatedAt: string;
  reply: InquiryReplyDto | null;
}

export interface NoticeResponseDto {
  id: string;
  title: string;
  isImportant: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface NoticeDetailResponseDto {
  id: string;
  title: string;
  content: string;
  isImportant: boolean;
  viewCount: number;
  adminNickname: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface FaqResponseDto {
  id: string;
  category: FaqCategory;
  question: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface FaqDetailResponseDto {
  id: string;
  category: FaqCategory;
  question: string;
  answer: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
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

