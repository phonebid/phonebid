package com.phonebid.app.trade.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {
    READY("배송 준비", "배송 준비 중인 상태"),
    SHIPPED("배송 중", "상품이 발송된 상태"),
    DELIVERED("배송 완료", "상품이 배송 완료된 상태");

    private final String displayName;
    private final String description;

    public boolean isReady() {
        return this == READY;
    }

    public boolean isShipped() {
        return this == SHIPPED;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean canShip() {
        return this == READY;
    }

    public boolean canDeliver() {
        return this == SHIPPED;
    }

    public boolean isCompleted() {
        return this == DELIVERED;
    }

    public boolean isInProgress() {
        return this == READY || this == SHIPPED;
    }
} 