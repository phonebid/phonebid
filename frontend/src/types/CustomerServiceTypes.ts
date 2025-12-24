export const INQUIRY_CATEGORY = {
  PAYMENT: "PAYMENT",
  DELIVERY: "DELIVERY",
  ACCOUNT: "ACCOUNT",
  PRODUCT: "PRODUCT",
  ETC: "ETC",
} as const;

export type InquiryCategory =
  typeof INQUIRY_CATEGORY[keyof typeof INQUIRY_CATEGORY];

export const INQUIRY_CATEGORY_LABELS: Record<InquiryCategory, string> = {
  [INQUIRY_CATEGORY.PAYMENT]: "결제",
  [INQUIRY_CATEGORY.DELIVERY]: "배송",
  [INQUIRY_CATEGORY.ACCOUNT]: "계정",
  [INQUIRY_CATEGORY.PRODUCT]: "상품",
  [INQUIRY_CATEGORY.ETC]: "기타",
};

export const INQUIRY_STATUS = {
  PENDING: "PENDING",
  ANSWERED: "ANSWERED",
  CLOSED: "CLOSED",
} as const;

export type InquiryStatus = typeof INQUIRY_STATUS[keyof typeof INQUIRY_STATUS];

export const INQUIRY_STATUS_LABELS: Record<InquiryStatus, string> = {
  [INQUIRY_STATUS.PENDING]: "대기중",
  [INQUIRY_STATUS.ANSWERED]: "답변완료",
  [INQUIRY_STATUS.CLOSED]: "종료",
};

export const FAQ_CATEGORY = {
  SERVICE: "SERVICE",
  PAYMENT: "PAYMENT",
  DELIVERY: "DELIVERY",
  ACCOUNT: "ACCOUNT",
  PRODUCT: "PRODUCT",
  ETC: "ETC",
} as const;

export type FaqCategory = typeof FAQ_CATEGORY[keyof typeof FAQ_CATEGORY];

export const FAQ_CATEGORY_LABELS: Record<FaqCategory, string> = {
  [FAQ_CATEGORY.SERVICE]: "서비스 이용",
  [FAQ_CATEGORY.PAYMENT]: "결제/환불",
  [FAQ_CATEGORY.DELIVERY]: "배송",
  [FAQ_CATEGORY.ACCOUNT]: "계정",
  [FAQ_CATEGORY.PRODUCT]: "상품",
  [FAQ_CATEGORY.ETC]: "기타",
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

