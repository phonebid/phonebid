package com.phonebid.app.notification.scheduler;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 아카이빙 스케줄러
 * 90일 경과 알림을 자동으로 삭제하여 DB 용량 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationArchiveScheduler {

    private final NotificationRepository notificationRepository;

    private static final int BATCH_SIZE = 1000;

    /**
     * 90일 경과 알림 자동 삭제
     * 매일 새벽 2시에 실행
     * 메모리 효율을 위해 페이지 단위로 처리
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        
        int totalDeleted = 0;
        int pageNumber = 0;
        Page<Notification> page;
        
        do {
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            page = notificationRepository.findNotificationsOlderThan(cutoffDate, pageable);
            
            if (page.isEmpty()) {
                break;
            }
            
            int deletedInThisPage = deleteNotificationsInBatch(page.getContent());
            totalDeleted += deletedInThisPage;
            
            log.debug("페이지 {}번 처리 완료: {}개 삭제", pageNumber, deletedInThisPage);
            pageNumber++;
            
        } while (page.hasNext());
        
        if (totalDeleted == 0) {
            log.debug("아카이빙 대상 알림 없음: cutoffDate={}", cutoffDate);
        } else {
            log.info("알림 아카이빙 완료: 총 삭제된 알림 수={}, cutoffDate={}", totalDeleted, cutoffDate);
        }
    }

    /**
     * 배치 단위로 알림 삭제 (별도 트랜잭션)
     */
    @Transactional
    protected int deleteNotificationsInBatch(java.util.List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return 0;
        }
        
        int count = notifications.size();
        notificationRepository.deleteAll(notifications);
        return count;
    }
}

