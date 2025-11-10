import type { WebSocketConnectionStatus } from "types/ChatTypes";

interface ChatConnectionStatusProps {
  status: WebSocketConnectionStatus;
}

/**
 * 채팅 연결 상태 표시 컴포넌트
 */
export function ChatConnectionStatus({ status }: ChatConnectionStatusProps) {
  const getStatusText = (): string => {
    switch (status) {
      case "CONNECTED":
        return "연결됨";
      case "CONNECTING":
        return "연결 중...";
      case "ERROR":
        return "연결 오류";
      case "DISCONNECTED":
        return "연결 끊김";
      default:
        return "알 수 없음";
    }
  };

  const getStatusColor = (): string => {
    switch (status) {
      case "CONNECTED":
        return "text-green-600";
      case "CONNECTING":
        return "text-yellow-600";
      case "ERROR":
        return "text-red-600";
      case "DISCONNECTED":
        return "text-gray-500";
      default:
        return "text-gray-500";
    }
  };

  return (
    <div className="flex items-center space-x-2">
      <div
        className={`w-2 h-2 rounded-full ${
          status === "CONNECTED"
            ? "bg-green-500"
            : status === "CONNECTING"
              ? "bg-yellow-500 animate-pulse"
              : status === "ERROR"
                ? "bg-red-500"
                : "bg-gray-400"
        }`}
      />
      <p className={`text-xs ${getStatusColor()}`}>{getStatusText()}</p>
    </div>
  );
}

