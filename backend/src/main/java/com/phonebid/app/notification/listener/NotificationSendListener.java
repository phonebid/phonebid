package com.phonebid.app.notification.listener;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.event.NotificationSendEvent;
import com.phonebid.app.notification.retry.RetryableNotificationSender;
import com.phonebid.app.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 알림 발송 이벤트 리스너
 * 트랜잭션 커밋 후 비동기로 알림을 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSendListener {

    private final List<NotificationSender> notificationSenders;
    private final RetryableNotificationSender retryableNotificationSender;

    /**
     * 알림 발송 이벤트 처리
     * 트랜잭션 커밋 후 비동기로 실행되어 DB 커넥션을 빠르게 반환
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationSend(NotificationSendEvent event) {
        Notification notification = event.getNotification();
        log.debug("알림 발송 이벤트 수신: notificationId={}, channel={}", 
                 notification.getId(), notification.getChannel());

        NotificationSender sender = findSender(notification);
        if (sender == null) {
            log.warn("지원하지 않는 채널: channel={}, notificationId={}", 
                    notification.getChannel(), notification.getId());
            return;
        }

        // 외부 API 연동이 필요한 채널은 재시도 메커니즘 적용
        boolean success;
        if (notification.getChannel().requiresExternalApi()) {
            success = retryableNotificationSender.sendWithRetry(sender, notification);
        } else {
            success = sender.send(notification);
        }

        if (!success) {
            log.warn("알림 발송 실패: notificationId={}, channel={}", 
                    notification.getId(), notification.getChannel());
        } else {
            log.debug("알림 발송 성공: notificationId={}, channel={}", 
                     notification.getId(), notification.getChannel());
        }
    }

    /**
     * 채널별 발송자 찾기
     */
    private NotificationSender findSender(Notification notification) {
        return notificationSenders.stream()
                .filter(sender -> sender.supports(notification.getChannel()))
                .findFirst()
                .orElse(null);
    }
}
