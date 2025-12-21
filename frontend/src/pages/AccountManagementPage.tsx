import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mypageService } from "services/mypageService";
import { toast } from "react-toastify";
import type {
  AccountCreateRequestDto,
  AccountResponseDto,
  Page,
} from "types/MyPageTypes";
import { BANK_LIST } from "types/MyPageTypes";
import { getErrorMessage, logError } from "utils/errorUtils";

const AccountManagementPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingAccounts, setIsLoadingAccounts] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [accountsPage, setAccountsPage] = useState<Page<AccountResponseDto>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
    first: true,
    last: true,
  });

  const [formData, setFormData] = useState<AccountCreateRequestDto>({
    bankName: "",
    accountNumber: "",
    accountHolderName: "",
  });

  const [errors, setErrors] = useState({
    bankName: "",
    accountNumber: "",
    accountHolderName: "",
  });

  useEffect(() => {
    loadAccounts();
  }, [currentPage]);

  const loadAccounts = async () => {
    try {
      setIsLoadingAccounts(true);
      const data = await mypageService.getAccounts(currentPage, 1);
      setAccountsPage(data);
    } catch (error: unknown) {
      logError("계좌 목록 조회 실패:", error);
      const errorMessage = getErrorMessage(error) || "계좌 목록을 불러오는데 실패했습니다.";
      toast.error(errorMessage);
    } finally {
      setIsLoadingAccounts(false);
    }
  };

  const validateBankName = (bankName: string): string => {
    if (!bankName.trim()) return "은행명을 선택해주세요.";
    return "";
  };

  const validateAccountNumber = (accountNumber: string): string => {
    const accountNumberRegex = /^[0-9-]+$/;
    if (!accountNumber.trim()) return "계좌번호를 입력해주세요.";
    if (accountNumber.length < 10 || accountNumber.length > 20) {
      return "계좌번호는 10자 이상 20자 이하여야 합니다.";
    }
    if (!accountNumberRegex.test(accountNumber)) {
      return "계좌번호는 숫자와 하이픈(-)만 입력 가능합니다.";
    }
    return "";
  };

  const validateAccountHolderName = (accountHolderName: string): string => {
    const accountHolderRegex = /^[가-힣a-zA-Z0-9\s]+$/;
    if (!accountHolderName.trim()) return "예금주명을 입력해주세요.";
    if (accountHolderName.length < 2 || accountHolderName.length > 50) {
      return "예금주명은 2자 이상 50자 이하여야 합니다.";
    }
    if (!accountHolderRegex.test(accountHolderName)) {
      return "예금주명은 한글, 영문, 숫자, 공백만 입력 가능합니다.";
    }
    return "";
  };

  const handleInputChange = (
    field: keyof AccountCreateRequestDto,
    value: string
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    let error = "";
    switch (field) {
      case "bankName":
        error = validateBankName(value);
        break;
      case "accountNumber":
        error = validateAccountNumber(value);
        break;
      case "accountHolderName":
        error = validateAccountHolderName(value);
        break;
    }

    setErrors((prev) => ({ ...prev, [field]: error }));
  };

  const handleSubmit = async () => {
    const bankNameError = validateBankName(formData.bankName);
    const accountNumberError = validateAccountNumber(formData.accountNumber);
    const accountHolderNameError = validateAccountHolderName(
      formData.accountHolderName
    );

    if (bankNameError || accountNumberError || accountHolderNameError) {
      setErrors({
        bankName: bankNameError,
        accountNumber: accountNumberError,
        accountHolderName: accountHolderNameError,
      });
      return;
    }

    setIsLoading(true);
    try {
      await mypageService.createAccount({
        bankName: formData.bankName.trim(),
        accountNumber: formData.accountNumber.trim(),
        accountHolderName: formData.accountHolderName.trim(),
      });
      toast.success("계좌가 등록되었습니다.");
      setFormData({
        bankName: "",
        accountNumber: "",
        accountHolderName: "",
      });
      setErrors({
        bankName: "",
        accountNumber: "",
        accountHolderName: "",
      });
      loadAccounts();
    } catch (error: unknown) {
      logError("계좌 등록 실패:", error);
      const errorMessage = getErrorMessage(error) || "계좌 등록에 실패했습니다.";
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async (accountId: string) => {
    if (!window.confirm("정말 이 계좌를 삭제하시겠습니까?")) {
      return;
    }

    try {
      await mypageService.deleteAccount(accountId);
      toast.success("계좌가 삭제되었습니다.");
      loadAccounts();
    } catch (error: unknown) {
      logError("계좌 삭제 실패:", error);
      const errorMessage = getErrorMessage(error) || "계좌 삭제에 실패했습니다.";
      toast.error(errorMessage);
    }
  };

  const handleBack = () => {
    navigate("/mypage");
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < accountsPage.totalPages) {
      setCurrentPage(newPage);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="flex items-center mb-8">
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
            계좌 관리
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 계좌 등록 폼 */}
        <div className="bg-white rounded-lg p-4 mb-6 shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            계좌 등록
          </h2>

          <div className="space-y-4">
            {/* 은행명 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                은행명
              </label>
              <div className="relative">
                <select
                  value={formData.bankName}
                  onChange={(e) => handleInputChange("bankName", e.target.value)}
                  className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                    errors.bankName ? "border-red-500" : "border-gray-300"
                  }`}
                >
                  <option value="">은행을 선택해주세요</option>
                  {BANK_LIST.map((bank) => (
                    <option key={bank} value={bank}>
                      {bank}
                    </option>
                  ))}
                </select>
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                  <svg
                    className="w-4 h-4 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 9l-7 7-7-7"
                    />
                  </svg>
                </div>
              </div>
              {errors.bankName && (
                <p className="mt-1 text-xs text-red-600">{errors.bankName}</p>
              )}
            </div>

            {/* 계좌번호 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                계좌번호
              </label>
              <input
                type="text"
                placeholder="000-000-000"
                value={formData.accountNumber}
                onChange={(e) =>
                  handleInputChange("accountNumber", e.target.value)
                }
                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                  errors.accountNumber ? "border-red-500" : "border-gray-300"
                }`}
              />
              {errors.accountNumber && (
                <p className="mt-1 text-xs text-red-600">
                  {errors.accountNumber}
                </p>
              )}
            </div>

            {/* 예금주 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                예금주
              </label>
              <input
                type="text"
                placeholder="Enter your name"
                value={formData.accountHolderName}
                onChange={(e) =>
                  handleInputChange("accountHolderName", e.target.value)
                }
                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                  errors.accountHolderName ? "border-red-500" : "border-gray-300"
                }`}
              />
              {errors.accountHolderName && (
                <p className="mt-1 text-xs text-red-600">
                  {errors.accountHolderName}
                </p>
              )}
            </div>

            {/* 계좌 등록 버튼 */}
            <button
              onClick={handleSubmit}
              disabled={isLoading}
              className="w-full px-4 py-2 border border-black rounded-lg text-black font-medium hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? "등록 중..." : "계좌 등록"}
            </button>
          </div>
        </div>

        {/* 등록된 계좌 목록 */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            등록된 계좌
          </h2>

          {isLoadingAccounts ? (
            <div className="text-center py-8 text-gray-500">로딩 중...</div>
          ) : accountsPage.content.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              등록된 계좌가 없습니다.
            </div>
          ) : (
            <>
              {/* 계좌 목록 (한 개씩 표시) */}
              <div className="mb-4">
                {accountsPage.content.map((account) => (
                  <div
                    key={account.accountId}
                    className="bg-gray-50 rounded-lg p-4 relative min-h-[120px]"
                  >
                    <div className="flex-1">
                      <div className="font-medium text-gray-900">
                        {account.bankName}
                      </div>
                      <div className="text-sm text-gray-600 mt-1">
                        {account.accountNumber}
                      </div>
                      <div className="text-xs text-gray-500 mt-1">
                        예금주 : {account.accountHolderName}
                      </div>
                    </div>
                    <button
                      onClick={() => handleDelete(account.accountId)}
                      className="absolute top-4 right-4 p-2 text-gray-400 hover:text-red-600 transition-colors"
                      aria-label="계좌 삭제"
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
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                        />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>

              {/* 페이징 컨트롤 */}
              {accountsPage.totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 pt-4 border-t border-gray-200">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={accountsPage.first}
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
                    {currentPage + 1}/{accountsPage.totalPages}
                  </span>
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={accountsPage.last}
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
  );
};

export default AccountManagementPage;

