package com.phonebid.app.auction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BidStatus {
    ACTIVE("활성", "입찰 진행 중인 상태"),
    SELECTED("선택됨", "소비자가 선택한 입찰"),
    CANCELLED("취소됨", "판매자가 취소한 입찰");

    private final String displayName;
    private final String description;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isSelected() {
        return this == SELECTED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean canModify() {
        return this == ACTIVE;
    }
} 