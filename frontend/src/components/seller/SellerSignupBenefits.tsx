interface SellerSignupBenefitsProps {
  currentStep: 1 | 2;
}

const SellerSignupBenefits = ({ currentStep }: SellerSignupBenefitsProps) => {
  return (
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
              <p className="text-xs text-gray-600">매주 화요일 자동 정산</p>
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
          <h3 className="text-lg font-semibold text-gray-900">가입 절차</h3>
        </div>
        <div className="space-y-3">
          <div className="flex items-start gap-3">
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                currentStep === 1 ? "bg-blue-600" : "bg-gray-200"
              }`}
            >
              <span
                className={`text-sm font-bold ${
                  currentStep === 1 ? "text-white" : "text-gray-600"
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
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                currentStep === 2 ? "bg-blue-600" : "bg-gray-200"
              }`}
            >
              <span
                className={`text-sm font-bold ${
                  currentStep === 2 ? "text-white" : "text-gray-600"
                }`}
              >
                2
              </span>
            </div>
            <div className="flex-1">
              <h4 className="text-sm font-semibold text-gray-900 mb-0.5">
                서류 심사
              </h4>
              <p className="text-xs text-gray-600">1-2 영업일 소요</p>
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
              <p className="text-xs text-gray-600">판매 시작</p>
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
  );
};

export default SellerSignupBenefits;

