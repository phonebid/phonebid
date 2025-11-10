import { useEffect } from "react";
import useSWR from "swr";
import { getChatRooms } from "services/chatService";
import { realtimeDataConfig } from "services/swrConfig";
import type { PaginatedChatRooms } from "types/ChatTypes";
import { ChatRoomCard } from "components/chat/ChatRoomCard";
import { UnreadBadge } from "components/chat/UnreadBadge";

const ChatListPage: React.FC = () => {
  useEffect(() => {
    document.title = "채팅 목록 | PhoneBid";
  }, []);

  // 채팅방 목록 조회
  const {
    data: chatRoomsData,
    error,
    isLoading,
  } = useSWR<PaginatedChatRooms>(
    "/chat/rooms?page=0&size=20",
    {
      ...realtimeDataConfig,
      fetcher: () => getChatRooms({ page: 0, size: 20 }),
    }
  );

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold mb-6">채팅 목록</h1>
        <div className="bg-white rounded-lg shadow p-6">
          <p className="text-gray-500">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold mb-6">채팅 목록</h1>
        <div className="bg-white rounded-lg shadow p-6">
          <p className="text-red-500">채팅방 목록을 불러오는 중 오류가 발생했습니다.</p>
        </div>
      </div>
    );
  }

  const chatRooms = chatRoomsData?.content || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-2xl font-bold mb-6">채팅 목록</h1>

      {/* 읽지 않은 메시지 배지 (상단) */}
      <div className="mb-4">
        <div className="inline-flex items-center px-3 py-1 rounded-full bg-red-100 text-red-800 text-sm font-medium">
          <span className="mr-2">읽지 않은 메시지</span>
          {/* TODO: 실제 읽지 않은 메시지 수 계산 */}
          <UnreadBadge count={0} />
        </div>
      </div>

      {/* 채팅방 목록 */}
      <div className="space-y-4">
        {chatRooms.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-6 text-center">
            <p className="text-gray-500">채팅방이 없습니다.</p>
          </div>
        ) : (
          chatRooms.map((room) => (
            <ChatRoomCard
              key={room.id}
              room={room}
              unreadCount={0}
              // TODO: 실제 데이터로 교체 필요
              // lastMessage={room.lastMessage}
              // sellerName={room.sellerName}
              // sellerAvatar={room.sellerAvatar}
            />
          ))
        )}
      </div>
    </div>
  );
};

export default ChatListPage;

