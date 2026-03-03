package com.phonebid.app.notification.sender;

import com.phonebid.app.common.errorcode.NotificationErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.notification.client.AligoKakaoClient;
import com.phonebid.app.notification.config.AligoProperties;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.dto.aligo.AligoKakaoRequest;
import com.phonebid.app.notification.dto.aligo.AligoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 카카오 알림톡 발송 전략 구현 (알리고 연동)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTalkNotificationSender implements NotificationSender {

    private final AligoKakaoClient aligoClient;
    private final AligoProperties aligoProperties;
    
    @Override
    public boolean send(Notification notification) {
        try {
            // 1. 사용자 전화번호 조회
            String phoneNumber = notification.getUser().getPhone();
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                log.warn("전화번호 없음, 알림톡 발송 스킵: userId={}, notificationId={}", 
                        notification.getUser().getId(), notification.getId());
                return false;
            }
            
            // 2. 템플릿 코드 매핑
            String templateCode = getTemplateCode(notification.getType());
            if (templateCode == null || templateCode.isEmpty()) {
                log.warn("템플릿 코드 없음, 알림톡 발송 스킵: type={}, notificationId={}", 
                        notification.getType(), notification.getId());
                return false;
            }
            
            // 3. 메시지 내용 구성
            String message = buildMessage(notification);
            
            // 4. 버튼 URL 생성 (옵션)
            String buttonUrl = buildButtonUrl(notification);
            
            // 5. API 요청 객체 생성
            AligoKakaoRequest request = AligoKakaoRequest.builder()
                    .receiver(normalizePhoneNumber(phoneNumber))
                    .templateCode(templateCode)
                    .message(message)
                    .buttonUrl(buttonUrl)
                    .buttonName("자세히보기")
                    .build();
            
            // 6. API 호출 (동기 방식, 10초 타임아웃)
            AligoResponse response = aligoClient.sendKakaoNotification(request)
                    .block(Duration.ofSeconds(10));
            
            if (response != null && response.isSuccess()) {
                log.info("알림톡 발송 성공: userId={}, notificationId={}, type={}", 
                        notification.getUser().getId(), notification.getId(), notification.getType());
                return true;
            } else {
                log.error("알림톡 발송 실패: userId={}, notificationId={}, response={}", 
                        notification.getUser().getId(), notification.getId(), response);
                return false;
            }
            
        } catch (Exception e) {
            log.error("알림톡 발송 중 예외 발생: userId={}, notificationId={}, error={}", 
                     notification.getUser().getId(), notification.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.KAKAO;
    }
    
    /**
     * 알림 타입별 템플릿 코드 매핑
     */
    private String getTemplateCode(NotificationType type) {
        return switch (type) {
            case BID_ARRIVED -> aligoProperties.getTemplate().getBidArrived();
            case BID_SELECTED -> aligoProperties.getTemplate().getBidSelected();
            case CONTRACT_SIGNED -> aligoProperties.getTemplate().getContractSigned();
            case PAYMENT_COMPLETED -> aligoProperties.getTemplate().getPaymentCompleted();
            case DELIVERY_STARTED -> aligoProperties.getTemplate().getDeliveryStarted();
            case DELIVERY_COMPLETED -> aligoProperties.getTemplate().getDeliveryCompleted();
            default -> null;
        };
    }
    
    /**
     * 알림 메시지 구성
     * 템플릿에 맞게 메시지 포맷팅
     */
    private String buildMessage(Notification notification) {
        // 알리고 템플릿의 변수는 #{변수명} 형식
        // 실제 템플릿 승인 시 정의한 변수명에 맞게 치환 필요
        
        // 현재는 Notification의 message를 그대로 사용
        // 실제 템플릿 변수 치환이 필요한 경우 여기서 처리
        return notification.getMessage();
    }
    
    /**
     * 알림 타입별 버튼 URL 생성
     */
    private String buildButtonUrl(Notification notification) {
        if (notification.getReferenceId() == null) {
            return "https://phonebid.com/notifications";
        }
        
        // 프론트엔드 도메인 (실제 도메인으로 변경 필요)
        String baseUrl = "https://phonebid.com";
        
        // 알림 타입별 URL 생성
        return switch (notification.getType()) {
            case BID_ARRIVED, QUOTE_EXPIRING_SOON -> 
                String.format("%s/quotes/%s", baseUrl, notification.getReferenceId());
            case BID_SELECTED, CONTRACT_SIGNED -> 
                String.format("%s/contracts/%s", baseUrl, notification.getReferenceId());
            case PAYMENT_COMPLETED -> 
                String.format("%s/orders/%s", baseUrl, notification.getReferenceId());
            case DELIVERY_STARTED, DELIVERY_COMPLETED -> 
                String.format("%s/deliveries/%s", baseUrl, notification.getReferenceId());
            default -> baseUrl + "/notifications";
        };
    }
    
    /**
     * 전화번호 정규화 (하이픈 제거)
     * 010-1234-5678 -> 01012345678
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null) {
            throw new CustomException(NotificationErrorCode.KAKAO_ALIMTALK_INVALID_PHONE);
        }
        
        String normalized = phone.replaceAll("[^0-9]", "");
        
        // 전화번호 유효성 검증 (010으로 시작하는 11자리)
        if (!normalized.matches("^01[0-9]{8,9}$")) {
            log.warn("유효하지 않은 전화번호 형식: {}", phone);
            throw new CustomException(NotificationErrorCode.KAKAO_ALIMTALK_INVALID_PHONE);
        }
        
        return normalized;
    }
}

