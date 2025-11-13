import type { QuoteDetail } from "types/QuoteTypes";
import { ChatAvatar } from "components/chat/ChatAvatar";

interface BidQuoteCardProps {
  quote: QuoteDetail;
  bidPrice?: number;
  sellerName?: string;
  sellerAvatar?: string;
}

/**
 * 입찰 견적 정보 카드 컴포넌트
 * 채팅방 상단에 견적 정보와 입찰 가격을 표시합니다.
 */
export function BidQuoteCard({
  quote,
  bidPrice,
  sellerName,
  sellerAvatar,
}: BidQuoteCardProps) {
  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat("ko-KR").format(price) + "원";
  };

  const formatCarrier = (carrier: string): string => {
    const carrierMap: Record<string, string> = {
      SKT: "SKT",
      KT: "KT",
      LGU: "LG U+",
      SKT_ALD: "SKT 알뜰폰",
      KT_ALD: "KT 알뜰폰",
      LGU_ALD: "LG U+ 알뜰폰",
    };
    return carrierMap[carrier] || carrier;
  };

  return (
    <div className="flex items-start gap-2 mb-4 justify-start">
      {/* 프로필 이미지 (왼쪽) */}
      <ChatAvatar avatar={sellerAvatar} name={sellerName} size="sm" alt={sellerName || "판매자"} />

      {/* 카드 */}
      <div className="flex flex-col w-[280px] items-start">
        <div className="bg-white rounded-lg border-2 border-indigo-500 p-4 shadow-sm w-full">
          {/* 판매자명 */}
          {sellerName && (
            <div className="mb-2">
              <p className="text-sm font-semibold text-indigo-500">{sellerName}</p>
            </div>
          )}

          {/* 입찰 내역 제목 */}
          <div className="mb-3">
            <h4 className="text-sm font-semibold text-gray-900">입찰 내역</h4>
          </div>

          {/* 구분선 */}
          <div className="border-t border-gray-200 mb-3"></div>

          {/* 예상 금액 */}
          {bidPrice !== undefined && (
            <div className="mb-4">
              <p className="text-xs text-gray-600 mb-1">예상 금액</p>
              <p className="text-sm font-bold text-gray-900">
                {formatPrice(bidPrice)}
              </p>
            </div>
          )}

          {/* 구분선 */}
          <div className="border-t border-gray-200 mb-3"></div>

          {/* 견적 정보 */}
          <div className="space-y-2 text-sm">
            <div className="flex justify-between gap-4">
              <span className="text-gray-600">기종</span>
              <span className="text-gray-900 font-medium text-right">{quote.model}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-gray-600">컬러</span>
              <span className="text-gray-900 font-medium text-right">
                {quote.color || "상관 없음"}
              </span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-gray-600">용량</span>
              <span className="text-gray-900 font-medium text-right">{quote.storage}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-gray-600">통신사</span>
              <span className="text-gray-900 font-medium text-right">
                {formatCarrier(quote.carrier)}
                {quote.carrier.includes("_") ? "(상관 없음)" : ""}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

