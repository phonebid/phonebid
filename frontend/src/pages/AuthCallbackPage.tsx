import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuthStore } from "store/authStore";

const AuthCallbackPage = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const processCallback = async () => {
      try {
        // URL에서 파라미터 추출
        const urlParams = new URLSearchParams(window.location.search);
        const provider = urlParams.get("provider");
        const error = urlParams.get("error");

        // 에러 체크
        if (error) {
          throw new Error("OAuth 인증이 취소되었습니다.");
        }

        // Provider 확인
        if (!provider || (provider !== "KAKAO" && provider !== "NAVER")) {
          throw new Error("OAuth 제공자를 확인할 수 없습니다.");
        }

        // 쿠키에 토큰이 이미 설정되어 있으므로 프로필 API로 사용자 정보 조회
        await useAuthStore.getState().checkAuth();

        toast.success(`${provider} 로그인이 완료되었습니다.`);

        // 세션 스토리지 정리
        sessionStorage.removeItem("oauth_state");
        sessionStorage.removeItem("oauth_provider");

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
  }, [navigate]);

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
