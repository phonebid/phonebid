import { ChatAvatar } from "components/chat/ChatAvatar";

interface TypingIndicatorProps {
  senderName?: string;
  senderAvatar?: string;
}

/**
 * 타이핑 인디케이터 컴포넌트
 * 상대방이 입력 중일 때 표시되는 "..." 애니메이션
 */
export function TypingIndicator({
  senderName,
  senderAvatar,
}: TypingIndicatorProps) {
  return (
    <div className="flex items-end gap-2 justify-start">
      {/* 상대방 프로필 이미지 */}
      {senderAvatar && (
        <ChatAvatar avatar={senderAvatar} name={senderName} alt={senderName || "상대방"} />
      )}

      <div className="flex flex-col items-start">
        {/* 발신자명 표시 */}
        {senderName && (
          <span className="text-xs text-gray-500 mb-1 px-1">
            {senderName}
          </span>
        )}

        {/* 타이핑 인디케이터 말풍선 */}
        <div className="px-4 py-2.5 rounded-2xl rounded-bl-sm bg-white border border-gray-200 shadow-sm">
          <div className="flex items-center gap-1">
            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: "0ms" }} />
            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: "150ms" }} />
            <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: "300ms" }} />
          </div>
        </div>
      </div>
    </div>
  );
}

