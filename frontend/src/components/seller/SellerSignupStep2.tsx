import type { Step2Data } from "utils/sellerValidation";
import Button from "components/common/Button";

interface SellerSignupStep2Props {
  step2Data: Step2Data;
  errors: Record<string, string>;
  isLoading: boolean;
  setStep2Data: React.Dispatch<React.SetStateAction<Step2Data>>;
  handleSubmit: () => Promise<void>;
  onBack: () => void;
}

const SellerSignupStep2 = ({
  step2Data,
  errors,
  isLoading,
  setStep2Data,
  handleSubmit,
  onBack,
}: SellerSignupStep2Props) => {
  return (
    <div className="space-y-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        회원 정보 입력
      </h3>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          아이디 <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          placeholder="아이디를 입력하세요"
          value={step2Data.username}
          onChange={(e) =>
            setStep2Data((prev) => ({
              ...prev,
              username: e.target.value.toLowerCase(),
            }))
          }
          className={`w-full px-3 py-2 border rounded-md ${
            errors.username ? "border-red-500" : "border-gray-300"
          }`}
        />
        {errors.username && (
          <p className="mt-1 text-sm text-red-600">{errors.username}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          비밀번호 <span className="text-red-500">*</span>
        </label>
        <input
          type="password"
          placeholder="비밀번호를 입력하세요"
          value={step2Data.password}
          onChange={(e) =>
            setStep2Data((prev) => ({
              ...prev,
              password: e.target.value,
            }))
          }
          className={`w-full px-3 py-2 border rounded-md ${
            errors.password ? "border-red-500" : "border-gray-300"
          }`}
        />
        {errors.password && (
          <p className="mt-1 text-sm text-red-600">{errors.password}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          비밀번호 확인 <span className="text-red-500">*</span>
        </label>
        <input
          type="password"
          placeholder="비밀번호를 다시 입력하세요"
          value={step2Data.confirmPassword}
          onChange={(e) =>
            setStep2Data((prev) => ({
              ...prev,
              confirmPassword: e.target.value,
            }))
          }
          className={`w-full px-3 py-2 border rounded-md ${
            errors.confirmPassword ? "border-red-500" : "border-gray-300"
          }`}
        />
        {errors.confirmPassword && (
          <p className="mt-1 text-sm text-red-600">
            {errors.confirmPassword}
          </p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          이름 <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          placeholder="이름을 입력하세요"
          value={step2Data.name}
          onChange={(e) =>
            setStep2Data((prev) => ({
              ...prev,
              name: e.target.value,
            }))
          }
          className={`w-full px-3 py-2 border rounded-md ${
            errors.name ? "border-red-500" : "border-gray-300"
          }`}
        />
        {errors.name && (
          <p className="mt-1 text-sm text-red-600">{errors.name}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          닉네임 <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          placeholder="닉네임을 입력하세요"
          value={step2Data.nickname}
          onChange={(e) =>
            setStep2Data((prev) => ({
              ...prev,
              nickname: e.target.value,
            }))
          }
          className={`w-full px-3 py-2 border rounded-md ${
            errors.nickname ? "border-red-500" : "border-gray-300"
          }`}
        />
        {errors.nickname && (
          <p className="mt-1 text-sm text-red-600">{errors.nickname}</p>
        )}
      </div>

      <div>
        <label className="flex items-center gap-2 mb-4">
          <input
            type="checkbox"
            checked={step2Data.termsOfService}
            onChange={(e) =>
              setStep2Data((prev) => ({
                ...prev,
                termsOfService: e.target.checked,
              }))
            }
            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <span className="text-sm text-gray-700">
            이용약관에 동의합니다 <span className="text-red-500">*</span>
          </span>
        </label>
        <label className="flex items-center gap-2">
          <input
            type="checkbox"
            checked={step2Data.privacyPolicy}
            onChange={(e) =>
              setStep2Data((prev) => ({
                ...prev,
                privacyPolicy: e.target.checked,
              }))
            }
            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <span className="text-sm text-gray-700">
            개인정보처리방침에 동의합니다{" "}
            <span className="text-red-500">*</span>
          </span>
        </label>
        {errors.agreements && (
          <p className="mt-2 text-sm text-red-600">{errors.agreements}</p>
        )}
      </div>

      <div className="flex gap-4 pt-4">
        <Button onClick={onBack} variant="secondary" className="flex-1">
          이전 단계
        </Button>
        <Button onClick={handleSubmit} disabled={isLoading} className="flex-1">
          {isLoading ? "가입 중..." : "가입 완료"}
        </Button>
      </div>
    </div>
  );
};

export default SellerSignupStep2;

