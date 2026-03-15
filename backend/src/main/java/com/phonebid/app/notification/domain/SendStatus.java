package com.phonebid.app.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 발송 상태
 */
@Getter
@RequiredArgsConstructor
public enum SendStatus {
    PENDING("대기 중", "발송 대기 중"),
    SENT("발송 완료", "발송 완료"),
    FAILED("발송 실패", "발송 실패");

    private final String displayName;
    private final String description;
}
