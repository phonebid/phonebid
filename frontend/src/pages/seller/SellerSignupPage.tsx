import { useNavigate } from "react-router-dom";
import { useSellerSignup } from "hooks/useSellerSignup";
import SellerSignupStep1 from "components/seller/SellerSignupStep1";
import SellerSignupStep2 from "components/seller/SellerSignupStep2";
import SellerSignupBenefits from "components/seller/SellerSignupBenefits";

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
  const {
    step,
    step1Data,
    step2Data,
    errors,
    isLoading,
    setStep,
    setStep1Data,
    setStep2Data,
    handleAddressSearch,
    handleFileChange,
    handleStep1Next,
    handleSubmit,
  } = useSellerSignup();

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
              <SellerSignupStep1
                step1Data={step1Data}
                errors={errors}
                setStep1Data={setStep1Data}
                handleAddressSearch={handleAddressSearch}
                handleFileChange={handleFileChange}
                handleStep1Next={handleStep1Next}
                onCancel={() => navigate("/seller/login")}
              />
            ) : (
              <SellerSignupStep2
                step2Data={step2Data}
                errors={errors}
                isLoading={isLoading}
                setStep2Data={setStep2Data}
                handleSubmit={handleSubmit}
                onBack={() => setStep(1)}
              />
            )}
          </div>
        </div>

        {/* 우측: 정보 섹션 */}
        <div className="lg:w-2/5 bg-gray-50 p-8 lg:p-12 lg:pl-2">
          <SellerSignupBenefits currentStep={step} />
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
