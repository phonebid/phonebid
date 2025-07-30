import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import SocialLoginButton from "components/auth/SocialLoginButton";

const LoginPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, loginWithKakao, loginWithNaver } = useAuthStore();

  // 이미 로그인된 사용자는 홈으로 리다이렉트
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

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
          <div className="text-center mb-12 min-h-[60vh] flex flex-col justify-center">
            <p className="text-lg text-gray-600">가장 저렴한 핸드폰 구매는</p>
            <h1 className="text-4xl font-bold text-gray-900 mb-3">PhoneBid</h1>
          </div>

          {/* 소셜 로그인 버튼들 */}
          <div className="space-y-4 w-full ">
            <SocialLoginButton provider="kakao" onClick={handleKakaoLogin} />

            <SocialLoginButton provider="naver" onClick={handleNaverLogin} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
