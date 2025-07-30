import { ButtonHTMLAttributes } from "react";

interface SocialLoginButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement> {
  provider: "kakao" | "naver";
  loading?: boolean;
}

const SocialLoginButton = ({
  provider,
  loading = false,
  disabled,
  onClick,
  ...props
}: SocialLoginButtonProps) => {
  const providerConfig = {
    kakao: {
      name: "카카오",
      bgColor: "bg-yellow-300 hover:bg-yellow-400",
      textColor: "text-gray-900",
      icon: "💬",
      borderColor: "border-yellow-300",
    },
    naver: {
      name: "네이버",
      bgColor: "bg-green-500 hover:bg-green-600",
      textColor: "text-white",
      icon: "N",
      borderColor: "border-green-500",
    },
  };

  const config = providerConfig[provider];
  const isDisabled = disabled || loading;

  return (
    <button
      onClick={onClick}
      disabled={isDisabled}
      className={`
        w-full py-3 px-4 rounded-lg font-medium text-base
        flex items-center justify-center space-x-3
        transition-all duration-200 transform
        ${config.bgColor} ${config.textColor} border ${config.borderColor}
        ${
          isDisabled
            ? "opacity-50 cursor-not-allowed"
            : "hover:scale-[1.02] active:scale-[0.98] shadow-md hover:shadow-lg"
        }
        focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500
      `}
      {...props}
    >
      {loading ? (
        <>
          <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
          <span>로그인 중...</span>
        </>
      ) : (
        <>
          <span className="text-lg font-bold">
            {provider === "naver" ? (
              <div className="w-5 h-5 bg-white text-green-500 rounded text-sm font-black flex items-center justify-center">
                N
              </div>
            ) : (
              config.icon
            )}
          </span>
          <span>{config.name} 로그인</span>
        </>
      )}
    </button>
  );
};

export default SocialLoginButton;
