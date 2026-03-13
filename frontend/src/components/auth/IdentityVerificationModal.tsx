import { useState } from "react";
import { loadPortOneSdk } from "utils/portoneLoader";
import {
  getVerificationInitInfo,
  confirmVerification,
} from "services/identityVerificationService";
import { useAuthStore } from "store/authStore";
import { toast } from "react-toastify";

interface Props {
  isOpen: boolean;
  onClose: () => void;
}

export const IdentityVerificationModal: React.FC<Props> = ({
  isOpen,
  onClose,
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const setIdentityVerified = useAuthStore((s) => s.setIdentityVerified);

  const handleVerify = async () => {
    setIsLoading(true);
    try {
      const { storeId, channelKey } = await getVerificationInitInfo();

      const PortOne = await loadPortOneSdk();

      const identityVerificationId = `iv-${crypto.randomUUID().replace(/-/g, "")}`;

      const response = await PortOne.requestIdentityVerification({
        storeId,
        identityVerificationId,
        channelKey,
      });

      if (response?.code) {
        toast.error(response.message || "본인인증이 취소되었습니다.");
        return;
      }

      const result = await confirmVerification(identityVerificationId);

      if (result.verified) {
        setIdentityVerified();
        toast.success("본인인증이 완료되었습니다.");
        onClose();
      }
    } catch (error) {
      console.error("본인인증 오류:", error);
      toast.error("본인인증 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white rounded-2xl p-8 max-w-md w-full mx-4 shadow-xl">
        <div className="text-center mb-6">
          <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg
              className="w-8 h-8 text-blue-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
              />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-gray-900">
            본인인증이 필요합니다
          </h2>
          <p className="text-gray-500 mt-2 text-sm leading-relaxed">
            서비스를 이용하시려면 본인인증을 완료해주세요.
            <br />
            휴대폰 인증으로 간편하게 진행됩니다.
          </p>
        </div>
        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 py-3 border border-gray-300 rounded-xl text-gray-600 font-medium hover:bg-gray-50 transition-colors"
          >
            나중에 하기
          </button>
          <button
            onClick={handleVerify}
            disabled={isLoading}
            className="flex-1 py-3 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? "인증 중..." : "본인인증 하기"}
          </button>
        </div>
      </div>
    </div>
  );
};
