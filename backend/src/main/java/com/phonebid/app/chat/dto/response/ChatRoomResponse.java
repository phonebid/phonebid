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

    private ChatRoomResponse(UUID id, UUID quoteId, UUID consumerId, UUID sellerId,
                             ChatRoomStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.quoteId = quoteId;
        this.consumerId = consumerId;
        this.sellerId = sellerId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getQuote().getId(),
                chatRoom.getConsumer().getId(),
                chatRoom.getSeller().getSellerId(),
                chatRoom.getStatus(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }
}


