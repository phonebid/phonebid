import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import SocialLoginButton from "components/auth/SocialLoginButton";
import Input from "components/common/Input";
import Button from "components/common/Button";
import { loginWithKakao, loginWithNaver } from "services/authService";
import { apiClient } from "services/apiClient";
import { toast } from "react-toastify";

const LoginPage = () => {
  const navigate = useNavigate();
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

  // 이미 로그인된 사용자는 홈으로 리다이렉트
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

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
        const response = await apiClient.post("/users/login", credentials);
        const responseData = response as {
          data: { accessToken: string; user: any };
        };
        const { accessToken, user } = responseData.data;

        // 상태 업데이트 및 토큰 저장
        login(user, accessToken);

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
