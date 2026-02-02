package com.phonebid.app.notification.dto.response;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponseDto {
    private UUID id;
    private NotificationType type;
    private NotificationChannel channel;
    private String title;
    private String message;
    private Boolean isRead;
    private UUID referenceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}

