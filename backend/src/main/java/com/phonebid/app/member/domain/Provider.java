package com.phonebid.app.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    KAKAO("카카오", "https://kauth.kakao.com"),
    NAVER("네이버", "https://nid.naver.com");

    private final String displayName;
    private final String authUrl;

    public boolean isKakao() {
        return this == KAKAO;
    }

    public boolean isNaver() {
        return this == NAVER;
    }
} 