package com.phonebid.app.customerservice.errorcode;

import com.phonebid.app.common.errorcode.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum CustomerServiceErrorCode implements ErrorCode {

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."),
    INQUIRY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 문의에 접근할 수 없습니다."),
    INQUIRY_CANNOT_MODIFY(HttpStatus.BAD_REQUEST, "답변이 완료된 문의는 수정할 수 없습니다."),
    INQUIRY_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다."),
    INQUIRY_REPLY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 답변이 작성된 문의입니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    FAQ_NOT_FOUND(HttpStatus.NOT_FOUND, "FAQ를 찾을 수 없습니다."),
    ONLY_ADMIN_ALLOWED(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");

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

