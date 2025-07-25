package com.phonebid.app.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    QUOTE_CREATED("견적 등록", "새로운 견적이 등록되었습니다"),
    BID_ARRIVED("입찰 도착", "새로운 입찰이 도착했습니다"),
    BID_SELECTED("입찰 선택", "귀하의 입찰이 선택되었습니다"),
    CONTRACT_SIGNED("계약 체결", "계약이 체결되었습니다"),
    PAYMENT_COMPLETED("결제 완료", "결제가 완료되었습니다"),
    DELIVERY_STARTED("배송 시작", "상품이 발송되었습니다"),
    DELIVERY_COMPLETED("배송 완료", "배송이 완료되었습니다"),
    SELLER_APPROVED("판매자 승인", "판매자 등록이 승인되었습니다"),
    SELLER_REJECTED("판매자 거부", "판매자 등록이 거부되었습니다");

    private final String displayName;
    private final String defaultMessage;

    // 알림 유형별 분류 메서드
    public boolean isQuoteRelated() {
        return this == QUOTE_CREATED;
    }

    public boolean isBidRelated() {
        return this == BID_ARRIVED || this == BID_SELECTED;
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
        return this == SELLER_APPROVED || this == SELLER_REJECTED;
    }

    // 수신 대상별 분류
    public boolean isForConsumer() {
        return this == BID_ARRIVED || this == DELIVERY_STARTED || this == DELIVERY_COMPLETED;
    }

    public boolean isForSeller() {
        return this == QUOTE_CREATED || this == BID_SELECTED || this == CONTRACT_SIGNED || 
               this == PAYMENT_COMPLETED || this == SELLER_APPROVED || this == SELLER_REJECTED;
    }

    // 우선순위 (높을수록 중요)
    public int getPriority() {
        return switch (this) {
            case CONTRACT_SIGNED, PAYMENT_COMPLETED -> 5; // 매우 중요
            case BID_SELECTED, SELLER_APPROVED, SELLER_REJECTED -> 4; // 중요
            case BID_ARRIVED, DELIVERY_STARTED, DELIVERY_COMPLETED -> 3; // 보통
            case QUOTE_CREATED -> 2; // 낮음
        };
    }
} 