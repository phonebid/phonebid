export interface AddressDto {
  postalCode: string;
  address: string;
  detailAddress?: string;
}

export interface SettlementAccountDto {
  bankName: string;
  accountNumber: string;
  accountHolderName: string;
}

export interface SellerRegisterRequestDto {
  // 사업자 정보
  businessNumber: string;
  businessLicenseFileUrl: string;
  storeName: string;
  representativeName: string;
  isAgent: boolean;
  businessAddress: AddressDto;
  
  // 사전승낙서 정보
  storeAddress: AddressDto;
  consentNumber?: string;
  consentFormFileUrl?: string;
  
  // 연락처 정보
  representativePhone: string;
  email: string;
  customerServicePhone?: string;
  
  // 정산 계좌 정보
  settlementAccount: SettlementAccountDto;
  
  // 회원 정보
  userInfo: {
    username: string;
    password: string;
    name: string;
    nickname: string;
  };
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

export type BankName = typeof BANK_LIST[number];

