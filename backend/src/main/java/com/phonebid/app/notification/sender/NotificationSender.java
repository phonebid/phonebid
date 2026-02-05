package com.phonebid.app.notification.sender;

import com.phonebid.app.notification.domain.Notification;

/**
 * 알림 발송 전략 인터페이스 (Strategy Pattern)
 * 각 채널별 발송 로직을 구현
 */
public interface NotificationSender {
    /**
     * 알림 발송
     * 
     * @param notification 발송할 알림
     * @return 발송 성공 여부
     */
    boolean send(Notification notification);

    /**
     * 해당 발송자가 지원하는 채널인지 확인
     * 
     * @param channel 알림 채널
     * @return 지원 여부
     */
    boolean supports(com.phonebid.app.notification.domain.NotificationChannel channel);
}

