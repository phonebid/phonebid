import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { customerService } from "services/customerService";
import { toast } from "react-toastify";
import type { FaqDetailResponseDto } from "types/CustomerServiceTypes";
import { FAQ_CATEGORY_LABELS } from "types/CustomerServiceTypes";

const FAQDetailPage = () => {
  const navigate = useNavigate();
  const { faqId } = useParams<{ faqId: string }>();
  const [isLoading, setIsLoading] = useState(true);
  const [faq, setFaq] = useState<FaqDetailResponseDto | null>(null);

  useEffect(() => {
    if (faqId) {
      loadFaqDetail();
    }
  }, [faqId]);

  const loadFaqDetail = async () => {
    if (!faqId) return;

    try {
      setIsLoading(true);
      const data = await customerService.getFaqDetail(faqId);
      setFaq(data);
    } catch (error: any) {
      console.error("FAQ 상세 조회 실패:", error);
      toast.error("FAQ 상세 정보를 불러오는데 실패했습니다.");
      navigate("/mypage/customer-service/faqs");
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/customer-service/faqs");
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (!faq) {
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
            고객센터 - FAQ
          </h1>
          <div className="w-9"></div>
        </div>

        {/* FAQ 상세 */}
        <div className="bg-gray-50 rounded-lg p-4 shadow-sm border border-gray-200">
          <div className="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 rounded inline-block mb-4">
            {FAQ_CATEGORY_LABELS[faq.category]}
          </div>

          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            {faq.question}
          </h2>

          <div className="text-sm text-gray-700 whitespace-pre-wrap">
            {faq.answer}
          </div>
        </div>
      </div>
    </div>
  );
};

export default FAQDetailPage;

