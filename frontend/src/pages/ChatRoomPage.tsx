import { useParams, useNavigate } from "react-router-dom";

const ChatRoomPage: React.FC = () => {
  const { chatRoomId } = useParams<{ chatRoomId: string }>();
  const navigate = useNavigate();

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center mb-6">
        <button
          onClick={() => navigate("/chat")}
          className="mr-4 text-gray-600 hover:text-gray-900"
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
        <h1 className="text-2xl font-bold">채팅 상세</h1>
      </div>
      <div className="bg-white rounded-lg shadow p-6">
        <p className="text-gray-500">
          채팅방 ID: {chatRoomId}
        </p>
        {/* TODO: 채팅 메시지 및 입력 영역 구현 */}
      </div>
    </div>
  );
};

export default ChatRoomPage;

