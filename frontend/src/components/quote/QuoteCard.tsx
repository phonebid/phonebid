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
    <div className="bg-gray-100 rounded-lg p-4 mb-3">
      <div className="flex gap-4 items-stretch">
        <div className="flex-shrink-0">
          <div className="w-32 h-full bg-gray-200 rounded-lg flex items-center justify-center min-h-[120px]">
            <svg
              className="w-20 h-20 text-gray-400"
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
          <div className="flex items-center gap-2 mb-2">
            <span className="px-2 py-0.5 text-xs font-medium bg-gray-600 text-white rounded">
              {getCarrierDisplayName(quote.carrier)}
            </span>
            <span className="px-2 py-0.5 text-xs font-medium bg-gray-600 text-white rounded">
              {getPurchaseMethodDisplayName(quote.purchaseMethod)}
            </span>
          </div>
          <h3 className="text-base font-bold text-gray-900 mb-3">
            {modelDisplayName}
          </h3>

          <div className="flex items-center justify-between mb-2">
            <span className="text-sm text-gray-900">최저가격</span>
            <span className="text-sm font-bold text-gray-900">
              {formatPrice(quote.lowestPrice)}
            </span>
          </div>
          {bidCount > 0 && (
            <button
              onClick={handleViewBids}
              className="text-sm font-bold text-gray-900 hover:text-gray-700 flex items-center"
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
  );
};

export default QuoteCard;

