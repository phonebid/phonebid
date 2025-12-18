import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mypageService } from "services/mypageService";
import { toast } from "react-toastify";
import type {
  PurchaseHistoryResponseDto,
  Page,
} from "types/MyPageTypes";

const PurchaseHistoryPage = () => {
  const navigate = useNavigate();
  const [activeFilter, setActiveFilter] = useState<"COMPLETED" | "CANCELLED">(
    "COMPLETED"
  );
  const [isLoading, setIsLoading] = useState(true);
  const [purchaseHistory, setPurchaseHistory] =
    useState<Page<PurchaseHistoryResponseDto> | null>(null);

  useEffect(() => {
    loadPurchaseHistory();
  }, [activeFilter]);

  const loadPurchaseHistory = async () => {
    try {
      setIsLoading(true);
      const data = await mypageService.getPurchaseHistory(activeFilter);
      setPurchaseHistory(data);
    } catch (error: any) {
      console.error("구매내역 조회 실패:", error);
      toast.error("구매내역을 불러오는데 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage");
  };

  const handlePurchaseClick = (contractId: string) => {
    navigate(`/mypage/purchases/${contractId}`);
  };

  const handleReviewClick = (
    e: React.MouseEvent,
    contractId: string,
    canReview: boolean
  ) => {
    e.stopPropagation();
    if (!canReview) {
      toast.info("후기를 작성할 수 없는 구매내역입니다.");
      return;
    }
    navigate(`/mypage/purchases/${contractId}/review`);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}년${month}월${day}일`;
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat("ko-KR").format(price) + "원";
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-600">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto px-4 py-4">
        {/* 헤더 */}
        <div className="flex items-center mb-6">
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
            구매내역
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 필터 버튼 */}
        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setActiveFilter("COMPLETED")}
            className={`flex-1 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              activeFilter === "COMPLETED"
                ? "bg-gray-800 text-white"
                : "bg-white text-gray-700 border border-gray-300"
            }`}
          >
            구매완료
          </button>
          <button
            onClick={() => setActiveFilter("CANCELLED")}
            className={`flex-1 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              activeFilter === "CANCELLED"
                ? "bg-gray-800 text-white"
                : "bg-white text-gray-700 border border-gray-300"
            }`}
          >
            취소/환불
          </button>
        </div>

        {/* 구매내역 목록 */}
        {purchaseHistory && purchaseHistory.content.length > 0 ? (
          <div className="space-y-4">
            {purchaseHistory.content.map((purchase) => (
              <div
                key={purchase.contractId}
                onClick={() => handlePurchaseClick(purchase.contractId)}
                className="bg-white rounded-lg p-4 border border-gray-200 cursor-pointer hover:shadow-md transition-shadow"
              >
                {/* 상품명과 날짜, 상태 */}
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <div className="text-base font-semibold text-gray-900 mb-1">
                      {purchase.productName}
                    </div>
                    <div className="text-sm text-gray-500">
                      {formatDate(purchase.transactionDate)}
                    </div>
                  </div>
                  <span
                    className={`px-2 py-1 rounded text-xs font-medium ${
                      purchase.status === "거래완료"
                        ? "bg-gray-100 text-gray-700"
                        : "bg-red-100 text-red-700"
                    }`}
                  >
                    {purchase.status}
                  </span>
                </div>

                {/* 상품 이미지 및 정보 */}
                <div className="flex gap-3 mb-3">
                  {/* 상품 이미지 */}
                  <div className="w-20 h-20 bg-gray-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    {purchase.productImageUrl ? (
                      <img
                        src={purchase.productImageUrl}
                        alt={purchase.productName}
                        className="w-full h-full object-cover rounded-lg"
                      />
                    ) : (
                      <svg
                        className="w-10 h-10 text-gray-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                        />
                      </svg>
                    )}
                  </div>

                  {/* 상품 정보 */}
                  <div className="flex-1 bg-gray-50 rounded-lg p-3">
                    <div className="text-sm text-gray-700 mb-1">
                      {purchase.productInfo.brand} {purchase.productInfo.model}
                    </div>
                    {purchase.productInfo.storage && (
                      <div className="text-sm text-gray-600 mb-1">
                        용량: {purchase.productInfo.storage}
                      </div>
                    )}
                    {purchase.productInfo.color && (
                      <div className="text-sm text-gray-600 mb-1">
                        색상: {purchase.productInfo.color}
                      </div>
                    )}
                    <div className="text-sm text-gray-600 mb-1">
                      통신사: {purchase.productInfo.carrier}
                    </div>
                    <div className="text-base font-semibold text-gray-900 mt-2">
                      {formatPrice(purchase.price)}
                    </div>
                  </div>
                </div>

                {/* 후기 남기기 버튼 */}
                {purchase.canReview && (
                  <button
                    onClick={(e) =>
                      handleReviewClick(e, purchase.contractId, purchase.canReview)
                    }
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
                  >
                    후기 남기기
                  </button>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white rounded-lg p-8 text-center">
            <div className="text-gray-500 text-sm">
              {activeFilter === "COMPLETED"
                ? "구매완료된 내역이 없습니다."
                : "취소/환불된 내역이 없습니다."}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PurchaseHistoryPage;

