import { useState, useCallback, KeyboardEvent, useEffect, useRef } from "react";
import type { WebSocketConnectionStatus } from "types/ChatTypes";
import { uploadChatImage } from "services/chatService";
import { toast } from "react-toastify";

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  onSendImage?: (imageUrl: string) => void;
  onTyping?: (isTyping: boolean) => void;
  connectionStatus: WebSocketConnectionStatus;
  chatRoomId?: string;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 메시지 입력 필드 및 전송 버튼 컴포넌트
 */
export function MessageInput({
  onSendMessage,
  onSendImage,
  onTyping,
  connectionStatus,
  chatRoomId,
  disabled = false,
  placeholder = "메시지를 입력하세요.",
}: MessageInputProps) {
  const [inputMessage, setInputMessage] = useState("");
  const [isUploadingImage, setIsUploadingImage] = useState(false);
  const typingTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const isTypingRef = useRef(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

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

  const handleImageUploadClick = useCallback(() => {
    if (!chatRoomId) {
      toast.error("채팅방 정보가 없습니다.");
      return;
    }
    fileInputRef.current?.click();
  }, [chatRoomId]);

  const handleImageFileChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file || !chatRoomId) return;

      // 파일 유효성 검사
      const allowedTypes = [
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp",
      ];
      if (!allowedTypes.includes(file.type)) {
        toast.error(
          "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)"
        );
        return;
      }

      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        toast.error("파일 크기는 5MB 이하여야 합니다.");
        return;
      }

      try {
        setIsUploadingImage(true);
        const response = await uploadChatImage(chatRoomId, file);
        if (onSendImage) {
          onSendImage(response.imageUrl);
        } else {
          // onSendImage가 없으면 onSendMessage로 이미지 URL 전송
          onSendMessage(response.imageUrl);
        }
      } catch (error) {
        console.error("Failed to upload image", error);
      } finally {
        setIsUploadingImage(false);
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
      }
    },
    [chatRoomId, onSendImage, onSendMessage]
  );

  const isDisabled = disabled || connectionStatus !== "CONNECTED" || isUploadingImage;

  return (
    <div className="border-t bg-white p-4">
      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
        className="hidden"
        onChange={handleImageFileChange}
        disabled={isDisabled}
      />
      <div className="flex items-end gap-2">
        <button
          onClick={handleImageUploadClick}
          disabled={isDisabled}
          className="bg-gray-100 text-gray-600 rounded-full w-10 h-10 flex items-center justify-center hover:bg-gray-200 disabled:bg-gray-100 disabled:cursor-not-allowed transition-colors flex-shrink-0"
          aria-label="이미지 업로드"
          title="이미지 업로드"
        >
          {isUploadingImage ? (
            <svg
              className="w-5 h-5 animate-spin"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
          ) : (
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
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          )}
        </button>
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

