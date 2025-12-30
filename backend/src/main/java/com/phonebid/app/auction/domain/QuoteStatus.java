package com.phonebid.app.auction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuoteStatus {
    OPEN("진행중", "입찰 접수 중인 견적"),
    CLOSED("마감", "입찰 마감된 견적"),
    CONTRACTED("계약완료", "입찰 선택하여 계약이 체결된 견적");

    private final String displayName;
    private final String description;

    public boolean isOpen() {
        return this == OPEN;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isContracted() {
        return this == CONTRACTED;
    }

    public boolean canReceiveBids() {
        return this == OPEN;
    }

    public boolean canSelectBid() {
        return this == OPEN;
    }

    public boolean canClose() {
        return this == OPEN;
    }
}
