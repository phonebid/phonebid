import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getApiBaseUrl } from "utils/apiUtils";
import type { ChatMessage, ChatMessageSendRequest, TypingEvent } from "types/ChatTypes";
import { WebSocketConnectionStatus } from "types/ChatTypes";

/**
 * WebSocket 서비스 클래스
 * STOMP 프로토콜을 사용하여 실시간 채팅 메시지 송수신을 관리합니다.
 */
class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private connectionStatus: WebSocketConnectionStatus =
    WebSocketConnectionStatus.DISCONNECTED;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000; // 3초

  /**
   * WebSocket 연결 상태 변경 콜백
   */
  private onStatusChangeCallbacks: Array<
    (status: WebSocketConnectionStatus) => void
  > = [];

  /**
   * 메시지 수신 콜백 (채팅방 ID별)
   */
  private messageCallbacks: Map<
    string,
    Array<(message: ChatMessage) => void>
  > = new Map();

  /**
   * 타이핑 이벤트 수신 콜백 (채팅방 ID별)
   */
  private typingCallbacks: Map<
    string,
    Array<(event: TypingEvent) => void>
  > = new Map();

  /**
   * 타이핑 구독 (채팅방 ID별)
   */
  private typingSubscriptions: Map<string, StompSubscription> = new Map();

  /**
   * 읽음 상태 업데이트 콜백 (채팅방 ID별)
   */
  private readStatusCallbacks: Map<
    string,
    Array<(message: ChatMessage) => void>
  > = new Map();

  /**
   * 읽음 상태 구독 (채팅방 ID별)
   */
  private readStatusSubscriptions: Map<string, StompSubscription> = new Map();

  /**
   * WebSocket 연결 초기화
   */
  connect(): void {
    // 이미 연결되어 있으면 중복 연결 방지
    if (this.client?.connected) {
      console.log("WebSocket already connected");
      return;
    }

    // 이미 연결 중이면 중복 연결 방지
    if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING) {
      console.log("WebSocket connection already in progress");
      return;
    }

    const baseUrl = getApiBaseUrl();
    const token = localStorage.getItem("accessToken");
    
    if (!token) {
      console.error("JWT 토큰이 없습니다. WebSocket 연결을 중단합니다.");
      this.connectionStatus = WebSocketConnectionStatus.ERROR;
      this.notifyStatusChange();
      return;
    }

    // JWT 토큰을 쿼리 파라미터로 전달 (SockJS는 헤더 설정이 제한적이므로)
    const wsUrl = `${baseUrl}/ws/chat?token=${encodeURIComponent(token)}`;

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as unknown as WebSocket,
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log("STOMP:", str);
        }
      },
      onConnect: () => {
        console.log("WebSocket connected");
        this.connectionStatus = WebSocketConnectionStatus.CONNECTED;
        this.reconnectAttempts = 0;
        this.notifyStatusChange();
      },
      onDisconnect: () => {
        console.log("WebSocket disconnected");
        this.connectionStatus = WebSocketConnectionStatus.DISCONNECTED;
        this.notifyStatusChange();
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame);
        this.connectionStatus = WebSocketConnectionStatus.ERROR;
        this.notifyStatusChange();
        this.handleReconnect();
      },
      onWebSocketError: (event) => {
        console.error("WebSocket error:", event);
        this.connectionStatus = WebSocketConnectionStatus.ERROR;
        this.notifyStatusChange();
        this.handleReconnect();
      },
    });

    this.connectionStatus = WebSocketConnectionStatus.CONNECTING;
    this.notifyStatusChange();
    this.client.activate();
  }

  /**
   * WebSocket 연결 해제
   */
  disconnect(): void {
    if (this.client) {
      // 모든 구독 해제
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();

      // 타이핑 구독 해제
      this.typingSubscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.typingSubscriptions.clear();
      this.typingCallbacks.clear();

      // 읽음 상태 구독 해제
      this.readStatusSubscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.readStatusSubscriptions.clear();
      this.readStatusCallbacks.clear();

      // 연결 해제
      this.client.deactivate();
      this.client = null;
      this.connectionStatus = WebSocketConnectionStatus.DISCONNECTED;
      this.notifyStatusChange();
    }
  }

  /**
   * 특정 채팅방 구독
   */
  subscribeToChatRoom(
    chatRoomId: string,
    onMessage: (message: ChatMessage) => void
  ): () => void {
    if (!this.client?.connected) {
      // 연결 중이면 연결 완료를 기다림
      if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING) {
        const checkConnection = () => {
          if (this.client?.connected) {
            this.subscribeToChatRoom(chatRoomId, onMessage);
          } else if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING) {
            setTimeout(checkConnection, 500);
          }
        };
        setTimeout(checkConnection, 500);
        return () => {};
      }
      
      // 연결되지 않았으면 연결 시도
      console.warn("WebSocket not connected. Attempting to connect...");
      this.connect();
      
      // 연결 대기 후 재시도 (최대 5초)
      let attempts = 0;
      const maxAttempts = 10;
      const checkAndSubscribe = () => {
        attempts++;
        if (this.client?.connected) {
          this.subscribeToChatRoom(chatRoomId, onMessage);
        } else if (attempts < maxAttempts && this.connectionStatus !== WebSocketConnectionStatus.ERROR) {
          setTimeout(checkAndSubscribe, 500);
        } else {
          console.error("Failed to subscribe: WebSocket connection failed");
        }
      };
      setTimeout(checkAndSubscribe, 500);
      return () => {};
    }

    // 이미 구독 중이면 기존 구독 해제
    if (this.subscriptions.has(chatRoomId)) {
      this.unsubscribeFromChatRoom(chatRoomId);
    }

    const topic = `/topic/chat/${chatRoomId}`;

    // 메시지 콜백 등록
    if (!this.messageCallbacks.has(chatRoomId)) {
      this.messageCallbacks.set(chatRoomId, []);
    }
    this.messageCallbacks.get(chatRoomId)?.push(onMessage);

    // STOMP 구독
    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        const callbacks = this.messageCallbacks.get(chatRoomId);
        callbacks?.forEach((callback) => callback(chatMessage));
      } catch (error) {
        console.error("Failed to parse chat message:", error);
      }
    });

    this.subscriptions.set(chatRoomId, subscription);
    console.log(`Subscribed to chat room: ${chatRoomId}`);

    // 구독 해제 함수 반환
    return () => {
      this.unsubscribeFromChatRoom(chatRoomId);
      const callbacks = this.messageCallbacks.get(chatRoomId);
      if (callbacks) {
        const index = callbacks.indexOf(onMessage);
        if (index > -1) {
          callbacks.splice(index, 1);
        }
      }
    };
  }

  /**
   * 특정 채팅방 구독 해제
   */
  unsubscribeFromChatRoom(chatRoomId: string): void {
    const subscription = this.subscriptions.get(chatRoomId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(chatRoomId);
      this.messageCallbacks.delete(chatRoomId);
      console.log(`Unsubscribed from chat room: ${chatRoomId}`);
    }

    // 타이핑 구독도 해제
    const typingSubscription = this.typingSubscriptions.get(chatRoomId);
    if (typingSubscription) {
      typingSubscription.unsubscribe();
      this.typingSubscriptions.delete(chatRoomId);
      this.typingCallbacks.delete(chatRoomId);
      console.log(`Unsubscribed from typing events: ${chatRoomId}`);
    }

    // 읽음 상태 구독도 해제
    const readStatusSubscription = this.readStatusSubscriptions.get(chatRoomId);
    if (readStatusSubscription) {
      readStatusSubscription.unsubscribe();
      this.readStatusSubscriptions.delete(chatRoomId);
      this.readStatusCallbacks.delete(chatRoomId);
      console.log(`Unsubscribed from read status events: ${chatRoomId}`);
    }
  }

  /**
   * 읽음 상태 업데이트 구독
   */
  subscribeToReadStatus(
    chatRoomId: string,
    onReadStatus: (message: ChatMessage) => void
  ): () => void {
    if (!this.client?.connected) {
      // 연결 중이면 연결 완료를 기다림
      if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING) {
        const checkConnection = () => {
          if (this.client?.connected) {
            this.subscribeToReadStatus(chatRoomId, onReadStatus);
          } else if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING) {
            setTimeout(checkConnection, 500);
          }
        };
        setTimeout(checkConnection, 500);
        return () => {};
      }
      
      // 연결되지 않았으면 연결 시도
      console.warn("WebSocket not connected. Attempting to connect...");
      this.connect();
      
      // 연결 대기 후 재시도 (최대 5초)
      let attempts = 0;
      const maxAttempts = 10;
      const checkAndSubscribe = () => {
        attempts++;
        if (this.client?.connected) {
          this.subscribeToReadStatus(chatRoomId, onReadStatus);
        } else if (attempts < maxAttempts && this.connectionStatus !== WebSocketConnectionStatus.ERROR) {
          setTimeout(checkAndSubscribe, 500);
        } else {
          console.error("Failed to subscribe: WebSocket connection failed");
        }
      };
      setTimeout(checkAndSubscribe, 500);
      return () => {};
    }

    // 이미 구독 중이면 기존 구독 해제
    if (this.readStatusSubscriptions.has(chatRoomId)) {
      const existing = this.readStatusSubscriptions.get(chatRoomId);
      existing?.unsubscribe();
    }

    const topic = `/topic/chat/${chatRoomId}/read`;

    // 읽음 상태 콜백 등록
    if (!this.readStatusCallbacks.has(chatRoomId)) {
      this.readStatusCallbacks.set(chatRoomId, []);
    }
    this.readStatusCallbacks.get(chatRoomId)?.push(onReadStatus);

    // STOMP 구독
    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        const callbacks = this.readStatusCallbacks.get(chatRoomId);
        callbacks?.forEach((callback) => callback(chatMessage));
      } catch (error) {
        console.error("Failed to parse read status update:", error);
      }
    });

    this.readStatusSubscriptions.set(chatRoomId, subscription);
    console.log(`Subscribed to read status updates: ${chatRoomId}`);

    // 구독 해제 함수 반환
    return () => {
      subscription.unsubscribe();
      this.readStatusSubscriptions.delete(chatRoomId);
      const callbacks = this.readStatusCallbacks.get(chatRoomId);
      if (callbacks) {
        const index = callbacks.indexOf(onReadStatus);
        if (index > -1) {
          callbacks.splice(index, 1);
        }
      }
    };
  }

  /**
   * 타이핑 이벤트 구독
   */
  subscribeToTyping(
    chatRoomId: string,
    onTyping: (event: TypingEvent) => void
  ): () => void {
    if (!this.client?.connected) {
      // 연결 중이면 연결 완료를 기다림
      if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING) {
        const checkConnection = () => {
          if (this.client?.connected) {
            this.subscribeToTyping(chatRoomId, onTyping);
          } else if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING) {
            setTimeout(checkConnection, 500);
          }
        };
        setTimeout(checkConnection, 500);
        return () => {};
      }
      
      // 연결되지 않았으면 연결 시도
      console.warn("WebSocket not connected. Attempting to connect...");
      this.connect();
      
      // 연결 대기 후 재시도 (최대 5초)
      let attempts = 0;
      const maxAttempts = 10;
      const checkAndSubscribe = () => {
        attempts++;
        if (this.client?.connected) {
          this.subscribeToTyping(chatRoomId, onTyping);
        } else if (attempts < maxAttempts && this.connectionStatus !== WebSocketConnectionStatus.ERROR) {
          setTimeout(checkAndSubscribe, 500);
        } else {
          console.error("Failed to subscribe: WebSocket connection failed");
        }
      };
      setTimeout(checkAndSubscribe, 500);
      return () => {};
    }

    // 이미 구독 중이면 기존 구독 해제
    if (this.typingSubscriptions.has(chatRoomId)) {
      const existing = this.typingSubscriptions.get(chatRoomId);
      existing?.unsubscribe();
    }

    const topic = `/topic/chat/${chatRoomId}/typing`;

    // 타이핑 콜백 등록
    if (!this.typingCallbacks.has(chatRoomId)) {
      this.typingCallbacks.set(chatRoomId, []);
    }
    this.typingCallbacks.get(chatRoomId)?.push(onTyping);

    // STOMP 구독
    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const typingEvent: TypingEvent = JSON.parse(message.body);
        const callbacks = this.typingCallbacks.get(chatRoomId);
        callbacks?.forEach((callback) => callback(typingEvent));
      } catch (error) {
        console.error("Failed to parse typing event:", error);
      }
    });

    this.typingSubscriptions.set(chatRoomId, subscription);
    console.log(`Subscribed to typing events: ${chatRoomId}`);

    // 구독 해제 함수 반환
    return () => {
      subscription.unsubscribe();
      this.typingSubscriptions.delete(chatRoomId);
      const callbacks = this.typingCallbacks.get(chatRoomId);
      if (callbacks) {
        const index = callbacks.indexOf(onTyping);
        if (index > -1) {
          callbacks.splice(index, 1);
        }
      }
    };
  }

  /**
   * 타이핑 이벤트 전송
   */
  sendTyping(chatRoomId: string, senderId: string, isTyping: boolean): void {
    if (!this.client?.connected) {
      console.error("WebSocket not connected. Cannot send typing event.");
      return;
    }

    try {
      const destination = `/app/chat/${chatRoomId}/typing`;
      this.client.publish({
        destination,
        body: JSON.stringify({
          senderId,
          isTyping,
        }),
      });
    } catch (error) {
      console.error("Failed to send typing event:", error);
    }
  }

  /**
   * 메시지 전송
   */
  sendMessage(request: ChatMessageSendRequest): void {
    if (!this.client?.connected) {
      console.error("WebSocket not connected. Cannot send message.");
      // 연결 시도
      this.connect();
      // 연결 대기 후 재시도
      setTimeout(() => {
        if (this.client?.connected) {
          this.sendMessage(request);
        } else {
          console.error("Failed to reconnect. Message not sent.");
        }
      }, 1000);
      return;
    }

    try {
      const destination = `/app/chat/${request.chatRoomId}/send`;
      this.client.publish({
        destination,
        body: JSON.stringify(request),
      });
    } catch (error) {
      console.error("Failed to send message:", error);
    }
  }

  /**
   * 연결 상태 구독
   */
  onStatusChange(callback: (status: WebSocketConnectionStatus) => void): () => void {
    this.onStatusChangeCallbacks.push(callback);
    // 현재 상태 즉시 알림
    callback(this.connectionStatus);

    // 구독 해제 함수 반환
    return () => {
      const index = this.onStatusChangeCallbacks.indexOf(callback);
      if (index > -1) {
        this.onStatusChangeCallbacks.splice(index, 1);
      }
    };
  }

  /**
   * 현재 연결 상태 반환
   */
  getConnectionStatus(): WebSocketConnectionStatus {
    return this.connectionStatus;
  }

  /**
   * 연결 상태 변경 알림
   */
  private notifyStatusChange(): void {
    this.onStatusChangeCallbacks.forEach((callback) => {
      callback(this.connectionStatus);
    });
  }

  /**
   * 재연결 처리
   */
  private handleReconnect(): void {
    // 이미 연결 중이거나 연결되어 있으면 재연결 시도하지 않음
    if (this.connectionStatus === WebSocketConnectionStatus.CONNECTING ||
        this.connectionStatus === WebSocketConnectionStatus.CONNECTED) {
      return;
    }

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error("Max reconnect attempts reached. Stopping reconnection attempts.");
      this.connectionStatus = WebSocketConnectionStatus.ERROR;
      this.notifyStatusChange();
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * this.reconnectAttempts;

    console.log(
      `Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts}) in ${delay}ms`
    );

    setTimeout(() => {
      // 재연결 시도 전에 상태 확인
      if (this.connectionStatus !== WebSocketConnectionStatus.CONNECTED &&
          this.connectionStatus !== WebSocketConnectionStatus.CONNECTING) {
        this.connect();
      }
    }, delay);
  }
}

// 싱글톤 인스턴스
export const websocketService = new WebSocketService();

