import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuthStore } from "store/authStore";

const AuthCallbackPage = () => {
  const navigate = useNavigate();
  const { handleOAuthCallback } = useAuthStore();

  useEffect(() => {
    const processCallback = async () => {
      try {
        // URL에서 파라미터 추출
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get("code");
        const state = urlParams.get("state");
        const error = urlParams.get("error");
        const errorDescription = urlParams.get("error_description");

        // 에러 체크
        if (error) {
          const errorMsg = errorDescription || "OAuth 인증이 취소되었습니다.";
          throw new Error(errorMsg);
        }

        // 필수 파라미터 체크
        if (!code) {
          throw new Error("인증 코드가 없습니다.");
        }

        if (!state) {
          throw new Error("State 파라미터가 없습니다.");
        }

        // State에서 provider 정보 추출
        // state 형식: "KAKAO_randomstring" 또는 "NAVER_randomstring"
        let provider = "";
        if (state.startsWith("KAKAO_")) {
          provider = "KAKAO";
        } else if (state.startsWith("NAVER_")) {
          provider = "NAVER";
        } else {
          // 세션 스토리지에서 provider 정보 확인 (fallback)
          const savedProvider = sessionStorage.getItem("oauth_provider");
          if (savedProvider) {
            provider = savedProvider;
            sessionStorage.removeItem("oauth_provider");
          } else {
            throw new Error("OAuth 제공자를 확인할 수 없습니다.");
          }
        }

        // handle OAuth callback

        // AuthStore를 통해 콜백 처리
        await handleOAuthCallback(provider, code, state);

        // 로그인 성공 시 홈으로 이동
        navigate("/", { replace: true });
      } catch (error) {
        console.error("OAuth 콜백 처리 실패:", error);
        toast.error(
          error instanceof Error
            ? error.message
            : "로그인 처리 중 오류가 발생했습니다."
        );
        navigate("/login", { replace: true });
      }
    };

    processCallback();
  }, [navigate, handleOAuthCallback]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
      <div className="text-center">
        <div className="w-16 h-16 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          로그인 처리 중...
        </h2>
        <p className="text-gray-600">잠시만 기다려주세요.</p>
      </div>
    </div>
  );
};

export default AuthCallbackPage;
