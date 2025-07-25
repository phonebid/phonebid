package com.phonebid.app.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {
    BUSINESS_LICENSE("사업자등록증", "사업자등록증 파일"),
    CONSENT_FORM("사전승낙서", "통신판매업 사전승낙서");

    private final String displayName;
    private final String description;

    public boolean isBusinessLicense() {
        return this == BUSINESS_LICENSE;
    }

    public boolean isConsentForm() {
        return this == CONSENT_FORM;
    }

    public boolean isRequired() {
        return this == BUSINESS_LICENSE || this == CONSENT_FORM;
    }
} 