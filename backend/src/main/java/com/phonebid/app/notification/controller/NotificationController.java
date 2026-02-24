package com.phonebid.app.notification.controller;

import com.phonebid.app.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.dto.response.NotificationResponseDto;
import com.phonebid.app.notification.dto.response.UnreadCountResponseDto;
import com.phonebid.app.notification.service.NotificationService;
import com.phonebid.app.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 알림 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     * 
     * @param userDetails 인증된 사용자 정보
     * @param pageable 페이징 정보
     * @return 알림 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponseDto>>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        UUID userId = userDetails.getUser().getId();
        Page<Notification> notifications = notificationService.getNotificationsByUserId(userId, pageable);
        
        Page<NotificationResponseDto> response = notifications.map(NotificationResponseDto::from);
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "알림 목록 조회 성공", response));
    }

    /**
     * 알림 읽음 처리
     * 
     * @param notificationId 알림 ID
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID notificationId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        UUID userId = userDetails.getUser().getId();
        notificationService.markAsRead(notificationId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "알림 읽음 처리 완료", null));
    }

    /**
     * 미읽음 알림 개수 조회
     * 
     * @param userDetails 인증된 사용자 정보
     * @return 미읽음 개수
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponseDto>> getUnreadCount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        UUID userId = userDetails.getUser().getId();
        long count = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "미읽음 개수 조회 성공", UnreadCountResponseDto.of(count)));
    }

    /**
     * 모든 알림 읽음 처리 (일괄 읽음)
     * 
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답 (읽음 처리된 개수 포함)
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        UUID userId = userDetails.getUser().getId();
        int count = notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, 
                String.format("%d개의 알림을 읽음 처리했습니다", count), count));
    }

    /**
     * 모든 알림 삭제 (일괄 삭제)
     * 
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답 (삭제된 개수 포함)
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Integer>> deleteAllNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        UUID userId = userDetails.getUser().getId();
        int count = notificationService.deleteAllNotifications(userId);
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, 
                String.format("%d개의 알림을 삭제했습니다", count), count));
    }
}

