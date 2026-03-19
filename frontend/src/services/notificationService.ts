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
    return `${baseURL}/api/v1/notifications/stream`;
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

    // Spring Data Page 직렬화 결과는 보통 `hasNext`를 그대로 내려주지 않기 때문에,
    // 프론트에서 기대하는 `NotificationListResponse.hasNext`를 파생 계산해서 반환합니다.
    type SpringPageLike<T> = {
      content: T[];
      totalElements: number;
      totalPages: number;
      number: number; // current page
      size: number;
      last?: boolean; // 페이지 마지막 여부
      hasNext?: boolean; // 보통 존재하지 않지만 혹시 모를 케이스
    };

    const rawPage = await apiClient.get<SpringPageLike<Notification>>(
      `/notifications?${params.toString()}`
    );

    const currentPage = rawPage?.number ?? page;
    const totalPages = rawPage?.totalPages ?? 0;
    const pageSize = rawPage?.size ?? size;

    const hasNext =
      typeof rawPage?.hasNext === "boolean"
        ? rawPage.hasNext
        : typeof rawPage?.last === "boolean"
          ? !rawPage.last
          : currentPage + 1 < totalPages;

    return {
      content: rawPage?.content ?? [],
      totalElements: rawPage?.totalElements ?? 0,
      totalPages,
      currentPage,
      size: pageSize,
      hasNext,
    };
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
    return response.unreadCount;
  }

  /**
   * 알림 읽음 처리
   * @param notificationId 알림 ID
   */
  async markAsRead(notificationId: string): Promise<MarkAsReadResponse> {
    return apiClient.put<MarkAsReadResponse>(
      `/notifications/${notificationId}/read`
    );
  }

  /**
   * 모든 알림 읽음 처리
   */
  async markAllAsRead(): Promise<MarkAllAsReadResponse> {
    return apiClient.put<MarkAllAsReadResponse>("/notifications/read-all");
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
