package com.phonebid.app.common.errorcode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    MISSING_USER_INFO(HttpStatus.BAD_REQUEST, "사용자 정보는 필수입니다."),
    MISSING_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "알림 유형은 필수입니다."),
    MISSING_NOTIFICATION_CHANNEL(HttpStatus.BAD_REQUEST, "알림 채널은 필수입니다."),
    MISSING_NOTIFICATION_TITLE(HttpStatus.BAD_REQUEST, "알림 제목은 필수입니다."),
    MISSING_NOTIFICATION_MESSAGE(HttpStatus.BAD_REQUEST, "알림 메시지는 필수입니다."),
    TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "알림 제목은 100자를 초과할 수 없습니다."),
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "알림 메시지는 1000자를 초과할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
