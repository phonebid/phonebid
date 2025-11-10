import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import useSWR from "swr";
import { getChatRooms } from "services/chatService";
import { realtimeDataConfig } from "services/swrConfig";
import type { PaginatedChatRooms } from "types/ChatTypes";

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

  const handleChatRoomClick = (chatRoomId: string) => {
    navigate(`/chat/${chatRoomId}`);
  };

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
          <span className="bg-red-500 text-white rounded-full px-2 py-0.5 text-xs">
            {/* TODO: 실제 읽지 않은 메시지 수 계산 */}
            0
          </span>
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
            <div
              key={room.id}
              onClick={() => handleChatRoomClick(room.id)}
              className="bg-white rounded-lg shadow p-4 cursor-pointer hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4 flex-1">
                  {/* 프로필 이미지 영역 */}
                  <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center">
                    <span className="text-gray-500 text-sm">프로필</span>
                  </div>

                  {/* 채팅방 정보 */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="text-sm font-medium text-gray-900 truncate">
                        채팅방 {room.id.slice(0, 8)}
                      </h3>
                      <span className="text-xs text-gray-500">
                        {new Date(room.updatedAt).toLocaleDateString("ko-KR", {
                          month: "2-digit",
                          day: "2-digit",
                        })}
                      </span>
                    </div>
                    <p className="text-sm text-gray-500 truncate">
                      마지막 메시지 미리보기
                    </p>
                  </div>
                </div>

                {/* 읽지 않은 메시지 배지 */}
                <div className="ml-4">
                  <div className="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                    <span className="text-white text-xs">0</span>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default ChatListPage;

