package com.phonebid.app.common.errorcode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {

    INVALID_QUOTE_STATUS(HttpStatus.BAD_REQUEST, "잘못된 견적 상태입니다."),
    QUOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "견적을 찾을 수 없습니다."),
    INVALID_BID_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 입찰 금액입니다."),
    QUOTE_ALREADY_CLOSED(HttpStatus.CONFLICT, "이미 종료된 견적입니다."),
    BID_NOT_ALLOWED(HttpStatus.FORBIDDEN, "입찰이 허용되지 않습니다."),
    INVALID_PURCHASE_METHOD(HttpStatus.BAD_REQUEST, "잘못된 구매 방법입니다."),
    INVALID_ACTIVATION_METHOD(HttpStatus.BAD_REQUEST, "잘못된 개통 방법입니다."),
    MISSING_CURRENT_CARRIER(HttpStatus.BAD_REQUEST, "기존 통신사 정보가 필요합니다."),
    QUOTE_CREATE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "사용자 인증이 필요합니다."),
    QUOTE_CREATE_CURRENT_REQUIRED(HttpStatus.BAD_REQUEST, "번호이동/기기변경 시 기존 통신사를 입력해야 합니다."),
    BID_MODIFICATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "수정할 수 없는 입찰입니다."),
    INVALID_PRICE_PLAN_NAME(HttpStatus.BAD_REQUEST, "요금제 이름이 유효하지 않습니다."),
    INVALID_PRICE_PLAN_PRICE(HttpStatus.BAD_REQUEST, "요금제 가격이 유효하지 않습니다."),
    INVALID_END_TIME(HttpStatus.BAD_REQUEST, "마감 시간은 현재 시간보다 이후여야 합니다.");

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
