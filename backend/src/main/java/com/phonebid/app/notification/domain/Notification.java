package com.phonebid.app.notification.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_type", columnList = "type"),
    @Index(name = "idx_notifications_channel", columnList = "channel"),
    @Index(name = "idx_notifications_is_read", columnList = "is_read")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "reference_id")
    private UUID referenceId; // 관련 엔터티 ID (Quote, Bid, Contract 등)

    @Builder
    public Notification(User user, NotificationType type, NotificationChannel channel, 
                       String title, String message, UUID referenceId) {
        validateNotificationCreation(user, type, channel, title, message);
        
        this.user = user;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.isRead = false; // 기본값: 읽지 않음
    }

    // 정적 팩토리 메서드
    public static Notification createWithDefaultMessage(User user, NotificationType type, 
                                                       NotificationChannel channel, UUID referenceId) {
        return Notification.builder()
                .user(user)
                .type(type)
                .channel(channel)
                .title(type.getDisplayName())
                .message(type.getDefaultMessage())
                .referenceId(referenceId)
                .build();
    }

    // 비즈니스 메서드
    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsUnread() {
        this.isRead = false;
    }

    public boolean isUnread() {
        return !isRead;
    }

    public boolean hasReference() {
        return referenceId != null;
    }

    public boolean isHighPriority() {
        return type.getPriority() >= 4;
    }

    public boolean isForConsumer() {
        return type.isForConsumer();
    }

    public boolean isForSeller() {
        return type.isForSeller();
    }

    public String getNotificationSummary() {
        String readStatus = isRead ? "읽음" : "읽지않음";
        return String.format("[%s] %s - %s (%s)", 
            channel.getDisplayName(), title, readStatus, getCreatedAt().toLocalDate());
    }

    public boolean shouldSendImmediately() {
        return channel.isInstant() && isHighPriority();
    }

    // 검증 메서드
    private void validateNotificationCreation(User user, NotificationType type, 
                                            NotificationChannel channel, String title, String message) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보는 필수입니다.");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("알림 유형은 필수입니다.");
        }
        
        if (channel == null) {
            throw new IllegalArgumentException("알림 채널은 필수입니다.");
        }
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("알림 제목은 필수입니다.");
        }
        
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("알림 메시지는 필수입니다.");
        }
        
        if (title.length() > 100) {
            throw new IllegalArgumentException("알림 제목은 100자를 초과할 수 없습니다.");
        }
        
        if (message.length() > 1000) {
            throw new IllegalArgumentException("알림 메시지는 1000자를 초과할 수 없습니다.");
        }
    }
} 