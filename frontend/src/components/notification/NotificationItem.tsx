import { useNavigate } from "react-router-dom";
import type { NotificationDisplayItem } from "types/NotificationTypes";
import {
  getNotificationIcon,
  getNotificationColor,
  getTimeAgo,
  getNotificationRoute,
} from "utils/notificationUtils";
import { useNotifications } from "hooks/useNotifications";
import { cn } from "utils/cn";

interface NotificationItemProps {
  notification: NotificationDisplayItem;
  onClose?: () => void;
  showDelete?: boolean;
}

export function NotificationItem({
  notification,
  onClose,
  showDelete = true,
}: NotificationItemProps) {
  const navigate = useNavigate();
  const { markAsRead, deleteNotification } = useNotifications();

  const icon = getNotificationIcon(notification.type);
  const color = getNotificationColor(notification.type);
  const timeAgo = getTimeAgo(notification.createdAt);

  const handleClick = async () => {
    // 읽지 않은 알림이면 읽음 처리
    if (!notification.isRead) {
      try {
        await markAsRead(notification.id);
      } catch (error) {
        console.error("알림 읽음 처리 실패:", error);
      }
    }

    // 관련 페이지로 이동
    const route = getNotificationRoute(notification.type, notification.referenceId);
    navigate(route);
    onClose?.();
  };

  const handleDelete = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await deleteNotification(notification.id);
    } catch (error) {
      console.error("알림 삭제 실패:", error);
    }
  };

  return (
    <div
      onClick={handleClick}
      className={cn(
        "flex items-start gap-3 p-4 hover:bg-gray-50 cursor-pointer transition-colors border-b last:border-b-0",
        !notification.isRead && "bg-indigo-50/50"
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
        <div className="flex items-start justify-between gap-2">
          <h4
            className={cn(
              "text-sm font-medium text-gray-900",
              !notification.isRead && "font-semibold"
            )}
          >
            {notification.title}
          </h4>
          {!notification.isRead && (
            <span className="flex-shrink-0 w-2 h-2 bg-indigo-600 rounded-full"></span>
          )}
        </div>

        <p className="mt-1 text-sm text-gray-600 line-clamp-2">
          {notification.message}
        </p>

        <div className="mt-2 flex items-center justify-between">
          <span className="text-xs text-gray-500">{timeAgo}</span>

          {showDelete && (
            <button
              onClick={handleDelete}
              className="text-xs text-gray-400 hover:text-red-600 transition-colors"
              aria-label="알림 삭제"
            >
              삭제
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
