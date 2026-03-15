package com.phonebid.app.notification.service;

import com.phonebid.app.member.domain.User;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.domain.UserNotificationSetting;
import com.phonebid.app.notification.repository.UserNotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 알림 설정 서비스
 * 알림 수신 동의 관리 및 기본값 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationSettingService {

    private final UserNotificationSettingRepository settingRepository;

    /**
     * 알림 수신 동의 여부 확인
     * 설정이 없는 경우 기본값 적용:
     * - SSE: 동의 (true)
     * - 그 외 채널: 비동의 (false)
     * 
     * @param user 사용자
     * @param type 알림 타입
     * @param channel 알림 채널
     * @return 동의 여부
     */
    @Transactional(readOnly = true)
    public boolean hasConsent(User user, NotificationType type, NotificationChannel channel) {
        Optional<UserNotificationSetting> settingOpt = settingRepository
                .findByUserIdAndTypeAndChannel(user.getId(), type, channel);

        if (settingOpt.isEmpty()) {
            // 설정이 없는 경우 기본값 적용
            return getDefaultConsent(channel);
        }

        UserNotificationSetting setting = settingOpt.get();
        return Boolean.TRUE.equals(setting.getIsAgreed());
    }

    /**
     * 채널별 기본 동의 여부
     * SSE는 기본 동의, 그 외는 기본 비동의
     */
    private boolean getDefaultConsent(NotificationChannel channel) {
        return channel == NotificationChannel.SSE;
    }

    /**
     * 알림 설정 생성 또는 업데이트
     * 
     * @param user 사용자
     * @param type 알림 타입
     * @param channel 알림 채널
     * @param isAgreed 동의 여부
     * @param isMarketing 마케팅 알림 여부
     */
    @Transactional
    public UserNotificationSetting saveOrUpdateSetting(User user,
                                                       NotificationType type,
                                                       NotificationChannel channel,
                                                       Boolean isAgreed,
                                                       Boolean isMarketing) {
        
        Optional<UserNotificationSetting> existingOpt = settingRepository
                .findByUserIdAndTypeAndChannel(user.getId(), type, channel);

        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            // 기존 설정 업데이트
            UserNotificationSetting existing = existingOpt.get();
            
            // 마케팅 알림 동의 변경 시 로그 기록
            boolean isMarketingValue = Boolean.TRUE.equals(isMarketing);
            if (isMarketingValue || Boolean.TRUE.equals(existing.getIsMarketing())) {
                boolean previousAgreement = Boolean.TRUE.equals(existing.getIsAgreed());
                boolean newAgreement = Boolean.TRUE.equals(isAgreed);
                
                if (previousAgreement != newAgreement) {
                    log.info("마케팅 알림 동의 변경: userId={}, type={}, channel={}, " +
                            "previous={}, new={}, timestamp={}",
                            user.getId(), type, channel, previousAgreement, newAgreement, now);
                }
            }
            
            existing.updateAgreement(isAgreed, now);
            // isMarketing은 생성 시에만 설정되며 이후 변경 불가 (데이터 정합성 유지)
            
            return settingRepository.save(existing);
        } else {
            // 새 설정 생성
            UserNotificationSetting newSetting = UserNotificationSetting.builder()
                    .user(user)
                    .notificationType(type)
                    .notificationChannel(channel)
                    .isAgreed(isAgreed != null ? isAgreed : false)
                    .isMarketing(isMarketing != null ? isMarketing : false)
                    .agreedAt(Boolean.TRUE.equals(isAgreed) ? now : null)
                    .agreedAtMarketing(
                            Boolean.TRUE.equals(isMarketing) && Boolean.TRUE.equals(isAgreed) 
                            ? now : null)
                    .build();
            
            if (Boolean.TRUE.equals(isMarketing) && Boolean.TRUE.equals(isAgreed)) {
                log.info("마케팅 알림 동의 신규 등록: userId={}, type={}, channel={}, timestamp={}",
                        user.getId(), type, channel, now);
            }
            
            return settingRepository.save(newSetting);
        }
    }

    /**
     * 사용자의 특정 채널에 대한 동의 여부 확인 (타입 무관)
     * 
     * @param user 사용자
     * @param channel 알림 채널
     * @return 동의 여부 (하나라도 동의한 타입이 있으면 true)
     */
    @Transactional(readOnly = true)
    public boolean hasConsentForChannel(User user, NotificationChannel channel) {
        List<UserNotificationSetting> settings = settingRepository
                .findByUserIdAndChannel(user.getId(), channel);
        
        // 설정이 하나라도 있고 동의한 경우가 있으면 true
        if (!settings.isEmpty()) {
            return settings.stream()
                    .anyMatch(s -> Boolean.TRUE.equals(s.getIsAgreed()));
        }
        
        // 설정이 없으면 기본값
        return getDefaultConsent(channel);
    }

    /**
     * 사용자의 모든 알림 설정 조회
     */
    @Transactional(readOnly = true)
    public List<UserNotificationSetting> getUserSettings(UUID userId) {
        return settingRepository.findByUserId(userId);
    }

    /**
     * 마케팅 알림 동의 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasMarketingConsent(User user, NotificationType type, NotificationChannel channel) {
        Optional<UserNotificationSetting> settingOpt = settingRepository
                .findByUserIdAndTypeAndChannel(user.getId(), type, channel);
        
        if (settingOpt.isEmpty()) {
            return false; // 마케팅 알림은 기본 비동의
        }
        
        UserNotificationSetting setting = settingOpt.get();
        return setting.isMarketingAgreed();
    }
}

