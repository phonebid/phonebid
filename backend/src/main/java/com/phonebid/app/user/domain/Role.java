package com.phonebid.app.user.domain;

import lombok.Getter;

@Getter
public enum Role {
    CONSUMER("소비자", "견적을 요청하는 일반 사용자", Authority.CONSUMER),
    SELLER("판매자", "입찰에 참여하는 판매업체", Authority.SELLER),
    ADMIN("관리자", "시스템 관리자", Authority.ADMIN);

    private final String displayName;
    private final String description;
    private final String authority;

    Role(String displayName, String description, String authority) {
        this.displayName = displayName;
        this.description = description;
        this.authority = authority;
    }

    public String getAuthority() {
        return this.authority;
    }

    public boolean hasSellerPrivilege() {
        return this == SELLER || this == ADMIN;
    }

    public boolean hasAdminPrivilege() {
        return this == ADMIN;
    }

    // 사용자 권한을 관리하는 inner 클래스
    public static class Authority {
        public static final String CONSUMER = "ROLE_CONSUMER";
        public static final String SELLER = "ROLE_SELLER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
} 