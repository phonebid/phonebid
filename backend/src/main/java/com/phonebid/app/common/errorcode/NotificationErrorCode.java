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
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "알림 메시지는 1000자를 초과할 수 없습니다."),
    SSE_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SSE 연결에 실패했습니다."),
    KAKAO_TALK_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 알림톡 발송에 실패했습니다."),
    NOTIFICATION_CONSENT_REQUIRED(HttpStatus.BAD_REQUEST, "알림 수신 동의가 필요합니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");

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
