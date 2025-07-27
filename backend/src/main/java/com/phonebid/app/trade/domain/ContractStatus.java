package com.phonebid.app.trade.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractStatus {
    SIGNING("서명 대기", "전자계약서 서명 진행 중인 상태"),
    SIGNED("서명 완료", "계약이 체결된 상태"),
    CANCELLED("계약 취소", "계약이 취소된 상태");

    private final String displayName;
    private final String description;

    public boolean isSigning() {
        return this == SIGNING;
    }

    public boolean isSigned() {
        return this == SIGNED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean canSign() {
        return this == SIGNING;
    }

    public boolean canCancel() {
        return this == SIGNING;
    }
}