package com.phonebid.app.trade.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    REQUESTED("결제 요청", "결제가 요청된 상태"),
    PENDING_APPROVAL("승인 대기", "PG사 승인 대기 중인 상태"),
    PAID("결제 완료", "결제가 완료된 상태"),
    FAILED("결제 실패", "결제가 실패한 상태");

    private final String displayName;
    private final String description;

    public boolean isRequested() {
        return this == REQUESTED;
    }

    public boolean isPendingApproval() {
        return this == PENDING_APPROVAL;
    }

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isFailed() {
        return this == FAILED;
    }



    public boolean canComplete() {
        return this == PENDING_APPROVAL;
    }

    public boolean canFail() {
        return this == REQUESTED || this == PENDING_APPROVAL;
    }

    public boolean isCompleted() {
        return this == PAID;
    }

    public boolean isProcessing() {
        return this == REQUESTED || this == PENDING_APPROVAL;
    }
}