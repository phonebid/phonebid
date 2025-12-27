import type { QuoteListItem } from "types/QuoteTypes";
import { formatPrice, getCarrierDisplayName, getPurchaseMethodDisplayName } from "utils/quoteUtils";

interface QuoteCardProps {
  quote: QuoteListItem;
  onViewBids: (quoteId: string) => void;
}

const QuoteCard = ({ quote, onViewBids }: QuoteCardProps) => {
  const handleViewBids = () => {
    onViewBids(quote.id);
  };

  const modelDisplayName = `${quote.phoneModel.model} ${quote.storage?.optionValue || ""}`.trim();
  const bidCount = quote.bidCount ?? 0;

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4 mb-3">
      <div className="flex gap-4">
        <div className="flex-shrink-0">
          <div className="w-20 h-20 bg-gray-200 rounded-lg flex items-center justify-center">
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

        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between mb-2">
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <span className="px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-700 rounded">
                  {getCarrierDisplayName(quote.carrier)}
                </span>
                <span className="px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-700 rounded">
                  {getPurchaseMethodDisplayName(quote.purchaseMethod)}
                </span>
              </div>
              <h3 className="text-base font-bold text-gray-900 mb-1">
                {modelDisplayName}
              </h3>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm text-gray-500 mr-2">최저가격</span>
              <span className="text-lg font-bold text-gray-900">
                {formatPrice(quote.lowestPrice)}
              </span>
            </div>
            {bidCount > 0 && (
              <button
                onClick={handleViewBids}
                className="text-sm text-gray-500 hover:text-gray-700 flex items-center"
              >
                견적 {bidCount}개 보기{" "}
                <svg
                  className="w-4 h-4 ml-1"
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
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuoteCard;

