import { useState, useCallback, KeyboardEvent, useEffect, useRef } from "react";
import type { WebSocketConnectionStatus } from "types/ChatTypes";

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  onTyping?: (isTyping: boolean) => void;
  connectionStatus: WebSocketConnectionStatus;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 메시지 입력 필드 및 전송 버튼 컴포넌트
 */
export function MessageInput({
  onSendMessage,
  onTyping,
  connectionStatus,
  disabled = false,
  placeholder = "메시지를 입력하세요.",
}: MessageInputProps) {
  const [inputMessage, setInputMessage] = useState("");
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const isTypingRef = useRef(false);

  // 타이핑 이벤트 전송 (디바운싱)
  const handleTyping = useCallback(() => {
    if (!onTyping || connectionStatus !== "CONNECTED" || disabled) {
      return;
    }

    // 타이핑 시작
    if (!isTypingRef.current) {
      isTypingRef.current = true;
      onTyping(true);
    }

    // 기존 타이머 클리어
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    // 3초 후 타이핑 종료 이벤트 전송
    typingTimeoutRef.current = setTimeout(() => {
      if (isTypingRef.current) {
        isTypingRef.current = false;
        onTyping(false);
      }
    }, 3000);
  }, [onTyping, connectionStatus, disabled]);

  // 컴포넌트 언마운트 시 타이머 정리
  useEffect(() => {
    return () => {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
      // 언마운트 시 타이핑 종료 이벤트 전송
      if (isTypingRef.current && onTyping) {
        onTyping(false);
      }
    };
  }, [onTyping]);

  const handleSend = useCallback(() => {
    if (!inputMessage.trim() || connectionStatus !== "CONNECTED" || disabled) {
      return;
    }

    // 타이핑 종료 이벤트 전송
    if (isTypingRef.current && onTyping) {
      isTypingRef.current = false;
      onTyping(false);
    }
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    onSendMessage(inputMessage.trim());
    setInputMessage("");
  }, [inputMessage, connectionStatus, disabled, onSendMessage, onTyping]);

  const handleKeyPress = useCallback(
    (e: KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        handleSend();
      }
    },
    [handleSend]
  );

  const isDisabled = disabled || connectionStatus !== "CONNECTED";

  return (
    <div className="border-t bg-white p-4">
      <div className="flex items-end gap-2">
        <textarea
          value={inputMessage}
          onChange={(e) => {
            setInputMessage(e.target.value);
            handleTyping();
          }}
          onKeyPress={handleKeyPress}
          placeholder={placeholder}
          disabled={isDisabled}
          className="flex-1 min-h-[44px] max-h-32 px-4 py-2.5 border border-gray-200 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-[#615FFF] focus:border-[#615FFF] disabled:bg-gray-100 disabled:cursor-not-allowed text-sm"
          rows={1}
        />
        <button
          onClick={handleSend}
          disabled={!inputMessage.trim() || isDisabled}
          className="bg-[#615FFF] text-white rounded-full w-10 h-10 flex items-center justify-center hover:bg-[#615FFF]/90 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex-shrink-0"
          aria-label="메시지 전송"
        >
          <svg
            className="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 10l7-7m0 0l7 7m-7-7v18"
            />
          </svg>
        </button>
      </div>
    </div>
  );
}

