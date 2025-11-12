import { useNavigate } from "react-router-dom";
import type { ChatRoom } from "types/ChatTypes";

interface ChatRoomCardProps {
  room: ChatRoom;
  unreadCount?: number;
  sellerAvatar?: string;
}

/**
 * 채팅방 목록의 개별 카드 컴포넌트 (토스 스타일)
 */
export function ChatRoomCard({
  room,
  unreadCount = 0,
  sellerAvatar,
}: ChatRoomCardProps) {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/chat/${room.id}`);
  };

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

  const displayName = room.sellerName || `채팅방 ${room.id.slice(0, 8)}`;
  const lastMessage = room.lastMessage || "메시지가 없습니다.";
  const totalPrice = room.totalPrice || 0;

  return (
    <div
      onClick={handleClick}
      className="px-4 py-3 cursor-pointer hover:bg-gray-50 active:bg-gray-100 transition-colors"
    >
      <div className="flex items-start gap-3">
        {/* 프로필 이미지 영역 */}
        <div className="flex-shrink-0">
          {sellerAvatar ? (
            <img
              src={sellerAvatar}
              alt={displayName}
              className="w-12 h-12 rounded-full object-cover"
            />
          ) : (
            <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center">
              <span className="text-gray-400 text-xs">프로필</span>
            </div>
          )}
        </div>

        {/* 채팅방 정보 */}
        <div className="flex-1 min-w-0">
          {/* 업체명 영역 */}
          <div className="flex items-center gap-2 mb-2">
            <h3 className="text-sm font-bold text-gray-900 truncate">
              {displayName}
            </h3>
            {unreadCount > 0 && (
              <span className="bg-red-500 text-white text-xs font-medium rounded-full px-1.5 py-0.5 min-w-[18px] text-center flex-shrink-0">
                {unreadCount > 99 ? "99+" : unreadCount}
              </span>
            )}
          </div>
          
          {/* 마지막 메시지 영역 */}
          <p className="text-sm text-gray-500 truncate mb-2">{lastMessage}</p>
          
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
  );
}


