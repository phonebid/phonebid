import { apiClient } from "./apiClient";
import { toast } from "react-toastify";
import type {
  ProfileResponseDto,
  ProfileUpdateRequestDto,
  PurchaseHistoryResponseDto,
  PurchaseDetailResponseDto,
  AccountCreateRequestDto,
  AccountResponseDto,
  DeliveryAddressCreateRequestDto,
  DeliveryAddressResponseDto,
  Page,
} from "types/MyPageTypes";
import { logError } from "utils/errorUtils";

// API 엔드포인트 상수화
const ENDPOINTS = {
  PROFILE: "/mypage/profile",
  PURCHASES: "/mypage/purchases",
  ACCOUNTS: "/mypage/accounts",
  DELIVERY_ADDRESSES: "/mypage/delivery-addresses",
} as const;

export const mypageService = {
  getProfile: async (): Promise<ProfileResponseDto> => {
    try {
      return await apiClient.get<ProfileResponseDto>(ENDPOINTS.PROFILE);
    } catch (error: unknown) {
      logError("프로필 조회 실패:", error);
      toast.error("프로필 정보를 불러오는데 실패했습니다.");
      throw error;
    }
  },

  updateProfile: async (
    data: ProfileUpdateRequestDto
  ): Promise<void> => {
    try {
      return await apiClient.put<void>(ENDPOINTS.PROFILE, data);
    } catch (error: unknown) {
      logError("프로필 수정 실패:", error);
      toast.error("프로필 수정에 실패했습니다.");
      throw error;
    }
  },

  getPurchaseHistory: async (
    status: "COMPLETED" | "CANCELLED" = "COMPLETED",
    page: number = 0,
    size: number = 10
  ): Promise<Page<PurchaseHistoryResponseDto>> => {
    try {
      const params = new URLSearchParams({
        status,
        page: page.toString(),
        size: size.toString(),
      });
      return await apiClient.get<Page<PurchaseHistoryResponseDto>>(
        `${ENDPOINTS.PURCHASES}?${params.toString()}`
      );
    } catch (error: unknown) {
      logError("구매내역 조회 실패:", error);
      toast.error("구매내역을 불러오는데 실패했습니다.");
      throw error;
    }
  },

  getPurchaseDetail: async (
    contractId: string
  ): Promise<PurchaseDetailResponseDto> => {
    try {
      return await apiClient.get<PurchaseDetailResponseDto>(
        `${ENDPOINTS.PURCHASES}/${contractId}`
      );
    } catch (error: unknown) {
      logError("구매내역 상세 조회 실패:", error);
      toast.error("구매내역 상세 정보를 불러오는데 실패했습니다.");
      throw error;
    }
  },

  createAccount: async (
    data: AccountCreateRequestDto
  ): Promise<void> => {
    try {
      return await apiClient.post<void>(ENDPOINTS.ACCOUNTS, data);
    } catch (error: unknown) {
      logError("계좌 등록 실패:", error);
      toast.error("계좌 등록에 실패했습니다.");
      throw error;
    }
  },

  getAccounts: async (
    page: number = 0,
    size: number = 10
  ): Promise<Page<AccountResponseDto>> => {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      return await apiClient.get<Page<AccountResponseDto>>(
        `${ENDPOINTS.ACCOUNTS}?${params.toString()}`
      );
    } catch (error: unknown) {
      logError("계좌 목록 조회 실패:", error);
      toast.error("계좌 목록을 불러오는데 실패했습니다.");
      throw error;
    }
  },

  deleteAccount: async (accountId: string): Promise<void> => {
    try {
      return await apiClient.delete<void>(`${ENDPOINTS.ACCOUNTS}/${accountId}`);
    } catch (error: unknown) {
      logError("계좌 삭제 실패:", error);
      toast.error("계좌 삭제에 실패했습니다.");
      throw error;
    }
  },

  getDefaultDeliveryAddress: async (): Promise<DeliveryAddressResponseDto | null> => {
    try {
      return await apiClient.get<DeliveryAddressResponseDto>(
        `${ENDPOINTS.DELIVERY_ADDRESSES}/default`
      );
    } catch (error: unknown) {
      // 기본 배송지가 없는 경우 404 에러가 발생할 수 있으므로 null 반환
      const errorCode = (error as { response?: { status?: number } })?.response?.status;
      if (errorCode === 404) {
        return null;
      }
      logError("기본 배송지 조회 실패:", error);
      toast.error("기본 배송지를 불러오는데 실패했습니다.");
      throw error;
    }
  },

  createDeliveryAddress: async (
    data: DeliveryAddressCreateRequestDto
  ): Promise<void> => {
    try {
      return await apiClient.post<void>(ENDPOINTS.DELIVERY_ADDRESSES, data);
    } catch (error: unknown) {
      logError("배송지 등록 실패:", error);
      toast.error("배송지 등록에 실패했습니다.");
      throw error;
    }
  },
};

