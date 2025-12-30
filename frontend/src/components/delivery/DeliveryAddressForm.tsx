export interface DeliveryFormData {
  addressName: string;
  recipientName: string;
  postalCode: string;
  address: string;
  detailAddress: string;
  phone: string;
  saveAsDefault: boolean;
}

export interface DeliveryFormErrors {
  addressName?: string;
  recipientName?: string;
  postalCode?: string;
  address?: string;
  phone?: string;
}

interface DeliveryAddressFormProps {
  formData: DeliveryFormData;
  errors: DeliveryFormErrors;
  onInputChange: (field: keyof DeliveryFormData, value: string | boolean) => void;
  onAddressSearch: () => void;
}

const DeliveryAddressForm = ({
  formData,
  errors,
  onInputChange,
  onAddressSearch,
}: DeliveryAddressFormProps) => {
  return (
    <div className="space-y-3">
      {/* 배송지명 */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">배송지명</label>
        <input
          type="text"
          placeholder="배송지명을 입력해주세요"
          value={formData.addressName}
          onChange={(e) => onInputChange("addressName", e.target.value)}
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
        <label className="block text-sm font-medium text-gray-700 mb-1">받는사람</label>
        <input
          type="text"
          placeholder="받는사람을 입력해주세요"
          value={formData.recipientName}
          onChange={(e) => onInputChange("recipientName", e.target.value)}
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
        <label className="block text-sm font-medium text-gray-700 mb-1">주소</label>
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
            onClick={onAddressSearch}
            className="px-4 py-2 bg-gray-200 text-gray-900 text-sm font-medium rounded-lg hover:bg-gray-300"
          >
            주소검색
          </button>
        </div>
        {formData.postalCode && (
          <p className="mt-1 text-xs text-gray-500">우편번호: {formData.postalCode}</p>
        )}
        {errors.address && <p className="mt-1 text-xs text-red-600">{errors.address}</p>}
      </div>

      {/* 상세주소 */}
      {formData.address && (
        <div>
          <input
            type="text"
            placeholder="상세주소를 입력해주세요"
            value={formData.detailAddress}
            onChange={(e) => onInputChange("detailAddress", e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
      )}

      {/* 연락처 */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">연락처</label>
        <input
          type="text"
          placeholder="연락처를 입력해주세요"
          value={formData.phone}
          onChange={(e) => onInputChange("phone", e.target.value)}
          className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
            errors.phone ? "border-red-500" : "border-gray-300"
          }`}
        />
        {errors.phone && <p className="mt-1 text-xs text-red-600">{errors.phone}</p>}
      </div>

      {/* 기본배송지로 저장하기 */}
      <label className="flex items-center cursor-pointer">
        <input
          type="checkbox"
          checked={formData.saveAsDefault}
          onChange={(e) => onInputChange("saveAsDefault", e.target.checked)}
          className="w-4 h-4 text-indigo-600 rounded focus:ring-indigo-500"
        />
        <span className="ml-2 text-sm text-gray-900">기본배송지로 저장하기</span>
      </label>
    </div>
  );
};

export default DeliveryAddressForm;

