package com.phonebid.app.notification.service;

import com.phonebid.app.common.errorcode.NotificationErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.dto.response.NotificationDisplayItem;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.factory.NotificationFactory;
import com.phonebid.app.notification.repository.NotificationRepository;
import com.phonebid.app.notification.retry.RetryableNotificationSender;
import com.phonebid.app.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 알림 서비스
 * 알림 생성, 저장, 발송을 담당하는 핵심 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final List<NotificationSender> notificationSenders;
    private final RetryableNotificationSender retryableNotificationSender;
    private final UserNotificationSettingService userNotificationSettingService;
    private final NotificationGroupingService notificationGroupingService;

    /**
     * 알림 생성 및 발송
     * 
     * @param user 알림 수신자
     * @param type 알림 타입
     * @param channels 발송 채널 목록
     * @param referenceId 관련 엔터티 ID
     */
    @Transactional
    public void createAndSendNotification(User user, NotificationType type, 
                                         List<NotificationChannel> channels, UUID referenceId) {
        for (NotificationChannel channel : channels) {
            // 알림 수신 동의 확인 (UserNotificationSettingService 사용)
            if (!userNotificationSettingService.hasConsent(user, type, channel)) {
                log.debug("알림 수신 동의 없음, 채널 스킵: userId={}, type={}, channel={}", 
                         user.getId(), type, channel);
                continue;
            }

            Notification notification = notificationFactory.createNotification(
                    user, type, channel, referenceId);

            Notification savedNotification = notificationRepository.save(notification);
            log.debug("알림 생성 완료: notificationId={}, userId={}, type={}, channel={}", 
                     savedNotification.getId(), user.getId(), type, channel);

            // 알림 발송 (트랜잭션 커밋 후 비동기로 실행됨)
            sendNotification(savedNotification);
        }
    }

    /**
     * 커스텀 메시지로 알림 생성 및 발송
     */
    @Transactional
    public void createAndSendNotification(User user, NotificationType type,
                                         List<NotificationChannel> channels,
                                         String title, String message, UUID referenceId) {
        for (NotificationChannel channel : channels) {
            // 알림 수신 동의 확인 (UserNotificationSettingService 사용)
            if (!userNotificationSettingService.hasConsent(user, type, channel)) {
                log.debug("알림 수신 동의 없음, 채널 스킵: userId={}, type={}, channel={}", 
                         user.getId(), type, channel);
                continue;
            }

            Notification notification = notificationFactory.createNotification(
                    user, type, channel, title, message, referenceId);

            Notification savedNotification = notificationRepository.save(notification);
            log.debug("알림 생성 완료 (커스텀): notificationId={}, userId={}, type={}, channel={}", 
                     savedNotification.getId(), user.getId(), type, channel);

            // 알림 발송 (트랜잭션 커밋 후 비동기로 실행됨)
            sendNotification(savedNotification);
        }
    }

    /**
     * 알림 발송 (이벤트 리스너에서 호출)
     */
    public void sendNotification(Notification notification) {
        NotificationSender sender = findSender(notification.getChannel());
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
        }
    }

    /**
     * 최근 미읽음 알림 조회 (SSE 초기 전송용)
     * 5분 이내 동일 타입 알림은 그룹화하여 요약 형태로 반환
     * 
     * @param userId 사용자 ID
     * @param limit 최대 조회 개수
     * @return 그룹화 적용된 최근 미읽음 알림 목록
     */
    @Transactional(readOnly = true)
    public List<NotificationDisplayItem> getRecentUnreadNotifications(UUID userId, int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24); // 최근 24시간
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50)); // 최대 50개

        // DB에서 원본 Notification 엔티티 조회
        List<Notification> notifications = notificationRepository.findRecentUnreadByUserId(
                userId, since, pageable);

        // 그룹화 서비스를 통해 DisplayItem으로 변환 (메시지 커스터마이징)
        List<NotificationDisplayItem> grouped = new ArrayList<>(
                notificationGroupingService.groupNotifications(userId, notifications));
        log.debug("최근 미읽음 알림 조회: userId={}, count={}", userId, grouped.size());
        return grouped;
    }

    /**
     * 사용자별 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByUserId(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
        notificationRepository.save(notification);
        log.debug("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);
    }

    /**
     * 미읽음 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 모든 알림 읽음 처리 (일괄 읽음)
     * 
     * @param userId 사용자 ID
     * @return 읽음 처리된 알림 개수
     */
    @Transactional
    public int markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        
        notificationRepository.saveAll(unreadNotifications);
        
        int count = unreadNotifications.size();
        return count;
    }

    /**
     * 모든 알림 삭제 (일괄 삭제 - 소프트 삭제)
     * 
     * @param userId 사용자 ID
     * @return 삭제된 알림 개수
     */
    @Transactional
    public int deleteAllNotifications(UUID userId) {
        List<Notification> notifications = notificationRepository.findAllByUserId(userId);
        
        // 소프트 삭제 (BaseEntity의 @SQLDelete 어노테이션에 의해 is_delete = true로 업데이트)
        notificationRepository.deleteAll(notifications);
        
        int count = notifications.size();
        return count;
    }

    /**
     * 채널별 발송자 찾기
     */
    private NotificationSender findSender(NotificationChannel channel) {
        return notificationSenders.stream()
                .filter(sender -> sender.supports(channel))
                .findFirst()
                .orElse(null);
    }
}

