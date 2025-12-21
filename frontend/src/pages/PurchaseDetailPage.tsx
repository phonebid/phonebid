import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { mypageService } from "services/mypageService";
import { toast } from "react-toastify";
import type { PurchaseDetailResponseDto } from "types/MyPageTypes";
import { logError } from "utils/errorUtils";

const PurchaseDetailPage = () => {
  const navigate = useNavigate();
  const { contractId } = useParams<{ contractId: string }>();
  const [isLoading, setIsLoading] = useState(true);
  const [purchaseDetail, setPurchaseDetail] =
    useState<PurchaseDetailResponseDto | null>(null);

  useEffect(() => {
    if (contractId) {
      loadPurchaseDetail();
    }
  }, [contractId]);

  const loadPurchaseDetail = async () => {
    if (!contractId) return;

    try {
      setIsLoading(true);
      const data = await mypageService.getPurchaseDetail(contractId);
      setPurchaseDetail(data);
    } catch (error: unknown) {
      logError("구매내역 상세 조회 실패:", error);
      const msg = error instanceof Error ? error.message : String(error);
      toast.error(`구매내역 상세 정보를 불러오는데 실패했습니다: ${msg}`);
      navigate("/mypage/purchases");
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/purchases");
  };

  const handleReviewClick = () => {
    if (!purchaseDetail?.canReview) {
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
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${year}년${month}월${day}일 ${hours}:${minutes}`;
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

  if (!purchaseDetail) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-600">구매내역을 찾을 수 없습니다.</div>
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
            구매내역 상세
          </h1>
          <div className="w-9"></div>
        </div>

        <div className="space-y-4">
          {/* 거래 상태 */}
          <div className="bg-white rounded-lg p-4 border border-gray-200">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">거래 상태</span>
              <span
                className={`px-3 py-1 rounded-full text-sm font-medium ${
                  purchaseDetail.status === "거래완료"
                    ? "bg-green-100 text-green-700"
                    : purchaseDetail.status === "취소/환불"
                    ? "bg-red-100 text-red-700"
                    : "bg-yellow-100 text-yellow-700"
                }`}
              >
                {purchaseDetail.status}
              </span>
            </div>
          </div>

          {/* 상품 정보 */}
          <div className="bg-white rounded-lg p-4 border border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              상품 정보
            </h2>
            <div className="flex gap-4 mb-4">
              {/* 상품 이미지 */}
              <div className="w-24 h-24 bg-gray-100 rounded-lg flex items-center justify-center flex-shrink-0">
                {purchaseDetail.productImageUrl ? (
                  <img
                    src={purchaseDetail.productImageUrl}
                    alt={purchaseDetail.productName}
                    className="w-full h-full object-cover rounded-lg"
                  />
                ) : (
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
                      d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                )}
              </div>

              {/* 상품 상세 정보 */}
              <div className="flex-1">
                <div className="text-base font-semibold text-gray-900 mb-2">
                  {purchaseDetail.productName}
                </div>
                <div className="space-y-1 text-sm text-gray-600">
                  <div>
                    브랜드: {purchaseDetail.productInfo.brand}{" "}
                    {purchaseDetail.productInfo.model}
                  </div>
                  {purchaseDetail.productInfo.storage && (
                    <div>용량: {purchaseDetail.productInfo.storage}</div>
                  )}
                  {purchaseDetail.productInfo.color && (
                    <div>색상: {purchaseDetail.productInfo.color}</div>
                  )}
                  <div>통신사: {purchaseDetail.productInfo.carrier}</div>
                </div>
                <div className="text-lg font-bold text-gray-900 mt-3">
                  {formatPrice(purchaseDetail.price)}
                </div>
              </div>
            </div>
          </div>

          {/* 거래 정보 */}
          <div className="bg-white rounded-lg p-4 border border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              거래 정보
            </h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">거래일시</span>
                <span className="text-sm text-gray-900">
                  {formatDate(purchaseDetail.transactionDate)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">계약번호</span>
                <span className="text-sm text-gray-900 font-mono">
                  {purchaseDetail.contractId.substring(0, 8)}...
                </span>
              </div>
            </div>
          </div>

          {/* 판매자 정보 */}
          {purchaseDetail.sellerInfo && (
            <div className="bg-white rounded-lg p-4 border border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                판매자 정보
              </h2>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">판매자명</span>
                  <span className="text-sm text-gray-900">
                    {purchaseDetail.sellerInfo.sellerName}
                  </span>
                </div>
                {purchaseDetail.sellerInfo.rating !== null && (
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">평점</span>
                    <span className="text-sm text-gray-900">
                      ⭐ {purchaseDetail.sellerInfo.rating.toFixed(1)}
                    </span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 결제 정보 */}
          {purchaseDetail.paymentInfo && (
            <div className="bg-white rounded-lg p-4 border border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                결제 정보
              </h2>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">결제 수단</span>
                  <span className="text-sm text-gray-900">
                    {purchaseDetail.paymentInfo.method}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">결제일시</span>
                  <span className="text-sm text-gray-900">
                    {formatDate(purchaseDetail.paymentInfo.paidAt)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">결제 금액</span>
                  <span className="text-sm font-semibold text-gray-900">
                    {formatPrice(purchaseDetail.price)}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* 배송 정보 */}
          {purchaseDetail.deliveryInfo && (
            <div className="bg-white rounded-lg p-4 border border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                배송 정보
              </h2>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">배송 상태</span>
                  <span className="text-sm text-gray-900">
                    {purchaseDetail.deliveryInfo.status}
                  </span>
                </div>
                {purchaseDetail.deliveryInfo.trackingNumber && (
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">송장번호</span>
                    <span className="text-sm text-gray-900 font-mono">
                      {purchaseDetail.deliveryInfo.trackingNumber}
                    </span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 후기 남기기 버튼 */}
          {purchaseDetail.canReview && (
            <button
              onClick={handleReviewClick}
              className="w-full px-4 py-3 bg-indigo-500 text-white rounded-lg text-base font-medium hover:bg-indigo-600 transition-colors"
            >
              후기 남기기
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default PurchaseDetailPage;

