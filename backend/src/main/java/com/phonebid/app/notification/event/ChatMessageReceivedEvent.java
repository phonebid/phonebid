package com.phonebid.app.notification.event;

import com.phonebid.app.chat.domain.ChatMessage;
import com.phonebid.app.notification.domain.NotificationType;

import java.util.UUID;

/**
 * 채팅 메시지 수신 이벤트
 * 채팅 상대방에게 메시지 수신 알림 발송
 */
public class ChatMessageReceivedEvent extends NotificationEvent {
    private final ChatMessage chatMessage;
    private final UUID recipientUserId;

    public ChatMessageReceivedEvent(Object source, ChatMessage chatMessage, UUID recipientUserId) {
        super(source, recipientUserId, NotificationType.CHAT_MESSAGE_RECEIVED, chatMessage.getId());
        this.chatMessage = chatMessage;
        this.recipientUserId = recipientUserId;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }
}

