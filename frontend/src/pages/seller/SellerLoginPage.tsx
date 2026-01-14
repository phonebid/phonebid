import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import Button from "components/common/Button";
import { apiClient } from "services/apiClient";
import { toast } from "react-toastify";
import type { LoginResponse, User } from "@/types/UserTypes";

const SellerLoginPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, login } = useAuthStore();

  const [credentials, setCredentials] = useState({
    username: "",
    password: "",
  });
  const [errors, setErrors] = useState({
    username: "",
    password: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [keepLoggedIn, setKeepLoggedIn] = useState(false);

  useEffect(() => {
    // 이미 로그인된 판매자만 대시보드로 리다이렉트
    if (isAuthenticated && user?.role === "SELLER") {
      navigate("/seller-center", { replace: true });
    }
    // 구매자로 로그인된 경우에는 로그인 페이지를 보여줌 (판매자 계정으로 로그인하라는 메시지 표시 가능)
  }, [isAuthenticated, user, navigate]);

  const handleLogin = async () => {
    const newErrors = {
      username: "",
      password: "",
    };

    if (!credentials.username.trim()) {
      newErrors.username = "아이디를 입력해주세요.";
    }
    if (!credentials.password.trim()) {
      newErrors.password = "비밀번호를 입력해주세요.";
    }

    setErrors(newErrors);

    if (!newErrors.username && !newErrors.password) {
      setIsLoading(true);
      try {
        const response = await apiClient.post<LoginResponse>(
          "/users/login",
          credentials
        );

        const { username, nickname, role } = response;
        
        if (role !== "SELLER") {
          toast.error("판매자 계정으로만 로그인할 수 있습니다.");
          setIsLoading(false);
          return;
        }

        const userData: User = {
          username,
          nickname,
          role,
        };

        login(userData);

        toast.success("로그인이 완료되었습니다.");
        navigate("/seller-center", { replace: true });
      } catch (error: any) {
        console.error("로그인 실패:", error);
        const errorMessage =
          error.response?.data?.message || "로그인에 실패했습니다.";
        toast.error(errorMessage);
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleLogin();
    }
  };

  return (
    <div className="min-h-screen w-full flex flex-col bg-white">
      {/* 헤더 */}
      <header className="w-full px-6 py-4 flex items-center justify-between border-b border-gray-200">
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

      {/* 메인 콘텐츠 */}
      <div className="flex-1 flex flex-col lg:flex-row">
        {/* 좌측 프로모션 섹션 */}
        <div className="lg:w-1/2 bg-gradient-to-b from-blue-600 to-blue-800 p-12 lg:p-16 flex flex-col justify-center">
          <div className="max-w-2xl">
            <h2 className="text-4xl lg:text-5xl font-bold text-white mb-8 leading-loose">
              <div className="mb-2 text-3xl">동네 1등을 넘어,</div>
              <div className="mb-2">대한민국 대표 성지로</div>
              <div>도약하세요</div>
            </h2>
            <p className="text-xl text-blue-100 mb-16 leading-relaxed">
              견적 입찰부터 배송, 정산까지.<br />
              클릭 몇 번으로 끝내는 간편한 판매 시스템
            </p>

            <div className="space-y-8">
              {/* 실시간 견적 요청 */}
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center flex-shrink-0">
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
                </div>
                <div>
                  <h3 className="text-xl font-semibold text-white mb-2">
                    실시간 견적 요청
                  </h3>
                  <p className="text-blue-100">
                    전국에서 쏟아지는 견적 요청을 실시간으로 확인하고 선점하세요.
                  </p>
                </div>
              </div>

              {/* 확실한 정산 시스템 */}
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center flex-shrink-0">
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
                      d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                    />
                  </svg>
                </div>
                <div>
                  <h3 className="text-xl font-semibold text-white mb-2">
                    확실한 정산 시스템
                  </h3>
                  <p className="text-blue-100">
                    100% 지급을 보장합니다. 미수금 걱정 없는 에스크로 시스템으로 안심하세요.
                  </p>
                </div>
              </div>

              {/* 투명한 정산 */}
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center flex-shrink-0">
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
                      d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                    />
                  </svg>
                </div>
                <div>
                  <h3 className="text-xl font-semibold text-white mb-2">
                    투명한 정산
                  </h3>
                  <p className="text-blue-100">
                    판매 내역부터 예상 정산금까지. 한눈에 매출 흐름을 파악하세요.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 우측 로그인 섹션 */}
        <div className="lg:w-1/2 bg-white p-8 lg:p-12 flex flex-col justify-center">
          <div className="max-w-md w-full mx-auto">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">로그인</h2>
            <p className="text-gray-600 mb-8">판매자 계정으로 로그인하세요</p>

            {/* 구매자로 로그인된 경우 안내 메시지 */}
            {isAuthenticated && user?.role !== "SELLER" && (
              <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <div className="flex items-start gap-3">
                  <svg
                    className="w-5 h-5 text-yellow-600 mt-0.5 flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                    />
                  </svg>
                  <div className="flex-1">
                    <p className="text-sm text-yellow-800">
                      현재 구매자 계정으로 로그인되어 있습니다. 판매자 계정으로 로그인하려면 먼저 로그아웃해주세요.
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* 로그인 폼 */}
            <div className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  아이디
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg
                      className="h-5 w-5 text-gray-400"
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
                  </div>
                  <input
                    type="text"
                    className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="아이디를 입력하세요"
                    value={credentials.username}
                    onChange={(e) =>
                      setCredentials((prev) => ({ ...prev, username: e.target.value }))
                    }
                    onKeyPress={handleKeyPress}
                  />
                </div>
                {errors.username && (
                  <p className="mt-1 text-sm text-red-600">{errors.username}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  비밀번호
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg
                      className="h-5 w-5 text-gray-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                      />
                    </svg>
                  </div>
                  <input
                    type="password"
                    className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="비밀번호를 입력하세요"
                    value={credentials.password}
                    onChange={(e) =>
                      setCredentials((prev) => ({ ...prev, password: e.target.value }))
                    }
                    onKeyPress={handleKeyPress}
                  />
                </div>
                {errors.password && (
                  <p className="mt-1 text-sm text-red-600">{errors.password}</p>
                )}
              </div>

              <div className="flex items-center justify-between">
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                    checked={keepLoggedIn}
                    onChange={(e) => setKeepLoggedIn(e.target.checked)}
                  />
                  <span className="ml-2 text-sm text-gray-600">로그인 상태 유지</span>
                </label>
                <div className="flex items-center gap-2 text-sm">
                  <a
                    href="#"
                    className="text-gray-600 hover:text-gray-900 transition-colors"
                  >
                    아이디 찾기
                  </a>
                  <span className="text-gray-300">|</span>
                  <a
                    href="#"
                    className="text-gray-600 hover:text-gray-900 transition-colors"
                  >
                    비밀번호 찾기
                  </a>
                </div>
              </div>

              <Button
                onClick={handleLogin}
                disabled={isLoading}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-md font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? "로그인 중..." : "로그인"}
              </Button>

              {/* 구분선 */}
              <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-300"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-white text-gray-500">또는</span>
                </div>
              </div>

              {/* 판매자 회원가입 버튼 */}
              <Button
                onClick={() => navigate("/seller/signup")}
                variant="secondary"
                className="w-full border-2 border-blue-600 text-blue-600 hover:bg-blue-50 py-3 rounded-md font-medium transition-colors"
              >
                판매자 회원가입
              </Button>

              {/* 판매자 인증 안내 */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex items-start gap-3">
                  <div className="w-5 h-5 bg-blue-600 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                    <span className="text-white text-xs font-bold">i</span>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-blue-900 mb-1">
                      판매자 인증 안내
                    </h4>
                    <p className="text-xs text-blue-800 leading-relaxed">
                      본 서비스는 사업자 인증을 완료한 판매자만 이용 가능합니다. 회원가입 시 사업자등록번호 및 사전승낙서 인증이 필요합니다.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 푸터 */}
      <footer className="w-full px-6 py-4 border-t border-gray-200 bg-gray-50">
        <div className="w-full flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4">
          <div className="text-sm text-gray-600">
            <p>© 2025 PhoneBid 판매자. All rights reserved.</p>
            <p className="mt-1">사업자등록번호: 123-45-67890 | 대표: 홍길동</p>
          </div>
          <div className="flex items-center gap-6">
            <a
              href="#"
              className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
            >
              이용약관
            </a>
            <a
              href="#"
              className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
            >
              개인정보처리방침
            </a>
            <a
              href="#"
              className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
            >
              고객센터
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default SellerLoginPage;

