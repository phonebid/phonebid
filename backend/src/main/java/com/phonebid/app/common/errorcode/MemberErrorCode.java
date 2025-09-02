package com.phonebid.app.common.errorcode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자 정보를 찾을 수 없습니다."),
    SELLER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 판매자로 등록된 사용자입니다."),
    BUSINESS_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 사업자등록번호입니다."),
    SELLER_NOT_APPROVED(HttpStatus.FORBIDDEN, "승인되지 않은 판매자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MISSING_FILE_URL(HttpStatus.BAD_REQUEST, "파일 URL은 필수입니다."),
    MISSING_SELLER_INFO(HttpStatus.BAD_REQUEST, "판매자 정보는 필수입니다."),
    MISSING_DOCUMENT_TYPE(HttpStatus.BAD_REQUEST, "문서 종류는 필수입니다."),
    SELLER_CANNOT_APPROVE(HttpStatus.BAD_REQUEST, "승인 대기 상태가 아닌 판매자는 승인할 수 없습니다."),
    SELLER_CANNOT_REJECT(HttpStatus.BAD_REQUEST, "승인 대기 상태가 아닌 판매자는 거부할 수 없습니다."),
    MISSING_FILE(HttpStatus.BAD_REQUEST, "업로드할 파일은 필수입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다. (최대 10MB)"),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "유효하지 않은 파일명입니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, pdf만 가능)"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 문서를 찾을 수 없습니다.");

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
