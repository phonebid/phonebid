import { useState, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import type { ChatRoom } from "types/ChatTypes";
import { ChatAvatar } from "components/chat/ChatAvatar";
import { UnreadBadge } from "components/chat/UnreadBadge";
import { leaveChatRoom } from "services/chatService";
import { mutate } from "swr";
import { useAuthStore } from "store/authStore";

interface ChatRoomCardProps {
  room: ChatRoom;
  unreadCount?: number;
  sellerAvatar?: string;
  fromPath?: string;
  onLeave?: () => void;
}

/**
 * 채팅방 목록의 개별 카드 컴포넌트
 * 스와이프 액션으로 채팅방 나가기 기능 제공
 */
export function ChatRoomCard({
  room,
  unreadCount = 0,
  sellerAvatar,
  fromPath,
  onLeave,
}: ChatRoomCardProps) {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [swipeOffset, setSwipeOffset] = useState(0);
  const [isSwiping, setIsSwiping] = useState(false);
  const [isLeaving, setIsLeaving] = useState(false);
  const startXRef = useRef<number | null>(null);
  const currentXRef = useRef<number | null>(null);
  const SWIPE_THRESHOLD = 80; // 스와이프 임계값 (픽셀)

  const handleClick = () => {
    if (swipeOffset === 0) {
      navigate(`/chat/${room.id}`, {
        state: { from: fromPath },
      });
    }
  };

  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    const touch = e.touches[0];
    if (!touch) return;
    startXRef.current = touch.clientX;
    currentXRef.current = touch.clientX;
    setIsSwiping(true);
  }, []);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    if (startXRef.current === null) return;
    const touch = e.touches[0];
    if (!touch) return;
    currentXRef.current = touch.clientX;
    const diff = startXRef.current - currentXRef.current;
    
    // 왼쪽으로 스와이프만 허용 (양수 = 왼쪽)
    if (diff > 0) {
      setSwipeOffset(Math.min(diff, SWIPE_THRESHOLD));
    } else {
      setSwipeOffset(0);
    }
  }, []);

  const handleTouchEnd = useCallback(() => {
    if (startXRef.current === null || currentXRef.current === null) return;
    
    const diff = startXRef.current - currentXRef.current;
    
    if (diff >= SWIPE_THRESHOLD * 0.5) {
      // 임계값의 50% 이상 스와이프하면 완전히 열림
      setSwipeOffset(SWIPE_THRESHOLD);
    } else {
      // 그렇지 않으면 원래 위치로
      setSwipeOffset(0);
    }
    
    startXRef.current = null;
    currentXRef.current = null;
    setIsSwiping(false);
  }, []);

  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    startXRef.current = e.clientX;
    currentXRef.current = e.clientX;
    setIsSwiping(true);
  }, []);

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (startXRef.current === null || !isSwiping) return;
    currentXRef.current = e.clientX;
    const diff = startXRef.current - currentXRef.current;
    
    if (diff > 0) {
      setSwipeOffset(Math.min(diff, SWIPE_THRESHOLD));
    } else {
      setSwipeOffset(0);
    }
  }, [isSwiping]);

  const handleMouseUp = useCallback(() => {
    if (startXRef.current === null || currentXRef.current === null) return;
    
    const diff = startXRef.current - currentXRef.current;
    
    if (diff >= SWIPE_THRESHOLD * 0.5) {
      setSwipeOffset(SWIPE_THRESHOLD);
    } else {
      setSwipeOffset(0);
    }
    
    startXRef.current = null;
    currentXRef.current = null;
    setIsSwiping(false);
  }, []);

  const handleLeave = useCallback(async (e: React.MouseEvent) => {
    e.stopPropagation();
    
    if (isLeaving) return;
    
    if (!window.confirm("채팅방을 나가시겠습니까?")) {
      setSwipeOffset(0);
      return;
    }

    setIsLeaving(true);
    try {
      await leaveChatRoom(room.id);
      toast.success("채팅방을 나갔습니다.");
      
      // 목록 갱신
      mutate("/chat/rooms?page=0&size=20");
      
      // 콜백 호출
      if (onLeave) {
        onLeave();
      }
      
      // 스와이프 상태 초기화
      setSwipeOffset(0);
    } catch (error: any) {
      console.error("채팅방 나가기 실패:", error);
      toast.error(error.response?.data?.message || "채팅방 나가기에 실패했습니다.");
      setSwipeOffset(0);
    } finally {
      setIsLeaving(false);
    }
  }, [room.id, isLeaving, onLeave]);

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return "오늘";
    } else if (date.toDateString() === yesterday.toDateString()) {
      return "어제";
    } else {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}.${month}.${day}`;
    }
  };

  const formatPrice = (price?: number): string => {
    if (price === null || price === undefined) {
      return "0";
    }
    return price.toLocaleString("ko-KR");
  };

  // 현재 사용자 역할에 따라 상대방 이름 표시
  const displayName = user?.role === "CONSUMER"
    ? (room.sellerName || `채팅방 ${room.id.slice(0, 8)}`)
    : (room.consumerName || `채팅방 ${room.id.slice(0, 8)}`);
  const lastMessage = room.lastMessage || "메시지가 없습니다.";
  const totalPrice = room.totalPrice || 0;

  return (
    <div className="relative overflow-hidden bg-white">
      {/* 나가기 버튼 (스와이프 시 나타남) */}
      <div
        className="absolute right-0 top-0 h-full flex items-center justify-center bg-red-500 text-white px-6 z-10"
        style={{
          width: `${SWIPE_THRESHOLD}px`,
          transform: `translateX(${SWIPE_THRESHOLD - swipeOffset}px)`,
          transition: isSwiping ? "none" : "transform 0.2s ease-out",
        }}
      >
        <button
          onClick={handleLeave}
          disabled={isLeaving}
          className="text-sm font-medium whitespace-nowrap"
        >
          {isLeaving ? "처리 중..." : "나가기"}
        </button>
      </div>

      {/* 채팅방 카드 */}
      <div
        onClick={handleClick}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        className="px-4 py-3 cursor-pointer hover:bg-gray-50 active:bg-gray-100 transition-colors relative z-20 bg-white"
        style={{
          transform: `translateX(-${swipeOffset}px)`,
          transition: isSwiping ? "none" : "transform 0.2s ease-out",
        }}
      >
        <div className="flex items-start gap-3">
        {/* 프로필 이미지 영역 */}
        <ChatAvatar
          avatar={sellerAvatar}
          name={displayName}
          size="lg"
          alt={displayName}
        />

        {/* 채팅방 정보 */}
        <div className="flex-1 min-w-0">
          {/* 업체명 영역 */}
          <div className="flex items-center justify-between gap-2 mb-2">
            <h3 className="text-sm font-bold text-gray-900 truncate flex-1 min-w-0">
              {displayName}
            </h3>
            <UnreadBadge count={unreadCount} className="flex-shrink-0" />
          </div>
          
          {/* 마지막 메시지 영역 */}
          <p className={`text-sm truncate mb-2 ${unreadCount > 0 ? "font-semibold text-gray-900" : "text-gray-500"}`}>
            {lastMessage}
          </p>
          
          {/* 구분선 */}
          <div className="border-t border-gray-100 my-2"></div>
          
          {/* 하단 정보 영역 (가격 및 날짜) */}
          <div className="flex items-center justify-between pt-1">
            <span className="text-xs text-gray-400">
              총 {formatPrice(totalPrice)}원
            </span>
            <span className="text-xs text-gray-400">
              {formatDate(room.updatedAt)}
            </span>
          </div>
        </div>
        </div>
      </div>
    </div>
  );
}


