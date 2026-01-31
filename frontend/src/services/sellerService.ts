import { apiClient } from "./apiClient";
import { toast } from "react-toastify";
import type { SellerRegisterRequestDto, SellerDashboardStats, BidCreateRequest, SellerProfileResponseDto } from "types/SellerTypes";
import type { QuoteListItem } from "types/QuoteTypes";
import { logError } from "utils/errorUtils";

export const sellerService = {
  registerSeller: async (data: SellerRegisterRequestDto): Promise<void> => {
    try {
      await apiClient.post<void>("/sellers/register", data);
      toast.success("판매자 회원가입이 완료되었습니다.");
    } catch (error: unknown) {
      logError("판매자 회원가입 실패:", error);
      throw error;
    }
  },

  uploadDocument: async (
    file: File,
    documentType: "BUSINESS_LICENSE" | "CONSENT_FORM"
  ): Promise<string> => {
    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("documentType", documentType);

      // 회원가입 단계에서는 임시 업로드 API 사용 (인증 불필요)
      const response = await apiClient.post<string>(
        "/sellers/documents/temp",
        formData
      );
      return response;
    } catch (error: unknown) {
      logError("문서 업로드 실패:", error);
      toast.error("문서 업로드에 실패했습니다.");
      throw error;
    }
  },

  getDashboardStats: async (): Promise<SellerDashboardStats> => {
    try {
      // TODO: 실제 API 엔드포인트로 교체 필요
      // 현재는 플레이스홀더 데이터 반환
      return {
        totalBidsSent: 89,
        inProgressTransactions: 34,
        completedTransactions: 124,
      };
    } catch (error: unknown) {
      logError("대시보드 통계 조회 실패:", error);
      toast.error("대시보드 통계를 불러오는데 실패했습니다.");
      throw error;
    }
  },

  getSellerQuotes: async (): Promise<QuoteListItem[]> => {
    try {
      const response = await apiClient.get<QuoteListItem[]>("/auction/quotes");
      return response;
    } catch (error: unknown) {
      logError("견적 목록 조회 실패:", error);
      toast.error("견적 목록을 불러오는데 실패했습니다.");
      throw error;
    }
  },

  createBid: async (data: BidCreateRequest): Promise<void> => {
    try {
      await apiClient.post<void>("/auction/bids", data);
      toast.success("견적이 성공적으로 전송되었습니다.");
    } catch (error: unknown) {
      logError("견적 생성 실패:", error);
      toast.error("견적 전송에 실패했습니다.");
      throw error;
    }
  },

  getSellerProfile: async (): Promise<SellerProfileResponseDto> => {
    try {
      const response = await apiClient.get<SellerProfileResponseDto>("/sellers/profile");
      return response;
    } catch (error: unknown) {
      logError("판매자 프로필 조회 실패:", error);
      throw error;
    }
  },
};

