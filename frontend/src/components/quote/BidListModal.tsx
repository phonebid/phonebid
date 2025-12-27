import { useEffect, useState } from "react";
import { getBidsByQuoteId } from "services/quoteService";
import type { BidListItem } from "types/QuoteTypes";
import { formatPrice } from "utils/quoteUtils";
import { logError } from "utils/errorUtils";

interface BidListModalProps {
  quoteId: string;
  isOpen: boolean;
  onClose: () => void;
}

const BidListModal = ({ quoteId, isOpen, onClose }: BidListModalProps) => {
  const [bids, setBids] = useState<BidListItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (isOpen && quoteId) {
      loadBids();
    }
  }, [isOpen, quoteId]);

  const loadBids = async () => {
    try {
      setIsLoading(true);
      const data = await getBidsByQuoteId(quoteId);
      setBids(data);
    } catch (error: unknown) {
      logError("견적 목록 조회 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg max-w-md w-full mx-4 max-h-[80vh] flex flex-col">
        <div className="flex items-center justify-between p-4 border-b border-gray-200">
          <h2 className="text-lg font-bold text-gray-900">견적 목록</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
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
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-4">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="text-gray-500">로딩 중...</div>
            </div>
          ) : bids.length === 0 ? (
            <div className="flex items-center justify-center py-8">
              <div className="text-gray-500">견적이 없습니다.</div>
            </div>
          ) : (
            <div className="space-y-3">
              {bids.map((bid) => (
                <div
                  key={bid.id}
                  className="border border-gray-200 rounded-lg p-4"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <h3 className="font-semibold text-gray-900">
                        {bid.sellerStoreName}
                      </h3>
                      {bid.sellerRating !== null && (
                        <div className="flex items-center mt-1">
                          <svg
                            className="w-4 h-4 text-yellow-400 mr-1"
                            fill="currentColor"
                            viewBox="0 0 20 20"
                          >
                            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                          </svg>
                          <span className="text-sm text-gray-600">
                            {bid.sellerRating.toFixed(1)}
                          </span>
                        </div>
                      )}
                    </div>
                    <div className="text-right">
                      <div className="text-lg font-bold text-gray-900">
                        {formatPrice(bid.installmentPrincipal)}
                      </div>
                      {bid.pricePlanName && (
                        <div className="text-xs text-gray-500 mt-1">
                          {bid.pricePlanName}
                        </div>
                      )}
                    </div>
                  </div>
                  {bid.totalMaintenanceCost > 0 && (
                    <div className="text-xs text-gray-500 mt-2">
                      총 유지비: {formatPrice(bid.totalMaintenanceCost)}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default BidListModal;

