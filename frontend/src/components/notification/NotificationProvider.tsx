import { useEffect } from "react";
import { useAuthStore } from "store/authStore";
import { useNotificationSSE } from "hooks/useNotificationSSE";
import { useNotifications } from "hooks/useNotifications";
import { NotificationToast } from "components/notification/NotificationToast";

/**
 * 알림 프로바이더
 * SSE 연결을 관리하고 토스트를 표시합니다.
 */
export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  const { fetchUnreadCount, fetchNotifications } = useNotifications();

  // SSE 연결 (로그인된 사용자만)
  useNotificationSSE({
    enabled: isAuthenticated,
    onNotification: (notification) => {
      console.log("새 알림 수신:", notification);
    },
    onConnect: () => {
      console.log("SSE 연결 성공");
    },
    onDisconnect: () => {
      console.log("SSE 연결 종료");
    },
    onError: (error) => {
      console.error("SSE 연결 오류:", error);
    },
  });

  // 초기 알림 데이터 로드
  useEffect(() => {
    if (isAuthenticated) {
      // 미읽음 개수 로드
      fetchUnreadCount().catch((error) => {
        console.error("미읽음 개수 로드 실패:", error);
      });

      // 최근 알림 목록 로드 (최대 20개)
      fetchNotifications(0, 20, "all").catch((error) => {
        console.error("알림 목록 로드 실패:", error);
      });
    }
  }, [isAuthenticated, fetchUnreadCount, fetchNotifications]);

  return (
    <>
      {children}
      <NotificationToast />
    </>
  );
}
