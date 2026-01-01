import type { DeliveryAddressResponseDto } from "types/MyPageTypes";

interface DeliveryAddressCardProps {
  address: DeliveryAddressResponseDto;
  onSetDefault: (addressId: string) => void;
  onDelete: (addressId: string) => void;
}

const DeliveryAddressCard = ({
  address,
  onSetDefault,
  onDelete,
}: DeliveryAddressCardProps) => {
  const isDefault = address.isDefault;
  const cardClassName = isDefault
    ? "border-2 border-indigo-200 bg-indigo-50 rounded-lg p-5 space-y-4"
    : "border border-gray-200 bg-white rounded-lg p-5 space-y-4";

  return (
    <div className={cardClassName}>
      {/* 배송지명과 기본 배송지 뱃지 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <svg
            className={`w-5 h-5 ${isDefault ? "text-indigo-600" : "text-gray-600"}`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
            />
          </svg>
          <h3 className="text-base font-bold text-gray-900">{address.addressName}</h3>
        </div>
        {isDefault && (
          <span className="px-2 py-1 text-xs font-semibold text-indigo-700 bg-indigo-200 rounded-full">
            기본 배송지
          </span>
        )}
      </div>

      {/* 배송지 정보 */}
      <div className={`space-y-3 pt-2 ${isDefault ? "border-t border-indigo-200" : "border-t border-gray-200"}`}>
        <div className="flex items-start gap-3">
          <svg
            className="w-5 h-5 text-gray-400 mt-0.5 flex-shrink-0"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
            />
          </svg>
          <div className="flex-1">
            <p className="text-xs text-gray-500 mb-0.5">받는사람</p>
            <p className="text-sm font-medium text-gray-900">{address.recipientName}</p>
          </div>
        </div>

        <div className="flex items-start gap-3">
          <svg
            className="w-5 h-5 text-gray-400 mt-0.5 flex-shrink-0"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
            />
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
            />
          </svg>
          <div className="flex-1">
            <p className="text-xs text-gray-500 mb-0.5">주소</p>
            <p className="text-sm text-gray-900 leading-relaxed">
              <span className="text-gray-600">({address.postalCode})</span> {address.address}
              {address.detailAddress && (
                <>
                  <br />
                  <span className="text-gray-600">{address.detailAddress}</span>
                </>
              )}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-3">
          <svg
            className="w-5 h-5 text-gray-400 mt-0.5 flex-shrink-0"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
            />
          </svg>
          <div className="flex-1">
            <p className="text-xs text-gray-500 mb-0.5">연락처</p>
            <p className="text-sm font-medium text-gray-900">{address.phone}</p>
          </div>
        </div>
      </div>

      {/* 액션 버튼 */}
      <div className="flex items-center justify-end gap-2 pt-3 border-t border-gray-200">
        {!isDefault && (
          <button
            onClick={() => onSetDefault(address.addressId)}
            className="px-3 py-1.5 text-sm font-medium text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50 rounded-lg transition-colors"
          >
            기본 배송지로 설정
          </button>
        )}
        <button
          onClick={() => onDelete(address.addressId)}
          className="px-3 py-1.5 text-sm font-medium text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
        >
          삭제
        </button>
      </div>
    </div>
  );
};

export default DeliveryAddressCard;

