interface ChatAvatarProps {
  avatar?: string;
  name?: string;
  size?: "sm" | "md";
  alt?: string;
}

/**
 * 채팅 프로필 이미지 컴포넌트
 * 프로필 이미지가 없을 경우 기본 사람 아이콘을 표시합니다.
 */
export function ChatAvatar({
  avatar,
  name,
  size = "sm",
  alt,
}: ChatAvatarProps) {
  const sizeClasses = {
    sm: "w-8 h-8",
    md: "w-10 h-10",
  };

  const iconSizeClasses = {
    sm: "w-6 h-6",
    md: "w-6 h-6",
  };

  return (
    <div className={`flex-shrink-0 ${sizeClasses[size]}`}>
      {avatar ? (
        <img
          src={avatar}
          alt={alt || name || "사용자"}
          className={`${sizeClasses[size]} rounded-full object-cover`}
        />
      ) : (
        <div
          className={`${sizeClasses[size]} bg-gray-300 rounded-full flex items-center justify-center`}
        >
          <svg
            className={`${iconSizeClasses[size]} text-gray-500`}
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
      )}
    </div>
  );
}

