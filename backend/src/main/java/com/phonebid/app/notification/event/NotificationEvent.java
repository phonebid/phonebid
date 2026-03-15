package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * 알림 이벤트 추상 클래스
 * 모든 도메인 이벤트는 이 클래스를 상속받아 구현
 */
@Getter
public abstract class NotificationEvent extends ApplicationEvent {
    private final UUID userId;
    private final NotificationType notificationType;
    private final UUID referenceId;

    protected NotificationEvent(Object source, UUID userId, NotificationType notificationType, UUID referenceId) {
        super(source);
        this.userId = userId;
        this.notificationType = notificationType;
        this.referenceId = referenceId;
    }
}

