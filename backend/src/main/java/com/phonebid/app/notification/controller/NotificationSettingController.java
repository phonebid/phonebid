package com.phonebid.app.notification.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.notification.domain.UserNotificationSetting;
import com.phonebid.app.notification.dto.request.NotificationSettingRequestDto;
import com.phonebid.app.notification.dto.response.NotificationSettingResponseDto;
import com.phonebid.app.notification.service.UserNotificationSettingService;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 설정 컨트롤러
 * 사용자 알림 수신 동의 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final UserNotificationSettingService settingService;

    /**
     * 마케팅 여부 기본값 처리 헬퍼 메서드
     */
    private boolean getIsMarketingOrDefault(NotificationSettingRequestDto request) {
        return request.getIsMarketing() != null ? request.getIsMarketing() : false;
    }

    /**
     * 알림 설정 조회 (사용자별 전체)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationSettingResponseDto>>> getSettings(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<UserNotificationSetting> settings = settingService.getUserSettings(userDetails.getUser().getId());
        
        List<NotificationSettingResponseDto> response = settings.stream()
                .map(NotificationSettingResponseDto::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "알림 설정 조회 성공", response));
    }

    /**
     * 알림 설정 생성 또는 업데이트
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationSettingResponseDto>> updateSetting(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                     @Valid @RequestBody NotificationSettingRequestDto request) {
        
        UserNotificationSetting setting = settingService.saveOrUpdateSetting(
                userDetails.getUser(),
                request.getNotificationType(),
                request.getNotificationChannel(),
                request.getIsAgreed(),
                getIsMarketingOrDefault(request)
        );
        
        NotificationSettingResponseDto response = NotificationSettingResponseDto.from(setting);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "알림 설정 저장 성공", response));
    }

    /**
     * 알림 설정 일괄 업데이트
     */
    @PutMapping("/batch")
    public ResponseEntity<ApiResponse<List<NotificationSettingResponseDto>>> updateSettingsBatch(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                                 @Valid @RequestBody List<NotificationSettingRequestDto> requests) {
        
        List<NotificationSettingResponseDto> responses = requests.stream()
                .map(request -> {
                    UserNotificationSetting setting = settingService.saveOrUpdateSetting(
                            userDetails.getUser(),
                            request.getNotificationType(),
                            request.getNotificationChannel(),
                            request.getIsAgreed(),
                            getIsMarketingOrDefault(request)
                    );
                    return NotificationSettingResponseDto.from(setting);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "알림 설정 일괄 업데이트 성공", responses));
    }
}

