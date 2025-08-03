import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import SocialLoginButton from "components/auth/SocialLoginButton";
import Input from "components/common/Input";
import Button from "components/common/Button";

const LoginPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, loginWithKakao, loginWithNaver } = useAuthStore();

  // 일반 로그인 상태
  const [credentials, setCredentials] = useState({
    username: "",
    password: "",
  });
  const [errors, setErrors] = useState({
    username: "",
    password: "",
  });

  // 이미 로그인된 사용자는 홈으로 리다이렉트
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  // 일반 로그인 처리
  const handleLogin = () => {
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

    // 검증 통과 시 로그인 처리 (추후 API 연결)
    if (!newErrors.username && !newErrors.password) {
      console.log("로그인 시도:", credentials);
      // TODO: API 연결 후 실제 로그인 처리
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
              className="bg-indigo-100 text-indigo-700 w-full px-6 py-3 rounded-lg font-medium text-sm border border-indigo-200 hover:bg-indigo-200 hover:border-indigo-300 transition-all duration-200"
            >
              로그인
            </Button>
          </div>

          {/* 구분선 */}
          <div className="relative mb-8">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-white text-gray-500">또는</span>
            </div>
          </div>

          {/* 소셜 로그인 버튼들 */}
          <div className="space-y-4 w-full">
            <SocialLoginButton provider="kakao" onClick={handleKakaoLogin} />

            <SocialLoginButton provider="naver" onClick={handleNaverLogin} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
