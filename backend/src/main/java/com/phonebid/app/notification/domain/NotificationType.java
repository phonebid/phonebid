package com.phonebid.app.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    QUOTE_CREATED("견적 등록", "새로운 견적이 등록되었습니다"),
    QUOTE_EXPIRING_SOON("견적 마감 임박", "견적이 곧 마감됩니다"),
    BID_ARRIVED("입찰 도착", "새로운 입찰이 도착했습니다"),
    BID_SELECTED("입찰 선택", "귀하의 입찰이 선택되었습니다"),
    LOWEST_PRICE_UPDATED("최저가 갱신", "더 낮은 가격의 입찰이 도착했습니다"),
    CONTRACT_SIGNED("계약 체결", "계약이 체결되었습니다"),
    PAYMENT_COMPLETED("결제 완료", "결제가 완료되었습니다"),
    DELIVERY_STARTED("배송 시작", "상품이 발송되었습니다"),
    DELIVERY_COMPLETED("배송 완료", "배송이 완료되었습니다"),
    CHAT_MESSAGE_RECEIVED("채팅 수신", "새로운 채팅 메시지가 도착했습니다"),
    SELLER_APPROVAL_REQUESTED("판매자 승인 요청", "판매자 등록 승인이 요청되었습니다"),
    SELLER_APPROVED("판매자 승인", "판매자 등록이 승인되었습니다"),
    SELLER_REJECTED("판매자 거부", "판매자 등록이 거부되었습니다"),
    REPORT_RECEIVED("신고 접수", "신고가 접수되었습니다"),
    SYSTEM_ANOMALY("시스템 이상", "시스템 이상 징후가 감지되었습니다"),
    STATISTICS_SUMMARY("통계 요약", "일일 통계 요약이 생성되었습니다");

    private final String displayName;
    private final String defaultMessage;

    // 알림 유형별 분류 메서드
    public boolean isQuoteRelated() {
        return this == QUOTE_CREATED || this == QUOTE_EXPIRING_SOON;
    }

    public boolean isBidRelated() {
        return this == BID_ARRIVED || this == BID_SELECTED || this == LOWEST_PRICE_UPDATED;
    }

    public boolean isContractRelated() {
        return this == CONTRACT_SIGNED;
    }

    public boolean isPaymentRelated() {
        return this == PAYMENT_COMPLETED;
    }

    public boolean isDeliveryRelated() {
        return this == DELIVERY_STARTED || this == DELIVERY_COMPLETED;
    }

    public boolean isSellerRelated() {
        return this == SELLER_APPROVAL_REQUESTED || this == SELLER_APPROVED || this == SELLER_REJECTED;
    }

    public boolean isAdminRelated() {
        return this == SELLER_APPROVAL_REQUESTED || this == REPORT_RECEIVED || 
               this == SYSTEM_ANOMALY || this == STATISTICS_SUMMARY;
    }

    public boolean isChatRelated() {
        return this == CHAT_MESSAGE_RECEIVED;
    }

    // 수신 대상별 분류
    public boolean isForConsumer() {
        return this == BID_ARRIVED || this == QUOTE_EXPIRING_SOON || 
               this == LOWEST_PRICE_UPDATED ||
               this == CONTRACT_SIGNED || this == PAYMENT_COMPLETED ||
               this == DELIVERY_STARTED || this == DELIVERY_COMPLETED || 
               this == CHAT_MESSAGE_RECEIVED;
    }

    public boolean isForSeller() {
        return this == QUOTE_CREATED || this == BID_SELECTED || this == LOWEST_PRICE_UPDATED ||
               this == CONTRACT_SIGNED || this == PAYMENT_COMPLETED || 
               this == SELLER_APPROVED || this == SELLER_REJECTED ||
               this == CHAT_MESSAGE_RECEIVED;
    }

    public boolean isForAdmin() {
        return this == SELLER_APPROVAL_REQUESTED || this == REPORT_RECEIVED || 
               this == SYSTEM_ANOMALY || this == STATISTICS_SUMMARY;
    }

    // 우선순위 (높을수록 중요)
    public int getPriority() {
        return switch (this) {
            case CONTRACT_SIGNED, PAYMENT_COMPLETED, SYSTEM_ANOMALY -> 5; // 매우 중요
            case BID_SELECTED, SELLER_APPROVED, SELLER_REJECTED, REPORT_RECEIVED -> 4; // 중요
            case BID_ARRIVED, LOWEST_PRICE_UPDATED, DELIVERY_STARTED, DELIVERY_COMPLETED, 
                 CHAT_MESSAGE_RECEIVED, QUOTE_EXPIRING_SOON -> 3; // 보통
            case QUOTE_CREATED, SELLER_APPROVAL_REQUESTED, STATISTICS_SUMMARY -> 2; // 낮음
        };
    }

    /**
     * 그룹화 시 N건 요약 메시지 포맷
     * 그룹화 가능한 타입에만 정의, 그 외는 null
     */
    public String getGroupedMessageFormat(int count) {
        return switch (this) {
            case BID_ARRIVED -> "입찰 %d건이 도착했습니다";
            case QUOTE_CREATED -> "견적 %d건이 등록되었습니다";
            case LOWEST_PRICE_UPDATED -> "최저가 %d건이 갱신되었습니다";
            case CHAT_MESSAGE_RECEIVED -> "채팅 메시지 %d건이 도착했습니다";
            default -> null;
        };
    }
} 