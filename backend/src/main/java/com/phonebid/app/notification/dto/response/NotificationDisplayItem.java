package com.phonebid.app.notification.dto.response;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;
/**
 * 알림 표시용 DTO (단일/그룹화 통합)
 */
public record NotificationDisplayItem(
        UUID id,
        NotificationType type,
        NotificationChannel channel,
        String title,
        String message,  // 이 필드만 그룹화 시 커스터마이징 가능
        Boolean isRead,
        UUID referenceId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 일반 알림 → DisplayItem 변환 (메시지 그대로)
     */
    public static NotificationDisplayItem from(Notification notification) {
        return new NotificationDisplayItem(
                notification.getId(),
                notification.getType(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getMessage(),  // 원본 메시지 그대로
                notification.getIsRead(),
                notification.getReferenceId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    /**
     * 그룹화된 알림 → DisplayItem 변환 (메시지 커스터마이징)
     * 
     * @param representative 그룹의 대표 알림 (가장 최신)
     * @param count 그룹에 포함된 알림 개수
     * @param groupedMessage "입찰 3건이 도착했습니다" 같은 요약 메시지
     */
    public static NotificationDisplayItem fromGrouped(Notification representative, int count, String groupedMessage) {
        return new NotificationDisplayItem(
                representative.getId(),  // 대표 알림의 ID 사용 (읽음 처리용)
                representative.getType(),
                representative.getChannel(),
                representative.getTitle(),
                groupedMessage,  // 그룹화 메시지로 교체
                representative.getIsRead(),
                representative.getReferenceId(),
                representative.getCreatedAt(),
                representative.getUpdatedAt()
        );
    }
}
