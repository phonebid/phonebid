import { Link } from "react-router-dom";
import { useNotificationStore } from "store/notificationStore";
import { useNotifications } from "hooks/useNotifications";
import { NotificationItem } from "components/notification/NotificationItem";
import { NotificationStatusBanner } from "components/notification/NotificationStatusBanner";
import { Button } from "components/ui/button";
import type { NotificationType } from "types/NotificationTypes";
import { useMemo } from "react";

interface NotificationDropdownProps {
  onClose?: () => void;
  typesFilter?: NotificationType[];
  viewAllPath?: string;
}

export function NotificationDropdown({
  onClose,
  typesFilter,
  viewAllPath = "/notifications",
}: NotificationDropdownProps) {
  const notifications = useNotificationStore((state) => state.notifications);
  const { markAllAsRead } = useNotifications();

  const isFilteredView = Boolean(typesFilter && typesFilter.length > 0);
  const safeTypesFilter = typesFilter ?? [];
  const filteredNotifications =
    isFilteredView
      ? notifications.filter((notification) =>
          safeTypesFilter.includes(notification.type)
        )
      : notifications;

  const filteredUnreadCount = useMemo(
    () =>
      filteredNotifications.reduce(
        (count, n) => (n.isRead ? count : count + 1),
        0
      ),
    [filteredNotifications]
  );

  // 최근 알림 5개만 표시
  const recentNotifications = filteredNotifications.slice(0, 5);
  const hasNotifications = recentNotifications.length > 0;

  const handleMarkAllAsRead = async () => {
    try {
      if (filteredUnreadCount === 0) return;

      // 필터 뷰에서는 id별 markAsRead를 N번 호출하게 되어 요청 폭주가 날 수 있어
      // "모두 읽음" 버튼 자체를 숨깁니다. (여기서는 방어적으로 no-op)
      if (isFilteredView) return;

      await markAllAsRead();
    } catch (error) {
      console.error("모든 알림 읽음 처리 실패:", error);
    }
  };

  return (
    <div className="w-[calc(100vw-2rem)] sm:w-96 max-w-md bg-white rounded-lg shadow-xl border border-gray-200 overflow-hidden">
      {/* 헤더 */}
      <div className="px-3 py-2.5 sm:px-4 sm:py-3 bg-gradient-to-r from-indigo-50 to-purple-50 border-b border-gray-200 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h3 className="text-sm font-semibold text-gray-900">알림</h3>
          {filteredUnreadCount > 0 && (
            <span className="px-2 py-0.5 bg-indigo-100 text-indigo-700 text-xs font-bold rounded-full">
              {filteredUnreadCount}
            </span>
          )}
        </div>
        {filteredUnreadCount > 0 && !isFilteredView && (
          <Button
            variant="ghost"
            size="sm"
            onClick={handleMarkAllAsRead}
            className="text-xs h-7 px-2 hover:bg-indigo-100 transition-colors"
          >
            모두 읽음
          </Button>
        )}
      </div>

      <div className="px-3 pt-3 sm:px-4">
        <NotificationStatusBanner />
      </div>

      {/* 알림 목록 */}
      <div className="max-h-[450px] sm:max-h-[500px] overflow-y-auto custom-scrollbar">
        {hasNotifications ? (
          <div className="divide-y divide-gray-100">
            {recentNotifications.map((notification, index) => (
              <div
                key={notification.id}
                className="animate-scale-in"
                style={{ animationDelay: `${index * 50}ms` }}
              >
                <NotificationItem
                  notification={notification}
                  onClose={onClose}
                  showDelete={false}
                />
              </div>
            ))}
          </div>
        ) : (
          <div className="py-12 text-center">
            <svg
              className="mx-auto w-12 h-12 text-gray-300 animate-pulse"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
            <p className="mt-2 text-sm text-gray-500">새로운 알림이 없습니다</p>
          </div>
        )}
      </div>

      {/* 푸터 */}
      {hasNotifications && (
        <div className="px-3 py-2.5 sm:px-4 sm:py-3 bg-gradient-to-r from-indigo-50 to-purple-50 border-t border-gray-200">
          <Link
            to={viewAllPath}
            onClick={onClose}
            className="block text-center text-sm text-indigo-600 hover:text-indigo-700 font-medium transition-colors hover:underline"
          >
            모두 보기 →
          </Link>
        </div>
      )}
    </div>
  );
}
