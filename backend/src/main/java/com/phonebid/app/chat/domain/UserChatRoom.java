package com.phonebid.app.chat.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "user_chat_rooms", indexes = {
    @Index(name = "idx_user_chat_room_user", columnList = "user_id"),
    @Index(name = "idx_user_chat_room_chat_room", columnList = "chat_room_id"),
    @Index(name = "idx_user_chat_room_deleted_at", columnList = "deleted_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("사용자-채팅방 관계 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @Comment("채팅방 ID")
    private ChatRoom chatRoom;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @Comment("채팅방 참여 시각")
    private LocalDateTime joinedAt;

    @Builder
    public UserChatRoom(User user, ChatRoom chatRoom) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.joinedAt = LocalDateTime.now();
    }

    /**
     * 채팅방 나가기 (soft delete)
     * BaseEntity의 deletedAt, deletedBy, isDelete 필드도 함께 설정
     */
    public void leave(String deletedBy) {
        if (isDeleted()) {
            throw new IllegalStateException("이미 나간 채팅방입니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        this.deletedAt = now;
        this.deletedBy = deletedBy;
        this.isDelete = true;
    }

    /**
     * 나간 채팅방인지 확인
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 채팅방에 접근 가능한지 확인
     */
    public boolean isActive() {
        return !isDeleted();
    }
}

