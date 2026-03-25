import { useState, useCallback } from "react";
import { notificationService } from "services/notificationService";
import { useNotificationStore } from "store/notificationStore";
import type { NotificationFilter } from "types/NotificationTypes";
import { toast } from "react-toastify";

/**
 * 알림 데이터 관리 Hook
 * API 호출 및 상태 관리를 담당합니다.
 */
export function useNotifications() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const {
    setNotifications,
    appendNotifications,
    setUnreadCount,
    markAsRead: markAsReadInStore,
    markAllAsRead: markAllAsReadInStore,
    removeNotification: removeNotificationInStore,
    clearNotifications: clearNotificationsInStore,
  } = useNotificationStore();

  /**
   * 알림 목록 조회
   */
  const fetchNotifications = useCallback(
    async (page = 0, size = 20, filter: NotificationFilter = "all") => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await notificationService.getNotifications(
          page,
          size,
          filter
        );
        
        // Notification[]을 NotificationDisplayItem[]으로 변환
        const displayItems = response.content.map((notification) => ({
          id: notification.id,
          type: notification.type,
          channel: notification.channel,
          title: notification.title,
          message: notification.message,
          referenceId: notification.referenceId,
          isRead: notification.isRead,
          createdAt: notification.createdAt,
          updatedAt: notification.updatedAt,
        }));
        
        // 첫 페이지면 기존 목록 대체, 아니면 추가
        if (page === 0) {
          setNotifications(displayItems);
        } else {
          appendNotifications(displayItems);
        }

        return { ...response, content: displayItems };
      } catch (err) {
        const error = err as Error;
        setError(error);
        toast.error("알림 목록을 불러오는데 실패했습니다.");
        throw error;
      } finally {
        setIsLoading(false);
      }
    },
    [setNotifications, appendNotifications]
  );

  /**
   * 미읽음 알림 개수 조회
   */
  const fetchUnreadCount = useCallback(async () => {
    try {
      const count = await notificationService.getUnreadCount();
      setUnreadCount(count);
      return count;
    } catch (err) {
      console.error("미읽음 알림 개수 조회 실패:", err);
      throw err;
    }
  }, [setUnreadCount]);

  /**
   * 알림 읽음 처리
   */
  const markAsRead = useCallback(
    async (notificationId: string) => {
      try {
        await notificationService.markAsRead(notificationId);
        markAsReadInStore(notificationId);
      } catch (err) {
        const error = err as Error;
        setError(error);
        toast.error("알림 읽음 처리에 실패했습니다.");
        throw error;
      }
    },
    [markAsReadInStore]
  );

  /**
   * 모든 알림 읽음 처리
   */
  const markAllAsRead = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await notificationService.markAllAsRead();
      markAllAsReadInStore();
      return response;
    } catch (err) {
      const error = err as Error;
      setError(error);
      toast.error("모든 알림 읽음 처리에 실패했습니다.");
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [markAllAsReadInStore]);

  /**
   * 알림 삭제
   */
  const deleteNotification = useCallback(
    async (notificationId: string) => {
      try {
        await notificationService.deleteNotification(notificationId);
        removeNotificationInStore(notificationId);
        toast.success("알림을 삭제했습니다.");
      } catch (err) {
        const error = err as Error;
        setError(error);
        toast.error("알림 삭제에 실패했습니다.");
        throw error;
      }
    },
    [removeNotificationInStore]
  );

  /**
   * 모든 알림 삭제
   */
  const deleteAllNotifications = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await notificationService.deleteAllNotifications();
      clearNotificationsInStore();
      toast.success(`${response.count}개의 알림을 삭제했습니다.`);
      return response;
    } catch (err) {
      const error = err as Error;
      setError(error);
      toast.error("모든 알림 삭제에 실패했습니다.");
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [clearNotificationsInStore]);

  /**
   * 최근 미읽음 알림 조회 (SSE 초기 연결용)
   */
  const fetchRecentUnreadNotifications = useCallback(async (limit = 10) => {
    try {
      const notifications =
        await notificationService.getRecentUnreadNotifications(limit);
      return notifications;
    } catch (err) {
      console.error("최근 미읽음 알림 조회 실패:", err);
      throw err;
    }
  }, []);

  return {
    isLoading,
    error,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    deleteAllNotifications,
    fetchRecentUnreadNotifications,
  };
}
