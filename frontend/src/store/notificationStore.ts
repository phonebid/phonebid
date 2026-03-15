import { create } from "zustand";
import { devtools } from "zustand/middleware";
import type {
  NotificationDisplayItem,
  SSEConnectionStatus,
} from "types/NotificationTypes";

interface NotificationStore {
  // SSE 연결 상태
  connectionStatus: SSEConnectionStatus;
  
  // 알림 데이터
  notifications: NotificationDisplayItem[];
  unreadCount: number;
  
  // 토스트 알림 큐
  toastQueue: NotificationDisplayItem[];
  
  // Actions
  setConnectionStatus: (status: SSEConnectionStatus) => void;
  
  // 알림 관리
  addNotification: (notification: NotificationDisplayItem) => void;
  updateNotification: (id: string, updates: Partial<NotificationDisplayItem>) => void;
  removeNotification: (id: string) => void;
  setNotifications: (notifications: NotificationDisplayItem[]) => void;
  clearNotifications: () => void;
  
  // 미읽음 개수
  setUnreadCount: (count: number) => void;
  incrementUnreadCount: () => void;
  decrementUnreadCount: () => void;
  
  // 읽음 처리
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  
  // 토스트 큐 관리
  addToast: (notification: NotificationDisplayItem) => void;
  removeToast: (id: string) => void;
  clearToasts: () => void;
}

export const useNotificationStore = create<NotificationStore>()(
  devtools(
    (set, get) => ({
      // Initial state
      connectionStatus: "disconnected",
      notifications: [],
      unreadCount: 0,
      toastQueue: [],

      // SSE 연결 상태 관리
      setConnectionStatus: (status) => {
        set({ connectionStatus: status }, false, "notification/setConnectionStatus");
      },

      // 알림 추가 (최신 알림이 앞으로)
      addNotification: (notification) => {
        set(
          (state) => ({
            notifications: [notification, ...state.notifications],
          }),
          false,
          "notification/addNotification"
        );
        
        // 읽지 않은 알림이면 카운트 증가
        if (!notification.isRead) {
          get().incrementUnreadCount();
        }
      },

      // 알림 업데이트
      updateNotification: (id, updates) => {
        set(
          (state) => ({
            notifications: state.notifications.map((n) =>
              n.id === id ? { ...n, ...updates } : n
            ),
          }),
          false,
          "notification/updateNotification"
        );
      },

      // 알림 제거
      removeNotification: (id) => {
        set(
          (state) => {
            const notification = state.notifications.find((n) => n.id === id);
            const shouldDecrementCount = notification && !notification.isRead;
            
            return {
              notifications: state.notifications.filter((n) => n.id !== id),
              unreadCount: shouldDecrementCount
                ? Math.max(0, state.unreadCount - 1)
                : state.unreadCount,
            };
          },
          false,
          "notification/removeNotification"
        );
      },

      // 알림 목록 설정 (초기 로드 시)
      setNotifications: (notifications) => {
        set(
          { notifications },
          false,
          "notification/setNotifications"
        );
      },

      // 모든 알림 제거
      clearNotifications: () => {
        set(
          { notifications: [], unreadCount: 0 },
          false,
          "notification/clearNotifications"
        );
      },

      // 미읽음 개수 관리
      setUnreadCount: (count) => {
        set(
          { unreadCount: Math.max(0, count) },
          false,
          "notification/setUnreadCount"
        );
      },

      incrementUnreadCount: () => {
        set(
          (state) => ({ unreadCount: state.unreadCount + 1 }),
          false,
          "notification/incrementUnreadCount"
        );
      },

      decrementUnreadCount: () => {
        set(
          (state) => ({ unreadCount: Math.max(0, state.unreadCount - 1) }),
          false,
          "notification/decrementUnreadCount"
        );
      },

      // 읽음 처리
      markAsRead: (id) => {
        set(
          (state) => {
            const notification = state.notifications.find((n) => n.id === id);
            if (!notification || notification.isRead) {
              return state;
            }

            return {
              notifications: state.notifications.map((n) =>
                n.id === id ? { ...n, isRead: true } : n
              ),
              unreadCount: Math.max(0, state.unreadCount - 1),
            };
          },
          false,
          "notification/markAsRead"
        );
      },

      markAllAsRead: () => {
        set(
          (state) => ({
            notifications: state.notifications.map((n) => ({
              ...n,
              isRead: true,
            })),
            unreadCount: 0,
          }),
          false,
          "notification/markAllAsRead"
        );
      },

      // 토스트 큐 관리
      addToast: (notification) => {
        set(
          (state) => ({
            toastQueue: [...state.toastQueue, notification],
          }),
          false,
          "notification/addToast"
        );
      },

      removeToast: (id) => {
        set(
          (state) => ({
            toastQueue: state.toastQueue.filter((n) => n.id !== id),
          }),
          false,
          "notification/removeToast"
        );
      },

      clearToasts: () => {
        set(
          { toastQueue: [] },
          false,
          "notification/clearToasts"
        );
      },
    }),
    {
      name: "notification-store",
    }
  )
);
