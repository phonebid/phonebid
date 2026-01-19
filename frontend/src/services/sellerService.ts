import { apiClient } from "./apiClient";
import { toast } from "react-toastify";
import type { SellerRegisterRequestDto } from "types/SellerTypes";
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
};

