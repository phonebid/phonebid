import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getBidDetail, getQuoteDetail } from "services/quoteService";
import type { BidDetail, QuoteDetail } from "types/QuoteTypes";
import { Confetti } from "@/components/ui/confetti";
import { logError } from "utils/errorUtils";
import { toast } from "react-toastify";

const PurchaseCompletePage = () => {
  const navigate = useNavigate();
  const { quoteId, bidId } = useParams<{ quoteId: string; bidId: string }>();
  const [bid, setBid] = useState<BidDetail | null>(null);
  const [quote, setQuote] = useState<QuoteDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (quoteId && bidId) {
      loadData();
    }
  }, [quoteId, bidId]);

  const loadData = async () => {
    if (!quoteId || !bidId) return;

    try {
      setIsLoading(true);
      const [bidData, quoteData] = await Promise.all([
        getBidDetail(bidId),
        getQuoteDetail(quoteId),
      ]);
      setBid(bidData);
      setQuote(quoteData);
    } catch (error: unknown) {
      logError("견적 상세 조회 실패:", error);
      toast.error("견적 정보를 불러오는데 실패했습니다.");
      navigate("/mypage/quotes");
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoHome = () => {
    navigate("/");
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (!bid || !quote) {
    return null;
  }

  const modelDisplayName = `${quote.model} ${quote.storage ? quote.storage : ""}`.trim();

  return (
    <div className="min-h-screen bg-gray-50 relative">
      {/* Confetti 효과 */}
      <Confetti
        className="fixed left-0 top-0 z-50 w-full h-full pointer-events-none"
        manualstart={false}
        options={{
          particleCount: 150,
          spread: 90,
          origin: { y: 0.7 },
        }}
      />

      <div className="max-w-md mx-auto bg-white min-h-screen relative z-10">
        {/* 헤더 */}
        <div className="sticky top-0 bg-white border-b border-gray-200 z-10">
          <div className="flex items-center px-4 py-3">
            <div className="flex-1">
              <h1 className="text-sm text-gray-500">{modelDisplayName}</h1>
            </div>
          </div>
        </div>

        <div className="px-4 pt-4 pb-8 flex flex-col items-center justify-center min-h-[calc(100vh-80px)]">
          {/* 구매완료 제목 */}
          <h2 className="text-medium font-bold text-gray-900 mb-20">구매완료</h2>

          {/* 메시지 */}
          <div className="text-center mb-8">
            <p className="text-base text-gray-900 mb-2">구매가 완료되었습니다.</p>
            <p className="text-2xl font-bold text-gray-900 mb-2">{modelDisplayName}</p>
            <p className="text-xl font-bold text-gray-900">구매를 축하드립니다!</p>
          </div>

          {/* 이미지 플레이스홀더 */}
          <div className="w-48 h-48 bg-gray-200 rounded-full flex items-center justify-center mb-8">
            <span className="text-sm text-gray-500">img</span>
          </div>

          {/* 홈으로 이동하기 버튼 */}
          <button
            onClick={handleGoHome}
            className="w-full max-w-xs bg-gray-200 text-gray-900 text-sm font-semibold py-3 rounded-lg hover:bg-gray-300"
          >
            홈으로 이동하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default PurchaseCompletePage;

