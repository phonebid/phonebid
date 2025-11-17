import { useEffect, useRef, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import useSWR from "swr";
import { getChatRoom, getChatMessages, markMessagesAsRead } from "services/chatService";
import { getQuoteDetail } from "services/quoteService";
import { useWebSocket } from "hooks/useWebSocket";
import { useAuthStore } from "store/authStore";
import { realtimeDataConfig } from "services/swrConfig";
import type { ChatRoom, ChatMessage } from "types/ChatTypes";
import { MessageType } from "types/ChatTypes";
import type { QuoteDetail } from "types/QuoteTypes";
import { MessageBubble } from "components/chat/MessageBubble";
import { DateSeparator } from "components/chat/DateSeparator";
import { MessageInput } from "components/chat/MessageInput";
import { BidQuoteCard } from "components/chat/BidQuoteCard";
import { TypingIndicator } from "components/chat/TypingIndicator";

const ChatRoomPage: React.FC = () => {
  const { chatRoomId } = useParams<{ chatRoomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isOtherUserTyping, setIsOtherUserTyping] = useState(false);
  const [typingUserId, setTypingUserId] = useState<string | null>(null);
  const isInitialLoadRef = useRef<boolean>(true);
  const prevChatRoomIdRef = useRef<string | undefined>(undefined);
  
  // 읽음 처리된 메시지 ID를 로컬 스토리지에 저장하는 헬퍼 함수
  const getReadMessageIds = useCallback((roomId: string): Set<string> => {
    try {
      const key = `chat-read-messages-${roomId}`;
      const stored = localStorage.getItem(key);
      return stored ? new Set(JSON.parse(stored)) : new Set();
    } catch {
      return new Set();
    }
  }, []);
  
  const saveReadMessageIds = useCallback((roomId: string, messageIds: string[]) => {
    try {
      const key = `chat-read-messages-${roomId}`;
      const existing = getReadMessageIds(roomId);
      messageIds.forEach(id => existing.add(id));
      localStorage.setItem(key, JSON.stringify(Array.from(existing)));
    } catch (error) {
      console.error("Failed to save read message IDs:", error);
    }
  }, [getReadMessageIds]);

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

  // 견적 정보 조회
  const {
    data: quote,
    isLoading: quoteLoading,
  } = useSWR<QuoteDetail>(
    chatRoom?.quoteId ? `/auction/quotes/${chatRoom.quoteId}` : null,
    {
      ...realtimeDataConfig,
      fetcher: () => getQuoteDetail(chatRoom!.quoteId),
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

  // 초기 메시지 설정 및 읽음 상태 보존
  useEffect(() => {
    if (initialMessages && chatRoomId && user && chatRoom) {
      const currentUserId = user.role === "CONSUMER" 
        ? chatRoom.consumerId 
        : chatRoom.sellerId;
      
      setMessages((prevMessages) => {
        // 로컬 스토리지에서 읽음 처리된 메시지 ID 가져오기
        const readMessageIds = getReadMessageIds(chatRoomId);
        
        // 채팅방이 변경되었거나 기존 메시지가 없으면 (채팅방 진입 시)
        const isNewChatRoom = prevChatRoomIdRef.current !== chatRoomId;
        if (isNewChatRoom || prevMessages.length === 0) {
          // 내가 보낸 메시지 중 로컬 스토리지에 읽음 처리된 메시지는 읽음 상태로 설정
          const messagesWithReadStatus = initialMessages.map((msg) => {
            // 내가 보낸 메시지이고 로컬 스토리지에 읽음 처리되어 있으면 읽음 상태로 설정
            if (msg.senderId === currentUserId && readMessageIds.has(msg.id)) {
              return { ...msg, isRead: true };
            }
            return msg;
          });
          
          // 읽지 않은 메시지를 먼저 읽음 처리하고, 메시지 상태를 읽음으로 설정
          const unreadMessages = messagesWithReadStatus.filter(
            (msg) => msg.senderId !== currentUserId && !msg.isRead
          );
          
          // 채팅방 진입 시 항상 읽지 않은 메시지를 읽음 처리
          if (unreadMessages.length > 0) {
            const messageIds = unreadMessages.map((msg) => msg.id);
            
            // 읽음 처리 API 호출 (중복 호출 방지를 위해 한 번만 실행)
            if (isInitialLoadRef.current) {
              isInitialLoadRef.current = false;
              
              markMessagesAsRead({
                chatRoomId: chatRoomId,
                messageIds: messageIds,
              })
                .then(() => {
                  // 읽음 처리 성공 시 로컬 스토리지에 저장
                  saveReadMessageIds(chatRoomId, messageIds);
                  // 채팅방 목록 갱신을 위해 SWR 캐시 무효화
                  if (typeof window !== 'undefined' && window.location.pathname === `/chat/${chatRoomId}`) {
                    // SWR 캐시 무효화는 useSWR의 mutate를 사용하거나 페이지 이동 시 자동 갱신됨
                  }
                })
                .catch((error) => {
                  console.error("Failed to mark messages as read:", error);
                });
            }
            
            // 메시지를 읽음 상태로 설정하여 즉시 렌더링
            return messagesWithReadStatus.map((msg) =>
              messageIds.includes(msg.id) ? { ...msg, isRead: true } : msg
            );
          }
          
          // 읽지 않은 메시지가 없으면 로컬 스토리지 상태 반영한 메시지 반환
          if (isInitialLoadRef.current) {
            isInitialLoadRef.current = false;
          }
          return messagesWithReadStatus;
        }
        
        // 기존 메시지가 있으면 (SWR 리프레시 시) 읽음 상태 병합
        const readStatusMap = new Map<string, boolean>();
        prevMessages.forEach((msg) => {
          readStatusMap.set(msg.id, msg.isRead);
        });
        
        // 새 메시지 목록에 기존 읽음 상태 병합
        const mergedMessages = initialMessages.map((newMsg) => {
          const existingReadStatus = readStatusMap.get(newMsg.id);
          // 기존에 읽음 처리된 메시지는 읽음 상태 유지
          if (existingReadStatus === true) {
            return { ...newMsg, isRead: true };
          }
          // 내가 보낸 메시지이고 로컬 스토리지에 읽음 처리되어 있으면 읽음 상태로 설정
          if (newMsg.senderId === currentUserId && readMessageIds.has(newMsg.id)) {
            return { ...newMsg, isRead: true };
          }
          // 기존 메시지가 없거나 읽지 않은 상태면 서버 상태 사용
          return newMsg;
        });
        
        return mergedMessages;
      });
    }
  }, [initialMessages, chatRoomId, user, chatRoom, getReadMessageIds, saveReadMessageIds]);

  // 채팅방 진입 시 읽지 않은 메시지 자동 읽음 처리
  useEffect(() => {
    if (!chatRoomId || !initialMessages || !user || !chatRoom) {
      return;
    }

    // 역할에 따라 현재 사용자 ID 결정
    let currentUserId: string | undefined;
    if (user.role === "CONSUMER") {
      currentUserId = chatRoom.consumerId;
    } else if (user.role === "SELLER") {
      currentUserId = chatRoom.sellerId;
    } else {
      // 알 수 없는 역할인 경우 처리하지 않음
      console.warn(`[ChatRoomPage] 알 수 없는 사용자 역할: ${user.role}`);
      return;
    }

    // currentUserId가 없으면 처리하지 않음
    if (!currentUserId) {
      console.warn(`[ChatRoomPage] currentUserId를 찾을 수 없습니다. role: ${user.role}, consumerId: ${chatRoom.consumerId}, sellerId: ${chatRoom.sellerId}`);
      return;
    }
    
    // 읽지 않은 메시지 필터링 (상대방이 보낸 메시지 중 읽지 않은 메시지)
    const unreadMessages = initialMessages.filter(
      (msg) => msg.senderId !== currentUserId && !msg.isRead
    );
    
    // 읽지 않은 메시지가 있으면 읽음 처리
    if (unreadMessages.length > 0) {
      const messageIds = unreadMessages.map((msg) => msg.id);
      
      // 읽음 처리 API 호출
      markMessagesAsRead({
        chatRoomId: chatRoomId,
        messageIds: messageIds,
      })
        .then(() => {
          // 읽음 처리 성공 시 로컬 스토리지에 저장
          saveReadMessageIds(chatRoomId, messageIds);
        })
        .catch((error) => {
          console.error("Failed to mark messages as read:", error);
        });
    }
  }, [chatRoomId, initialMessages, user, chatRoom, saveReadMessageIds]);

  // 채팅방 변경 시 초기 로드 플래그 리셋 및 메시지 초기화
  useEffect(() => {
    // chatRoomId가 변경되었을 때만 메시지 초기화
    if (prevChatRoomIdRef.current !== undefined && prevChatRoomIdRef.current !== chatRoomId) {
      setMessages([]);
      isInitialLoadRef.current = true;
    }
    prevChatRoomIdRef.current = chatRoomId;
  }, [chatRoomId]);

  // WebSocket 연결 및 메시지 수신
  const { sendMessage: sendWebSocketMessage, sendTyping, connectionStatus } = useWebSocket({
    chatRoomId: chatRoomId || undefined,
    autoConnect: true,
    onMessage: (message: ChatMessage) => {
      // 현재 사용자 ID 확인 (채팅방 정보 사용)
      const currentUserId = chatRoom && user 
        ? (user.role === "CONSUMER" ? chatRoom.consumerId : chatRoom.sellerId)
        : null;

      // 메시지 중복 방지: 이미 존재하는 메시지는 업데이트만 수행
      setMessages((prev) => {
        // 현재 사용자가 보낸 메시지인지 확인
        const isMyMessage = currentUserId && message.senderId === currentUserId;
        
        // 임시 메시지 찾기 (같은 내용과 현재 사용자가 보낸 메시지인 경우, 최근 10초 이내)
        if (isMyMessage && currentUserId) {
          const now = Date.now();
          const tempMessageIndex = prev.findIndex(
            (msg) => {
              // 임시 메시지가 아니면 스킵
              if (!msg.id.startsWith("temp-")) return false;
              // 내용이 다르면 스킵
              if (msg.content !== message.content) return false;
              // senderId가 다르면 스킵 (중요: 임시 메시지의 senderId도 확인)
              if (msg.senderId !== currentUserId) return false;
              
              // 임시 메시지 생성 시간 확인 (10초 이내)
              const tempTimeMatch = msg.id.match(/temp-(\d+)-/);
              if (tempTimeMatch && tempTimeMatch[1]) {
                const tempTime = parseInt(tempTimeMatch[1], 10);
                const timeDiff = now - tempTime;
                return timeDiff < 10000; // 10초 이내
              }
              return false;
            }
          );

          if (tempMessageIndex !== -1) {
            // 임시 메시지를 서버 응답으로 교체
            const updated = [...prev];
            updated[tempMessageIndex] = message;
            return updated;
          }
        }

        // 일반 메시지 업데이트 (이미 존재하는 메시지인 경우)
        const exists = prev.some((msg) => msg.id === message.id);
        if (exists) {
          // 기존 메시지의 읽음 상태 업데이트
          return prev.map((msg) =>
            msg.id === message.id ? { ...msg, isRead: message.isRead } : msg
          );
        }
        
        // 새 메시지 추가
        return [...prev, message];
      });

      // 읽음 처리 (자신이 보낸 메시지가 아니고, 아직 읽지 않은 메시지인 경우에만)
      // senderId는 UUID이므로 채팅방 정보를 사용하여 비교
      if (user && chatRoom && currentUserId && message.senderId !== currentUserId && !message.isRead) {
        markMessagesAsRead({
          chatRoomId: chatRoomId!,
          messageIds: [message.id],
        })
          .then(() => {
            // 읽음 처리 성공 시 로컬 스토리지에 저장
            if (chatRoomId) {
              saveReadMessageIds(chatRoomId, [message.id]);
            }
            // 읽음 처리 성공 시 메시지 상태 업데이트
            setMessages((prev) =>
              prev.map((msg) =>
                msg.id === message.id ? { ...msg, isRead: true } : msg
              )
            );
          })
          .catch((error) => {
            console.error("Failed to mark message as read:", error);
          });
      }
    },
    onReadStatus: (message: ChatMessage) => {
      // 읽음 상태 업데이트만 처리 (읽음 처리 API 호출하지 않음)
      if (chatRoomId) {
        // 로컬 스토리지에 읽음 처리된 메시지 ID 저장
        saveReadMessageIds(chatRoomId, [message.id]);
      }
      
      setMessages((prev) => {
        const messageIndex = prev.findIndex((msg) => msg.id === message.id);
        if (messageIndex === -1) {
          // 메시지가 없으면 추가하지 않음 (읽음 상태 업데이트만)
          return prev;
        }
        // 읽음 상태 업데이트 (서버에서 받은 읽음 상태로 업데이트)
        return prev.map((msg) =>
          msg.id === message.id ? { ...msg, isRead: true } : msg
        );
      });
    },
    onTyping: (event) => {
      // 자신이 보낸 타이핑 이벤트는 무시
      if (user && event.senderId === user.username) {
        return;
      }

      setIsOtherUserTyping(event.isTyping);
      if (event.isTyping) {
        setTypingUserId(event.senderId);
      } else {
        setTypingUserId(null);
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

  // 타이핑 인디케이터 표시 시에도 스크롤
  useEffect(() => {
    if (isOtherUserTyping) {
      scrollToBottom();
    }
  }, [isOtherUserTyping, scrollToBottom]);

  // 메시지 전송
  const handleSendMessage = useCallback(
    (messageContent: string) => {
      if (!chatRoomId || !user || !chatRoom) {
        return;
      }

      // 현재 사용자 ID 확인 (채팅방 정보 사용)
      const currentUserId = user.role === "CONSUMER" 
        ? chatRoom.consumerId 
        : chatRoom.sellerId;

      // 임시 메시지 ID 생성 (서버 응답 전까지 사용)
      const tempId = `temp-${Date.now()}-${Math.random()}`;
      
      // 즉시 로컬 상태에 추가 (isRead: false로 설정하여 "1" 표시)
      // senderId는 UUID로 설정하여 서버 응답과 일치시킴
      const tempMessage: ChatMessage = {
        id: tempId,
        chatRoomId,
        senderId: currentUserId, // UUID 사용 (서버 응답과 일치)
        messageType: MessageType.TEXT,
        content: messageContent,
        isRead: false,
        createdAt: new Date().toISOString(),
      };

      // 임시 메시지를 먼저 추가
      setMessages((prev) => [...prev, tempMessage]);

      const messageRequest = {
        chatRoomId,
        senderId: user.username,
        messageType: MessageType.TEXT,
        content: messageContent,
      };

      sendWebSocketMessage(messageRequest);
    },
    [chatRoomId, user, chatRoom, sendWebSocketMessage]
  );

  // 타이핑 이벤트 전송 핸들러
  const handleTyping = useCallback(
    (isTyping: boolean) => {
      if (!chatRoomId || !user) {
        return;
      }
      sendTyping(user.username, isTyping);
    },
    [chatRoomId, user, sendTyping]
  );

  // 메시지가 같은 날짜인지 확인
  const isSameDate = (date1: string, date2: string): boolean => {
    const d1 = new Date(date1).toDateString();
    const d2 = new Date(date2).toDateString();
    return d1 === d2;
  };

  if (roomLoading || messagesLoading || quoteLoading) {
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
    if (!chatRoom || !user) {
      return false;
    }

    // 현재 사용자 ID 확인 (채팅방 정보 사용)
    // role에 따라 consumerId 또는 sellerId 사용
    // sellerId는 이제 Seller의 User ID를 반환하므로 메시지 senderId와 일치함
    const currentUserId = user.role === "CONSUMER" 
      ? chatRoom.consumerId 
      : chatRoom.sellerId;

    // senderId는 UUID이므로 직접 비교
    const isCurrent = currentUserId === senderId;
    return isCurrent;
  };

  return (
    <div className="flex flex-col h-full bg-indigo-50 overflow-hidden">
      {/* 헤더 - 고정 */}
      <div className="bg-white border-b flex-shrink-0 z-10">
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

      {/* 메시지 영역 - 스크롤 가능 */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3 scroll-smooth bg-indigo-50 min-h-0">
        {messages.length === 0 ? (
          <>
            {/* 채팅방 생성일 날짜 구분선 */}
            {chatRoom && (
              <DateSeparator date={chatRoom.createdAt} />
            )}
            {/* 견적 정보 카드 (채팅방 시작일 아래) */}
            {quote && (
              <BidQuoteCard
                quote={quote}
                bidPrice={chatRoom.totalPrice}
                sellerName={chatRoom.sellerName}
              />
            )}
            <div className="text-center text-gray-500 py-12">
              <p className="text-sm">메시지가 없습니다. 첫 메시지를 보내보세요!</p>
            </div>
          </>
        ) : (
          messages.map((message, index) => {
            const prevMessage = index > 0 ? messages[index - 1] : undefined;
            const showDateSeparator =
              index === 0 ||
              !prevMessage ||
              !isSameDate(message.createdAt, prevMessage.createdAt);
            const isCurrentUserMessage = isCurrentUser(message.senderId);
            
            // 첫 번째 메시지이고 채팅방 생성일과 같은 날짜인 경우, 날짜 구분선 아래에 견적 카드 표시
            const showBidCard = 
              index === 0 && 
              quote && 
              chatRoom &&
              isSameDate(message.createdAt, chatRoom.createdAt);

            return (
              <div key={message.id}>
                {/* 날짜 구분선 */}
                {showDateSeparator && (
                  <DateSeparator date={message.createdAt} />
                )}

                {/* 견적 정보 카드 (채팅방 시작일 아래) */}
                {showBidCard && (
                  <BidQuoteCard
                    quote={quote}
                    bidPrice={chatRoom.totalPrice}
                    sellerName={chatRoom.sellerName}
                  />
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

        {/* 타이핑 인디케이터 */}
        {isOtherUserTyping && typingUserId && (
          <TypingIndicator
            senderName={
              chatRoom?.sellerId === typingUserId
                ? chatRoom.sellerName
                : undefined
            }
            senderAvatar={undefined}
          />
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* 메시지 입력 영역 - 고정 */}
      <div className="flex-shrink-0">
        <MessageInput
          onSendMessage={handleSendMessage}
          onTyping={handleTyping}
          connectionStatus={connectionStatus}
        />
      </div>
    </div>
  );
};

export default ChatRoomPage;

