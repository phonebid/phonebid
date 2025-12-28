import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getQuoteDetail, getBidsByQuoteId, closeQuote } from "services/quoteService";
import type { QuoteDetail, BidListItem } from "types/QuoteTypes";
import {
  formatPrice,
  getCarrierDisplayName,
  getPurchaseMethodDisplayName,
  calculateRemainingTime,
  sortBidsByMaintenanceCost,
} from "utils/quoteUtils";
import { formatDateSimple } from "utils/formatters";
import { logError } from "utils/errorUtils";
import { toast } from "react-toastify";

const QuoteDetailPage = () => {
  const navigate = useNavigate();
  const { quoteId } = useParams<{ quoteId: string }>();
  const [quote, setQuote] = useState<QuoteDetail | null>(null);
  const [bids, setBids] = useState<BidListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [remainingTime, setRemainingTime] = useState<string>("00:00:00");
  const [isClosing, setIsClosing] = useState(false);

  useEffect(() => {
    if (quoteId) {
      loadData();
    }
  }, [quoteId]);

  useEffect(() => {
    if (!quote) return;

    const updateRemainingTime = () => {
      setRemainingTime(calculateRemainingTime(quote.expiredAt));
    };

    updateRemainingTime();
    const interval = setInterval(updateRemainingTime, 1000);

    return () => clearInterval(interval);
  }, [quote]);

  const loadData = async () => {
    if (!quoteId) return;

    try {
      setIsLoading(true);
      const [quoteData, bidsData] = await Promise.all([
        getQuoteDetail(quoteId),
        getBidsByQuoteId(quoteId),
      ]);
      setQuote(quoteData);
      const sortedBids = sortBidsByMaintenanceCost(bidsData);
      setBids(sortedBids);
    } catch (error: unknown) {
      logError("견적 상세 조회 실패:", error);
      toast.error("견적 정보를 불러오는데 실패했습니다.");
      navigate("/mypage/quotes");
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/quotes");
  };

  const handleBidClick = (bidId: string) => {
    if (quoteId) {
      navigate(`/mypage/quotes/${quoteId}/bids/${bidId}`);
    }
  };

  const handleCloseQuote = async () => {
    if (!quoteId) return;

    if (!window.confirm("정말 견적을 그만받으시겠습니까?")) {
      return;
    }

    try {
      setIsClosing(true);
      await closeQuote(quoteId);
      toast.success("견적이 종료되었습니다.");
      navigate("/mypage/quotes");
    } catch (error: unknown) {
      logError("견적 종료 실패:", error);
      toast.error("견적 종료에 실패했습니다.");
    } finally {
      setIsClosing(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (!quote) {
    return null;
  }

  const modelDisplayName = `${quote.model} ${quote.storage ? quote.storage : ""}`.trim();

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto bg-white min-h-screen">
        {/* 헤더 */}
        <div className="sticky top-0 bg-white border-b border-gray-200 z-10">
          <div className="flex items-center px-4 py-3">
            <button
              onClick={handleBack}
              className="mr-3 text-gray-600 hover:text-gray-900"
            >
              <svg
                className="w-6 h-6"
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
            <h1 className="text-lg font-bold text-gray-900 flex-1">
              받은 견적
            </h1>
            <div className="w-9"></div>
          </div>
        </div>

        <div className="px-4 py-4 space-y-4">
          {/* Product Header Card */}
          <div className="bg-white rounded-lg p-4 mb-1">
            <div className="flex items-center justify-between gap-4">
              <div className="flex-1 flex flex-col justify-center">
                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                  {modelDisplayName}
                </h2>
                <div className="text-sm text-gray-500">
                  작성일 {formatDateSimple(quote.createdAt)}
                </div>
              </div>
              <div className="w-20 h-20 bg-gray-200 rounded-lg flex items-center justify-center flex-shrink-0">
                <svg
                  className="w-12 h-12 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
                  />
                </svg>
              </div>
            </div>
          </div>

          {/* Product Specifications Card */}
          <div className="bg-gray-100 rounded-lg p-4">
            <div className="flex items-center justify-between gap-4">
              <div className="flex items-center">
                <span className="text-xs text-gray-600 mr-2">통신사</span>
                <span className="text-xs text-gray-900">
                  {getCarrierDisplayName(quote.carrier)}
                </span>
              </div>
              <div className="flex items-center">
                <span className="text-xs text-gray-600 mr-2">색상</span>
                <span className="text-xs text-gray-900">
                  {quote.color ?? "상관없음"}
                </span>
              </div>
              <div className="flex items-center">
                <span className="text-xs text-gray-600 mr-2">용량</span>
                <span className="text-xs text-gray-900">
                  {quote.storage ?? "상관없음"}
                </span>
              </div>
            </div>
          </div>

          {/* Auction Details Card */}
          <div className="bg-gray-100 rounded-lg p-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">남은시간</span>
                <span className="text-sm font-bold text-gray-900">
                  {remainingTime}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">입찰 수</span>
                <span className="text-sm text-gray-900">{quote.bidCount}건</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">최저가격 (할부원금)</span>
                <span className="text-sm font-bold text-gray-900">
                  {formatPrice(quote.lowestPrice)}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">개통조건</span>
                <span className="text-sm text-gray-900">
                  {getPurchaseMethodDisplayName(quote.purchaseMethod)}
                </span>
              </div>
            </div>
          </div>

          {/* Quotes Section */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold text-gray-900">
                받은 견적 총 {bids.length}개
              </h3>
              <div className="flex items-center text-xs text-gray-600">
                <span>↓ 유지비 적은 순</span>
              </div>
            </div>

            {bids.length === 0 ? (
              <div className="bg-gray-100 rounded-lg p-8 text-center">
                <div className="text-gray-500">받은 견적이 없습니다.</div>
              </div>
            ) : (
              <div className="space-y-3">
                {bids.map((bid) => (
                  <div
                    key={bid.id}
                    onClick={() => handleBidClick(bid.id)}
                    className="bg-gray-100 rounded-lg p-4 border border-gray-200 cursor-pointer hover:bg-gray-200 transition-colors"
                  >
                    <div className="flex items-start gap-4">
                      <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center flex-shrink-0">
                        <span className="text-xs text-gray-500">IMG</span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-1">
                          <h4 className="text-sm font-semibold text-gray-900">
                            {bid.sellerStoreName}
                          </h4>
                          {bid.sellerRating !== null && (
                            <div className="flex items-center gap-1">
                              <svg
                                className="w-4 h-4 text-yellow-400"
                                fill="currentColor"
                                viewBox="0 0 20 20"
                              >
                                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                              </svg>
                              <span className="text-xs text-gray-700">
                                {bid.sellerRating.toFixed(1)}
                              </span>
                            </div>
                          )}
                        </div>
                        <div className="space-y-1">
                          <div className="flex items-center justify-between">
                            <span className="text-xs text-gray-600">할부원금</span>
                            <span className="text-sm font-bold text-gray-900">
                              {formatPrice(bid.installmentPrincipal)}
                            </span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-xs text-gray-600">총 유지비</span>
                            <span className="text-sm font-bold text-gray-900">
                              {formatPrice(bid.totalMaintenanceCost)}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* 하단 버튼 */}
          {quote.status === "OPEN" && (
            <div className="pt-4 pb-8">
              <button
                onClick={handleCloseQuote}
                disabled={isClosing}
                className="w-full bg-gray-200 text-gray-900 text-sm font-semibold py-3 rounded-lg hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isClosing ? "처리 중..." : "견적 그만받기"}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default QuoteDetailPage;

