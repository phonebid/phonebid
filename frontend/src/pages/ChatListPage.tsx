import { useEffect, useMemo } from "react";
import useSWR from "swr";
import { getChatRooms } from "services/chatService";
import { realtimeDataConfig } from "services/swrConfig";
import type { PaginatedChatRooms } from "types/ChatTypes";
import { ChatRoomCard } from "components/chat/ChatRoomCard";

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

  const chatRooms = chatRoomsData?.content || [];

  // 각 채팅방의 읽지 않은 메시지 수 계산
  const unreadCounts = useMemo(() => {
    const counts: Record<string, number> = {};
    // TODO: 백엔드에서 unreadCount를 제공하도록 수정 필요
    // 현재는 임시로 0으로 설정
    chatRooms.forEach((room) => {
      counts[room.id] = 0;
    });
    return counts;
  }, [chatRooms]);

  // 전체 읽지 않은 메시지 수
  const totalUnreadCount = useMemo(() => {
    return Object.values(unreadCounts).reduce((sum, count) => sum + count, 0);
  }, [unreadCounts]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="bg-white border-b">
          <div className="max-w-2xl mx-auto px-4 py-4">
            <h1 className="text-lg font-semibold text-gray-900">채팅</h1>
          </div>
        </div>
        <div className="max-w-2xl mx-auto px-4 py-4">
          <div className="bg-white rounded-lg p-4">
            <p className="text-gray-500 text-sm">로딩 중...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="bg-white border-b">
          <div className="max-w-2xl mx-auto px-4 py-4">
            <h1 className="text-lg font-semibold text-gray-900">채팅</h1>
          </div>
        </div>
        <div className="max-w-2xl mx-auto px-4 py-4">
          <div className="bg-white rounded-lg p-4">
            <p className="text-red-500 text-sm">채팅방 목록을 불러오는 중 오류가 발생했습니다.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 헤더 */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-lg font-semibold text-gray-900">채팅</h1>
            {totalUnreadCount > 0 && (
              <span className="bg-red-500 text-white text-xs font-medium rounded-full px-2 py-0.5 min-w-[20px] text-center">
                {totalUnreadCount > 99 ? "99+" : totalUnreadCount}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* 채팅방 목록 */}
      <div className="max-w-2xl mx-auto">
        {chatRooms.length === 0 ? (
          <div className="px-4 py-8">
            <div className="bg-white rounded-lg p-6 text-center">
              <p className="text-gray-500 text-sm">채팅방이 없습니다.</p>
            </div>
          </div>
        ) : (
          <div className="divide-y divide-gray-100 bg-white">
            {chatRooms.map((room) => (
              <ChatRoomCard
                key={room.id}
                room={room}
                unreadCount={unreadCounts[room.id] || 0}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatListPage;

