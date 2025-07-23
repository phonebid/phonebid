package com.phonebid.app.seller.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {
    PENDING("승인 대기", "판매자 가입 승인을 대기 중인 상태"),
    APPROVED("승인 완료", "판매자 가입이 승인되어 활동 가능한 상태"),
    REJECTED("승인 거부", "판매자 가입이 거부된 상태");

    private final String displayName;
    private final String description;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean canSell() {
        return this == APPROVED;
    }
} 