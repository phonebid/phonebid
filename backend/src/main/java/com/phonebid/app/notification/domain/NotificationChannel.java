package com.phonebid.app.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationChannel {
    SSE("SSE 실시간 알림", "Server-Sent Events를 통한 실시간 웹 알림"),
    KAKAO("카카오 알림톡", "카카오톡을 통한 알림톡 발송"),
    PUSH("웹 푸시", "브라우저 푸시 알림"),
    EMAIL("이메일", "이메일을 통한 알림");

    private final String displayName;
    private final String description;

    public boolean isKakao() {
        return this == KAKAO;
    }

    public boolean isPush() {
        return this == PUSH;
    }

    public boolean isEmail() {
        return this == EMAIL;
    }

    public boolean isSse() {
        return this == SSE;
    }

    // 즉시 발송 가능 여부
    public boolean isInstant() {
        return this == SSE || this == KAKAO || this == PUSH;
    }

    // 외부 API 연동 필요 여부
    public boolean requiresExternalApi() {
        return this == KAKAO || this == EMAIL;
    }

    // 채널별 우선순위 (높을수록 우선)
    public int getPriority() {
        return switch (this) {
            case SSE -> 4;   // 가장 높음 (실시간성, 웹 전용)
            case KAKAO -> 3; // 높음 (즉시성, 높은 도달률)
            case PUSH -> 2;   // 중간 (즉시성, 브라우저 의존)
            case EMAIL -> 1;  // 낮음 (지연 가능성, 스팸 위험)
        };
    }

    // 비용 수준 (높을수록 비쌈)
    public int getCostLevel() {
        return switch (this) {
            case SSE -> 1;   // 무료 (서버 리소스만 사용)
            case PUSH -> 1;  // 무료
            case EMAIL -> 2;  // 저렴 (대량 발송 시 과금)
            case KAKAO -> 3; // 유료 (건당 과금)
        };
    }
} 