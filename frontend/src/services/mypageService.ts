import { apiClient } from "./apiClient";
import type {
  ProfileResponseDto,
  ProfileUpdateRequestDto,
  PurchaseHistoryResponseDto,
  PurchaseDetailResponseDto,
  AccountCreateRequestDto,
  AccountResponseDto,
  Page,
} from "types/MyPageTypes";

export const mypageService = {
  getProfile: async (): Promise<ProfileResponseDto> => {
    return await apiClient.get<ProfileResponseDto>("/mypage/profile");
  },

  updateProfile: async (
    data: ProfileUpdateRequestDto
  ): Promise<void> => {
    return await apiClient.put<void>("/mypage/profile", data);
  },

  getPurchaseHistory: async (
    status: "COMPLETED" | "CANCELLED" = "COMPLETED",
    page: number = 0,
    size: number = 10
  ): Promise<Page<PurchaseHistoryResponseDto>> => {
    const params = new URLSearchParams({
      status,
      page: page.toString(),
      size: size.toString(),
    });
    return await apiClient.get<Page<PurchaseHistoryResponseDto>>(
      `/mypage/purchases?${params.toString()}`
    );
  },

  getPurchaseDetail: async (
    contractId: string
  ): Promise<PurchaseDetailResponseDto> => {
    return await apiClient.get<PurchaseDetailResponseDto>(
      `/mypage/purchases/${contractId}`
    );
  },

  createAccount: async (
    data: AccountCreateRequestDto
  ): Promise<void> => {
    return await apiClient.post<void>("/mypage/accounts", data);
  },

  getAccounts: async (
    page: number = 0,
    size: number = 10
  ): Promise<Page<AccountResponseDto>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    return await apiClient.get<Page<AccountResponseDto>>(
      `/mypage/accounts?${params.toString()}`
    );
  },

  deleteAccount: async (accountId: string): Promise<void> => {
    return await apiClient.delete<void>(`/mypage/accounts/${accountId}`);
  },
};

