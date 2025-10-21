package com.phonebid.app.chat.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_room_created", columnList = "chat_room_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("채팅 메시지 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @Comment("채팅방 ID")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @Comment("발신자 ID")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Comment("메시지 타입 (TEXT, IMAGE, SYSTEM)")
    private MessageType messageType;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @Comment("메시지 내용")
    private String content;

    @Column(name = "is_read", nullable = false)
    @Comment("읽음 여부")
    private boolean isRead;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, MessageType messageType, String content) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.messageType = messageType;
        this.content = content;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}


