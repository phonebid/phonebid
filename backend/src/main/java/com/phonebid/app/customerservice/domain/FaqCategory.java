package com.phonebid.app.customerservice.domain;

import lombok.Getter;

@Getter
public enum FaqCategory {
    SERVICE("서비스 이용", "서비스 이용 방법 안내"),
    PAYMENT("결제/환불", "결제 및 환불 관련 안내"),
    DELIVERY("배송", "배송 관련 안내"),
    ACCOUNT("계정", "계정 관리 안내"),
    PRODUCT("상품", "상품 관련 안내"),
    ETC("기타", "기타 안내");

    private final String displayName;
    private final String description;

    FaqCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

