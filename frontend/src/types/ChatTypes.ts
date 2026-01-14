/**
 * 채팅 관련 타입 정의
 */

// 메시지 타입
export enum MessageType {
  TEXT = "TEXT",
  IMAGE = "IMAGE",
  SYSTEM = "SYSTEM",
}

// 채팅방 상태
export enum ChatRoomStatus {
  ACTIVE = "ACTIVE",
  CLOSED = "CLOSED",
}

// WebSocket 연결 상태
export enum WebSocketConnectionStatus {
  CONNECTING = "CONNECTING",
  CONNECTED = "CONNECTED",
  DISCONNECTED = "DISCONNECTED",
  ERROR = "ERROR",
}

// 채팅방 응답 타입
export interface ChatRoom {
  id: string;
  quoteId: string;
  consumerId: string;
  sellerId: string;
  status: ChatRoomStatus;
  createdAt: string;
  updatedAt: string;
  sellerName?: string;
  consumerName?: string;
  sellerProfileImageUrl?: string;
  consumerProfileImageUrl?: string;
  lastMessage?: string;
  totalPrice?: number;
  unreadCount?: number; // 읽지 않은 메시지 수
}

// 채팅 메시지 응답 타입
export interface ChatMessage {
  id: string;
  chatRoomId: string;
  senderId: string;
  messageType: MessageType;
  content: string;
  isRead: boolean;
  createdAt: string;
}

// 타이핑 이벤트 타입
export interface TypingEvent {
  type: "TYPING";
  senderId: string;
  isTyping: boolean;
}

// 채팅방 생성 요청 타입
export interface ChatRoomCreateRequest {
  quoteId: string;
  consumerId: string;
  sellerId: string;
}

// 메시지 전송 요청 타입
export interface ChatMessageSendRequest {
  chatRoomId: string;
  senderId: string;
  messageType: MessageType;
  content: string;
}

// 메시지 읽음 처리 요청 타입
export interface ChatMessageReadRequest {
  chatRoomId: string;
  messageIds: string[];
}

// 페이징된 채팅방 목록 응답 타입
export interface PaginatedChatRooms {
  content: ChatRoom[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 채팅방 목록 조회 파라미터
export interface ChatRoomsQueryParams {
  page?: number;
  size?: number;
}

// 페이징된 채팅 메시지 목록 응답 타입
export interface PaginatedChatMessages {
  content: ChatMessage[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 채팅 메시지 페이징 조회 파라미터
export interface ChatMessagesQueryParams {
  page?: number;
  size?: number;
}

