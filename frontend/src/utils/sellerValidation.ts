/**
 * 판매자 회원가입 폼 검증 유틸리티
 */

export interface Step1Data {
  isAgent: boolean;
  businessNumber: string;
  businessLicenseFile: File | null;
  businessLicenseFileUrl: string;
  storeName: string;
  representativeName: string;
  businessPostalCode: string;
  businessAddress: string;
  businessDetailAddress: string;
  storePostalCode: string;
  storeAddress: string;
  storeDetailAddress: string;
  consentNumber: string;
  consentFormFile: File | null;
  consentFormFileUrl: string;
  representativePhone: string;
  email: string;
  customerServicePhone: string;
  bankName: string;
  accountNumber: string;
  accountHolderName: string;
}

export interface Step2Data {
  username: string;
  password: string;
  confirmPassword: string;
  name: string;
  nickname: string;
  termsOfService: boolean;
  privacyPolicy: boolean;
}

export interface ValidationErrors {
  [key: string]: string;
}

/**
 * 1단계 (사업자 정보) 검증
 */
export const validateStep1 = (
  step1Data: Step1Data
): { isValid: boolean; errors: ValidationErrors } => {
  const errors: ValidationErrors = {};

  if (!step1Data.businessNumber.trim()) {
    errors.businessNumber = "사업자등록번호를 입력해주세요.";
  } else if (!/^\d{3}-\d{2}-\d{5}$/.test(step1Data.businessNumber)) {
    errors.businessNumber =
      "사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)";
  }

  if (!step1Data.businessLicenseFileUrl) {
    errors.businessLicenseFile = "사업자등록증을 첨부해주세요.";
  }

  if (!step1Data.storeName.trim()) {
    errors.storeName = "상호명을 입력해주세요.";
  }

  if (!step1Data.representativeName.trim()) {
    errors.representativeName = "대표자명을 입력해주세요.";
  }

  if (!step1Data.businessPostalCode) {
    errors.businessAddress = "사업장 주소를 검색해주세요.";
  }

  if (!step1Data.isAgent) {
    if (!step1Data.storePostalCode) {
      errors.storeAddress = "판매점 주소를 검색해주세요.";
    }
    if (!step1Data.consentNumber.trim()) {
      errors.consentNumber = "승낙번호를 입력해주세요.";
    }
    if (!step1Data.consentFormFileUrl) {
      errors.consentFormFile = "사전승낙서를 첨부해주세요.";
    }
  }

  if (!step1Data.representativePhone.trim()) {
    errors.representativePhone = "대표 전화번호를 입력해주세요.";
  } else if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(step1Data.representativePhone)) {
    errors.representativePhone = "전화번호 형식이 올바르지 않습니다.";
  }

  if (!step1Data.email.trim()) {
    errors.email = "이메일 주소를 입력해주세요.";
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(step1Data.email)) {
    errors.email = "올바른 이메일 형식이 아닙니다.";
  }

  if (!step1Data.bankName) {
    errors.bankName = "은행을 선택해주세요.";
  }

  if (!step1Data.accountNumber.trim()) {
    errors.accountNumber = "계좌번호를 입력해주세요.";
  } else if (!/^\d+$/.test(step1Data.accountNumber)) {
    errors.accountNumber = "계좌번호는 숫자만 입력 가능합니다.";
  }

  if (!step1Data.accountHolderName.trim()) {
    errors.accountHolderName = "예금주명을 입력해주세요.";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};

/**
 * 2단계 (회원 정보) 검증
 */
export const validateStep2 = (
  step2Data: Step2Data
): { isValid: boolean; errors: ValidationErrors } => {
  const errors: ValidationErrors = {};

  if (!step2Data.username.trim()) {
    errors.username = "아이디를 입력해주세요.";
  } else if (!/^[a-z0-9]+$/.test(step2Data.username)) {
    errors.username = "아이디는 알파벳 소문자와 숫자로만 구성되어야 합니다.";
  } else if (
    step2Data.username.length < 4 ||
    step2Data.username.length > 10
  ) {
    errors.username = "아이디는 4자 이상 10자 이하여야 합니다.";
  }

  if (!step2Data.password.trim()) {
    errors.password = "비밀번호를 입력해주세요.";
  } else if (
    step2Data.password.length < 8 ||
    step2Data.password.length > 20
  ) {
    errors.password = "비밀번호는 8자 이상 20자 이하여야 합니다.";
  }

  if (step2Data.password !== step2Data.confirmPassword) {
    errors.confirmPassword = "비밀번호가 일치하지 않습니다.";
  }

  if (!step2Data.name.trim()) {
    errors.name = "이름을 입력해주세요.";
  }

  if (!step2Data.nickname.trim()) {
    errors.nickname = "닉네임을 입력해주세요.";
  } else if (
    step2Data.nickname.length < 2 ||
    step2Data.nickname.length > 10
  ) {
    errors.nickname = "닉네임은 2자 이상 10자 이하여야 합니다.";
  } else if (!/^[가-힣a-zA-Z0-9_-]+$/.test(step2Data.nickname)) {
    errors.nickname = "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다.";
  }

  if (!step2Data.termsOfService || !step2Data.privacyPolicy) {
    errors.agreements = "이용약관 및 개인정보처리방침에 동의해주세요.";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};

