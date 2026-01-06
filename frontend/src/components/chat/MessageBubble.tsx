import type { ChatMessage } from "types/ChatTypes";
import { MessageType } from "types/ChatTypes";
import { ChatAvatar } from "components/chat/ChatAvatar";
import { useState } from "react";

interface MessageBubbleProps {
  message: ChatMessage;
  isCurrentUser: boolean;
  showAvatar?: boolean;
  senderName?: string;
  senderAvatar?: string;
}

/**
 * 메시지 말풍선 컴포넌트
 * 상대방 메시지: 왼쪽 정렬, 흰색 말풍선, 왼쪽에 프로필 이미지
 * 내 메시지: 오른쪽 정렬, 회색 말풍선, 프로필 이미지 없음
 */
export function MessageBubble({
  message,
  isCurrentUser,
  showAvatar = true,
  senderName,
  senderAvatar,
}: MessageBubbleProps) {
  const [imageError, setImageError] = useState(false);
  const [isImageExpanded, setIsImageExpanded] = useState(false);

  const formatTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const isImageMessage = message.messageType === MessageType.IMAGE;

  // 상대방 메시지: 왼쪽 정렬, 흰색 말풍선
  // 내 메시지: 오른쪽 정렬, 회색 말풍선
  return (
    <div
      className={`flex items-end gap-2 ${
        isCurrentUser ? "justify-end" : "justify-start"
      }`}
    >
      {/* 상대방 메시지: 왼쪽 정렬, 프로필 이미지 표시 */}
      {!isCurrentUser && showAvatar && (
        <ChatAvatar avatar={senderAvatar} name={senderName} alt={senderName || "상대방"} />
      )}

      <div className={`flex flex-col max-w-[70%] ${isCurrentUser ? "items-end" : "items-start"} relative`}>
        <div className="flex items-end gap-2">
          {/* 읽음 표시 (내 메시지이고 읽지 않았을 때만, 말풍선 밖 왼쪽 하단) */}
          {isCurrentUser && !message.isRead && (
            <span className="text-xs text-gray-500 font-medium mb-1 flex-shrink-0">1</span>
          )}

          <div
            className={`px-4 py-2.5 rounded-2xl border ${
              isCurrentUser
                ? "bg-[#E0E7FF] text-gray-900 rounded-br-sm border-indigo-200"
                : "bg-white text-gray-900 rounded-bl-sm border-gray-200 shadow-sm"
            }`}
          >
            {isImageMessage ? (
              <div className="space-y-2">
                {imageError ? (
                  <div className="flex items-center justify-center w-64 h-48 bg-gray-100 rounded-lg">
                    <p className="text-sm text-gray-500">이미지를 불러올 수 없습니다.</p>
                  </div>
                ) : (
                  <img
                    src={message.content}
                    alt="전송된 이미지"
                    className={`rounded-lg cursor-pointer ${
                      isImageExpanded ? "max-w-2xl" : "max-w-xs"
                    }`}
                    onClick={() => setIsImageExpanded(!isImageExpanded)}
                    onError={() => setImageError(true)}
                  />
                )}
                <div className="flex items-center justify-end gap-1.5">
                  <p
                    className={`text-xs ${
                      isCurrentUser ? "text-gray-500" : "text-gray-400"
                    }`}
                  >
                    {formatTime(message.createdAt)}
                  </p>
                </div>
              </div>
            ) : (
              <>
                <p className="text-sm whitespace-pre-wrap break-words leading-relaxed">
                  {message.content}
                </p>
                <div className="flex items-center justify-end gap-1.5 mt-1.5">
                  <p
                    className={`text-xs ${
                      isCurrentUser ? "text-gray-500" : "text-gray-400"
                    }`}
                  >
                    {formatTime(message.createdAt)}
                  </p>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

