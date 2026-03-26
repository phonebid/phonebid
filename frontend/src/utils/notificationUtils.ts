import type { NotificationType, NotificationMeta } from "types/NotificationTypes";

/** 알림 아이콘/테두리 등에 쓰이는 Tailwind 색상 키 (정적 클래스용) */
export const NOTIFICATION_COLOR_KEYS = [
  "blue",
  "green",
  "emerald",
  "purple",
  "indigo",
  "orange",
  "teal",
  "red",
  "pink",
  "gray",
  "white",
] as const;

export type NotificationColorKey = (typeof NOTIFICATION_COLOR_KEYS)[number];

/** 색상별 Tailwind 클래스 (JIT 인식용 정적 문자열) */
export interface NotificationColorClasses {
  iconBg: string;
  iconText: string;
  border: string;
  accent: string;
}

export const COLORS_MAP: Record<NotificationColorKey, NotificationColorClasses> = {
  blue: {
    iconBg: "bg-blue-100",
    iconText: "text-blue-600",
    border: "border-blue-500",
    accent: "bg-blue-500",
  },
  green: {
    iconBg: "bg-green-100",
    iconText: "text-green-600",
    border: "border-green-500",
    accent: "bg-green-500",
  },
  emerald: {
    iconBg: "bg-emerald-100",
    iconText: "text-emerald-600",
    border: "border-emerald-500",
    accent: "bg-emerald-500",
  },
  purple: {
    iconBg: "bg-purple-100",
    iconText: "text-purple-600",
    border: "border-purple-500",
    accent: "bg-purple-500",
  },
  indigo: {
    iconBg: "bg-indigo-100",
    iconText: "text-indigo-600",
    border: "border-indigo-500",
    accent: "bg-indigo-500",
  },
  orange: {
    iconBg: "bg-orange-100",
    iconText: "text-orange-600",
    border: "border-orange-500",
    accent: "bg-orange-500",
  },
  teal: {
    iconBg: "bg-teal-100",
    iconText: "text-teal-600",
    border: "border-teal-500",
    accent: "bg-teal-500",
  },
  red: {
    iconBg: "bg-red-100",
    iconText: "text-red-600",
    border: "border-red-500",
    accent: "bg-red-500",
  },
  pink: {
    iconBg: "bg-pink-100",
    iconText: "text-pink-600",
    border: "border-pink-500",
    accent: "bg-pink-500",
  },
  gray: {
    iconBg: "bg-gray-100",
    iconText: "text-gray-600",
    border: "border-gray-500",
    accent: "bg-gray-500",
  },
  white: {
    iconBg: "bg-white",
    iconText: "text-gray-700",
    border: "border-gray-400",
    accent: "bg-gray-500",
  },
};

/**
 * 색상 문자열로 Tailwind 클래스 객체 반환 (알 수 없는 색상은 gray로 폴백)
 */
export function getNotificationColorClasses(
  color: string
): NotificationColorClasses {
  const key = NOTIFICATION_COLOR_KEYS.includes(
    color as NotificationColorKey
  )
    ? (color as NotificationColorKey)
    : "gray";
  return COLORS_MAP[key];
}

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
  QUOTE_EXPIRING_SOON: {
    icon: "⏳",
    color: "orange",
    displayName: "견적 마감 임박",
    priority: 3,
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
  LOWEST_PRICE_UPDATED: {
    icon: "📉",
    color: "green",
    displayName: "최저가 갱신",
    priority: 3,
  },
  CONTRACT_SIGNED: {
    icon: "📄",
    color: "gray",
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
  SELLER_APPROVAL_REQUESTED: {
    icon: "📩",
    color: "blue",
    displayName: "판매자 승인 요청",
    priority: 2,
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
  REPORT_RECEIVED: {
    icon: "🚩",
    color: "red",
    displayName: "신고 접수",
    priority: 4,
  },
  SYSTEM_ANOMALY: {
    icon: "⚠️",
    color: "red",
    displayName: "시스템 이상",
    priority: 5,
  },
  STATISTICS_SUMMARY: {
    icon: "📊",
    color: "indigo",
    displayName: "통계 요약",
    priority: 2,
  },
  CHAT_MESSAGE_RECEIVED: {
    icon: "💬",
    color: "gray",
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
  referenceId?: string,
  role?: string
): string {
  if (!referenceId) {
    return "/notifications";
  }

  switch (type) {
    case "QUOTE_CREATED":
      return `/seller-center/quotes/${referenceId}/bid`;
    
    case "BID_ARRIVED":
      return `/mypage/quotes/${referenceId}`;
    
    case "BID_SELECTED":
      return `/seller-center`;

    case "LOWEST_PRICE_UPDATED":
      return role === "SELLER"
        ? "/seller-center/notifications"
        : `/mypage/quotes/${referenceId}`;
    
    case "CONTRACT_SIGNED":
      return role === "SELLER"
        ? "/seller-center"
        : `/mypage/purchases/${referenceId}`;
    
    case "PAYMENT_COMPLETED":
      return role === "SELLER"
        ? "/seller-center"
        : `/mypage/purchases/${referenceId}`;
    
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
  if (Number.isNaN(date.getTime())) {
    return "알 수 없음";
  }
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
