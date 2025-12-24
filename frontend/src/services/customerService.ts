import { apiClient } from "./apiClient";
import type {
  InquiryCreateRequestDto,
  InquiryResponseDto,
  InquiryDetailResponseDto,
  NoticeResponseDto,
  NoticeDetailResponseDto,
  FaqResponseDto,
  FaqDetailResponseDto,
  Page,
  FaqCategory,
} from "types/CustomerServiceTypes";

export const customerService = {
  // 1:1 문의
  createInquiry: async (
    data: InquiryCreateRequestDto
  ): Promise<void> => {
    return await apiClient.post<void>(
      "/mypage/customerservice/inquiries",
      data
    );
  },

  getMyInquiries: async (
    page: number = 0,
    size: number = 10
  ): Promise<Page<InquiryResponseDto>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    return await apiClient.get<Page<InquiryResponseDto>>(
      `/mypage/customerservice/inquiries/my?${params.toString()}`
    );
  },

  getInquiryDetail: async (
    inquiryId: string
  ): Promise<InquiryDetailResponseDto> => {
    return await apiClient.get<InquiryDetailResponseDto>(
      `/mypage/customerservice/inquiries/${inquiryId}`
    );
  },

  // 공지사항
  getNotices: async (
    page: number = 0,
    size: number = 10
  ): Promise<Page<NoticeResponseDto>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    return await apiClient.get<Page<NoticeResponseDto>>(
      `/mypage/customerservice/notices?${params.toString()}`
    );
  },

  getNoticeDetail: async (
    noticeId: string
  ): Promise<NoticeDetailResponseDto> => {
    return await apiClient.get<NoticeDetailResponseDto>(
      `/mypage/customerservice/notices/${noticeId}`
    );
  },

  // FAQ
  getFaqs: async (
    category: FaqCategory | null = null,
    page: number = 0,
    size: number = 10
  ): Promise<Page<FaqResponseDto>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (category) {
      params.append("category", category);
    }
    return await apiClient.get<Page<FaqResponseDto>>(
      `/mypage/customerservice/faqs?${params.toString()}`
    );
  },

  getFaqDetail: async (faqId: string): Promise<FaqDetailResponseDto> => {
    return await apiClient.get<FaqDetailResponseDto>(
      `/mypage/customerservice/faqs/${faqId}`
    );
  },
};

