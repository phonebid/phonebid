package com.phonebid.app.notification.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.dto.response.NotificationResponseDto;
import com.phonebid.app.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

/**
 * SSE 알림 발송 전략 구현
 * 실시간 웹 알림을 SSE 스트림을 통해 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationSender implements NotificationSender {

    private final SseEmitterManager sseEmitterManager;
    private final ObjectMapper objectMapper;

    @Override
    public boolean send(Notification notification) {
        UUID userId = notification.getUser().getId();
        SseEmitter emitter = sseEmitterManager.getConnection(userId);

        if (emitter == null) {
            log.debug("SSE 연결이 없어 알림 발송 실패: userId={}, notificationId={}", 
                     userId, notification.getId());
            return false;
        }

        try {
            NotificationResponseDto dto = NotificationResponseDto.from(notification);
            String jsonData = objectMapper.writeValueAsString(dto);

            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("notification")
                    .data(jsonData);

            emitter.send(event);
            log.debug("SSE 알림 발송 성공: userId={}, notificationId={}", 
                     userId, notification.getId());
            return true;

        } catch (IOException e) {
            log.error("SSE 알림 발송 실패: userId={}, notificationId={}, error={}", 
                     userId, notification.getId(), e.getMessage(), e);
            // 연결이 끊어진 경우 저장소에서 제거
            sseEmitterManager.removeConnection(userId);
            return false;
        } catch (Exception e) {
            log.error("SSE 알림 발송 중 예상치 못한 에러: userId={}, notificationId={}", 
                     userId, notification.getId(), e);
            return false;
        }
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SSE;
    }

    /**
     * 초기 알림 일괄 전송 (SSE 연결 시 사용)
     * 
     * @param userId 사용자 ID
     * @param notifications 전송할 알림 목록
     * @return 전송 성공 여부
     */
    public boolean sendInitialNotifications(UUID userId, java.util.List<Notification> notifications) {
        SseEmitter emitter = sseEmitterManager.getConnection(userId);

        if (emitter == null) {
            log.debug("SSE 연결이 없어 초기 알림 전송 실패: userId={}", userId);
            return false;
        }

        try {
            java.util.List<NotificationResponseDto> dtos = notifications.stream()
                    .map(NotificationResponseDto::from)
                    .toList();

            String jsonData = objectMapper.writeValueAsString(dtos);

            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("initial-notifications")
                    .data(jsonData);

            emitter.send(event);
            log.info("SSE 초기 알림 전송 성공: userId={}, count={}", userId, notifications.size());
            return true;

        } catch (IOException e) {
            log.error("SSE 초기 알림 전송 실패: userId={}, error={}", userId, e.getMessage(), e);
            sseEmitterManager.removeConnection(userId);
            return false;
        } catch (Exception e) {
            log.error("SSE 초기 알림 전송 중 예상치 못한 에러: userId={}", userId, e);
            return false;
        }
    }
}

