package com.phonebid.app.chat.dto.response;

import com.phonebid.app.chat.domain.ChatMessage;
import com.phonebid.app.chat.domain.MessageType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageResponse {

    private UUID id;
    private UUID chatRoomId;
    private UUID senderId;
    private MessageType messageType;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;

    private ChatMessageResponse(UUID id, UUID chatRoomId, UUID senderId, MessageType messageType,
                                String content, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.messageType = messageType;
        this.content = content;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getChatRoom().getId(),
                chatMessage.getSender().getId(),
                chatMessage.getMessageType(),
                chatMessage.getContent(),
                chatMessage.isRead(),
                chatMessage.getCreatedAt()
        );
    }
}


