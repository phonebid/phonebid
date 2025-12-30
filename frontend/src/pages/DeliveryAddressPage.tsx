import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getBidDetail, getQuoteDetail } from "services/quoteService";
import type { BidDetail, QuoteDetail } from "types/QuoteTypes";
import {
  formatPrice,
  getCarrierDisplayName,
  getPurchaseMethodDisplayName,
} from "utils/quoteUtils";
import { logError } from "utils/errorUtils";
import { toast } from "react-toastify";

declare global {
  interface Window {
    daum: {
      Postcode: new (options: {
        oncomplete: (data: {
          zonecode: string;
          address: string;
          addressEnglish: string;
          addressType: string;
          bname: string;
          buildingName: string;
        }) => void;
        width?: string;
        height?: string;
      }) => {
        open: () => void;
      };
    };
  }
}

interface DeliveryFormData {
  addressType: "default" | "new";
  addressName: string;
  recipientName: string;
  postalCode: string;
  address: string;
  detailAddress: string;
  phone: string;
  saveAsDefault: boolean;
}

const DeliveryAddressPage = () => {
  const navigate = useNavigate();
  const { quoteId, bidId } = useParams<{ quoteId: string; bidId: string }>();
  const [bid, setBid] = useState<BidDetail | null>(null);
  const [quote, setQuote] = useState<QuoteDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [formData, setFormData] = useState<DeliveryFormData>({
    addressType: "default",
    addressName: "",
    recipientName: "",
    postalCode: "",
    address: "",
    detailAddress: "",
    phone: "",
    saveAsDefault: false,
  });
  const [errors, setErrors] = useState<Partial<Record<keyof DeliveryFormData, string>>>({});

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
      navigate(`/mypage/quotes/${quoteId}/bids/${bidId}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    if (quoteId && bidId) {
      navigate(`/mypage/quotes/${quoteId}/bids/${bidId}`);
    } else {
      navigate("/mypage/quotes");
    }
  };

  const handleInputChange = (field: keyof DeliveryFormData, value: string | boolean) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const handleAddressSearch = () => {
    if (!window.daum || !window.daum.Postcode) {
      toast.error("주소 검색 서비스를 불러올 수 없습니다.");
      return;
    }

    new window.daum.Postcode({
      oncomplete: (data) => {
        setFormData((prev) => ({
          ...prev,
          postalCode: data.zonecode,
          address: data.address,
        }));
      },
      width: "100%",
      height: "100%",
    }).open();
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof DeliveryFormData, string>> = {};

    if (formData.addressType === "new") {
      if (!formData.addressName.trim()) {
        newErrors.addressName = "배송지명을 입력해주세요.";
      }
      if (!formData.recipientName.trim()) {
        newErrors.recipientName = "받는사람을 입력해주세요.";
      }
      if (!formData.postalCode.trim()) {
        newErrors.postalCode = "주소를 검색해주세요.";
      }
      if (!formData.address.trim()) {
        newErrors.address = "주소를 검색해주세요.";
      }
      if (!formData.phone.trim()) {
        newErrors.phone = "연락처를 입력해주세요.";
      } else if (!/^[0-9-]+$/.test(formData.phone)) {
        newErrors.phone = "연락처는 숫자와 하이픈(-)만 입력 가능합니다.";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (!validateForm()) {
      toast.error("입력 정보를 확인해주세요.");
      return;
    }

    if (formData.addressType === "default") {
      toast.info("기본 배송지 기능은 준비 중입니다.");
      return;
    }

    toast.info("구매 기능은 준비 중입니다.");
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
              {modelDisplayName}
            </h1>
            <div className="w-9"></div>
          </div>
        </div>

        <div className="px-4 py-4 space-y-4">
          {/* 배송지 섹션 */}
          <div className="bg-white rounded-lg p-4">
            <div className="flex items-center gap-4 mb-4">
              <h2 className="text-lg font-bold text-gray-900">배송지</h2>
              <div className="flex items-center gap-4">
                <label className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="addressType"
                    value="default"
                    checked={formData.addressType === "default"}
                    onChange={(e) => handleInputChange("addressType", e.target.value)}
                    className="w-4 h-4 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span className="ml-2 text-sm text-gray-900">기본배송지</span>
                </label>
                <label className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="addressType"
                    value="new"
                    checked={formData.addressType === "new"}
                    onChange={(e) => handleInputChange("addressType", e.target.value)}
                    className="w-4 h-4 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span className="ml-2 text-sm text-gray-900">신규배송지</span>
                </label>
              </div>
            </div>

            {/* 신규배송지 입력 필드 */}
            {formData.addressType === "new" && (
              <div className="space-y-3">
                {/* 배송지명 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    배송지명
                  </label>
                  <input
                    type="text"
                    placeholder="배송지명을 입력해주세요"
                    value={formData.addressName}
                    onChange={(e) => handleInputChange("addressName", e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                      errors.addressName ? "border-red-500" : "border-gray-300"
                    }`}
                  />
                  {errors.addressName && (
                    <p className="mt-1 text-xs text-red-600">{errors.addressName}</p>
                  )}
                </div>

                {/* 받는사람 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    받는사람
                  </label>
                  <input
                    type="text"
                    placeholder="받는사람을 입력해주세요"
                    value={formData.recipientName}
                    onChange={(e) => handleInputChange("recipientName", e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                      errors.recipientName ? "border-red-500" : "border-gray-300"
                    }`}
                  />
                  {errors.recipientName && (
                    <p className="mt-1 text-xs text-red-600">{errors.recipientName}</p>
                  )}
                </div>

                {/* 주소 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    주소
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      placeholder="주소검색 버튼을 클릭해주세요"
                      value={formData.address}
                      readOnly
                      className={`flex-1 px-3 py-2 border rounded-lg bg-gray-50 ${
                        errors.address ? "border-red-500" : "border-gray-300"
                      }`}
                    />
                    <button
                      type="button"
                      onClick={handleAddressSearch}
                      className="px-4 py-2 bg-gray-200 text-gray-900 text-sm font-medium rounded-lg hover:bg-gray-300"
                    >
                      주소검색
                    </button>
                  </div>
                  {formData.postalCode && (
                    <p className="mt-1 text-xs text-gray-500">
                      우편번호: {formData.postalCode}
                    </p>
                  )}
                  {errors.address && (
                    <p className="mt-1 text-xs text-red-600">{errors.address}</p>
                  )}
                </div>

                {/* 상세주소 */}
                {formData.address && (
                  <div>
                    <input
                      type="text"
                      placeholder="상세주소를 입력해주세요"
                      value={formData.detailAddress}
                      onChange={(e) => handleInputChange("detailAddress", e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                )}

                {/* 연락처 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    연락처
                  </label>
                  <input
                    type="text"
                    placeholder="연락처를 입력해주세요"
                    value={formData.phone}
                    onChange={(e) => handleInputChange("phone", e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                      errors.phone ? "border-red-500" : "border-gray-300"
                    }`}
                  />
                  {errors.phone && (
                    <p className="mt-1 text-xs text-red-600">{errors.phone}</p>
                  )}
                </div>

                {/* 기본배송지로 저장하기 */}
                <label className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.saveAsDefault}
                    onChange={(e) => handleInputChange("saveAsDefault", e.target.checked)}
                    className="w-4 h-4 text-indigo-600 rounded focus:ring-indigo-500"
                  />
                  <span className="ml-2 text-sm text-gray-900">기본배송지로 저장하기</span>
                </label>
              </div>
            )}
          </div>

          {/* 주문상품 섹션 */}
          <div className="bg-white rounded-lg p-4">
            <h2 className="text-base font-bold text-gray-900 mb-4">주문상품</h2>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-900">{modelDisplayName}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">
                  {getCarrierDisplayName(bid.carrier)} {getPurchaseMethodDisplayName(bid.purchaseMethod)}
                </span>
                <span className="text-sm text-gray-900">
                  {quote.color ?? "상관없음"}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">할부원금</span>
                <span className="text-sm font-bold text-gray-900">
                  {formatPrice(bid.installmentPrincipal)}
                </span>
              </div>
            </div>
          </div>

          {/* 구매하기 버튼 */}
          <div className="pt-2 pb-4">
            <button
              onClick={handleSubmit}
              className="w-full bg-indigo-500 text-white text-sm font-semibold py-3 rounded-lg hover:bg-indigo-600"
            >
              구매하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeliveryAddressPage;

