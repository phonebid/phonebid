package com.phonebid.app.notification.controller;

import com.phonebid.app.notification.dto.response.NotificationDisplayItem;
import com.phonebid.app.notification.sender.SseNotificationSender;
import com.phonebid.app.notification.service.NotificationService;
import com.phonebid.app.notification.sse.SseEmitterManager;
import com.phonebid.app.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

/**
 * SSE 알림 스트림 컨트롤러
 * 실시간 알림을 SSE를 통해 전송
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationSseController {

    private final SseEmitterManager sseEmitterManager;
    private final NotificationService notificationService;
    private final SseNotificationSender sseNotificationSender;

    /**
     * SSE 연결 엔드포인트
     * 연결 성공 시 최근 미읽음 알림을 일괄 전송
     * 
     * @param userDetails 인증된 사용자 정보
     * @return SseEmitter
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        UUID userId = userDetails.getUser().getId();
        
        // SSE 연결 생성
        SseEmitter emitter = sseEmitterManager.createConnection(userId);
        
        // Nginx 버퍼링 방지 헤더 설정
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive");

        // 초기 알림 전송 (최근 24시간 내 미읽음 알림, 최대 50개, 그룹화 적용)
        try {
            List<NotificationDisplayItem> recentNotifications = notificationService.getRecentUnreadNotifications(userId, 50);
            if (!recentNotifications.isEmpty()) {
                sseNotificationSender.sendInitialNotifications(userId, recentNotifications);
            }
        } catch (Exception e) {
            log.error("SSE 초기 알림 전송 실패: userId={}", userId, e);
        }

        // Heartbeat는 SseEmitterManager의 @Scheduled 메소드에서 전역적으로 처리됨

        return responseBuilder.body(emitter);
    }
}

