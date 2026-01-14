import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { sellerService } from "services/sellerService";
import { toast } from "react-toastify";
import type {
  SellerRegisterRequestDto,
  AddressDto,
  SettlementAccountDto,
  BankName,
} from "types/SellerTypes";
import { BANK_LIST } from "types/SellerTypes";
import Button from "components/common/Button";

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

const SellerSignupPage = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2>(1);

  // 1단계: 사업자 정보
  const [step1Data, setStep1Data] = useState({
    isAgent: false,
    businessNumber: "",
    businessLicenseFile: null as File | null,
    businessLicenseFileUrl: "",
    storeName: "",
    representativeName: "",
    businessPostalCode: "",
    businessAddress: "",
    businessDetailAddress: "",
    storePostalCode: "",
    storeAddress: "",
    storeDetailAddress: "",
    consentNumber: "",
    consentFormFile: null as File | null,
    consentFormFileUrl: "",
    representativePhone: "",
    email: "",
    customerServicePhone: "",
    bankName: "" as BankName | "",
    accountNumber: "",
    accountHolderName: "",
  });

  // 2단계: 회원 정보
  const [step2Data, setStep2Data] = useState({
    username: "",
    password: "",
    confirmPassword: "",
    name: "",
    nickname: "",
    termsOfService: false,
    privacyPolicy: false,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // 다음 Postcode API 스크립트 로드 확인
    if (!window.daum) {
      const script = document.createElement("script");
      script.src =
        "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
      script.async = true;
      document.head.appendChild(script);
    }
  }, []);

  const handleAddressSearch = (type: "business" | "store") => {
    if (!window.daum || !window.daum.Postcode) {
      toast.error("주소 검색 서비스를 불러올 수 없습니다.");
      return;
    }

    new window.daum.Postcode({
      oncomplete: (data) => {
        if (type === "business") {
          setStep1Data((prev) => ({
            ...prev,
            businessPostalCode: data.zonecode,
            businessAddress: data.address,
          }));
        } else {
          setStep1Data((prev) => ({
            ...prev,
            storePostalCode: data.zonecode,
            storeAddress: data.address,
          }));
        }
      },
      width: "100%",
      height: "100%",
    }).open();
  };

  const handleFileUpload = async (
    file: File,
    type: "businessLicense" | "consentForm"
  ): Promise<string> => {
    try {
      const documentType =
        type === "businessLicense" ? "BUSINESS_LICENSE" : "CONSENT_FORM";
      const fileUrl = await sellerService.uploadDocument(file, documentType);
      return fileUrl;
    } catch (error) {
      throw error;
    }
  };

  const handleFileChange = async (
    e: React.ChangeEvent<HTMLInputElement>,
    type: "businessLicense" | "consentForm"
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 파일 크기 검증 (10MB)
    if (file.size > 10 * 1024 * 1024) {
      toast.error("파일 크기는 10MB 이하여야 합니다.");
      return;
    }

    // 파일 형식 검증
    const allowedTypes = ["image/jpeg", "image/png", "application/pdf"];
    if (!allowedTypes.includes(file.type)) {
      toast.error("JPG, PNG, PDF 파일만 업로드 가능합니다.");
      return;
    }

    setIsLoading(true);
    try {
      const fileUrl = await handleFileUpload(file, type);
      if (type === "businessLicense") {
        setStep1Data((prev) => ({
          ...prev,
          businessLicenseFile: file,
          businessLicenseFileUrl: fileUrl,
        }));
      } else {
        setStep1Data((prev) => ({
          ...prev,
          consentFormFile: file,
          consentFormFileUrl: fileUrl,
        }));
      }
      toast.success("파일이 업로드되었습니다.");
    } catch (error) {
      toast.error("파일 업로드에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const validateStep1 = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!step1Data.businessNumber.trim()) {
      newErrors.businessNumber = "사업자등록번호를 입력해주세요.";
    } else if (!/^\d{3}-\d{2}-\d{5}$/.test(step1Data.businessNumber)) {
      newErrors.businessNumber = "사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)";
    }

    if (!step1Data.businessLicenseFileUrl) {
      newErrors.businessLicenseFile = "사업자등록증을 첨부해주세요.";
    }

    if (!step1Data.storeName.trim()) {
      newErrors.storeName = "상호명을 입력해주세요.";
    }

    if (!step1Data.representativeName.trim()) {
      newErrors.representativeName = "대표자명을 입력해주세요.";
    }

    if (!step1Data.businessPostalCode) {
      newErrors.businessAddress = "사업장 주소를 검색해주세요.";
    }

    if (!step1Data.isAgent) {
      if (!step1Data.storePostalCode) {
        newErrors.storeAddress = "판매점 주소를 검색해주세요.";
      }
      if (!step1Data.consentNumber.trim()) {
        newErrors.consentNumber = "승낙번호를 입력해주세요.";
      }
      if (!step1Data.consentFormFileUrl) {
        newErrors.consentFormFile = "사전승낙서를 첨부해주세요.";
      }
    }

    if (!step1Data.representativePhone.trim()) {
      newErrors.representativePhone = "대표 전화번호를 입력해주세요.";
    } else if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(step1Data.representativePhone)) {
      newErrors.representativePhone = "전화번호 형식이 올바르지 않습니다.";
    }

    if (!step1Data.email.trim()) {
      newErrors.email = "이메일 주소를 입력해주세요.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(step1Data.email)) {
      newErrors.email = "올바른 이메일 형식이 아닙니다.";
    }

    if (!step1Data.bankName) {
      newErrors.bankName = "은행을 선택해주세요.";
    }

    if (!step1Data.accountNumber.trim()) {
      newErrors.accountNumber = "계좌번호를 입력해주세요.";
    } else if (!/^\d+$/.test(step1Data.accountNumber)) {
      newErrors.accountNumber = "계좌번호는 숫자만 입력 가능합니다.";
    }

    if (!step1Data.accountHolderName.trim()) {
      newErrors.accountHolderName = "예금주명을 입력해주세요.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateStep2 = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!step2Data.username.trim()) {
      newErrors.username = "아이디를 입력해주세요.";
    } else if (!/^[a-z0-9]+$/.test(step2Data.username)) {
      newErrors.username = "아이디는 알파벳 소문자와 숫자로만 구성되어야 합니다.";
    } else if (
      step2Data.username.length < 4 ||
      step2Data.username.length > 10
    ) {
      newErrors.username = "아이디는 4자 이상 10자 이하여야 합니다.";
    }

    if (!step2Data.password.trim()) {
      newErrors.password = "비밀번호를 입력해주세요.";
    } else if (
      step2Data.password.length < 8 ||
      step2Data.password.length > 20
    ) {
      newErrors.password = "비밀번호는 8자 이상 20자 이하여야 합니다.";
    }

    if (step2Data.password !== step2Data.confirmPassword) {
      newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
    }

    if (!step2Data.name.trim()) {
      newErrors.name = "이름을 입력해주세요.";
    }

    if (!step2Data.nickname.trim()) {
      newErrors.nickname = "닉네임을 입력해주세요.";
    } else if (
      step2Data.nickname.length < 2 ||
      step2Data.nickname.length > 10
    ) {
      newErrors.nickname = "닉네임은 2자 이상 10자 이하여야 합니다.";
    } else if (!/^[가-힣a-zA-Z0-9_-]+$/.test(step2Data.nickname)) {
      newErrors.nickname = "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다.";
    }

    if (!step2Data.termsOfService || !step2Data.privacyPolicy) {
      newErrors.agreements = "이용약관 및 개인정보처리방침에 동의해주세요.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleStep1Next = () => {
    if (validateStep1()) {
      setStep(2);
    }
  };

  const handleSubmit = async () => {
    if (!validateStep2()) {
      return;
    }

    setIsLoading(true);
    try {
      const businessAddress: AddressDto = {
        postalCode: step1Data.businessPostalCode,
        address: step1Data.businessAddress,
        detailAddress: step1Data.businessDetailAddress,
      };

      // 대리점인 경우 판매점 주소를 사업장 주소와 동일하게 설정
      const storeAddress: AddressDto = step1Data.isAgent
        ? {
            postalCode: step1Data.businessPostalCode,
            address: step1Data.businessAddress,
            detailAddress: step1Data.businessDetailAddress,
          }
        : {
            postalCode: step1Data.storePostalCode,
            address: step1Data.storeAddress,
            detailAddress: step1Data.storeDetailAddress,
          };

      const settlementAccount: SettlementAccountDto = {
        bankName: step1Data.bankName,
        accountNumber: step1Data.accountNumber,
        accountHolderName: step1Data.accountHolderName,
      };

      const requestDto: SellerRegisterRequestDto = {
        businessNumber: step1Data.businessNumber.replace(/-/g, ""),
        businessLicenseFileUrl: step1Data.businessLicenseFileUrl,
        storeName: step1Data.storeName,
        representativeName: step1Data.representativeName,
        isAgent: step1Data.isAgent,
        businessAddress,
        storeAddress,
        consentNumber: step1Data.consentNumber || undefined,
        consentFormFileUrl: step1Data.consentFormFileUrl || undefined,
        representativePhone: step1Data.representativePhone,
        email: step1Data.email,
        customerServicePhone: step1Data.customerServicePhone || undefined,
        settlementAccount,
        userInfo: {
          username: step2Data.username,
          password: step2Data.password,
          name: step2Data.name,
          nickname: step2Data.nickname,
        },
      };

      await sellerService.registerSeller(requestDto);
      navigate("/seller/login");
    } catch (error: any) {
      const errorMessage =
        error.response?.data?.message || "회원가입에 실패했습니다.";
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const formatBusinessNumber = (value: string) => {
    const numbers = value.replace(/-/g, "").slice(0, 10);
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 5)
      return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 5)}-${numbers.slice(5)}`;
  };

  const formatPhoneNumber = (value: string) => {
    const numbers = value.replace(/-/g, "").slice(0, 11);
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7)
      return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7)}`;
  };

  return (
    <div className="min-h-screen w-full flex flex-col bg-gradient-to-b from-sky-50 via-sky-50/50 to-white">
      {/* 헤더 */}
      <header className="w-full px-6 py-4 flex items-center justify-between border-b border-gray-200 bg-white">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-600 rounded flex items-center justify-center">
            <svg
              className="w-6 h-6 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
              />
            </svg>
          </div>
          <h1 className="text-xl font-bold text-gray-900">폰비드 판매자센터</h1>
        </div>
        <nav className="flex items-center gap-6">
          <a
            href="#"
            className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
          >
            서비스 소개
          </a>
          <a
            href="#"
            className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
          >
            이용 가이드
          </a>
          <a
            href="#"
            className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
          >
            고객센터
          </a>
        </nav>
      </header>

      {/* 진행 표시 바 */}
      <div className="w-full bg-white border-b border-gray-200 py-6">
        <div className="max-w-7xl mx-auto px-8 lg:px-12">
          <div className="flex items-center gap-20 max-w-md">
            {/* 1단계 */}
            <div className="flex items-center gap-3">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center ${
                  step === 1 ? "bg-blue-600" : "bg-gray-300"
                }`}
              >
                <span
                  className={`text-sm font-bold ${
                    step === 1 ? "text-white" : "text-gray-600"
                  }`}
                >
                  1
                </span>
              </div>
              <span
                className={`text-sm font-medium whitespace-nowrap ${
                  step === 1 ? "text-blue-600" : "text-gray-600"
                }`}
              >
                사업자 정보 입력
              </span>
            </div>
            
            {/* 연결선 */}
            <div
              className={`flex-1 h-1 rounded-full min-w-[160px] ${
                step >= 2 ? "bg-blue-600" : "bg-gray-300"
              }`}
            />
            
            {/* 2단계 */}
            <div className="flex items-center gap-3">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center ${
                  step === 2 ? "bg-blue-600" : "bg-gray-300"
                }`}
              >
                <span
                  className={`text-sm font-bold ${
                    step === 2 ? "text-white" : "text-gray-600"
                  }`}
                >
                  2
                </span>
              </div>
              <span
                className={`text-sm font-medium whitespace-nowrap ${
                  step === 2 ? "text-blue-600" : "text-gray-600"
                }`}
              >
                회원 정보 입력
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 메인 콘텐츠 */}
      <div className="flex-1 flex flex-col lg:flex-row bg-gray-50">
        {/* 좌측: 회원가입 폼 */}
        <div className="lg:w-3/5 p-8 lg:p-12 overflow-y-auto bg-white lg:pr-2">
          <div className="max-w-3xl mx-auto">

            <h2 className="text-3xl font-bold text-gray-900 mb-2">
              판매자 회원가입
            </h2>
            <p className="text-gray-600 mb-8">
              사업자 정보를 입력하고 판매를 시작하세요
            </p>

            {step === 1 ? (
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
                    <h3 className="text-lg font-semibold text-gray-900">
                      사업자 정보
                    </h3>
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
                              businessNumber: formatBusinessNumber(
                                e.target.value
                              ),
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
                            onChange={(e) =>
                              handleFileChange(e, "businessLicense")
                            }
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
                        <p className="mt-1 text-sm text-red-600">
                          {errors.storeName}
                        </p>
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
                          placeholder="판매점 이름 입력"
                          value={step1Data.storeName}
                          onChange={(e) =>
                            setStep1Data((prev) => ({
                              ...prev,
                              storeName: e.target.value,
                            }))
                          }
                          className="w-full px-3 py-2 border border-gray-300 rounded-md"
                        />
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
                          <label
                            htmlFor="consentForm"
                            className="cursor-pointer"
                          >
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
                            <p className="text-sm text-gray-600">
                              클릭하여 파일 선택
                            </p>
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
                    <h3 className="text-lg font-semibold text-gray-900">
                      연락처 정보
                    </h3>
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
                            representativePhone: formatPhoneNumber(
                              e.target.value
                            ),
                          }))
                        }
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
                        <p className="mt-1 text-sm text-red-600">
                          {errors.email}
                        </p>
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
                            customerServicePhone: formatPhoneNumber(
                              e.target.value
                            ),
                          }))
                        }
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
                        <p className="mt-1 text-sm text-red-600">
                          {errors.bankName}
                        </p>
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
                          errors.accountNumber
                            ? "border-red-500"
                            : "border-gray-300"
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
                  <Button
                    onClick={() => navigate("/seller/login")}
                    variant="secondary"
                    className="flex-1"
                  >
                    취소
                  </Button>
                  <Button onClick={handleStep1Next} className="flex-1">
                    다음 단계
                  </Button>
                </div>
              </div>
            ) : (
              <div className="space-y-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  회원 정보 입력
                </h3>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    아이디 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    placeholder="아이디를 입력하세요"
                    value={step2Data.username}
                    onChange={(e) =>
                      setStep2Data((prev) => ({
                        ...prev,
                        username: e.target.value.toLowerCase(),
                      }))
                    }
                    className={`w-full px-3 py-2 border rounded-md ${
                      errors.username ? "border-red-500" : "border-gray-300"
                    }`}
                  />
                  {errors.username && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.username}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    비밀번호 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="password"
                    placeholder="비밀번호를 입력하세요"
                    value={step2Data.password}
                    onChange={(e) =>
                      setStep2Data((prev) => ({
                        ...prev,
                        password: e.target.value,
                      }))
                    }
                    className={`w-full px-3 py-2 border rounded-md ${
                      errors.password ? "border-red-500" : "border-gray-300"
                    }`}
                  />
                  {errors.password && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.password}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    비밀번호 확인 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="password"
                    placeholder="비밀번호를 다시 입력하세요"
                    value={step2Data.confirmPassword}
                    onChange={(e) =>
                      setStep2Data((prev) => ({
                        ...prev,
                        confirmPassword: e.target.value,
                      }))
                    }
                    className={`w-full px-3 py-2 border rounded-md ${
                      errors.confirmPassword
                        ? "border-red-500"
                        : "border-gray-300"
                    }`}
                  />
                  {errors.confirmPassword && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.confirmPassword}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    이름 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    placeholder="이름을 입력하세요"
                    value={step2Data.name}
                    onChange={(e) =>
                      setStep2Data((prev) => ({
                        ...prev,
                        name: e.target.value,
                      }))
                    }
                    className={`w-full px-3 py-2 border rounded-md ${
                      errors.name ? "border-red-500" : "border-gray-300"
                    }`}
                  />
                  {errors.name && (
                    <p className="mt-1 text-sm text-red-600">{errors.name}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    닉네임 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    placeholder="닉네임을 입력하세요"
                    value={step2Data.nickname}
                    onChange={(e) =>
                      setStep2Data((prev) => ({
                        ...prev,
                        nickname: e.target.value,
                      }))
                    }
                    className={`w-full px-3 py-2 border rounded-md ${
                      errors.nickname ? "border-red-500" : "border-gray-300"
                    }`}
                  />
                  {errors.nickname && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.nickname}
                    </p>
                  )}
                </div>

                <div>
                  <label className="flex items-center gap-2 mb-4">
                    <input
                      type="checkbox"
                      checked={step2Data.termsOfService}
                      onChange={(e) =>
                        setStep2Data((prev) => ({
                          ...prev,
                          termsOfService: e.target.checked,
                        }))
                      }
                      className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                    />
                    <span className="text-sm text-gray-700">
                      이용약관에 동의합니다 <span className="text-red-500">*</span>
                    </span>
                  </label>
                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={step2Data.privacyPolicy}
                      onChange={(e) =>
                        setStep2Data((prev) => ({
                          ...prev,
                          privacyPolicy: e.target.checked,
                        }))
                      }
                      className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                    />
                    <span className="text-sm text-gray-700">
                      개인정보처리방침에 동의합니다{" "}
                      <span className="text-red-500">*</span>
                    </span>
                  </label>
                  {errors.agreements && (
                    <p className="mt-2 text-sm text-red-600">
                      {errors.agreements}
                    </p>
                  )}
                </div>

                <div className="flex gap-4 pt-4">
                  <Button
                    onClick={() => setStep(1)}
                    variant="secondary"
                    className="flex-1"
                  >
                    이전 단계
                  </Button>
                  <Button
                    onClick={handleSubmit}
                    disabled={isLoading}
                    className="flex-1"
                  >
                    {isLoading ? "가입 중..." : "가입 완료"}
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 우측: 정보 섹션 */}
        <div className="lg:w-2/5 bg-gray-50 p-8 lg:p-12 lg:pl-2">
          <div className="max-w-md mx-auto space-y-6">
            {/* 판매자 가입 혜택 */}
            <div className="shadow-lg">
              <div className="bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg p-6 mb-4">
                <div className="flex items-center gap-2">
                  <svg
                    className="w-6 h-6 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M13 10V3L4 14h7v7l9-11h-7z"
                    />
                  </svg>
                  <h3 className="text-lg font-semibold text-white">
                    판매자 가입 혜택
                  </h3>
                </div>
              </div>
              <div className="bg-white rounded-lg p-6 shadow-sm space-y-5">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                    <span className="text-blue-600 text-lg font-bold">%</span>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-gray-900 mb-1">
                      낮은 수수료
                    </h4>
                    <p className="text-xs text-gray-600">
                      판매금액의 5%만 수수료로 부과
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
                    <svg
                      className="w-5 h-5 text-green-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-gray-900 mb-1">
                      빠른 정산
                    </h4>
                    <p className="text-xs text-gray-600">
                      매주 화요일 자동 정산
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
                    <svg
                      className="w-5 h-5 text-purple-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                      />
                    </svg>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-gray-900 mb-1">
                      판매 분석
                    </h4>
                    <p className="text-xs text-gray-600">
                      실시간 판매 현황 및 통계 제공
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0">
                    <svg
                      className="w-5 h-5 text-orange-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z"
                      />
                    </svg>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-gray-900 mb-1">
                      전담 지원
                    </h4>
                    <p className="text-xs text-gray-600">
                      판매자 전용 고객센터 운영
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* 가입 절차 */}
            <div className="bg-white rounded-lg p-6 shadow-lg">
              <div className="flex items-center gap-2 mb-4">
                <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
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
                      d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
                    />
                  </svg>
                </div>
                <h3 className="text-lg font-semibold text-gray-900">
                  가입 절차
                </h3>
              </div>
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <div
                    className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                      step === 1
                        ? "bg-blue-600"
                        : "bg-gray-200"
                    }`}
                  >
                    <span
                      className={`text-sm font-bold ${
                        step === 1 ? "text-white" : "text-gray-600"
                      }`}
                    >
                      1
                    </span>
                  </div>
                  <div className="flex-1">
                    <h4 className="text-sm font-semibold text-gray-900 mb-0.5">
                      정보 입력
                    </h4>
                    <p className="text-xs text-gray-600">
                      사업자 정보 및 계좌 입력
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center flex-shrink-0">
                    <span className="text-sm font-bold text-gray-600">2</span>
                  </div>
                  <div className="flex-1">
                    <h4 className="text-sm font-semibold text-gray-900 mb-0.5">
                      서류 심사
                    </h4>
                    <p className="text-xs text-gray-600">
                      1-2 영업일 소요
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center flex-shrink-0">
                    <span className="text-sm font-bold text-gray-600">3</span>
                  </div>
                  <div className="flex-1">
                    <h4 className="text-sm font-semibold text-gray-900 mb-0.5">
                      승인 완료
                    </h4>
                    <p className="text-xs text-gray-600">
                      판매 시작
                    </p>
                  </div>
                </div>
              </div>
              <div className="border-t border-gray-200 mt-4 pt-4">
                <div className="flex items-center gap-3">
                  <svg
                    className="w-6 h-6 text-blue-600"
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
                  <div>
                    <p className="text-sm font-semibold text-gray-900">가입 문의</p>
                    <p className="text-base font-bold text-gray-900">1588-1234</p>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>

      {/* 푸터 */}
      <footer className="w-full px-6 py-8 bg-blue-900 text-white">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-6">
            <div>
              <h4 className="font-semibold mb-3">회사소개</h4>
              <ul className="space-y-2 text-sm text-blue-200">
                <li>
                  <a href="#" className="hover:text-white">
                    회사소개
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white">
                    인재 채용
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white">
                    공지사항
                  </a>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-3">판매자 정보</h4>
              <ul className="space-y-2 text-sm text-blue-200">
                <li>
                  <a href="#" className="hover:text-white">
                    판매자 가이드
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white">
                    정산 안내
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white">
                    수수료 안내
                  </a>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-3">고객센터</h4>
              <ul className="space-y-2 text-sm text-blue-200">
                <li>
                  <a href="#" className="hover:text-white">
                    FAQ
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white">
                    1:1 문의
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white">
                    이용약관
                  </a>
                </li>
              </ul>
            </div>
          </div>
          <div className="border-t border-blue-800 pt-6">
            <div className="flex flex-col md:flex-row justify-between items-center gap-4 text-sm text-blue-200">
              <div>
                <p>고객센터: 1588-1234</p>
                <p>평일 09:00-18:00</p>
                <p>이메일: support@marketplace.com</p>
              </div>
              <p>© 2024 MarketPlace. All rights reserved.</p>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default SellerSignupPage;

