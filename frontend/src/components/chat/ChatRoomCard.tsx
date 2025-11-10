import { useNavigate } from "react-router-dom";
import type { ChatRoom } from "types/ChatTypes";
import { UnreadBadge } from "./UnreadBadge";

interface ChatRoomCardProps {
  room: ChatRoom;
  lastMessage?: string;
  unreadCount?: number;
  sellerName?: string;
  sellerAvatar?: string;
}

/**
 * 채팅방 목록의 개별 카드 컴포넌트
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
    return new Date(dateString).toLocaleDateString("ko-KR", {
      month: "2-digit",
      day: "2-digit",
    });
  };

  const displayName = sellerName || `채팅방 ${room.id.slice(0, 8)}`;

  return (
    <div
      onClick={handleClick}
      className="bg-white rounded-lg shadow p-4 cursor-pointer hover:shadow-md transition-shadow"
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4 flex-1 min-w-0">
          {/* 프로필 이미지 영역 */}
          {sellerAvatar ? (
            <img
              src={sellerAvatar}
              alt={displayName}
              className="w-12 h-12 rounded-full object-cover flex-shrink-0"
            />
          ) : (
            <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center flex-shrink-0">
              <span className="text-gray-500 text-sm">프로필</span>
            </div>
          )}

          {/* 채팅방 정보 */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between mb-1">
              <h3 className="text-sm font-medium text-gray-900 truncate">
                {displayName}
              </h3>
              <span className="text-xs text-gray-500 ml-2 flex-shrink-0">
                {formatDate(room.updatedAt)}
              </span>
            </div>
            <p className="text-sm text-gray-500 truncate">{lastMessage}</p>
          </div>
        </div>

        {/* 읽지 않은 메시지 배지 */}
        {unreadCount > 0 && (
          <div className="ml-4 flex-shrink-0">
            <UnreadBadge count={unreadCount} />
          </div>
        )}
      </div>
    </div>
  );
}

