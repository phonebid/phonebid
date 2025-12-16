package com.phonebid.app.mypage.domain;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Bank {
    KB("KB국민은행", "KB"),
    SHINHAN("신한은행", "SHINHAN"),
    HANA("하나은행", "HANA"),
    WOORI("우리은행", "WOORI"),
    NH("NH농협은행", "NH"),
    IBK("IBK기업은행", "IBK"),
    KAKAO("카카오뱅크", "KAKAO"),
    TOSS("토스뱅크", "TOSS"),
    KEB("KEB하나은행", "KEB"),
    SC("SC제일은행", "SC"),
    CITI("한국씨티은행", "CITI"),
    KDB("KDB산업은행", "KDB"),
    SAVINGS("저축은행", "SAVINGS"),
    POST("우체국", "POST"),
    SUHYUP("수협은행", "SUHYUP");

    private final String displayName;
    private final String code;

    public static Bank fromDisplayName(String displayName) {
        for (Bank bank : values()) {
            if (bank.displayName.equals(displayName)) {
                return bank;
            }
        }
        throw new CustomException(CommonErrorCode.INVALID_BANK_NAME);
    }

    public static Bank fromCode(String code) {
        for (Bank bank : values()) {
            if (bank.code.equals(code)) {
                return bank;
            }
        }
        throw new CustomException(CommonErrorCode.INVALID_BANK_CODE);
    }
}

