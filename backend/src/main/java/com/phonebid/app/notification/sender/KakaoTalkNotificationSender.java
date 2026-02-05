package com.phonebid.app.notification.sender;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 카카오 알림톡 발송 전략 구현 (스텁)
 * 실제 카카오 알림톡 API 연동은 추후 구현 예정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTalkNotificationSender implements NotificationSender {

    @Override
    public boolean send(Notification notification) {
        // TODO: 카카오 알림톡 API 연동 구현
        // 현재는 스텁으로 로그만 기록
        
        log.info("카카오 알림톡 발송 (스텁): userId={}, notificationId={}, title={}, message={}", 
                notification.getUser().getId(), 
                notification.getId(),
                notification.getTitle(),
                notification.getMessage());
        
        // 스텁이므로 항상 성공으로 처리
        return true;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.KAKAO;
    }
}

