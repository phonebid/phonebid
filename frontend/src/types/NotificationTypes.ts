/**
 * 알림 타입 정의
 */
export type NotificationType =
  | "QUOTE_CREATED"
  | "QUOTE_EXPIRING_SOON"
  | "BID_ARRIVED"
  | "BID_SELECTED"
  | "LOWEST_PRICE_UPDATED"
  | "CONTRACT_SIGNED"
  | "PAYMENT_COMPLETED"
  | "DELIVERY_STARTED"
  | "DELIVERY_COMPLETED"
  | "SELLER_APPROVAL_REQUESTED"
  | "SELLER_APPROVED"
  | "SELLER_REJECTED"
  | "REPORT_RECEIVED"
  | "SYSTEM_ANOMALY"
  | "STATISTICS_SUMMARY"
  | "CHAT_MESSAGE_RECEIVED";

/**
 * 알림 채널 타입
 */
export type NotificationChannel = "SSE" | "KAKAO" | "EMAIL" | "PUSH";

/**
 * 알림 상태
 */
export type NotificationStatus = "PENDING" | "SENT" | "FAILED" | "READ";

/**
 * 알림 엔티티 (백엔드 NotificationResponseDto 매핑)
 */
export interface Notification {
  id: string;
  type: NotificationType;
  channel: NotificationChannel;
  title: string;
  message: string;
  referenceId?: string;
  isRead: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * 알림 표시 아이템 (그룹화된 알림)
 * 백엔드 NotificationDisplayItem DTO 매핑
 */
export interface NotificationDisplayItem {
  id: string;
  type: NotificationType;
  channel: NotificationChannel;
  title: string;
  message: string;
  referenceId?: string;
  isRead: boolean;
  createdAt: string;
  updatedAt: string;
  groupCount?: number; // 프론트엔드 전용 (그룹화 표시용)
}

/**
 * 알림 목록 조회 응답
 */
export interface NotificationListResponse {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
  hasNext: boolean;
}

/**
 * 미읽음 알림 개수 응답
 */
export interface UnreadCountResponse {
  unreadCount: number;
}

/**
 * 알림 읽음 처리 응답
 */
export interface MarkAsReadResponse {
  success: boolean;
  notificationId: string;
}

/**
 * 모든 알림 읽음 처리 응답
 */
export interface MarkAllAsReadResponse {
  success: boolean;
  count: number;
}

/**
 * 알림 삭제 응답
 */
export interface DeleteNotificationResponse {
  success: boolean;
  notificationId: string;
}

/**
 * 모든 알림 삭제 응답
 */
export interface DeleteAllNotificationsResponse {
  success: boolean;
  count: number;
}

/**
 * SSE 연결 상태
 */
export type SSEConnectionStatus =
  | "connecting"
  | "connected"
  | "disconnected"
  | "error";

/**
 * SSE 이벤트 타입
 */
export type SSEEventType = "connected" | "notification" | "heartbeat";

/**
 * SSE 알림 이벤트 데이터
 */
export interface SSENotificationEvent {
  notification: NotificationDisplayItem;
  unreadCount: number;
}

/**
 * 알림 타입별 메타데이터
 */
export interface NotificationMeta {
  icon: string;
  color: string;
  displayName: string;
  priority: number;
}

/**
 * 알림 필터 옵션
 */
export type NotificationFilter = "all" | "unread" | "read";

/**
 * 알림 정렬 옵션
 */
export type NotificationSort = "latest" | "oldest" | "priority";
