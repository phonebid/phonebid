import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getApiBaseUrl } from "utils/apiUtils";
import type { ChatMessage, ChatMessageSendRequest } from "types/ChatTypes";
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
   * WebSocket 연결 초기화
   */
  connect(): void {
    if (this.client?.connected) {
      console.log("WebSocket already connected");
      return;
    }

    const baseUrl = getApiBaseUrl();
    const wsUrl = `${baseUrl}/ws/chat`;

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
      console.warn("WebSocket not connected. Attempting to connect...");
      this.connect();
      // 연결 대기 후 재시도
      setTimeout(() => {
        this.subscribeToChatRoom(chatRoomId, onMessage);
      }, 1000);
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
  }

  /**
   * 메시지 전송
   */
  sendMessage(request: ChatMessageSendRequest): void {
    if (!this.client?.connected) {
      console.error("WebSocket not connected. Cannot send message.");
      return;
    }

    const destination = `/app/chat/${request.chatRoomId}/send`;
    this.client.publish({
      destination,
      body: JSON.stringify(request),
    });
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
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error("Max reconnect attempts reached");
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * this.reconnectAttempts;

    console.log(
      `Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts}) in ${delay}ms`
    );

    setTimeout(() => {
      if (this.connectionStatus !== WebSocketConnectionStatus.CONNECTED) {
        this.connect();
      }
    }, delay);
  }
}

// 싱글톤 인스턴스
export const websocketService = new WebSocketService();

