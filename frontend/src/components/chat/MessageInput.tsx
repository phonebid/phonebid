import { useState, useCallback, KeyboardEvent } from "react";
import type { WebSocketConnectionStatus } from "types/ChatTypes";

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  connectionStatus: WebSocketConnectionStatus;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 메시지 입력 필드 및 전송 버튼 컴포넌트
 */
export function MessageInput({
  onSendMessage,
  connectionStatus,
  disabled = false,
  placeholder = "메시지를 입력하세요.",
}: MessageInputProps) {
  const [inputMessage, setInputMessage] = useState("");

  const handleSend = useCallback(() => {
    if (!inputMessage.trim() || connectionStatus !== "CONNECTED" || disabled) {
      return;
    }

    onSendMessage(inputMessage.trim());
    setInputMessage("");
  }, [inputMessage, connectionStatus, disabled, onSendMessage]);

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
        {/* 프로필 이미지 (선택사항) */}
        <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center flex-shrink-0">
          <span className="text-gray-400 text-xs">나</span>
        </div>
        
        <textarea
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
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

