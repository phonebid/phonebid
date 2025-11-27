import { apiClient } from "services/apiClient";
import type {
  ChatRoom,
  ChatMessage,
  ChatRoomCreateRequest,
  ChatMessageReadRequest,
  PaginatedChatRooms,
  ChatRoomsQueryParams,
  PaginatedChatMessages,
  ChatMessagesQueryParams,
} from "types/ChatTypes";

const BASE_URL = "/chat/rooms";

/**
 * 채팅방 목록 조회 (페이징)
 */
export const getChatRooms = async (
  params?: ChatRoomsQueryParams
): Promise<PaginatedChatRooms> => {
  const queryParams = new URLSearchParams();
  if (params?.page !== undefined) {
    queryParams.append("page", String(params.page));
  }
  if (params?.size !== undefined) {
    queryParams.append("size", String(params.size));
  }

  const url = queryParams.toString()
    ? `${BASE_URL}?${queryParams.toString()}`
    : BASE_URL;

  return await apiClient.get<PaginatedChatRooms>(url);
};

/**
 * 채팅방 상세 조회
 */
export const getChatRoom = async (chatRoomId: string): Promise<ChatRoom> => {
  return await apiClient.get<ChatRoom>(`${BASE_URL}/${chatRoomId}`);
};

/**
 * 채팅방 생성
 */
export const createChatRoom = async (
  request: ChatRoomCreateRequest
): Promise<ChatRoom> => {
  return await apiClient.post<ChatRoom>(BASE_URL, request);
};

/**
 * 채팅 메시지 목록 조회 (전체)
 * @deprecated 역순 페이징 API 사용을 권장합니다. 대신 getChatMessagesPaginated를 사용하세요.
 */
export const getChatMessages = async (
  chatRoomId: string
): Promise<ChatMessage[]> => {
  return await apiClient.get<ChatMessage[]>(
    `${BASE_URL}/${chatRoomId}/messages`
  );
};

/**
 * 채팅 메시지 목록 역순 페이징 조회 (최신 메시지부터)
 * 역순 무한스크롤을 위해 사용합니다.
 */
export const getChatMessagesPaginated = async (
  chatRoomId: string,
  params?: ChatMessagesQueryParams
): Promise<PaginatedChatMessages> => {
  const queryParams = new URLSearchParams();
  if (params?.page !== undefined) {
    queryParams.append("page", String(params.page));
  }
  if (params?.size !== undefined) {
    queryParams.append("size", String(params.size));
  }

  const url = queryParams.toString()
    ? `${BASE_URL}/${chatRoomId}/messages/paginated?${queryParams.toString()}`
    : `${BASE_URL}/${chatRoomId}/messages/paginated`;

  return await apiClient.get<PaginatedChatMessages>(url);
};

/**
 * 채팅 메시지 읽음 처리
 */
export const markMessagesAsRead = async (
  request: ChatMessageReadRequest
): Promise<void> => {
  return await apiClient.post<void>(
    `${BASE_URL}/${request.chatRoomId}/messages/read`,
    request
  );
};

/**
 * 채팅방 나가기
 */
export const leaveChatRoom = async (chatRoomId: string): Promise<void> => {
  return await apiClient.delete<void>(`${BASE_URL}/${chatRoomId}`);
};

