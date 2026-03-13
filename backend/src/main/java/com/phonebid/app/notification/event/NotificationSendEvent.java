package com.phonebid.app.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * 알림 발송 이벤트
 * 알림이 DB에 저장된 후 발송을 위해 발행되는 이벤트
 * ID만 전달하여 이벤트 경량화 및 트랜잭션 경계 명확화
 */
@Getter
public class NotificationSendEvent extends ApplicationEvent {
    private final UUID notificationId;

    public NotificationSendEvent(Object source, UUID notificationId) {
        super(source);
        this.notificationId = notificationId;
    }
}
