import { useNavigate } from "react-router-dom";
import type { ChatRoom } from "types/ChatTypes";

interface ChatRoomCardProps {
  room: ChatRoom;
  lastMessage?: string;
  unreadCount?: number;
  sellerName?: string;
  sellerAvatar?: string;
}

/**
 * 채팅방 목록의 개별 카드 컴포넌트 (토스 스타일)
 */
export function ChatRoomCard({
  room,
  lastMessage = "마지막 메시지 미리보기",
  unreadCount = 0,
  sellerName,
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

  const displayName = sellerName || `채팅방 ${room.id.slice(0, 8)}`;

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
        <div className="flex-1 min-w-0 flex items-start justify-between gap-2">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <h3 className="text-sm font-medium text-gray-900 truncate">
                {displayName}
              </h3>
              {unreadCount > 0 && (
                <span className="bg-red-500 text-white text-xs font-medium rounded-full px-1.5 py-0.5 min-w-[18px] text-center flex-shrink-0">
                  {unreadCount > 99 ? "99+" : unreadCount}
                </span>
              )}
            </div>
            <p className="text-sm text-gray-500 truncate mb-1">{lastMessage}</p>
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-400">
                총 {0}원
              </span>
              <span className="text-xs text-gray-400">·</span>
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


