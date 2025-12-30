import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getBidDetail, getBidsByQuoteId, getQuoteDetail } from "services/quoteService";
import type { BidDetail, BidListItem, QuoteDetail } from "types/QuoteTypes";
import {
  formatPrice,
  getCarrierDisplayName,
  getPurchaseMethodDisplayName,
  getActivationMethodDisplayName,
  sortBidsByMaintenanceCost,
} from "utils/quoteUtils";
import { logError } from "utils/errorUtils";
import { toast } from "react-toastify";

const BidDetailPage = () => {
  const navigate = useNavigate();
  const { quoteId, bidId } = useParams<{ quoteId: string; bidId: string }>();
  const [bid, setBid] = useState<BidDetail | null>(null);
  const [quote, setQuote] = useState<QuoteDetail | null>(null);
  const [otherBids, setOtherBids] = useState<BidListItem[]>([]);
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
      const [bidData, quoteData, bidsData] = await Promise.all([
        getBidDetail(bidId),
        getQuoteDetail(quoteId),
        getBidsByQuoteId(quoteId),
      ]);
      setBid(bidData);
      setQuote(quoteData);
      const sortedBids = sortBidsByMaintenanceCost(bidsData);
      const filteredBids = sortedBids.filter((b) => b.id !== bidId);
      setOtherBids(filteredBids);
    } catch (error: unknown) {
      logError("견적 상세 조회 실패:", error);
      toast.error("견적 정보를 불러오는데 실패했습니다.");
      navigate(`/mypage/quotes/${quoteId}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    if (quoteId) {
      navigate(`/mypage/quotes/${quoteId}`);
    } else {
      navigate("/mypage/quotes");
    }
  };

  const handleBidClick = (clickedBidId: string) => {
    if (quoteId) {
      navigate(`/mypage/quotes/${quoteId}/bids/${clickedBidId}`);
    }
  };

  const handlePurchase = () => {
    if (!quoteId || !bidId) return;
    navigate(`/mypage/quotes/${quoteId}/bids/${bidId}/delivery`);
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
              견적 상세
            </h1>
            <div className="w-9"></div>
          </div>
        </div>

        <div className="px-4 py-4 space-y-4">
          {/* 판매자 정보 */}
          <div className="bg-white rounded-lg p-4 pb-0">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center flex-shrink-0">
                <span className="text-xs text-gray-500">IMG</span>
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-sm font-semibold text-gray-900 mb-1">
                  {bid.sellerStoreName}
                </h3>
                {bid.sellerRating !== null && (
                  <div className="flex items-center gap-1">
                    <svg
                      className="w-4 h-4 text-yellow-400"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                    </svg>
                    <span className="text-sm text-gray-700">
                      평점 {bid.sellerRating.toFixed(1)}
                    </span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* 요금제 */}
          {bid.pricePlanName && (
            <div className="bg-white rounded-lg p-4 pt-3 -mt-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-gray-900">요금제</span>
                <span className="text-sm text-gray-900">
                  {bid.pricePlanName}
                </span>
                <span className="text-sm font-semibold text-gray-900">
                  {formatPrice(bid.pricePlanPrice)}
                </span>
              </div>
              {bid.pricePlanPrice && bid.pricePlanPrice >= 10000 && (
                <p className="text-xs text-gray-500 mt-2">
                  3개월 후 10,000원 이상 요금제로 변경할 수 있습니다.
                </p>
              )}
            </div>
          )}

          {/* 부가서비스 */}
          {bid.additionalServices.length > 0 && (
            <div className="bg-white rounded-lg p-4 pt-1 -mt-2">
              <div className="mb-3 flex items-center justify-between">
                <span className="text-sm text-gray-600">부가서비스</span>
                <span className="text-sm text-gray-900">
                  {bid.additionalServicesCount}개
                </span>
                <span className="text-sm font-semibold text-gray-900">
                  {formatPrice(bid.additionalServicesTotalPrice)}
                </span>
              </div>
              <div className="space-y-4">
                {bid.additionalServices.map((service) => (
                  <div key={service.id} className="border-b border-gray-100 last:border-0 pb-3 last:pb-0">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-xs font-semibold text-gray-900">
                        {service.serviceName}
                      </span>
                      <span className="text-xs font-semibold text-gray-900">
                        {formatPrice(service.servicePrice)}
                      </span>
                    </div>
                    {service.cancellableAfterMonths && service.cancellableAfterMonths > 0 && (
                      <p className="text-xs text-gray-500 mt-1">
                        {service.cancellableAfterMonths}개월 후 {service.serviceName} 해지가 가능합니다.
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 가격 정보 */}
          <div className="bg-white rounded-lg p-4 pt-1 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">할부원금</span>
              <span className="text-sm font-bold text-gray-900">
                {formatPrice(bid.installmentPrincipal)}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">지원금</span>
              <span className="text-sm font-bold text-gray-900">
                {formatPrice(bid.price)}
              </span>
            </div>
            {bid.additionalSubsidy && bid.additionalSubsidy > 0 && (
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">추가지원금</span>
                <span className="text-sm font-bold text-gray-900">
                  {formatPrice(bid.additionalSubsidy)}
                </span>
              </div>
            )}
            <div className="flex items-center justify-between pt-2 border-t border-gray-200">
              <span className="text-sm font-semibold text-gray-900">총 유지비</span>
              <span className="text-sm font-bold text-gray-900">
                {formatPrice(bid.totalMaintenanceCost)}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">결제방법</span>
              <span className="text-sm text-gray-900">할부</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">개통조건</span>
              <span className="text-sm text-gray-900">
                {getPurchaseMethodDisplayName(bid.purchaseMethod)} / {getCarrierDisplayName(bid.carrier)}
              </span>
            </div>
          </div>

          {/* 구매하기 버튼 */}
          <div className="pt-2 pb-4">
            <button
              onClick={handlePurchase}
              className="w-full bg-indigo-500 text-white text-sm font-semibold py-3 rounded-lg hover:bg-indigo-600"
            >
              구매하기
            </button>
          </div>

          {/* 다른 견적 섹션 */}
          {otherBids.length > 0 && (
            <div className="pt-4">
              <div className="mb-3">
                <h3 className="text-sm font-semibold text-gray-900">
                  다른 견적
                </h3>
                <span className="text-xs text-gray-600 ml-1">
                  총 {otherBids.length}개
                </span>
              </div>
              <div className="space-y-3">
                {otherBids.map((otherBid) => (
                  <div
                    key={otherBid.id}
                    onClick={() => handleBidClick(otherBid.id)}
                    className="bg-gray-100 rounded-lg p-4 border border-gray-200 cursor-pointer hover:bg-gray-200 transition-colors"
                  >
                    <div className="flex items-start gap-4">
                      <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center flex-shrink-0">
                        <span className="text-xs text-gray-500">IMG</span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-1">
                          <h4 className="text-sm font-semibold text-gray-900">
                            {otherBid.sellerStoreName}
                          </h4>
                          {otherBid.sellerRating !== null && (
                            <div className="flex items-center gap-1">
                              <svg
                                className="w-4 h-4 text-yellow-400"
                                fill="currentColor"
                                viewBox="0 0 20 20"
                              >
                                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                              </svg>
                              <span className="text-xs text-gray-700">
                                {otherBid.sellerRating.toFixed(1)}
                              </span>
                            </div>
                          )}
                        </div>
                        <div className="space-y-1">
                          <div className="flex items-center justify-between">
                            <span className="text-xs text-gray-600">할부원금</span>
                            <span className="text-sm font-bold text-gray-900">
                              {formatPrice(otherBid.installmentPrincipal)}
                            </span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-xs text-gray-600">총 유지비</span>
                            <span className="text-sm font-bold text-gray-900">
                              {formatPrice(otherBid.totalMaintenanceCost)}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default BidDetailPage;

