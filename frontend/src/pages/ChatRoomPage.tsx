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
      // 메시지 중복 방지: 이미 존재하는 메시지는 추가하지 않음
      setMessages((prev) => {
        const exists = prev.some((msg) => msg.id === message.id);
        if (exists) {
          return prev;
        }
        return [...prev, message];
      });

      // 읽음 처리 (자신이 보낸 메시지가 아닌 경우)
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
          <p className="text-red-500 mb-4">
            채팅방을 불러오는 중 오류가 발생했습니다.
          </p>
          <button
            onClick={() => window.location.reload()}
            className="text-indigo-600 hover:text-indigo-700 text-sm font-medium"
          >
            새로고침
          </button>
        </div>
      </div>
    );
  }

  const isCurrentUser = (senderId: string): boolean => {
    // 채팅방 정보가 없으면 비교 불가
    if (!chatRoom) {
      return false;
    }

    // 채팅방의 consumerId 또는 sellerId와 비교
    // 현재 사용자가 consumer인지 seller인지 role로 판단
    const userRole = user?.role;
    
    if (userRole === "CONSUMER") {
      // 구매자인 경우 consumerId와 비교
      return chatRoom.consumerId === senderId;
    } else if (userRole === "SELLER") {
      // 판매자인 경우 sellerId와 비교
      return chatRoom.sellerId === senderId;
    }

    // role이 없거나 알 수 없는 경우, 둘 다 확인
    return chatRoom.consumerId === senderId || chatRoom.sellerId === senderId;
  };

  return (
    <div className="flex flex-col h-screen bg-indigo-50">
      {/* 헤더 */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="flex items-center px-4 py-3 relative">
          <button
            onClick={() => navigate("/chat")}
            className="text-gray-600 hover:text-gray-900 p-1 absolute left-4"
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
          <div className="flex-1 flex justify-center">
            <h1 className="text-base font-semibold text-gray-900">
              {chatRoom.sellerName || "채팅방"}
            </h1>
          </div>
        </div>
      </div>

      {/* 메시지 영역 */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3 scroll-smooth bg-indigo-50">
        {messages.length === 0 ? (
          <div className="text-center text-gray-500 py-12">
            <p className="text-sm">메시지가 없습니다. 첫 메시지를 보내보세요!</p>
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

