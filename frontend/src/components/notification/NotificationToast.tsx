import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useNotificationStore } from "store/notificationStore";
import {
  getNotificationIcon,
  getNotificationColor,
  getNotificationRoute,
} from "utils/notificationUtils";
import type { NotificationDisplayItem } from "types/NotificationTypes";
import { cn } from "utils/cn";

interface ToastItemProps {
  notification: NotificationDisplayItem;
  onClose: (id: string) => void;
}

function ToastItem({ notification, onClose }: ToastItemProps) {
  const navigate = useNavigate();
  const [isExiting, setIsExiting] = useState(false);

  const icon = getNotificationIcon(notification.type);
  const color = getNotificationColor(notification.type);

  useEffect(() => {
    // 5초 후 자동 사라짐
    const timer = setTimeout(() => {
      handleClose();
    }, 5000);

    return () => clearTimeout(timer);
  }, [notification.id]);

  const handleClose = () => {
    setIsExiting(true);
    setTimeout(() => {
      onClose(notification.id);
    }, 300); // 애니메이션 시간
  };

  const handleClick = () => {
    const route = getNotificationRoute(notification.type, notification.referenceId);
    navigate(route);
    handleClose();
  };

  return (
    <div
      onClick={handleClick}
      className={cn(
        "flex items-start gap-3 p-4 bg-white rounded-lg shadow-lg border border-gray-200 cursor-pointer hover:shadow-xl transition-all duration-300 max-w-sm",
        isExiting
          ? "animate-out fade-out slide-out-to-right-full"
          : "animate-in fade-in slide-in-from-right-full"
      )}
    >
      {/* 아이콘 */}
      <div
        className={cn(
          "flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center text-xl",
          `bg-${color}-100`
        )}
      >
        {icon}
      </div>

      {/* 내용 */}
      <div className="flex-1 min-w-0">
        <h4 className="text-sm font-semibold text-gray-900">
          {notification.title}
        </h4>
        <p className="mt-1 text-sm text-gray-600 line-clamp-2">
          {notification.message}
        </p>
      </div>

      {/* 닫기 버튼 */}
      <button
        onClick={(e) => {
          e.stopPropagation();
          handleClose();
        }}
        className="flex-shrink-0 text-gray-400 hover:text-gray-600 transition-colors"
        aria-label="닫기"
      >
        <svg
          className="w-5 h-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      </button>
    </div>
  );
}

/**
 * 알림 토스트 컨테이너
 * 화면 우측 상단에 토스트 알림을 표시합니다.
 */
export function NotificationToast() {
  const { toastQueue, removeToast } = useNotificationStore();

  if (toastQueue.length === 0) {
    return null;
  }

  return (
    <div className="fixed top-20 right-4 z-[9999] flex flex-col gap-3 pointer-events-none">
      <div className="flex flex-col gap-3 pointer-events-auto">
        {toastQueue.map((notification) => (
          <ToastItem
            key={notification.id}
            notification={notification}
            onClose={removeToast}
          />
        ))}
      </div>
    </div>
  );
}
