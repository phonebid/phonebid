package com.phonebid.app.common.errorcode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum TradeErrorCode implements ErrorCode {

    MISSING_TRACKING_NUMBER(HttpStatus.BAD_REQUEST, "송장번호는 필수입니다."),
    MISSING_RECIPIENT_NAME(HttpStatus.BAD_REQUEST, "수령인 이름은 필수입니다."),
    MISSING_RECIPIENT_PHONE(HttpStatus.BAD_REQUEST, "수령인 전화번호는 필수입니다."),
    MISSING_POSTAL_CODE(HttpStatus.BAD_REQUEST, "우편번호는 필수입니다."),
    MISSING_ADDRESS(HttpStatus.BAD_REQUEST, "주소는 필수입니다."),
    INVALID_PHONE_FORMAT(HttpStatus.BAD_REQUEST, "올바르지 않은 전화번호 형식입니다."),
    INVALID_POSTAL_CODE_FORMAT(HttpStatus.BAD_REQUEST, "우편번호는 5자리 숫자여야 합니다."),
    INVALID_BID_FOR_QUOTE(HttpStatus.BAD_REQUEST, "선택된 입찰이 해당 견적에 속하지 않습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액은 0보다 커야 합니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 계약 금액과 일치하지 않습니다."),
    INVALID_CONTRACT_STATUS(HttpStatus.BAD_REQUEST, "잘못된 계약 상태입니다."),
    CONTRACT_CANNOT_SIGN(HttpStatus.BAD_REQUEST, "서명할 수 없는 계약 상태입니다."),
    CONTRACT_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "취소할 수 없는 계약 상태입니다."),
    DELIVERY_CANNOT_SHIP(HttpStatus.BAD_REQUEST, "배송 시작할 수 없는 상태입니다."),
    DELIVERY_CANNOT_DELIVER(HttpStatus.BAD_REQUEST, "배송 완료 처리할 수 없는 상태입니다."),
    DELIVERY_MISSING_TRACKING_NUMBER(HttpStatus.BAD_REQUEST, "송장번호가 없어 배송 추적 URL을 생성할 수 없습니다."),
    PAYMENT_CANNOT_APPROVE(HttpStatus.BAD_REQUEST, "결제 요청 상태가 아닌 결제는 승인할 수 없습니다."),
    PAYMENT_CANNOT_COMPLETE(HttpStatus.BAD_REQUEST, "완료할 수 없는 결제 상태입니다."),
    PAYMENT_CANNOT_FAIL(HttpStatus.BAD_REQUEST, "실패 처리할 수 없는 결제 상태입니다.");

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
