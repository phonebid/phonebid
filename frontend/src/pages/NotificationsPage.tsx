import { useEffect, useState } from "react";
import { useNotificationStore } from "store/notificationStore";
import { useNotifications } from "hooks/useNotifications";
import { NotificationItem } from "components/notification/NotificationItem";
import { ConfirmModal } from "components/ui/ConfirmModal";
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
  const [showDeleteAllModal, setShowDeleteAllModal] = useState(false);

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

  const handleDeleteAllClick = () => {
    if (notifications.length === 0) return;
    setShowDeleteAllModal(true);
  };

  const handleDeleteAllConfirm = async () => {
    try {
      await deleteAllNotifications();
      setShowDeleteAllModal(false);
    } catch (error) {
      console.error("모든 알림 삭제 실패:", error);
    }
  };

  const handleDeleteAllCancel = () => {
    setShowDeleteAllModal(false);
  };

  const filteredNotifications = notifications.filter((n) => {
    if (filter === "unread") return !n.isRead;
    if (filter === "read") return n.isRead;
    return true;
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-indigo-50/30 to-purple-50/30 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="mb-6 animate-fade-in-down">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-100 rounded-lg">
              <svg
                className="w-6 h-6 text-indigo-600"
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
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">알림</h1>
              <p className="mt-1 text-sm text-gray-500">
                총 {notifications.length}개의 알림
                {unreadCount > 0 && (
                  <span className="ml-1 px-2 py-0.5 bg-indigo-100 text-indigo-700 text-xs font-bold rounded-full">
                    미읽음 {unreadCount}
                  </span>
                )}
              </p>
            </div>
          </div>
        </div>

        {/* 필터 및 액션 */}
        <div className="mb-6 bg-white rounded-lg shadow-md p-4 animate-scale-in">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            {/* 필터 탭 */}
            <div className="flex gap-2 flex-wrap">
              <button
                onClick={() => handleFilterChange("all")}
                className={cn(
                  "px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200",
                  filter === "all"
                    ? "bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-md"
                    : "text-gray-600 hover:bg-gray-100 border border-gray-200"
                )}
              >
                전체
              </button>
              <button
                onClick={() => handleFilterChange("unread")}
                className={cn(
                  "px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 flex items-center gap-1",
                  filter === "unread"
                    ? "bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-md"
                    : "text-gray-600 hover:bg-gray-100 border border-gray-200"
                )}
              >
                읽지 않음
                {unreadCount > 0 && (
                  <span className={cn(
                    "px-1.5 py-0.5 text-xs font-bold rounded-full",
                    filter === "unread" ? "bg-white/20" : "bg-indigo-100 text-indigo-700"
                  )}>
                    {unreadCount}
                  </span>
                )}
              </button>
              <button
                onClick={() => handleFilterChange("read")}
                className={cn(
                  "px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200",
                  filter === "read"
                    ? "bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-md"
                    : "text-gray-600 hover:bg-gray-100 border border-gray-200"
                )}
              >
                읽음
              </button>
            </div>

            {/* 액션 버튼 */}
            <div className="flex gap-2 flex-wrap">
              {unreadCount > 0 && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleMarkAllAsRead}
                  className="hover:bg-indigo-50 hover:border-indigo-300 transition-all"
                >
                  <svg
                    className="w-4 h-4 mr-1"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  모두 읽음
                </Button>
              )}
              {notifications.length > 0 && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleDeleteAllClick}
                  className="text-red-600 hover:text-red-700 hover:bg-red-50 hover:border-red-300 transition-all"
                >
                  <svg
                    className="w-4 h-4 mr-1"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    />
                  </svg>
                  모두 삭제
                </Button>
              )}
            </div>
          </div>
        </div>

        {/* 알림 목록 */}
        <div className="bg-white rounded-lg shadow-md overflow-hidden animate-scale-in">
          {filteredNotifications.length > 0 ? (
            <>
              <div className="divide-y divide-gray-100">
                {filteredNotifications.map((notification, index) => (
                  <div
                    key={notification.id}
                    className="animate-fade-in-down"
                    style={{ animationDelay: `${index * 30}ms` }}
                  >
                    <NotificationItem
                      notification={notification}
                      showDelete={true}
                    />
                  </div>
                ))}
              </div>

              {/* 더보기 버튼 */}
              {hasMore && (
                <div className="p-4 text-center border-t bg-gray-50">
                  <Button
                    variant="outline"
                    onClick={loadMore}
                    disabled={isLoading}
                    className="hover:bg-indigo-50 hover:border-indigo-300 transition-all"
                  >
                    {isLoading ? (
                      <>
                        <svg
                          className="animate-spin -ml-1 mr-2 h-4 w-4"
                          fill="none"
                          viewBox="0 0 24 24"
                        >
                          <circle
                            className="opacity-25"
                            cx="12"
                            cy="12"
                            r="10"
                            stroke="currentColor"
                            strokeWidth="4"
                          />
                          <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                          />
                        </svg>
                        로딩 중...
                      </>
                    ) : (
                      "더보기"
                    )}
                  </Button>
                </div>
              )}
            </>
          ) : (
            <div className="py-16 text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
                <svg
                  className="w-8 h-8 text-gray-400"
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
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-1">
                {filter === "unread"
                  ? "읽지 않은 알림이 없습니다"
                  : filter === "read"
                  ? "읽은 알림이 없습니다"
                  : "알림이 없습니다"}
              </h3>
              <p className="text-sm text-gray-500">
                새로운 알림이 도착하면 여기에 표시됩니다
              </p>
            </div>
          )}
        </div>
      </div>

      {/* 모두 삭제 확인 모달 */}
      <ConfirmModal
        isOpen={showDeleteAllModal}
        onClose={handleDeleteAllCancel}
        onConfirm={handleDeleteAllConfirm}
        title="모든 알림 삭제"
        message={`총 ${notifications.length}개의 알림을 모두 삭제하시겠습니까?\n삭제된 알림은 복구할 수 없습니다.`}
        confirmText="모두 삭제"
        cancelText="취소"
        variant="danger"
      />
    </div>
  );
}
