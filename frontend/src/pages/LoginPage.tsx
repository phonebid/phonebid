import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import SocialLoginButton from "components/auth/SocialLoginButton";
import Input from "components/common/Input";
import Button from "components/common/Button";
import { loginWithKakao, loginWithNaver } from "services/authService";
import { apiClient } from "services/apiClient";
import { toast } from "react-toastify";
import type { LoginResponse, User } from "@/types/UserTypes";

const LoginPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { isAuthenticated, login } = useAuthStore();

  // 일반 로그인 상태
  const [credentials, setCredentials] = useState({
    username: "",
    password: "",
  });
  const [errors, setErrors] = useState({
    username: "",
    password: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [showOAuthError, setShowOAuthError] = useState(false);

  // 이미 로그인된 사용자는 홈으로 리다이렉트
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  // URL 파라미터에서 OAuth 에러 확인
  useEffect(() => {
    const error = searchParams.get("error");
    if (error === "social_login_failed") {
      setShowOAuthError(true);
      toast.error("소셜 로그인에 실패했습니다. 아래 안내를 확인해주세요.");
      // URL에서 에러 파라미터 제거
      navigate("/login", { replace: true });
    }
  }, [searchParams, navigate]);

  // 일반 로그인 처리
  const handleLogin = async () => {
    // 기본 검증
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

    // 검증 통과 시 로그인 처리
    if (!newErrors.username && !newErrors.password) {
      setIsLoading(true);
      try {
        const response = await apiClient.post<LoginResponse>(
          "/users/login",
          credentials
        );

        // 쿠키에 토큰이 자동으로 저장되므로 사용자 정보만 저장
        const { username, nickname, role } = response;
        const userData: User = {
          username,
          nickname,
          role,
        };

        login(userData);

        toast.success("로그인이 완료되었습니다.");
        navigate("/", { replace: true });
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

  const handleKakaoLogin = () => {
    try {
      loginWithKakao(); // URL 리다이렉트이므로 await 불필요
    } catch (error) {
      console.error("카카오 로그인 실패:", error);
    }
  };

  const handleNaverLogin = () => {
    try {
      loginWithNaver(); // URL 리다이렉트이므로 await 불필요
    } catch (error) {
      console.error("네이버 로그인 실패:", error);
    }
  };

  return (
    // 전체화면 배경
    <div className="min-h-screen w-full flex flex-col">
      <div className="flex-1 flex items-center justify-center px-4">
        <div className="max-w-md w-full">
          {/* 로고 및 헤더 */}
          <div className="text-center mb-12">
            <p className="text-lg text-gray-600">가장 저렴한 핸드폰 구매는</p>
            <h1 className="text-4xl font-bold text-gray-900 mb-3">PhoneBid</h1>
          </div>

          {/* 일반 로그인 폼 */}
          <div className="space-y-4 mb-8">
            <Input
              label="아이디"
              type="text"
              placeholder="아이디를 입력하세요"
              value={credentials.username}
              onChange={(value) =>
                setCredentials((prev) => ({ ...prev, username: value }))
              }
              error={errors.username}
              required
            />

            <Input
              label="비밀번호"
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={credentials.password}
              onChange={(value) =>
                setCredentials((prev) => ({ ...prev, password: value }))
              }
              error={errors.password}
              required
            />

            <Button
              onClick={handleLogin}
              disabled={isLoading}
              className="bg-indigo-100 text-indigo-700 w-full px-6 py-3 rounded-lg font-medium text-sm border border-indigo-200 hover:bg-indigo-200 hover:border-indigo-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? "로그인 중..." : "로그인"}
            </Button>
          </div>

          {/* 소셜 로그인 버튼들 */}
          <div className="space-y-4 w-full">
            {showOAuthError && (
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-4">
                <div className="flex items-start">
                  <svg
                    className="w-5 h-5 text-yellow-600 mt-0.5 mr-2 flex-shrink-0"
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
                    <h3 className="text-sm font-semibold text-yellow-800 mb-1">
                      소셜 로그인이 되지 않나요? 다음 사항을 확인해주세요.
                    </h3>
                    <ul className="text-xs text-yellow-700 space-y-1 list-disc list-inside">
                      <li>
                        브라우저의 <strong>광고 차단 확장 프로그램</strong>을
                        비활성화해보세요
                      </li>
                      <li>
                        팝업 차단이 해제되어 있는지 확인해주세요
                      </li>
                      <li>
                        시크릿 모드(프라이빗 모드)에서 다시 시도해보세요
                      </li>
                      <li>
                        다른 브라우저(Chrome, Edge, Safari 등)에서
                        시도해보세요
                      </li>
                      <li>
                        문제가 계속되면 네이버 로그인을 이용하시거나 고객센터로
                        문의해주세요
                      </li>
                    </ul>
                    <button
                      onClick={() => setShowOAuthError(false)}
                      className="text-xs text-yellow-600 hover:text-yellow-800 mt-2 underline"
                    >
                      닫기
                    </button>
                  </div>
                </div>
              </div>
            )}

            <SocialLoginButton provider="kakao" onClick={handleKakaoLogin} />

            <SocialLoginButton provider="naver" onClick={handleNaverLogin} />

          </div>

          <div className="text-muted-foreground flex justify-center gap-1 text-sm mt-4">
            <p>아직 회원이 아니신가요?</p>
            <div
              onClick={() => navigate("/signup")}
              className="font-medium hover:underline text-indigo-700 cursor-pointer"
            >
              회원가입
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
