package com.phonebid.app.notification.dto.request;

import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 설정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class NotificationSettingRequestDto {

    @NotNull(message = "알림 타입은 필수입니다")
    private NotificationType notificationType;

    @NotNull(message = "알림 채널은 필수입니다")
    private NotificationChannel notificationChannel;

    @NotNull(message = "동의 여부는 필수입니다")
    private Boolean isAgreed;

    private Boolean isMarketing; // 마케팅 알림 여부 (기본값: false)
}

