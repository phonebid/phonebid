import { useEffect, useState } from "react";
import { useNotificationStore } from "store/notificationStore";
import { useNotifications } from "hooks/useNotifications";
import { NotificationItem } from "components/notification/NotificationItem";
import { Button } from "components/ui/button";
import type { NotificationFilter } from "types/NotificationTypes";
import { cn } from "utils/cn";

export function NotificationsPage() {
  const { notifications, unreadCount } = useNotificationStore();
  const {
    fetchNotifications,
    markAllAsRead,
    deleteAllNotifications,
    isLoading,
  } = useNotifications();

  const [filter, setFilter] = useState<NotificationFilter>("all");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  // 초기 로드
  useEffect(() => {
    loadNotifications(0, filter);
  }, [filter]);

  const loadNotifications = async (pageNum: number, filterType: NotificationFilter) => {
    try {
      const response = await fetchNotifications(pageNum, 20, filterType);
      setHasMore(response.hasNext);
      setPage(pageNum);
    } catch (error) {
      console.error("알림 목록 로드 실패:", error);
    }
  };

  const loadMore = () => {
    if (!hasMore || isLoading) return;
    loadNotifications(page + 1, filter);
  };

  const handleFilterChange = (newFilter: NotificationFilter) => {
    setFilter(newFilter);
    setPage(0);
  };

  const handleMarkAllAsRead = async () => {
    if (unreadCount === 0) return;
    try {
      await markAllAsRead();
    } catch (error) {
      console.error("모든 알림 읽음 처리 실패:", error);
    }
  };

  const handleDeleteAll = async () => {
    if (notifications.length === 0) return;
    
    const confirmed = window.confirm("모든 알림을 삭제하시겠습니까?");
    if (!confirmed) return;

    try {
      await deleteAllNotifications();
    } catch (error) {
      console.error("모든 알림 삭제 실패:", error);
    }
  };

  const filteredNotifications = notifications.filter((n) => {
    if (filter === "unread") return !n.isRead;
    if (filter === "read") return n.isRead;
    return true;
  });

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">알림</h1>
          <p className="mt-1 text-sm text-gray-500">
            총 {notifications.length}개의 알림
            {unreadCount > 0 && ` (미읽음 ${unreadCount}개)`}
          </p>
        </div>

        {/* 필터 및 액션 */}
        <div className="mb-6 bg-white rounded-lg shadow p-4">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            {/* 필터 탭 */}
            <div className="flex gap-2">
              <button
                onClick={() => handleFilterChange("all")}
                className={cn(
                  "px-4 py-2 text-sm font-medium rounded-md transition-colors",
                  filter === "all"
                    ? "bg-indigo-100 text-indigo-700"
                    : "text-gray-600 hover:bg-gray-100"
                )}
              >
                전체
              </button>
              <button
                onClick={() => handleFilterChange("unread")}
                className={cn(
                  "px-4 py-2 text-sm font-medium rounded-md transition-colors",
                  filter === "unread"
                    ? "bg-indigo-100 text-indigo-700"
                    : "text-gray-600 hover:bg-gray-100"
                )}
              >
                읽지 않음 ({unreadCount})
              </button>
              <button
                onClick={() => handleFilterChange("read")}
                className={cn(
                  "px-4 py-2 text-sm font-medium rounded-md transition-colors",
                  filter === "read"
                    ? "bg-indigo-100 text-indigo-700"
                    : "text-gray-600 hover:bg-gray-100"
                )}
              >
                읽음
              </button>
            </div>

            {/* 액션 버튼 */}
            <div className="flex gap-2">
              {unreadCount > 0 && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleMarkAllAsRead}
                >
                  모두 읽음
                </Button>
              )}
              {notifications.length > 0 && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleDeleteAll}
                  className="text-red-600 hover:text-red-700"
                >
                  모두 삭제
                </Button>
              )}
            </div>
          </div>
        </div>

        {/* 알림 목록 */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {filteredNotifications.length > 0 ? (
            <>
              <div>
                {filteredNotifications.map((notification) => (
                  <NotificationItem
                    key={notification.id}
                    notification={notification}
                    showDelete={true}
                  />
                ))}
              </div>

              {/* 더보기 버튼 */}
              {hasMore && (
                <div className="p-4 text-center border-t">
                  <Button
                    variant="outline"
                    onClick={loadMore}
                    disabled={isLoading}
                  >
                    {isLoading ? "로딩 중..." : "더보기"}
                  </Button>
                </div>
              )}
            </>
          ) : (
            <div className="py-16 text-center">
              <svg
                className="mx-auto w-16 h-16 text-gray-300"
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
              <p className="mt-4 text-sm text-gray-500">
                {filter === "unread"
                  ? "읽지 않은 알림이 없습니다"
                  : filter === "read"
                  ? "읽은 알림이 없습니다"
                  : "알림이 없습니다"}
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
