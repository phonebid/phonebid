import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { customerService } from "services/customerService";
import { toast } from "react-toastify";
import type { InquiryDetailResponseDto } from "types/CustomerServiceTypes";
import {
  INQUIRY_CATEGORY_LABELS,
  INQUIRY_STATUS_LABELS,
} from "types/CustomerServiceTypes";
import { formatDateSimple } from "utils/formatters";

const InquiryDetailPage = () => {
  const navigate = useNavigate();
  const { inquiryId } = useParams<{ inquiryId: string }>();
  const [isLoading, setIsLoading] = useState(true);
  const [inquiry, setInquiry] = useState<InquiryDetailResponseDto | null>(null);

  useEffect(() => {
    if (inquiryId) {
      loadInquiryDetail();
    }
  }, [inquiryId]);

  const loadInquiryDetail = async () => {
    if (!inquiryId) return;

    try {
      setIsLoading(true);
      const data = await customerService.getInquiryDetail(inquiryId);
      setInquiry(data);
    } catch (error: any) {
      console.error("문의 상세 조회 실패:", error);
      toast.error("문의 상세 정보를 불러오는데 실패했습니다.");
      navigate("/mypage/customer-service/inquiries/my");
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/customer-service/inquiries/my");
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (!inquiry) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="flex items-center mb-8">
          <button
            onClick={handleBack}
            className="text-gray-700 hover:text-gray-900 mr-4"
          >
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
          <h1 className="text-xl font-bold text-gray-900 flex-1 text-center">
            문의 상세
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 문의 내용 */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200 mb-4">
          <div className="flex items-center gap-2 mb-4">
            <span className="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 rounded">
              {INQUIRY_CATEGORY_LABELS[inquiry.category]}
            </span>
            <span
              className={`text-xs px-2 py-1 rounded ${
                inquiry.status === "ANSWERED"
                  ? "bg-green-100 text-green-700"
                  : inquiry.status === "PENDING"
                  ? "bg-yellow-100 text-yellow-700"
                  : "bg-gray-100 text-gray-700"
              }`}
            >
              {INQUIRY_STATUS_LABELS[inquiry.status]}
            </span>
          </div>

          <h2 className="text-lg font-semibold text-gray-900 mb-2">
            {inquiry.title}
          </h2>

          <div className="text-sm text-gray-500 mb-4">
            {formatDateSimple(inquiry.createdAt)}
          </div>

          <div className="text-sm text-gray-700 whitespace-pre-wrap">
            {inquiry.content}
          </div>
        </div>

        {/* 답변 */}
        {inquiry.reply ? (
          <div className="bg-green-50 rounded-lg p-4 shadow-sm border border-green-200">
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-semibold text-green-800">답변</h3>
              <div className="text-xs text-green-600">
                {inquiry.reply.adminNickname || "관리자"} ·{" "}
                {formatDateSimple(inquiry.reply.createdAt)}
              </div>
            </div>
            <div className="text-sm text-gray-700 whitespace-pre-wrap">
              {inquiry.reply.content}
            </div>
          </div>
        ) : (
          <div className="bg-gray-50 rounded-lg p-4 shadow-sm border border-gray-200">
            <div className="text-center text-gray-500 text-sm">
              아직 답변이 없습니다.
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default InquiryDetailPage;

