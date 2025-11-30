import { useEffect, useRef, useCallback, useState } from "react";
import { websocketService } from "services/websocketService";
import type {
  ChatMessage,
  ChatMessageSendRequest,
  WebSocketConnectionStatus,
  TypingEvent,
} from "types/ChatTypes";

interface UseWebSocketOptions {
  chatRoomId?: string;
  autoConnect?: boolean;
  onMessage?: (message: ChatMessage) => void;
  onTyping?: (event: TypingEvent) => void;
  onReadStatus?: (message: ChatMessage) => void;
}

interface UseWebSocketReturn {
  connectionStatus: WebSocketConnectionStatus;
  sendMessage: (request: ChatMessageSendRequest) => void;
  sendTyping: (senderId: string, isTyping: boolean) => void;
  connect: () => void;
  disconnect: () => void;
}

/**
 * WebSocket 연결 및 메시지 송수신을 관리하는 커스텀 훅
 */
export function useWebSocket(
  options: UseWebSocketOptions = {}
): UseWebSocketReturn {
  const { chatRoomId, autoConnect = true, onMessage, onTyping, onReadStatus } = options;
  const onMessageRef = useRef(onMessage);
  const onTypingRef = useRef(onTyping);
  const onReadStatusRef = useRef(onReadStatus);
  const unsubscribeRef = useRef<(() => void) | null>(null);
  const unsubscribeTypingRef = useRef<(() => void) | null>(null);
  const unsubscribeReadStatusRef = useRef<(() => void) | null>(null);

  // onMessage 콜백 최신화
  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  // onTyping 콜백 최신화
  useEffect(() => {
    onTypingRef.current = onTyping;
  }, [onTyping]);

  // onReadStatus 콜백 최신화
  useEffect(() => {
    onReadStatusRef.current = onReadStatus;
  }, [onReadStatus]);

  // 연결 상태 관리
  const [connectionStatus, setConnectionStatus] =
    useState<WebSocketConnectionStatus>(
      websocketService.getConnectionStatus()
    );

  // 연결 상태 구독
  useEffect(() => {
    const unsubscribe = websocketService.onStatusChange((status) => {
      setConnectionStatus(status);
    });

    return unsubscribe;
  }, []);

  // 자동 연결
  useEffect(() => {
    if (autoConnect) {
      websocketService.connect();
    }

    return () => {
      if (autoConnect) {
        websocketService.disconnect();
      }
    };
  }, [autoConnect]);

  // 채팅방 구독
  useEffect(() => {
    if (!chatRoomId || !onMessageRef.current) {
      return;
    }

    const unsubscribe = websocketService.subscribeToChatRoom(
      chatRoomId,
      (message) => {
        onMessageRef.current?.(message);
      }
    );

    unsubscribeRef.current = unsubscribe;

    return () => {
      unsubscribe();
      unsubscribeRef.current = null;
    };
  }, [chatRoomId]);

  // 타이핑 이벤트 구독
  useEffect(() => {
    if (!chatRoomId || !onTypingRef.current) {
      return;
    }

    const unsubscribe = websocketService.subscribeToTyping(
      chatRoomId,
      (event) => {
        onTypingRef.current?.(event);
      }
    );

    unsubscribeTypingRef.current = unsubscribe;

    return () => {
      unsubscribe();
      unsubscribeTypingRef.current = null;
    };
  }, [chatRoomId]);

  // 읽음 상태 업데이트 구독
  useEffect(() => {
    if (!chatRoomId || !onReadStatusRef.current) {
      return;
    }

    const unsubscribe = websocketService.subscribeToReadStatus(
      chatRoomId,
      (message) => {
        onReadStatusRef.current?.(message);
      }
    );

    unsubscribeReadStatusRef.current = unsubscribe;

    return () => {
      unsubscribe();
      unsubscribeReadStatusRef.current = null;
    };
  }, [chatRoomId]);

  // 메시지 전송
  const sendMessage = useCallback((request: ChatMessageSendRequest) => {
    websocketService.sendMessage(request);
  }, []);

  // 타이핑 이벤트 전송
  const sendTyping = useCallback(
    (senderId: string, isTyping: boolean) => {
      if (!chatRoomId) {
        return;
      }
      websocketService.sendTyping(chatRoomId, senderId, isTyping);
    },
    [chatRoomId]
  );

  // 연결
  const connect = useCallback(() => {
    websocketService.connect();
  }, []);

  // 연결 해제
  const disconnect = useCallback(() => {
    websocketService.disconnect();
  }, []);

  return {
    connectionStatus,
    sendMessage,
    sendTyping,
    connect,
    disconnect,
  };
}

