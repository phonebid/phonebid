package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.Notification;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 알림 발송 이벤트
 * 알림이 DB에 저장된 후 발송을 위해 발행되는 이벤트
 */
@Getter
public class NotificationSendEvent extends ApplicationEvent {
    private final Notification notification;

    public NotificationSendEvent(Object source, Notification notification) {
        super(source);
        this.notification = notification;
    }
}
