import { useEffect, useRef, useCallback } from "react";
import { useNotificationStore } from "store/notificationStore";
import { notificationService } from "services/notificationService";
import type {
  NotificationDisplayItem,
  SSENotificationEvent,
} from "types/NotificationTypes";

interface UseNotificationSSEOptions {
  enabled?: boolean;
  onNotification?: (notification: NotificationDisplayItem) => void;
  onError?: (error: Error) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
}

/**
 * SSE 알림 연결 Hook
 * EventSource를 사용하여 실시간 알림을 수신하고 자동 재연결을 관리합니다.
 */
export function useNotificationSSE(options: UseNotificationSSEOptions = {}) {
  const {
    enabled = true,
    onNotification,
    onError,
    onConnect,
    onDisconnect,
  } = options;

  const eventSourceRef = useRef<EventSource | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const maxReconnectAttempts = 10;

  const {
    setConnectionStatus,
    addNotification,
    setUnreadCount,
    addToast,
  } = useNotificationStore();

  /**
   * 지수 백오프 계산 (최대 30초)
   */
  const getReconnectDelay = useCallback((attempts: number): number => {
    const baseDelay = 1000; // 1초
    const maxDelay = 30000; // 30초
    const delay = Math.min(baseDelay * Math.pow(2, attempts), maxDelay);
    // 약간의 랜덤성 추가 (jitter)
    return delay + Math.random() * 1000;
  }, []);

  /**
   * SSE 연결 해제
   */
  const disconnect = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }

    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    setConnectionStatus("disconnected");
    onDisconnect?.();
  }, [setConnectionStatus, onDisconnect]);

  /**
   * SSE 연결 시작
   */
  const connect = useCallback(() => {
    // 이미 연결되어 있으면 종료
    if (eventSourceRef.current) {
      return;
    }

    try {
      setConnectionStatus("connecting");

      const sseUrl = notificationService.getSSEUrl();
      const eventSource = new EventSource(sseUrl, {
        withCredentials: true,
      });

      eventSourceRef.current = eventSource;

      // 연결 확립 이벤트
      eventSource.addEventListener("connected", () => {
        console.log("[SSE] 연결 확립됨");
        setConnectionStatus("connected");
        reconnectAttemptsRef.current = 0; // 재연결 카운터 리셋
        onConnect?.();
      });

      // 알림 수신 이벤트
      eventSource.addEventListener("notification", (event) => {
        try {
          const data: SSENotificationEvent = JSON.parse(event.data);
          const notification = data.notification;

          console.log("[SSE] 알림 수신:", notification);

          // 스토어에 알림 추가
          addNotification(notification);

          // 미읽음 개수 업데이트
          setUnreadCount(data.unreadCount);

          // 토스트 큐에 추가 (읽지 않은 알림만)
          if (!notification.isRead) {
            addToast(notification);
          }

          // 콜백 실행
          onNotification?.(notification);
        } catch (error) {
          console.error("[SSE] 알림 파싱 실패:", error);
        }
      });

      // Heartbeat 이벤트 (연결 유지 확인)
      eventSource.addEventListener("heartbeat", () => {
        console.log("[SSE] Heartbeat 수신");
      });

      // 에러 처리
      eventSource.onerror = (error) => {
        console.error("[SSE] 연결 오류:", error);
        setConnectionStatus("error");

        // EventSource 연결 종료
        eventSource.close();
        eventSourceRef.current = null;

        // 재연결 시도
        if (reconnectAttemptsRef.current < maxReconnectAttempts) {
          const delay = getReconnectDelay(reconnectAttemptsRef.current);
          console.log(
            `[SSE] ${delay}ms 후 재연결 시도 (${reconnectAttemptsRef.current + 1}/${maxReconnectAttempts})`
          );

          reconnectTimeoutRef.current = setTimeout(() => {
            reconnectAttemptsRef.current += 1;
            connect();
          }, delay);
        } else {
          console.error("[SSE] 최대 재연결 시도 횟수 초과");
          setConnectionStatus("disconnected");
          onError?.(
            new Error("SSE 연결 실패: 최대 재연결 시도 횟수 초과")
          );
        }
      };
    } catch (error) {
      console.error("[SSE] 연결 실패:", error);
      setConnectionStatus("error");
      onError?.(error as Error);
    }
  }, [
    setConnectionStatus,
    addNotification,
    setUnreadCount,
    addToast,
    onNotification,
    onConnect,
    onError,
    getReconnectDelay,
  ]);

  /**
   * 수동 재연결
   */
  const reconnect = useCallback(() => {
    disconnect();
    reconnectAttemptsRef.current = 0;
    connect();
  }, [connect, disconnect]);

  /**
   * Hook 초기화 및 정리
   */
  useEffect(() => {
    if (!enabled) {
      disconnect();
      return;
    }

    connect();

    // 컴포넌트 언마운트 시 정리
    return () => {
      disconnect();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled]);

  /**
   * 페이지 가시성 변경 시 재연결
   * (탭 전환 시 연결이 끊어질 수 있음)
   */
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible" && enabled) {
        // 페이지가 다시 보이면 연결 상태 확인
        if (!eventSourceRef.current || eventSourceRef.current.readyState !== EventSource.OPEN) {
          console.log("[SSE] 페이지 재활성화, 재연결 시도");
          reconnect();
        }
      }
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [enabled, reconnect]);

  return {
    reconnect,
    disconnect,
  };
}
