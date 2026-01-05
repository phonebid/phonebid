package com.phonebid.app.common.errorcode;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PhoneErrorCode implements ErrorCode {
    
    // 휴대폰 모델 관련 에러
    PHONE_MODEL_NOT_FOUND(HttpStatus.NOT_FOUND, "휴대폰 모델을 찾을 수 없습니다."),
    PHONE_MODEL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 휴대폰 모델이 존재합니다."),

    // 휴대폰 옵션 관련 에러
    PHONE_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "휴대폰 옵션을 찾을 수 없습니다."),
    PHONE_OPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 휴대폰 옵션이 존재합니다."),

    // 휴대폰 모델 이미지 관련 에러
    PHONE_MODEL_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "휴대폰 모델 이미지를 찾을 수 없습니다."),
    PHONE_MODEL_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "휴대폰 모델 이미지는 최대 10개까지 업로드할 수 있습니다.");

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
