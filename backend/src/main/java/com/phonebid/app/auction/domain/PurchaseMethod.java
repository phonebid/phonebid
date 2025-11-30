package com.phonebid.app.auction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseMethod {
    NUMBER_TRANSFER("번호이동", "기존 번호를 다른 통신사로 이동"),
    DEVICE_CHANGE("기기변경", "동일 통신사에서 단말기만 변경"),
    NEW_SUBSCRIPTION("신규가입", "새로운 번호로 가입"),
    LOWEST_PRICE("최저가", "기기 변경, 통신사 변경 상관 없이 최저가로 구매"),
    ANY("상관없음", "구매방법 무관");

    private final String displayName;
    private final String description;

    public boolean isNumberTransfer() {
        return this == NUMBER_TRANSFER;
    }

    public boolean isDeviceChange() {
        return this == DEVICE_CHANGE;
    }

    public boolean isNewSubscription() {
        return this == NEW_SUBSCRIPTION;
    }

    public boolean isLowestPrice() {
        return this == LOWEST_PRICE;
    }

    public boolean isAny() {
        return this == ANY;
    }

    // 통신사 정보가 필요한 구매방법인지 확인
    public boolean requiresCarrierInfo() {
        return this != ANY;
    }

    // 기존 통신사 정보가 필요한 구매방법인지 확인 (번호이동, 기기변경)
    public boolean requiresCurrentCarrier() {
        return this == NUMBER_TRANSFER || this == DEVICE_CHANGE;
    }

    // Bid에서 사용 가능한 구매방법인지 확인 (ANY 제외)
    public boolean isValidForBid() {
        return this != ANY;
    }
} 