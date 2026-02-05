package com.phonebid.app.notification.retry;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 재시도 가능한 알림 발송 래퍼
 * 지수 백오프(Exponential Backoff) 기반 재시도 메커니즘 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryableNotificationSender {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000L; // 1초

    /**
     * 알림 발송을 재시도 메커니즘과 함께 실행
     * 
     * @param sender 알림 발송자
     * @param notification 발송할 알림
     * @return 발송 성공 여부
     */
    public boolean sendWithRetry(NotificationSender sender, Notification notification) {
        int attempt = 0;
        long delay = INITIAL_DELAY_MS;

        while (attempt < MAX_RETRIES) {
            try {
                boolean success = sender.send(notification);
                
                if (success) {
                    if (attempt > 0) {
                        log.info("알림 발송 재시도 성공: notificationId={}, attempt={}", 
                                notification.getId(), attempt + 1);
                    }
                    return true;
                }

                // 실패한 경우 재시도
                attempt++;
                if (attempt < MAX_RETRIES) {
                    log.warn("알림 발송 실패, 재시도 예정: notificationId={}, attempt={}/{}, delay={}ms", 
                            notification.getId(), attempt, MAX_RETRIES, delay);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("재시도 대기 중 인터럽트 발생: notificationId={}", notification.getId());
                        return false;
                    }
                    
                    // 지수 백오프: 1초 -> 2초 -> 4초
                    delay *= 2;
                }

            } catch (Exception e) {
                attempt++;
                log.error("알림 발송 중 예외 발생: notificationId={}, attempt={}", 
                         notification.getId(), attempt, e);
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("재시도 대기 중 인터럽트 발생: notificationId={}", notification.getId());
                        return false;
                    }
                    delay *= 2;
                }
            }
        }

        log.error("알림 발송 최종 실패: notificationId={}, maxRetries={}", 
                 notification.getId(), MAX_RETRIES);
        return false;
    }
}

