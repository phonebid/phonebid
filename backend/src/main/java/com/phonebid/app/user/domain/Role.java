package com.phonebid.app.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    CONSUMER("소비자", "견적을 요청하는 일반 사용자"),
    SELLER("판매자", "입찰에 참여하는 판매업체"),
    ADMIN("관리자", "시스템 관리자");

    private final String displayName;
    private final String description;

    public boolean hasSellerPrivilege() {
        return this == SELLER || this == ADMIN;
    }

    public boolean hasAdminPrivilege() {
        return this == ADMIN;
    }
} 