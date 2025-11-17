package com.phonebid.app.chat.dto.response;

import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.domain.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomResponse {

    private UUID id;
    private UUID quoteId;
    private UUID consumerId;
    private UUID sellerId;
    private ChatRoomStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String sellerName;
    private String lastMessage;
    private Integer totalPrice;
    private Long unreadCount;

    private ChatRoomResponse(UUID id, UUID quoteId, UUID consumerId, UUID sellerId,
                             ChatRoomStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
                             String sellerName, String lastMessage, Integer totalPrice, Long unreadCount) {
        this.id = id;
        this.quoteId = quoteId;
        this.consumerId = consumerId;
        this.sellerId = sellerId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.sellerName = sellerName;
        this.lastMessage = lastMessage;
        this.totalPrice = totalPrice;
        this.unreadCount = unreadCount;
    }

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getQuote().getId(),
                chatRoom.getConsumer().getId(),
                chatRoom.getSeller().getUser().getId(), // Seller의 User ID 사용 (메시지 senderId와 일치)
                chatRoom.getStatus(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt(),
                null, // sellerName은 서비스에서 설정
                null, // lastMessage는 서비스에서 설정
                null, // totalPrice는 서비스에서 설정
                null  // unreadCount는 서비스에서 설정
        );
    }
    
    public static ChatRoomResponse from(ChatRoom chatRoom, String sellerName, String lastMessage, Integer totalPrice, Long unreadCount) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getQuote().getId(),
                chatRoom.getConsumer().getId(),
                chatRoom.getSeller().getUser().getId(), // Seller의 User ID 사용 (메시지 senderId와 일치)
                chatRoom.getStatus(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt(),
                sellerName,
                lastMessage,
                totalPrice,
                unreadCount
        );
    }
}


