import { apiClient } from "./apiClient";
import type {
  Notification,
  NotificationDisplayItem,
  NotificationListResponse,
  UnreadCountResponse,
  MarkAsReadResponse,
  MarkAllAsReadResponse,
  DeleteNotificationResponse,
  DeleteAllNotificationsResponse,
} from "types/NotificationTypes";

/**
 * 알림 서비스
 * 백엔드 알림 API와 통신하는 서비스
 */
class NotificationService {
  /**
   * SSE 연결 URL 반환
   * EventSource에서 직접 사용
   */
  getSSEUrl(): string {
    const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    return `${baseURL}/api/v1/notifications/sse`;
  }

  /**
   * 알림 목록 조회 (페이지네이션)
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지 크기
   * @param filter 필터 옵션 (all, unread, read)
   */
  async getNotifications(
    page = 0,
    size = 20,
    filter: "all" | "unread" | "read" = "all"
  ): Promise<NotificationListResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (filter !== "all") {
      params.append("isRead", filter === "read" ? "true" : "false");
    }

    return apiClient.get<NotificationListResponse>(
      `/notifications?${params.toString()}`
    );
  }

  /**
   * 최근 미읽음 알림 조회 (SSE 초기 전송용)
   * @param limit 최대 조회 개수 (기본값: 10)
   */
  async getRecentUnreadNotifications(
    limit = 10
  ): Promise<NotificationDisplayItem[]> {
    return apiClient.get<NotificationDisplayItem[]>(
      `/notifications/recent-unread?limit=${limit}`
    );
  }

  /**
   * 미읽음 알림 개수 조회
   */
  async getUnreadCount(): Promise<number> {
    const response = await apiClient.get<UnreadCountResponse>(
      "/notifications/unread-count"
    );
    return response.count;
  }

  /**
   * 알림 읽음 처리
   * @param notificationId 알림 ID
   */
  async markAsRead(notificationId: string): Promise<MarkAsReadResponse> {
    return apiClient.patch<MarkAsReadResponse>(
      `/notifications/${notificationId}/read`
    );
  }

  /**
   * 모든 알림 읽음 처리
   */
  async markAllAsRead(): Promise<MarkAllAsReadResponse> {
    return apiClient.post<MarkAllAsReadResponse>("/notifications/read-all");
  }

  /**
   * 알림 삭제 (소프트 삭제)
   * @param notificationId 알림 ID
   */
  async deleteNotification(
    notificationId: string
  ): Promise<DeleteNotificationResponse> {
    return apiClient.delete<DeleteNotificationResponse>(
      `/notifications/${notificationId}`
    );
  }

  /**
   * 모든 알림 삭제 (소프트 삭제)
   */
  async deleteAllNotifications(): Promise<DeleteAllNotificationsResponse> {
    return apiClient.delete<DeleteAllNotificationsResponse>(
      "/notifications/all"
    );
  }

  /**
   * 알림 상세 조회
   * @param notificationId 알림 ID
   */
  async getNotification(notificationId: string): Promise<Notification> {
    return apiClient.get<Notification>(`/notifications/${notificationId}`);
  }
}

export const notificationService = new NotificationService();
