import { useCallback, useEffect, useState } from "react";
import { useNotificationStore } from "store/notificationStore";
import { useNotifications } from "hooks/useNotifications";
import { NotificationItem } from "components/notification/NotificationItem";
import { ConfirmModal } from "components/ui/ConfirmModal";
import { NotificationStatusBanner } from "components/notification/NotificationStatusBanner";
import { Button } from "components/ui/button";
import type { NotificationFilter } from "types/NotificationTypes";
import { cn } from "utils/cn";

export function NotificationsPage() {
  const { notifications, unreadCount, setNotifications } = useNotificationStore();
  const {
    fetchNotifications,
    markAllAsRead,
    deleteAllNotifications,
    isLoading,
    error,
  } = useNotifications();

  const [filter, setFilter] = useState<NotificationFilter>("all");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showDeleteAllModal, setShowDeleteAllModal] = useState(false);

  const loadNotifications = useCallback(
    async (pageNum: number, filterType: NotificationFilter) => {
      try {
        // numbered pagination에서는 임의의 페이지로 이동할 때 store를 비운 뒤 로드해야 함
        // (useNotifications는 page > 0일 때 appendNotifications을 사용하므로)
        if (pageNum !== 0) {
          setNotifications([]);
        }

        const response = await fetchNotifications(pageNum, 10, filterType);
        setTotalPages(response.totalPages || 1);
        setPage(pageNum);
      } catch (error) {
        console.error("알림 목록 로드 실패:", error);
      }
    },
    [fetchNotifications, setNotifications]
  );

  // 초기 로드
  useEffect(() => {
    loadNotifications(0, filter);
  }, [filter, loadNotifications]);

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

  const visiblePageIndices = (() => {
    const total = Math.max(1, totalPages);
    const windowSize = 3;

    let start = Math.max(0, page - 1);
    let end = Math.min(total - 1, start + windowSize - 1);
    start = Math.max(0, end - windowSize + 1);

    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  })();

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
              {unreadCount > 0 && (
                <p className="mt-1 text-sm text-gray-500">
                  <span className="ml-0 px-2 py-0.5 bg-indigo-100 text-indigo-700 text-xs font-bold rounded-full">
                    미읽음 {unreadCount}
                  </span>
                </p>
              )}
            </div>
          </div>
        </div>

        <div className="mb-4">
          <NotificationStatusBanner />
          {error && (
            <div className="mt-3 w-full rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-900">
              <div className="font-semibold">알림을 불러오지 못했습니다</div>
              <div className="mt-1 text-xs opacity-90">
                잠시 후 다시 시도해 주세요.
              </div>
            </div>
          )}
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

              {/* 숫자 페이징 */}
              {totalPages > 1 && (
                <div className="p-4 text-center border-t bg-gray-50">
                  <div className="flex items-center justify-center gap-2">
                    {visiblePageIndices.map((pageIndex) => {
                      const label = pageIndex + 1;
                      const isActive = pageIndex === page;

                      return (
                        <Button
                          key={pageIndex}
                          variant={isActive ? "default" : "outline"}
                          size="sm"
                          onClick={() => loadNotifications(pageIndex, filter)}
                          disabled={isLoading || isActive}
                          className={
                            isActive
                              ? "shadow"
                              : "hover:bg-indigo-50 hover:border-indigo-300 transition-all"
                          }
                          aria-current={isActive ? "page" : undefined}
                        >
                          {label}
                        </Button>
                      );
                    })}
                  </div>
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
