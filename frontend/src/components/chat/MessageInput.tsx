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
      <div className="flex items-end space-x-2">
        <textarea
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder={placeholder}
          disabled={isDisabled}
          className="flex-1 min-h-[44px] max-h-32 px-4 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
          rows={1}
        />
        <button
          onClick={handleSend}
          disabled={!inputMessage.trim() || isDisabled}
          className="bg-indigo-500 text-white rounded-full p-3 hover:bg-indigo-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
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
              d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
            />
          </svg>
        </button>
      </div>
    </div>
  );
}

