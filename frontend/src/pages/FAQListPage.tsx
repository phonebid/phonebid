import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { customerService } from "services/customerService";
import { toast } from "react-toastify";
import type { FaqResponseDto, Page, FaqCategory } from "types/CustomerServiceTypes";
import { FAQ_CATEGORY, FAQ_CATEGORY_LABELS } from "types/CustomerServiceTypes";
import { getErrorMessage, logError } from "utils/errorUtils";

const FAQListPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedCategory, setSelectedCategory] = useState<FaqCategory | null>(null);
  const [faqsPage, setFaqsPage] = useState<Page<FaqResponseDto>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
    first: true,
    last: true,
  });

  useEffect(() => {
    loadFaqs();
  }, [currentPage, selectedCategory]);

  const loadFaqs = async () => {
    try {
      setIsLoading(true);
      const data = await customerService.getFaqs(selectedCategory, currentPage, 10);
      setFaqsPage(data);
    } catch (error: unknown) {
      const err = error instanceof Error ? error : new Error(String(error));
      logError("FAQ 목록 조회 실패:", err);
      const errorMessage = getErrorMessage(error);
      toast.error(`FAQ 목록을 불러오는데 실패했습니다: ${errorMessage}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/customer-service");
  };

  const handleFaqClick = (faqId: string) => {
    navigate(`/mypage/customer-service/faqs/${faqId}`);
  };

  const handleCategoryChange = (category: FaqCategory | null) => {
    setSelectedCategory(category);
    setCurrentPage(0);
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < faqsPage.totalPages) {
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
            고객센터 - FAQ
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 카테고리 필터 */}
        <div className="mb-4 flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
          <button
            onClick={() => handleCategoryChange(null)}
            className={`px-3 py-1 rounded-lg text-sm whitespace-nowrap ${
              selectedCategory === null
                ? "bg-black text-white"
                : "bg-white text-gray-700 border border-gray-300"
            }`}
          >
            전체
          </button>
          {Object.values(FAQ_CATEGORY).map((category) => (
            <button
              key={category}
              onClick={() => handleCategoryChange(category)}
              className={`px-3 py-1 rounded-lg text-sm whitespace-nowrap ${
                selectedCategory === category
                  ? "bg-black text-white"
                  : "bg-white text-gray-700 border border-gray-300"
              }`}
            >
              {FAQ_CATEGORY_LABELS[category]}
            </button>
          ))}
        </div>

        {/* FAQ 목록 */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
          {isLoading ? (
            <div className="text-center py-8 text-gray-500">로딩 중...</div>
          ) : faqsPage.content.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              등록된 FAQ가 없습니다.
            </div>
          ) : (
            <>
              <div className="space-y-3 mb-4">
                {faqsPage.content.map((faq) => (
                  <div
                    key={faq.id}
                    onClick={() => handleFaqClick(faq.id)}
                    className="bg-gray-50 rounded-lg p-4 cursor-pointer hover:bg-gray-100 transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 rounded inline-block mb-2">
                          {FAQ_CATEGORY_LABELS[faq.category]}
                        </div>
                        <div className="font-medium text-gray-900">
                          {faq.question}
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
              {faqsPage.totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 pt-4 border-t border-gray-200">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={faqsPage.first}
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
                    {currentPage + 1}/{faqsPage.totalPages}
                  </span>
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={faqsPage.last}
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
      </div>
    </div>
  );
};

export default FAQListPage;

