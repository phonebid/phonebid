import { useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import useSWR from "swr";
import { getChatRooms } from "services/chatService";
import { realtimeDataConfig } from "services/swrConfig";
import type { PaginatedChatRooms } from "types/ChatTypes";
import { ChatRoomCard } from "components/chat/ChatRoomCard";

const ChatListPage: React.FC = () => {
  const navigate = useNavigate();

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
    chatRooms.forEach((room) => {
      // 백엔드에서 unreadCount를 제공하면 사용하고, 없으면 0으로 설정
      counts[room.id] = room.unreadCount ?? 0;
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
        <div className="bg-white border-b sticky top-0 z-10">
          <div className="max-w-2xl mx-auto px-4 py-3">
            <div className="flex items-center relative">
              <button
                onClick={() => navigate("/")}
                className="text-gray-600 hover:text-gray-900 p-1 absolute left-0"
                aria-label="홈으로"
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
                <h1 className="text-base font-semibold text-gray-900">채팅</h1>
              </div>
            </div>
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
        <div className="bg-white border-b sticky top-0 z-10">
          <div className="max-w-2xl mx-auto px-4 py-3">
            <div className="flex items-center relative">
              <button
                onClick={() => navigate("/")}
                className="text-gray-600 hover:text-gray-900 p-1 absolute left-0"
                aria-label="홈으로"
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
                <h1 className="text-base font-semibold text-gray-900">채팅</h1>
              </div>
            </div>
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
        <div className="max-w-2xl mx-auto px-4 py-3">
          <div className="flex items-center relative">
            <button
              onClick={() => navigate("/")}
              className="text-gray-600 hover:text-gray-900 p-1 absolute left-0"
              aria-label="홈으로"
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
              <h1 className="text-base font-semibold text-gray-900">채팅</h1>
            </div>
            {totalUnreadCount > 0 && (
              <span className="bg-red-500 text-white text-[9px] font-medium rounded-md px-1 py-0.5 min-w-[16px] h-[16px] flex items-center justify-center leading-none absolute right-0">
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

