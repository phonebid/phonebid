import { formatBusinessNumber, formatPhoneNumber } from "utils/formatters";
import { BANK_LIST } from "types/SellerTypes";
import type { BankName } from "types/SellerTypes";
import type { Step1Data } from "utils/sellerValidation";
import Button from "components/common/Button";

interface SellerSignupStep1Props {
  step1Data: Step1Data;
  errors: Record<string, string>;
  setStep1Data: React.Dispatch<React.SetStateAction<Step1Data>>;
  handleAddressSearch: (type: "business" | "store") => void;
  handleFileChange: (
    e: React.ChangeEvent<HTMLInputElement>,
    type: "businessLicense" | "consentForm"
  ) => Promise<void>;
  handleStep1Next: () => void;
  onCancel: () => void;
}

const SellerSignupStep1 = ({
  step1Data,
  errors,
  setStep1Data,
  handleAddressSearch,
  handleFileChange,
  handleStep1Next,
  onCancel,
}: SellerSignupStep1Props) => {
  const createPhoneKeyDownHandler = (
    fieldName: "representativePhone" | "customerServicePhone"
  ) => {
    return (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Backspace") {
        const input = e.currentTarget;
        const cursorPosition = input.selectionStart || 0;
        const value = input.value;

        if (cursorPosition > 0 && value[cursorPosition - 1] === "-") {
          e.preventDefault();
          const beforeHyphen = value.slice(0, cursorPosition - 1);
          const afterHyphen = value.slice(cursorPosition);
          const digitsBeforeHyphen = beforeHyphen.replace(/\D/g, "");
          const newValue = beforeHyphen.slice(0, -1) + afterHyphen;
          const formattedValue = formatPhoneNumber(newValue);
          setStep1Data((prev) => ({
            ...prev,
            [fieldName]: formattedValue,
          }));
          setTimeout(() => {
            const targetDigitCount = digitsBeforeHyphen.length - 1;
            let newCursorPos = formattedValue.length;
            let digitCount = 0;
            for (let i = 0; i < formattedValue.length; i++) {
              if (/\d/.test(formattedValue?.[i] ?? "")) {
                digitCount++;
                if (digitCount === targetDigitCount) {
                  newCursorPos = i + 1;
                  if (
                    i + 1 < formattedValue.length &&
                    formattedValue[i + 1] === "-"
                  ) {
                    newCursorPos = i + 2;
                  }
                  break;
                }
              }
            }
            if (targetDigitCount <= 0) {
              newCursorPos = 0;
            }
            input.setSelectionRange(newCursorPos, newCursorPos);
          }, 0);
        }
      }
    };
  };

  return (
    <div className="space-y-8">
      {/* 대리점 여부 */}
      <div className="bg-blue-50 p-4 rounded-lg border border-blue-100">
        <label className="flex items-center gap-2">
          <input
            type="checkbox"
            checked={step1Data.isAgent}
            onChange={(e) =>
              setStep1Data((prev) => ({
                ...prev,
                isAgent: e.target.checked,
              }))
            }
            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <span className="text-sm font-medium text-gray-900">
            대리점이신가요? 체크해주세요
          </span>
        </label>
        <p className="text-xs text-gray-600 mt-2 ml-6">
          대리점의 경우 사전승낙서가 필요하지 않습니다
        </p>
      </div>

      {/* 사업자 정보 */}
      <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm">
        <div className="flex items-center gap-2 mb-4">
          <svg
            className="w-5 h-5 text-blue-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
            />
          </svg>
          <h3 className="text-lg font-semibold text-gray-900">사업자 정보</h3>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              사업자 등록번호 <span className="text-red-500">*</span>
            </label>
            <div className="flex items-start gap-2">
              <input
                type="text"
                placeholder="123-45-67890"
                value={step1Data.businessNumber}
                onChange={(e) =>
                  setStep1Data((prev) => ({
                    ...prev,
                    businessNumber: formatBusinessNumber(e.target.value),
                  }))
                }
                className={`flex-1 px-3 py-2 border rounded-md ${
                  errors.businessNumber
                    ? "border-red-500"
                    : "border-gray-300"
                }`}
              />
              <div className="flex flex-col">
                <input
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => handleFileChange(e, "businessLicense")}
                  className="hidden"
                  id="businessLicense"
                />
                <label
                  htmlFor="businessLicense"
                  className="px-4 py-2 bg-gray-100 border border-gray-300 rounded-md cursor-pointer hover:bg-gray-200 text-sm text-gray-700 flex items-center gap-2 whitespace-nowrap"
                >
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
                    />
                  </svg>
                  파일 첨부
                </label>
              </div>
            </div>
            <div className="flex items-start gap-2 mt-1">
              <svg
                className="w-4 h-4 text-gray-500 mt-0.5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <p className="text-xs text-gray-500">
                사업자등록증을 첨부해주세요 (PDF, JPG, JPEG, PNG, 최대 10MB)
              </p>
            </div>
            {step1Data.businessLicenseFile && (
              <p className="mt-1 text-sm text-gray-600">
                {step1Data.businessLicenseFile?.name}
              </p>
            )}
            {errors.businessNumber && (
              <p className="mt-1 text-sm text-red-600">
                {errors.businessNumber}
              </p>
            )}
            {errors.businessLicenseFile && (
              <p className="mt-1 text-sm text-red-600">
                {errors.businessLicenseFile}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              상호명 (매장명) <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              placeholder="예: 홍길동 쇼핑몰"
              value={step1Data.storeName}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  storeName: e.target.value,
                }))
              }
              className={`w-full px-3 py-2 border rounded-md ${
                errors.storeName ? "border-red-500" : "border-gray-300"
              }`}
            />
            {errors.storeName && (
              <p className="mt-1 text-sm text-red-600">{errors.storeName}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              대표자명 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              placeholder="홍길동"
              value={step1Data.representativeName}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  representativeName: e.target.value,
                }))
              }
              className={`w-full px-3 py-2 border rounded-md ${
                errors.representativeName
                  ? "border-red-500"
                  : "border-gray-300"
              }`}
            />
            {errors.representativeName && (
              <p className="mt-1 text-sm text-red-600">
                {errors.representativeName}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              사업장 주소 <span className="text-red-500">*</span>
            </label>
            <div className="flex gap-2 mb-2">
              <input
                type="text"
                placeholder="우편번호"
                value={step1Data.businessPostalCode}
                readOnly
                className="w-32 px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
              />
              <button
                type="button"
                onClick={() => handleAddressSearch("business")}
                className="px-4 py-2 bg-gray-200 text-gray-900 rounded-md hover:bg-gray-300"
              >
                주소 검색
              </button>
            </div>
            <input
              type="text"
              placeholder="기본 주소"
              value={step1Data.businessAddress}
              readOnly
              className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 mb-2"
            />
            <input
              type="text"
              placeholder="상세 주소"
              value={step1Data.businessDetailAddress}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  businessDetailAddress: e.target.value,
                }))
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
            {errors.businessAddress && (
              <p className="mt-1 text-sm text-red-600">
                {errors.businessAddress}
              </p>
            )}
          </div>
        </div>
      </div>

      {/* 사전승낙서 정보 */}
      {!step1Data.isAgent && (
        <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm">
          <div className="flex items-center gap-2 mb-4">
            <svg
              className="w-5 h-5 text-blue-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            <h3 className="text-lg font-semibold text-gray-900">
              사전승낙서 정보
            </h3>
          </div>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                판매점 이름 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={step1Data.storeName}
                readOnly
                className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
              />
              <p className="text-xs text-gray-500 mt-1">
                상호명과 동일하게 적용됩니다
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                판매점 주소 <span className="text-red-500">*</span>
              </label>
              <div className="flex gap-2 mb-2">
                <input
                  type="text"
                  placeholder="우편번호"
                  value={step1Data.storePostalCode}
                  readOnly
                  className="w-32 px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
                />
                <button
                  type="button"
                  onClick={() => handleAddressSearch("store")}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  주소 입력
                </button>
              </div>
              <input
                type="text"
                placeholder="기본 주소"
                value={step1Data.storeAddress}
                readOnly
                className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 mb-2"
              />
              <input
                type="text"
                placeholder="상세 주소"
                value={step1Data.storeDetailAddress}
                onChange={(e) =>
                  setStep1Data((prev) => ({
                    ...prev,
                    storeDetailAddress: e.target.value,
                  }))
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              />
              {errors.storeAddress && (
                <p className="mt-1 text-sm text-red-600">
                  {errors.storeAddress}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                승낙번호 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                placeholder="승낙번호 입력"
                value={step1Data.consentNumber}
                onChange={(e) =>
                  setStep1Data((prev) => ({
                    ...prev,
                    consentNumber: e.target.value,
                  }))
                }
                className={`w-full px-3 py-2 border rounded-md ${
                  errors.consentNumber
                    ? "border-red-500"
                    : "border-gray-300"
                }`}
              />
              {errors.consentNumber && (
                <p className="mt-1 text-sm text-red-600">
                  {errors.consentNumber}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                사전승낙서 이미지 정보{" "}
                <span className="text-red-500">*</span>
              </label>
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
                <input
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => handleFileChange(e, "consentForm")}
                  className="hidden"
                  id="consentForm"
                />
                <label htmlFor="consentForm" className="cursor-pointer">
                  <svg
                    className="w-12 h-12 mx-auto text-gray-400 mb-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                    />
                  </svg>
                  <p className="text-sm text-gray-600">클릭하여 파일 선택</p>
                  <p className="text-xs text-gray-500 mt-1">
                    PDF, JPG, JPEG, PNG (최대 10MB)
                  </p>
                </label>
              </div>
              {step1Data.consentFormFile && (
                <p className="mt-2 text-sm text-gray-600">
                  {step1Data.consentFormFile?.name}
                </p>
              )}
              {errors.consentFormFile && (
                <p className="mt-1 text-sm text-red-600">
                  {errors.consentFormFile}
                </p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 연락처 정보 */}
      <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm">
        <div className="flex items-center gap-2 mb-4">
          <svg
            className="w-5 h-5 text-blue-600"
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
          <h3 className="text-lg font-semibold text-gray-900">연락처 정보</h3>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              대표 전화번호 <span className="text-red-500">*</span>
            </label>
            <input
              type="tel"
              placeholder="010-1234-5678"
              value={step1Data.representativePhone}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  representativePhone: formatPhoneNumber(e.target.value),
                }))
              }
              onKeyDown={createPhoneKeyDownHandler("representativePhone")}
              className={`w-full px-3 py-2 border rounded-md ${
                errors.representativePhone
                  ? "border-red-500"
                  : "border-gray-300"
              }`}
            />
            {errors.representativePhone && (
              <p className="mt-1 text-sm text-red-600">
                {errors.representativePhone}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              이메일 주소 <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              placeholder="seller@example.com"
              value={step1Data.email}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  email: e.target.value,
                }))
              }
              className={`w-full px-3 py-2 border rounded-md ${
                errors.email ? "border-red-500" : "border-gray-300"
              }`}
            />
            <p className="text-xs text-gray-500 mt-1">
              주문 알림 및 정산 내역이 발송됩니다
            </p>
            {errors.email && (
              <p className="mt-1 text-sm text-red-600">{errors.email}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              고객센터 전화번호
            </label>
            <input
              type="tel"
              placeholder="1588-1234"
              value={step1Data.customerServicePhone}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  customerServicePhone: formatPhoneNumber(e.target.value),
                }))
              }
              onKeyDown={createPhoneKeyDownHandler("customerServicePhone")}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>
        </div>
      </div>

      {/* 정산 계좌 정보 */}
      <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm">
        <div className="flex items-center gap-2 mb-4">
          <svg
            className="w-5 h-5 text-blue-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
            />
          </svg>
          <h3 className="text-lg font-semibold text-gray-900">
            정산 계좌 정보
          </h3>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              은행 선택 <span className="text-red-500">*</span>
            </label>
            <select
              value={step1Data.bankName}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  bankName: e.target.value as BankName,
                }))
              }
              className={`w-full px-3 py-2 border rounded-md ${
                errors.bankName ? "border-red-500" : "border-gray-300"
              }`}
            >
              <option value="">은행을 선택하세요</option>
              {BANK_LIST.map((bank) => (
                <option key={bank} value={bank}>
                  {bank}
                </option>
              ))}
            </select>
            {errors.bankName && (
              <p className="mt-1 text-sm text-red-600">{errors.bankName}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              계좌번호 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              placeholder="숫자만 입력 (예: 123456789012)"
              value={step1Data.accountNumber}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  accountNumber: e.target.value.replace(/\D/g, ""),
                }))
              }
              className={`w-full px-3 py-2 border rounded-md ${
                errors.accountNumber ? "border-red-500" : "border-gray-300"
              }`}
            />
            {errors.accountNumber && (
              <p className="mt-1 text-sm text-red-600">
                {errors.accountNumber}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              예금주명 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              placeholder="예금주명 입력"
              value={step1Data.accountHolderName}
              onChange={(e) =>
                setStep1Data((prev) => ({
                  ...prev,
                  accountHolderName: e.target.value,
                }))
              }
              className={`w-full px-3 py-2 border rounded-md ${
                errors.accountHolderName
                  ? "border-red-500"
                  : "border-gray-300"
              }`}
            />
            <p className="text-xs text-gray-500 mt-1">
              대표자명 또는 법인명과 일치해야 합니다
            </p>
            {errors.accountHolderName && (
              <p className="mt-1 text-sm text-red-600">
                {errors.accountHolderName}
              </p>
            )}
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start gap-3">
              <div className="w-5 h-5 bg-blue-600 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                <span className="text-white text-xs font-bold">i</span>
              </div>
              <div>
                <h4 className="text-sm font-semibold text-blue-900 mb-1">
                  정산 안내
                </h4>
                <p className="text-xs text-blue-800">
                  매주 화요일 판매 금액이 자동으로 정산됩니다. 수수료는
                  판매금액의 5%입니다.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="flex gap-4 pt-4">
        <Button onClick={onCancel} variant="secondary" className="flex-1">
          취소
        </Button>
        <Button onClick={handleStep1Next} className="flex-1">
          다음 단계
        </Button>
      </div>
    </div>
  );
};

export default SellerSignupStep1;

