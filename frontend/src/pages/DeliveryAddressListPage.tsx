import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { mypageService } from "services/mypageService";
import { toast } from "react-toastify";
import type {
  DeliveryAddressCreateRequestDto,
  DeliveryAddressResponseDto,
  Page,
} from "types/MyPageTypes";
import type { DeliveryFormData, DeliveryFormErrors } from "components/delivery/DeliveryAddressForm";
import { logError } from "utils/errorUtils";
import DeliveryAddressCard from "components/delivery/DeliveryAddressCard";
import DeliveryAddressForm from "components/delivery/DeliveryAddressForm";
import EmptyDeliveryAddressCard from "components/delivery/EmptyDeliveryAddressCard";

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

const DeliveryAddressListPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingAddresses, setIsLoadingAddresses] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [addressesPage, setAddressesPage] = useState<Page<DeliveryAddressResponseDto>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
    first: true,
    last: true,
  });

  const [formData, setFormData] = useState<DeliveryFormData>({
    addressName: "",
    recipientName: "",
    postalCode: "",
    address: "",
    detailAddress: "",
    phone: "",
    saveAsDefault: false,
  });

  const [errors, setErrors] = useState<DeliveryFormErrors>({});

  const loadAddresses = useCallback(async () => {
    try {
      setIsLoadingAddresses(true);
      const data = await mypageService.getDeliveryAddresses(currentPage, 10);
      setAddressesPage(data);
    } catch (error: unknown) {
      logError("배송지 목록 조회 실패:", error);
    } finally {
      setIsLoadingAddresses(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadAddresses();
  }, [loadAddresses]);

  const validateForm = (): boolean => {
    const newErrors: DeliveryFormErrors = {};

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

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
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

  const handleSubmit = async () => {
    if (!validateForm()) {
      toast.error("입력 정보를 확인해주세요.");
      return;
    }

    setIsLoading(true);
    try {
      const requestData: DeliveryAddressCreateRequestDto = {
        addressName: formData.addressName.trim(),
        recipientName: formData.recipientName.trim(),
        phone: formData.phone.trim(),
        postalCode: formData.postalCode.trim(),
        address: formData.address.trim(),
        detailAddress: formData.detailAddress.trim() || undefined,
        saveAsDefault: formData.saveAsDefault,
      };

      await mypageService.createDeliveryAddress(requestData);
      toast.success("배송지가 등록되었습니다.");
      setFormData({
        addressName: "",
        recipientName: "",
        postalCode: "",
        address: "",
        detailAddress: "",
        phone: "",
        saveAsDefault: false,
      });
      setErrors({});
      loadAddresses();
    } catch (error: unknown) {
      logError("배송지 등록 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSetDefault = async (addressId: string) => {
    try {
      await mypageService.setDefaultDeliveryAddress(addressId);
      toast.success("기본 배송지로 설정되었습니다.");
      loadAddresses();
    } catch (error: unknown) {
      logError("기본 배송지 설정 실패:", error);
    }
  };

  const handleDelete = async (addressId: string) => {
    if (!window.confirm("정말 이 배송지를 삭제하시겠습니까?")) {
      return;
    }

    try {
      await mypageService.deleteDeliveryAddress(addressId);
      toast.success("배송지가 삭제되었습니다.");
      loadAddresses();
    } catch (error: unknown) {
      logError("배송지 삭제 실패:", error);
    }
  };

  const handleBack = () => {
    navigate("/mypage");
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < addressesPage.totalPages) {
      setCurrentPage(newPage);
    }
  };

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
            <h1 className="text-lg font-bold text-gray-900 flex-1">배송주소록</h1>
            <div className="w-9"></div>
          </div>
        </div>

        <div className="px-4 py-4 space-y-6">
          {/* 배송지 등록 폼 */}
          <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">배송지 등록</h2>

            <DeliveryAddressForm
              formData={formData}
              errors={errors}
              onInputChange={handleInputChange}
              onAddressSearch={handleAddressSearch}
            />

            {/* 배송지 등록 버튼 */}
            <button
              onClick={handleSubmit}
              disabled={isLoading}
              className="w-full mt-4 px-4 py-2 bg-indigo-500 text-white text-sm font-semibold rounded-lg hover:bg-indigo-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? "등록 중..." : "배송지 등록"}
            </button>
          </div>

          {/* 등록된 배송지 목록 */}
          <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">등록된 배송지</h2>

            {isLoadingAddresses ? (
              <div className="text-center py-8 text-gray-500">로딩 중...</div>
            ) : addressesPage.content.length === 0 ? (
              <EmptyDeliveryAddressCard />
            ) : (
              <>
                {/* 배송지 목록 */}
                <div className="space-y-4 mb-4">
                  {addressesPage.content.map((address) => (
                    <DeliveryAddressCard
                      key={address.addressId}
                      address={address}
                      onSetDefault={handleSetDefault}
                      onDelete={handleDelete}
                    />
                  ))}
                </div>

                {/* 페이징 컨트롤 */}
                {addressesPage.totalPages > 1 && (
                  <div className="flex items-center justify-center gap-2 pt-4 border-t border-gray-200">
                    <button
                      onClick={() => handlePageChange(currentPage - 1)}
                      disabled={addressesPage.first}
                      className="bg-gray-100 rounded-lg p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                      aria-label="이전 페이지"
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
                    <span className="text-sm text-gray-700">
                      {currentPage + 1}/{addressesPage.totalPages}
                    </span>
                    <button
                      onClick={() => handlePageChange(currentPage + 1)}
                      disabled={addressesPage.last}
                      className="bg-gray-100 rounded-lg p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                      aria-label="다음 페이지"
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
                          d="M9 5l7 7-7 7"
                        />
                      </svg>
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeliveryAddressListPage;

