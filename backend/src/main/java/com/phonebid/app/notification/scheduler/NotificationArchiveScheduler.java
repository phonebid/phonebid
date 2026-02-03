package com.phonebid.app.notification.scheduler;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 아카이빙 스케줄러
 * 90일 경과 알림을 자동으로 삭제하여 DB 용량 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationArchiveScheduler {

    private final NotificationRepository notificationRepository;

    /**
     * 90일 경과 알림 자동 삭제
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void archiveOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        
        List<Notification> oldNotifications = notificationRepository.findNotificationsOlderThan(cutoffDate);
        
        if (oldNotifications.isEmpty()) {
            log.debug("아카이빙 대상 알림 없음: cutoffDate={}", cutoffDate);
            return;
        }

        int count = oldNotifications.size();
        notificationRepository.deleteAll(oldNotifications);
        
        log.info("알림 아카이빙 완료: 삭제된 알림 수={}, cutoffDate={}", count, cutoffDate);
    }
}

