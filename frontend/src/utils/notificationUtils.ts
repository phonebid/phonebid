import type { NotificationType, NotificationMeta } from "types/NotificationTypes";

/**
 * 알림 타입별 메타데이터 매핑
 */
export const notificationMetaMap: Record<NotificationType, NotificationMeta> = {
  QUOTE_CREATED: {
    icon: "📝",
    color: "blue",
    displayName: "견적 등록",
    priority: 2,
  },
  BID_ARRIVED: {
    icon: "💰",
    color: "green",
    displayName: "입찰 도착",
    priority: 3,
  },
  BID_SELECTED: {
    icon: "✅",
    color: "emerald",
    displayName: "입찰 선택",
    priority: 4,
  },
  CONTRACT_SIGNED: {
    icon: "📄",
    color: "purple",
    displayName: "계약 체결",
    priority: 5,
  },
  PAYMENT_COMPLETED: {
    icon: "💳",
    color: "indigo",
    displayName: "결제 완료",
    priority: 5,
  },
  DELIVERY_STARTED: {
    icon: "🚚",
    color: "orange",
    displayName: "배송 시작",
    priority: 3,
  },
  DELIVERY_COMPLETED: {
    icon: "📦",
    color: "teal",
    displayName: "배송 완료",
    priority: 3,
  },
  SELLER_APPROVED: {
    icon: "👍",
    color: "green",
    displayName: "판매자 승인",
    priority: 4,
  },
  SELLER_REJECTED: {
    icon: "👎",
    color: "red",
    displayName: "판매자 거부",
    priority: 4,
  },
  CHAT_MESSAGE_RECEIVED: {
    icon: "💬",
    color: "pink",
    displayName: "채팅 수신",
    priority: 3,
  },
};

/**
 * 알림 타입에 해당하는 메타데이터 반환
 */
export function getNotificationMeta(type: NotificationType): NotificationMeta {
  return notificationMetaMap[type];
}

/**
 * 알림 타입에 해당하는 아이콘 반환
 */
export function getNotificationIcon(type: NotificationType): string {
  return notificationMetaMap[type]?.icon || "🔔";
}

/**
 * 알림 타입에 해당하는 색상 반환 (Tailwind 색상)
 */
export function getNotificationColor(type: NotificationType): string {
  return notificationMetaMap[type]?.color || "gray";
}

/**
 * 알림 타입에 해당하는 표시 이름 반환
 */
export function getNotificationDisplayName(type: NotificationType): string {
  return notificationMetaMap[type]?.displayName || "알림";
}

/**
 * 알림 타입별 라우팅 경로 반환
 * @param type 알림 타입
 * @param referenceId 참조 ID (quoteId, contractId, bidId 등)
 * @returns 이동할 경로
 */
export function getNotificationRoute(
  type: NotificationType,
  referenceId?: string
): string {
  if (!referenceId) {
    return "/notifications";
  }

  switch (type) {
    case "QUOTE_CREATED":
      return `/mypage/quotes/${referenceId}`;
    
    case "BID_ARRIVED":
      return `/mypage/quotes/${referenceId}`;
    
    case "BID_SELECTED":
      return `/seller-center`;
    
    case "CONTRACT_SIGNED":
      return `/mypage/purchases/${referenceId}`;
    
    case "PAYMENT_COMPLETED":
      return `/mypage/purchases/${referenceId}`;
    
    case "DELIVERY_STARTED":
    case "DELIVERY_COMPLETED":
      return `/mypage/purchases/${referenceId}`;
    
    case "SELLER_APPROVED":
      return "/seller-center";
    
    case "SELLER_REJECTED":
      return "/seller-center";
    
    case "CHAT_MESSAGE_RECEIVED":
      return referenceId ? `/chat/${referenceId}` : "/chat";
    
    default:
      return "/notifications";
  }
}

/**
 * 시간 경과 표시 (예: 방금 전, 5분 전, 1시간 전)
 */
export function getTimeAgo(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSeconds = Math.floor(diffMs / 1000);
  const diffMinutes = Math.floor(diffSeconds / 60);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSeconds < 60) {
    return "방금 전";
  } else if (diffMinutes < 60) {
    return `${diffMinutes}분 전`;
  } else if (diffHours < 24) {
    return `${diffHours}시간 전`;
  } else if (diffDays < 7) {
    return `${diffDays}일 전`;
  } else {
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }
}

/**
 * 알림 우선순위별 정렬 함수
 */
export function sortNotificationsByPriority<
  T extends { type: NotificationType }
>(notifications: T[]): T[] {
  return [...notifications].sort((a, b) => {
    const priorityA = notificationMetaMap[a.type]?.priority || 0;
    const priorityB = notificationMetaMap[b.type]?.priority || 0;
    return priorityB - priorityA; // 높은 우선순위가 먼저
  });
}
