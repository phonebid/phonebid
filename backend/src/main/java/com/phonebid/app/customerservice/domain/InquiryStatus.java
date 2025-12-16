package com.phonebid.app.customerservice.domain;

import lombok.Getter;

@Getter
public enum InquiryStatus {
    PENDING("대기중", "답변 대기 중인 문의"),
    ANSWERED("답변완료", "답변이 완료된 문의"),
    CLOSED("종료", "종료된 문의");

    private final String displayName;
    private final String description;

    InquiryStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

