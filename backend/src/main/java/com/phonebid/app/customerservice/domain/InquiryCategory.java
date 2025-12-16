package com.phonebid.app.customerservice.domain;

import lombok.Getter;

@Getter
public enum InquiryCategory {
    PAYMENT("결제", "결제 관련 문의"),
    DELIVERY("배송", "배송 관련 문의"),
    ACCOUNT("계정", "계정 관련 문의"),
    PRODUCT("상품", "상품 관련 문의"),
    ETC("기타", "기타 문의");

    private final String displayName;
    private final String description;

    InquiryCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

