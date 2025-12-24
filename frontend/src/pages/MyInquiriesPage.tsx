import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { customerService } from "services/customerService";
import { toast } from "react-toastify";
import type {
  InquiryResponseDto,
  Page,
} from "types/CustomerServiceTypes";
import {
  INQUIRY_CATEGORY_LABELS,
  INQUIRY_STATUS_LABELS,
} from "types/CustomerServiceTypes";
import { formatDateSimple } from "utils/formatters";
import { getErrorMessage, logError } from "utils/errorUtils";

const MyInquiriesPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [inquiriesPage, setInquiriesPage] = useState<Page<InquiryResponseDto>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
    first: true,
    last: true,
  });

  useEffect(() => {
    loadInquiries();
  }, [currentPage]);

  const loadInquiries = async () => {
    try {
      setIsLoading(true);
      const data = await customerService.getMyInquiries(currentPage, 10);
      setInquiriesPage(data);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : String(error);
      logError("문의 목록 조회 실패:", error);
      toast.error(`문의 목록을 불러오는데 실패했습니다: ${errorMessage}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/customer-service");
  };

  const handleInquiryClick = (inquiryId: string) => {
    navigate(`/mypage/customer-service/inquiries/${inquiryId}`);
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < inquiriesPage.totalPages) {
      setCurrentPage(newPage);
    }
  };

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
            나의 문의내역
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 문의 목록 */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
          {isLoading ? (
            <div className="text-center py-8 text-gray-500">로딩 중...</div>
          ) : inquiriesPage.content.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              등록된 문의가 없습니다.
            </div>
          ) : (
            <>
              <div className="space-y-3 mb-4">
                {inquiriesPage.content.map((inquiry) => (
                  <div
                    key={inquiry.id}
                    onClick={() => handleInquiryClick(inquiry.id)}
                    className="bg-gray-50 rounded-lg p-4 cursor-pointer hover:bg-gray-100 transition-colors"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
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
                        <div className="font-medium text-gray-900 mb-1">
                          {inquiry.title}
                        </div>
                        <div className="text-xs text-gray-500">
                          {formatDateSimple(inquiry.createdAt)}
                        </div>
                      </div>
                      <svg
                        className="w-5 h-5 text-gray-400 ml-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M9 5l7 7-7 7"
                        />
                      </svg>
                    </div>
                  </div>
                ))}
              </div>

              {/* 페이징 컨트롤 */}
              {inquiriesPage.totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 pt-4 border-t border-gray-200">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={inquiriesPage.first}
                    className="bg-gray-100 rounded-lg p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                    aria-label="이전 페이지"
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
                  <span className="text-sm text-gray-700">
                    {currentPage + 1}/{inquiriesPage.totalPages}
                  </span>
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={inquiriesPage.last}
                    className="bg-gray-100 rounded-lg p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                    aria-label="다음 페이지"
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
                        d="M9 5l7 7-7 7"
                      />
                    </svg>
                  </button>
                </div>
              )}
            </>
          )}
        </div>

        {/* 문의 작성 버튼 */}
        <button
          onClick={() => navigate("/mypage/customer-service/inquiry")}
          className="w-full mt-4 px-4 py-3 bg-indigo-500 text-white rounded-lg font-medium hover:bg-indigo-600 transition-colors"
        >
          새 문의 작성
        </button>
      </div>
    </div>
  );
};

export default MyInquiriesPage;

