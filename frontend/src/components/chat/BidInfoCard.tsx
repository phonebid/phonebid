interface BidInfo {
  estimatedPrice: number;
  model: string;
  color?: string;
  storage?: string;
  carrier?: string;
  sellerInfo?: string;
}

interface BidInfoCardProps {
  bidInfo: BidInfo;
  sellerName?: string;
  sellerAvatar?: string;
}

/**
 * 입찰 내역 정보 카드 컴포넌트
 * 판매자의 입찰 제안을 구조화된 형태로 표시합니다.
 */
export function BidInfoCard({
  bidInfo,
  sellerName,
  sellerAvatar,
}: BidInfoCardProps) {
  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat("ko-KR").format(price) + "원";
  };

  return (
    <div className="flex justify-start">
      {sellerAvatar ? (
        <img
          src={sellerAvatar}
          alt={sellerName || "판매자"}
          className="w-8 h-8 rounded-full mr-2 flex-shrink-0 object-cover"
        />
      ) : (
        <div className="w-8 h-8 bg-gray-300 rounded-full mr-2 flex-shrink-0" />
      )}
      <div className="bg-gray-100 rounded-lg px-4 py-3 max-w-xs lg:max-w-md">
        {sellerName && (
          <p className="text-sm font-medium text-gray-900 mb-2">{sellerName}</p>
        )}
        <div className="mb-3">
          <h4 className="text-sm font-semibold text-gray-900 mb-2">
            입찰 내역
          </h4>
          <div className="space-y-1 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">예상 금액:</span>
              <span className="font-medium text-gray-900">
                {formatPrice(bidInfo.estimatedPrice)}
              </span>
            </div>
            {bidInfo.model && (
              <div className="flex justify-between">
                <span className="text-gray-600">기종:</span>
                <span className="text-gray-900">{bidInfo.model}</span>
              </div>
            )}
            {bidInfo.color && (
              <div className="flex justify-between">
                <span className="text-gray-600">컬러:</span>
                <span className="text-gray-900">{bidInfo.color}</span>
              </div>
            )}
            {bidInfo.storage && (
              <div className="flex justify-between">
                <span className="text-gray-600">용량:</span>
                <span className="text-gray-900">{bidInfo.storage}</span>
              </div>
            )}
            {bidInfo.carrier && (
              <div className="flex justify-between">
                <span className="text-gray-600">통신사:</span>
                <span className="text-gray-900">{bidInfo.carrier}</span>
              </div>
            )}
          </div>
        </div>
        {bidInfo.sellerInfo && (
          <div className="mt-3 pt-3 border-t border-gray-200">
            <p className="text-sm text-gray-700 whitespace-pre-wrap">
              {bidInfo.sellerInfo}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

