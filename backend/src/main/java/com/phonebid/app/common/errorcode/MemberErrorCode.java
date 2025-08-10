package com.phonebid.app.common.errorcode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MISSING_FILE_URL(HttpStatus.BAD_REQUEST, "파일 URL은 필수입니다."),
    MISSING_SELLER_INFO(HttpStatus.BAD_REQUEST, "판매자 정보는 필수입니다."),
    MISSING_DOCUMENT_TYPE(HttpStatus.BAD_REQUEST, "문서 종류는 필수입니다."),
    SELLER_CANNOT_APPROVE(HttpStatus.BAD_REQUEST, "승인 대기 상태가 아닌 판매자는 승인할 수 없습니다."),
    SELLER_CANNOT_REJECT(HttpStatus.BAD_REQUEST, "승인 대기 상태가 아닌 판매자는 거부할 수 없습니다.");

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
