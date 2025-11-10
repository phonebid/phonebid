import { useEffect, useRef, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import useSWR from "swr";
import { getChatRoom, getChatMessages, markMessagesAsRead } from "services/chatService";
import { useWebSocket } from "hooks/useWebSocket";
import { useAuthStore } from "store/authStore";
import { realtimeDataConfig } from "services/swrConfig";
import type { ChatRoom, ChatMessage } from "types/ChatTypes";
import { MessageType } from "types/ChatTypes";
import { MessageBubble } from "components/chat/MessageBubble";
import { DateSeparator } from "components/chat/DateSeparator";
import { MessageInput } from "components/chat/MessageInput";

const ChatRoomPage: React.FC = () => {
  const { chatRoomId } = useParams<{ chatRoomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);

  // 채팅방 정보 조회
  const {
    data: chatRoom,
    error: roomError,
    isLoading: roomLoading,
  } = useSWR<ChatRoom>(
    chatRoomId ? `/chat/rooms/${chatRoomId}` : null,
    {
      ...realtimeDataConfig,
      fetcher: () => getChatRoom(chatRoomId!),
    }
  );

  // 메시지 목록 조회
  const {
    data: initialMessages,
    error: messagesError,
    isLoading: messagesLoading,
  } = useSWR<ChatMessage[]>(
    chatRoomId ? `/chat/rooms/${chatRoomId}/messages` : null,
    {
      ...realtimeDataConfig,
      fetcher: () => getChatMessages(chatRoomId!),
    }
  );

  // 초기 메시지 설정
  useEffect(() => {
    if (initialMessages) {
      setMessages(initialMessages);
    }
  }, [initialMessages]);

  // WebSocket 연결 및 메시지 수신
  const { sendMessage: sendWebSocketMessage, connectionStatus } = useWebSocket({
    chatRoomId: chatRoomId || undefined,
    autoConnect: true,
    onMessage: (message: ChatMessage) => {
      setMessages((prev) => [...prev, message]);
      // 읽음 처리
      if (user && message.senderId !== user.username) {
        markMessagesAsRead({
          chatRoomId: chatRoomId!,
          messageIds: [message.id],
        }).catch((error) => {
          console.error("Failed to mark message as read:", error);
        });
      }
    },
  });

  // 자동 스크롤
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  // 메시지 전송
  const handleSendMessage = useCallback(
    (messageContent: string) => {
      if (!chatRoomId || !user) {
        return;
      }

      const messageRequest = {
        chatRoomId,
        senderId: user.username,
        messageType: MessageType.TEXT,
        content: messageContent,
      };

      sendWebSocketMessage(messageRequest);
    },
    [chatRoomId, user, sendWebSocketMessage]
  );

  // 메시지가 같은 날짜인지 확인
  const isSameDate = (date1: string, date2: string): boolean => {
    const d1 = new Date(date1).toDateString();
    const d2 = new Date(date2).toDateString();
    return d1 === d2;
  };

  if (roomLoading || messagesLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <p className="text-gray-500">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (roomError || messagesError || !chatRoom) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <p className="text-red-500">채팅방을 불러오는 중 오류가 발생했습니다.</p>
        </div>
      </div>
    );
  }

  const isCurrentUser = (senderId: string): boolean => {
    return user?.username === senderId;
  };

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      {/* 헤더 */}
      <div className="flex items-center py-4 border-b bg-white">
        <button
          onClick={() => navigate("/chat")}
          className="mr-4 text-gray-600 hover:text-gray-900"
          aria-label="뒤로가기"
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
        </button>
        <div className="flex items-center space-x-3 flex-1">
          <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center">
            <span className="text-gray-500 text-sm">프로필</span>
          </div>
          <div>
            <h1 className="text-lg font-semibold">채팅방</h1>
            <p className="text-xs text-gray-500">
              {connectionStatus === "CONNECTED" ? "연결됨" : "연결 중..."}
            </p>
          </div>
        </div>
      </div>

      {/* 메시지 영역 */}
      <div className="flex-1 overflow-y-auto bg-gray-50 p-4 space-y-4">
        {messages.length === 0 ? (
          <div className="text-center text-gray-500 py-8">
            메시지가 없습니다. 첫 메시지를 보내보세요!
          </div>
        ) : (
          messages.map((message, index) => {
            const prevMessage = index > 0 ? messages[index - 1] : undefined;
            const showDateSeparator =
              index === 0 ||
              !prevMessage ||
              !isSameDate(message.createdAt, prevMessage.createdAt);
            const isCurrentUserMessage = isCurrentUser(message.senderId);

            return (
              <div key={message.id}>
                {/* 날짜 구분선 */}
                {showDateSeparator && (
                  <DateSeparator date={message.createdAt} />
                )}

                {/* 메시지 */}
                <MessageBubble
                  message={message}
                  isCurrentUser={isCurrentUserMessage}
                />
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 메시지 입력 영역 */}
      <MessageInput
        onSendMessage={handleSendMessage}
        connectionStatus={connectionStatus}
      />
    </div>
  );
};

export default ChatRoomPage;

