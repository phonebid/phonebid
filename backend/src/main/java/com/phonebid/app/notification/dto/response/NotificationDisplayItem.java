package com.phonebid.app.notification.dto.response;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 알림 표시용 데이터 (단일/그룹화 통합)
 * 그룹화 시 요약 메시지로 변환된 형태를 담는다.
 */
public record NotificationDisplayItem(
        UUID id,
        NotificationType type,
        NotificationChannel channel,
        String title,
        String message,
        Boolean isRead,
        UUID referenceId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationDisplayItem from(Notification notification) {
        return new NotificationDisplayItem(
                notification.getId(),
                notification.getType(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getReferenceId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    public static NotificationDisplayItem fromGrouped(Notification representative, int count, String groupedMessage) {
        return new NotificationDisplayItem(
                representative.getId(),
                representative.getType(),
                representative.getChannel(),
                representative.getTitle(),
                groupedMessage,
                representative.getIsRead(),
                representative.getReferenceId(),
                representative.getCreatedAt(),
                representative.getUpdatedAt()
        );
    }
}
