package com.phonebid.app.notification.dto.response;

import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.domain.UserNotificationSetting;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 알림 설정 응답 DTO
 */
@Getter
@NoArgsConstructor
public class NotificationSettingResponseDto {

    private UUID id;
    private NotificationType notificationType;
    private NotificationChannel notificationChannel;
    private Boolean isAgreed;
    private Boolean isMarketing;
    private LocalDateTime agreedAt;
    private LocalDateTime agreedAtMarketing;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationSettingResponseDto from(UserNotificationSetting setting) {
        NotificationSettingResponseDto dto = new NotificationSettingResponseDto();
        dto.id = setting.getId();
        dto.notificationType = setting.getNotificationType();
        dto.notificationChannel = setting.getNotificationChannel();
        dto.isAgreed = setting.getIsAgreed();
        dto.isMarketing = setting.getIsMarketing();
        dto.agreedAt = setting.getAgreedAt();
        dto.agreedAtMarketing = setting.getAgreedAtMarketing();
        dto.createdAt = setting.getCreatedAt();
        dto.updatedAt = setting.getUpdatedAt();
        return dto;
    }
}

