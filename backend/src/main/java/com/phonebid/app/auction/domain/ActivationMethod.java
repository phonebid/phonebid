package com.phonebid.app.auction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivationMethod {
    SELECTIVE_SUBSIDY("선택약정", "약정 기간에 따른 할인 혜택"),
    COMMON_SUBSIDY("공시지원금", "통신사 공통 지원금 적용"),
    ANY("상관없음", "개통방법 무관");

    private final String displayName;
    private final String description;

    public boolean isSelectiveSubsidy() {
        return this == SELECTIVE_SUBSIDY;
    }

    public boolean isCommonSubsidy() {
        return this == COMMON_SUBSIDY;
    }

    public boolean isAny() {
        return this == ANY;
    }

    // 약정이 필요한 개통방법인지 확인
    public boolean requiresContract() {
        return this == SELECTIVE_SUBSIDY;
    }

    // Bid에서 사용 가능한 개통방법인지 확인 (ANY 제외)
    public boolean isValidForBid() {
        return this != ANY;
    }

    // 할인 혜택이 있는 개통방법인지 확인
    public boolean hasDiscount() {
        return this == SELECTIVE_SUBSIDY || this == COMMON_SUBSIDY;
    }
} 