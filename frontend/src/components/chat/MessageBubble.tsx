import type { ChatMessage } from "types/ChatTypes";

interface MessageBubbleProps {
  message: ChatMessage;
  isCurrentUser: boolean;
  showAvatar?: boolean;
}

/**
 * 메시지 말풍선 컴포넌트
 */
export function MessageBubble({
  message,
  isCurrentUser,
  showAvatar = true,
}: MessageBubbleProps) {
  const formatTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  return (
    <div
      className={`flex ${
        isCurrentUser ? "justify-end" : "justify-start"
      }`}
    >
      {!isCurrentUser && showAvatar && (
        <div className="w-8 h-8 bg-gray-300 rounded-full mr-2 flex-shrink-0" />
      )}
      <div
        className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
          isCurrentUser
            ? "bg-indigo-500 text-white"
            : "bg-white text-gray-900"
        }`}
      >
        <p className="text-sm whitespace-pre-wrap break-words">
          {message.content}
        </p>
        <p
          className={`text-xs mt-1 ${
            isCurrentUser ? "text-indigo-100" : "text-gray-500"
          }`}
        >
          {formatTime(message.createdAt)}
        </p>
      </div>
    </div>
  );
}

